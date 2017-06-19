package com.sgo.hpku.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.BuildConfig;
import com.sgo.hpku.R;
import com.sgo.hpku.adapter.BbsMemberShopAdapter;
import com.sgo.hpku.coreclass.BaseActivity;
import com.sgo.hpku.coreclass.CustomSecurePref;
import com.sgo.hpku.coreclass.DateTimeFormat;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.HashMessage;
import com.sgo.hpku.coreclass.MyApiClient;
import com.sgo.hpku.coreclass.WebParams;
import com.sgo.hpku.dialogs.AlertDialogLogout;
import com.sgo.hpku.dialogs.DefinedDialog;
import com.sgo.hpku.models.ShopDetail;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;


public class BbsMemberShopActivity extends BaseActivity {

    ProgressDialog progdialog;
    SecurePreferences sp;
    ArrayList<ShopDetail> shopDetails;
    ListView lvReport;
    private BbsMemberShopAdapter bbsMemberShopAdapter;
    String flagApprove;
    String title;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        shopDetails             = new ArrayList<>();
        progdialog              = DefinedDialog.CreateProgressDialog(getApplicationContext(), "");
        sp                      = CustomSecurePref.getInstance().getmSecurePrefs();
        lvReport                = (ListView) findViewById(R.id.list);
        flagApprove             = getIntent().getStringExtra("flagApprove");

        if ( flagApprove.equals(DefineValue.STRING_YES) ) {
            title               = getString(R.string.shop_list);
        } else {
            title               = getString(R.string.list_approval);
        }

        initializeToolbar();

        bbsMemberShopAdapter = new BbsMemberShopAdapter(BbsMemberShopActivity.this, shopDetails);
        lvReport.setAdapter(bbsMemberShopAdapter);
        lvReport.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(BbsMemberShopActivity.this,BbsMemberShopDetailActivity.class);
                intent.putExtra("memberId", shopDetails.get(position).getMemberId());
                intent.putExtra("shopId", shopDetails.get(position).getShopId());
                intent.putExtra("flagApprove", flagApprove);
                startActivity(intent);
            }
        });

        RequestParams params    = new RequestParams();
        UUID rcUUID             = UUID.randomUUID();
        String  dtime           = DateTimeFormat.getCurrentDateTime();
        String customerId       = sp.getString(DefineValue.USERID_PHONE, "");

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.AppID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.CUSTOMER_ID, customerId);
        params.put(WebParams.FLAG_APPROVE, flagApprove);

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + customerId + BuildConfig.AppID + flagApprove));

        params.put(WebParams.SIGNATURE, signature);

        MyApiClient.getMemberShopList(getApplication(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progdialog.dismiss();

                try {

                    String code = response.getString(WebParams.ERROR_CODE);
                    if (code.equals(WebParams.SUCCESS_CODE)) {
                        JSONArray members = response.getJSONArray("member");
                        for (int i = 0; i < members.length(); i++) {
                            JSONObject object       = members.getJSONObject(i);

                            ShopDetail shopDetail   = new ShopDetail();
                            shopDetail.setMemberId(object.getString("member_id"));
                            shopDetail.setShopId(object.getString("shop_id"));
                            shopDetail.setMemberCode(object.getString("member_code"));
                            shopDetail.setMemberName(object.getString("member_name"));
                            shopDetail.setMemberType(object.getString("member_type"));
                            shopDetail.setCommName(object.getString("comm_name"));
                            shopDetail.setCommCode(object.getString("comm_code"));
                            shopDetail.setShopAddress(object.getString("address1"));
                            shopDetail.setShopDistrict(object.getString("district"));
                            shopDetail.setShopProvince(object.getString("province"));
                            shopDetail.setShopCountry(object.getString("country"));

                            shopDetails.add(shopDetail);
                        }

                        if ( shopDetails.size() == 1 ) {
                            Intent intent=new Intent(BbsMemberShopActivity.this,BbsMemberShopDetailActivity.class);
                            intent.putExtra("memberId", shopDetails.get(0).getMemberId());
                            intent.putExtra("shopId", shopDetails.get(0).getShopId());
                            intent.putExtra("flagApprove", flagApprove);
                            startActivity(intent);
                        } else {
                            bbsMemberShopAdapter.notifyDataSetChanged();
                        }

                    } else if ( code.equals(WebParams.LOGOUT_CODE) ) {

                    } else {
                        code = response.getString(WebParams.ERROR_MESSAGE);
                        Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();
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
                if (MyApiClient.PROD_FAILURE_FLAG)
                    Toast.makeText(getApplication(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplication(), throwable.toString(), Toast.LENGTH_SHORT).show();

                progdialog.dismiss();
                Timber.w("Error Koneksi login:" + throwable.toString());

            }

        });
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs_member_shop;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //listener ketika button back di action bar diklik
        if (id == android.R.id.home) {
            //kembali ke activity sebelumnya
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(title);
    }
}
