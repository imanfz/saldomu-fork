package com.sgo.saldomu.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.EasyAdapter;
import com.sgo.saldomu.adapter.ListSettingAdapter;
import com.sgo.saldomu.coreclass.BaseActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.models.ShopDetail;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import timber.log.Timber;

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
        InitializeToolbar();

        menu                    = new ArrayList<>();
        _data                   = getResources().getStringArray(R.array.list_bbs_setting_kelola);
        lvList                  = (ListView) findViewById(R.id.list);
        sp                      = CustomSecurePref.getInstance().getmSecurePrefs();

        progdialog              = DefinedDialog.CreateProgressDialog(this, "");



        flagApprove             = DefineValue.STRING_BOTH;

        RequestParams params    = new RequestParams();
        UUID rcUUID             = UUID.randomUUID();
        String  dtime           = DateTimeFormat.getCurrentDateTime();
        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID );
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID );
        params.put(WebParams.CUSTOMER_ID, sp.getString(DefineValue.USERID_PHONE, ""));
        params.put(WebParams.FLAG_APPROVE, flagApprove);

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime +
                DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID +
                sp.getString(DefineValue.USERID_PHONE, "") + BuildConfig.APP_ID + flagApprove));

        params.put(WebParams.SIGNATURE, signature);

        MyApiClient.getMemberShopList(BbsListSettingKelolaActivity.this, params, false, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progdialog.dismiss();

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
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                ifFailure(throwable);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                ifFailure(throwable);
            }

            private void ifFailure(Throwable throwable) {
                //if (MyApiClient.PROD_FAILURE_FLAG)
                //Toast.makeText(getApplication(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                //else
                Toast.makeText(BbsListSettingKelolaActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();

                progdialog.dismiss();
                Timber.w("Error Koneksi login:" + throwable.toString());

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

    public void InitializeToolbar(){
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
