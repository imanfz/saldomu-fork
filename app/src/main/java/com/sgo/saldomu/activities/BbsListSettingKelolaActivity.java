package com.sgo.saldomu.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ListView;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.ListSettingAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
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
    String shopId, memberId, shopName, memberType, category, agentName, commName, province, district, address, stepApprove;
    ProgressDialog progdialog, progdialog2;
    String flagApprove;
    SecurePreferences sp;
    ArrayList<ShopDetail> shopDetails = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        menu                    = new ArrayList<>();
        _data                   = getResources().getStringArray(R.array.list_bbs_setting_kelola);
        lvList                  = (ListView) findViewById(R.id.list);
        sp                      = CustomSecurePref.getInstance().getmSecurePrefs();

        progdialog              = DefinedDialog.CreateProgressDialog(this, "");



        flagApprove             = DefineValue.STRING_BOTH;

        String extraSignature = flagApprove;
        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_MEMBER_SHOP_LIST, extraSignature);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID );
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID );
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
                                JSONArray members = response.getJSONArray("member");

                                for (int i = 0; i < members.length(); i++) {
                                    JSONObject object = members.getJSONObject(i);

                                    ShopDetail shopDetail = new ShopDetail();
                                    shopDetail.setMemberId(object.getString("member_id"));
                                    shopDetail.setMemberCode(object.getString("member_code"));
                                    shopDetail.setMemberName(object.getString("member_name"));
                                    shopDetail.setMemberType(object.getString("member_type"));
                                    shopDetail.setCommName(object.getString("comm_name"));
                                    shopDetail.setCommCode(object.getString("comm_code"));
                                    shopDetail.setShopId(object.getString("shop_id"));
                                    shopDetail.setShopName(object.getString("shop_name"));
                                    shopDetail.setShopFirstAddress(object.getString("address1"));
                                    shopDetail.setShopDistrict(object.getString("district"));
                                    shopDetail.setShopProvince(object.getString("province"));
                                    shopDetail.setShopCountry(object.getString("country"));
                                    shopDetail.setStepApprove(object.getString("step_approve"));

                                    agentName = object.getString("member_name");
                                    stepApprove = object.getString("step_approve");
                                    JSONArray categories = object.getJSONArray("category");

                                    for (int j = 0; j < categories.length(); j++) {
                                        JSONObject object2 = categories.getJSONObject(j);
                                        shopDetail.setCategories(object2.getString("category_name"));
                                    }

                                    shopDetails.add(shopDetail);
                                    category = TextUtils.join(", ", shopDetail.getCategories());

                                }


                                for(int i =0; i <= (_data.length-1); i++) {
                                    String temp = _data[i];

                                    if ( i == 0 ) {
                                        temp += " : " + agentName;
                                    } else if ( i == 1 ) {
                                        temp += " : " + category;
                                    }

                                    if ( i == 2 ) {
                                        if (stepApprove.equals(DefineValue.STRING_NO)) {
                                            menu.add(temp);
                                        }
                                    } else if ( i == 3 ) {
                                        if (stepApprove.equals(DefineValue.STRING_YES)) {
                                            menu.add(temp);
                                        }
                                    } else {
                                        menu.add(temp);
                                    }
                                }

                                listSettingAdapter = new ListSettingAdapter(BbsListSettingKelolaActivity.this, menu, flagApprove, shopDetails);
                                lvList.setAdapter(listSettingAdapter);

                            } else {



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


        memberId        = getIntent().getStringExtra("memberId");
        shopId          = getIntent().getStringExtra("shopId");
        shopName        = getIntent().getStringExtra("shopName");
        memberType      = getIntent().getStringExtra("memberType");
        category        = getIntent().getStringExtra("category");
        agentName       = getIntent().getStringExtra("memberName");
        commName        = getIntent().getStringExtra("commName");
        province        = getIntent().getStringExtra("province");
        district        = getIntent().getStringExtra("district");
        address         = getIntent().getStringExtra("address");


    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs_list_setting_kelola;
    }

    public void initializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_title_setting));
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
