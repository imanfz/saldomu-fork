package com.sgo.saldomu.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.BbsMemberShopAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.models.ShopDetail;
import com.sgo.saldomu.widgets.BaseActivity;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("memberId", shopDetails.get(position).getMemberId());
                intent.putExtra("shopId", shopDetails.get(position).getShopId());
                intent.putExtra("flagApprove", flagApprove);
                startActivity(intent);
                finish();
            }
        });

        String extraSignature = flagApprove;
        RequestParams params            = MyApiClient.getSignatureWithParams(commIDLogin, MyApiClient.LINK_MEMBER_SHOP_LIST,
                userPhoneID, accessKey, extraSignature);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.CUSTOMER_ID, userPhoneID);
        params.put(WebParams.FLAG_APPROVE, flagApprove);
        params.put(WebParams.USER_ID, userPhoneID);


        progdialog              = DefinedDialog.CreateProgressDialog(this, "");
        MyApiClient.getMemberShopList(getApplication(), params, false, new JsonHttpResponseHandler() {
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
                        //Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();

                        AlertDialog alertDialog = new AlertDialog.Builder(BbsMemberShopActivity.this).create();
                        alertDialog.setTitle(getString(R.string.alertbox_title_information));
                        alertDialog.setMessage(response.getString(WebParams.ERROR_MESSAGE));
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        Intent i = new Intent(BbsMemberShopActivity.this, MainPage.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(i);
                                        finish();
                                    }
                                });
                        alertDialog.show();
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
