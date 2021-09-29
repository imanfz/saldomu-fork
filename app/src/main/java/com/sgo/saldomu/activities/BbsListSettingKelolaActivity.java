package com.sgo.saldomu.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ListView;

import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.ListSettingAdapter;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.ShopDetail;
import com.sgo.saldomu.widgets.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class BbsListSettingKelolaActivity extends BaseActivity {

    String[] _data;
    private ListSettingAdapter listSettingAdapter;
    ArrayList<String> menu;
    ListView lvList;
    String category, agentName, stepApprove;
    ProgressDialog progdialog;
    String flagApprove;
    ArrayList<ShopDetail> shopDetails = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        menu = new ArrayList<>();
        _data = getResources().getStringArray(R.array.list_bbs_setting_kelola);
        lvList = (ListView) findViewById(R.id.list);

        progdialog = DefinedDialog.CreateProgressDialog(this, "");


        flagApprove = DefineValue.STRING_BOTH;

        String extraSignature = flagApprove;
        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_MEMBER_SHOP_LIST, extraSignature);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.CUSTOMER_ID, userPhoneID);
        params.put(WebParams.FLAG_APPROVE, flagApprove);
        params.put(WebParams.USER_ID, userPhoneID);

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_MEMBER_SHOP_LIST, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {

                            String code = response.getString(WebParams.ERROR_CODE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                JSONArray members = response.getJSONArray(WebParams.MEMBER);

                                for (int i = 0; i < members.length(); i++) {
                                    JSONObject object = members.getJSONObject(i);

                                    ShopDetail shopDetail = new ShopDetail();
                                    shopDetail.setMemberId(object.getString(WebParams.MEMBER_ID));
                                    shopDetail.setMemberCode(object.getString(WebParams.MEMBER_CODE));
                                    shopDetail.setMemberName(object.getString(WebParams.MEMBER_NAME));
                                    shopDetail.setMemberType(object.getString(WebParams.MEMBER_TYPE));
                                    shopDetail.setCommName(object.getString(WebParams.COMM_NAME));
                                    shopDetail.setCommCode(object.getString(WebParams.COMM_CODE));
                                    shopDetail.setShopId(object.getString(WebParams.SHOP_ID));
                                    shopDetail.setShopName(object.getString(WebParams.SHOP_NAME));
                                    shopDetail.setShopFirstAddress(object.getString(WebParams.ADDRESS1));
                                    shopDetail.setShopDistrict(object.getString(WebParams.DISTRICT));
                                    shopDetail.setShopProvince(object.getString(WebParams.PROVINCE));
                                    shopDetail.setShopCountry(object.getString(WebParams.COUNTRY));
                                    shopDetail.setStepApprove(object.getString(WebParams.STEP_APPROVE));

                                    agentName = object.getString(WebParams.MEMBER_NAME);
                                    stepApprove = object.getString(WebParams.STEP_APPROVE);
                                    JSONArray categories = object.getJSONArray(WebParams.CATEGORY);

                                    for (int j = 0; j < categories.length(); j++) {
                                        JSONObject object2 = categories.getJSONObject(j);
                                        shopDetail.setCategories(object2.getString(WebParams.CATEGORY));
                                    }

                                    shopDetails.add(shopDetail);
                                    category = TextUtils.join(", ", shopDetail.getCategories());

                                }

                                for (int i = 0; i <= (_data.length - 1); i++) {
                                    String temp = _data[i];

                                    if (i == 0) {
                                        temp += " : " + agentName;
                                    } else if (i == 1) {
                                        temp += " : " + category;
                                    }

                                    if (i == 2) {
                                        if (stepApprove.equals(DefineValue.STRING_NO)) {
                                            menu.add(temp);
                                        }
                                    } else if (i == 3) {
                                        if (stepApprove.equals(DefineValue.STRING_YES)) {
                                            menu.add(temp);
                                        }
                                    } else {
                                        menu.add(temp);
                                    }
                                }

                                listSettingAdapter = new ListSettingAdapter(BbsListSettingKelolaActivity.this, menu, flagApprove);
                                lvList.setAdapter(listSettingAdapter);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                        progdialog.dismiss();
                    }
                });

        category = getIntent().getStringExtra(DefineValue.CATEGORY);
        agentName = getIntent().getStringExtra(DefineValue.MEMBER_NAME);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs_list_setting_kelola;
    }

    public void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_title_setting));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
