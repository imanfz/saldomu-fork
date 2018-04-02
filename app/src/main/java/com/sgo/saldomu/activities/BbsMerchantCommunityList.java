package com.sgo.saldomu.activities;

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
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.BbsMerchantCommunityListAdapter;
import com.sgo.saldomu.widgets.BaseActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.entityRealm.MerchantCommunityList;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import io.realm.Realm;
import timber.log.Timber;

import static io.realm.Realm.getDefaultInstance;

public class BbsMerchantCommunityList extends BaseActivity {

    ProgressDialog progdialog;
    private ListView lvCommunityList;
    private BbsMerchantCommunityListAdapter bbsMerchantCommunityListAdapter;
    private ArrayList<MerchantCommunityList> merchantCommunityListModel;
    private Realm myRealm;
    private static BbsMerchantCommunityList instance;
    SecurePreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myRealm                 = getDefaultInstance();

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        initializeToolbar();

        progdialog              = DefinedDialog.CreateProgressDialog(this, "");
        lvCommunityList         = (ListView) findViewById(R.id.lvCommunityList);

        lvCommunityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Intent intent=new Intent(MainActivity.this,PersonDetailsActivity.class);
                //intent.putExtra("PersonID", personDetailsModelArrayList.get(position).getId());
                //startActivity(intent);
            }
        });

        merchantCommunityListModel = new ArrayList<>();
        bbsMerchantCommunityListAdapter = new BbsMerchantCommunityListAdapter(BbsMerchantCommunityList.this, merchantCommunityListModel);
        lvCommunityList.setAdapter(bbsMerchantCommunityListAdapter);

        lvCommunityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(BbsMerchantCommunityList.this, BbsMemberLocationActivity.class);
                intent.putExtra("memberId", merchantCommunityListModel.get(position).getMemberId());
                intent.putExtra("shopId", merchantCommunityListModel.get(position).getShopId());
                startActivity(intent);
            }
        });

        RequestParams params    = new RequestParams();
        UUID rcUUID             = UUID.randomUUID();
        String  dtime           = DateTimeFormat.getCurrentDateTime();

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.CUSTOMER_ID, sp.getString(DefineValue.USERID_PHONE, ""));
        params.put(WebParams.FLAG_APPROVE, DefineValue.STRING_YES);

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + sp.getString(DefineValue.USERID_PHONE, "") + BuildConfig.APP_ID + DefineValue.STRING_YES));

        params.put(WebParams.SIGNATURE, signature);

        MyApiClient.getMemberShopList(getApplication(), params, false, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progdialog.dismiss();

                try {

                    String code = response.getString(WebParams.ERROR_CODE);
                    if (code.equals(WebParams.SUCCESS_CODE)) {
                        JSONArray members = response.getJSONArray("member");

                        merchantCommunityListModel.clear();

                        myRealm.beginTransaction();
                        for (int i = 0; i < members.length(); i++) {
                            JSONObject object = members.getJSONObject(i);

                            myRealm.where(MerchantCommunityList.class).equalTo("memberId", object.getString("member_id"))
                                    .findAll().deleteFirstFromRealm();

                            MerchantCommunityList agentDetailModel = myRealm.createObject(MerchantCommunityList.class, object.getString("member_id") );
                            agentDetailModel.setMemberName( object.getString("member_name") );
                            agentDetailModel.setMemberCode(object.getString("member_code"));
                            agentDetailModel.setMemberType(object.getString("member_type"));
                            agentDetailModel.setCommName(object.getString("comm_name"));
                            agentDetailModel.setCommCode(object.getString("comm_code"));
                            agentDetailModel.setShopId(object.getString("shop_id"));
                            agentDetailModel.setShopName(object.getString("shop_name"));
                            agentDetailModel.setAddress1(object.getString("address1"));
                            agentDetailModel.setDistrict(object.getString("district"));
                            agentDetailModel.setProvince(object.getString("province"));
                            agentDetailModel.setCountry(object.getString("country"));
                            agentDetailModel.setMemberCust(sp.getString(DefineValue.USERID_PHONE, ""));
                            merchantCommunityListModel.add(myRealm.copyFromRealm(agentDetailModel));


                        }
                        myRealm.commitTransaction();

                        if ( members.length() == 1 ) {

                        }

                        bbsMerchantCommunityListAdapter.notifyDataSetChanged();

                    } else {
                        Intent i = new Intent(BbsMerchantCommunityList.this, MainPage.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
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
                    Toast.makeText(getApplication(), throwable.toString(), Toast.LENGTH_SHORT).show();

                progdialog.dismiss();
                Timber.w("Error Koneksi login:" + throwable.toString());

            }

        });
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs_merchant_community_list;
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

    @Override
    protected void onDestroy() {
        RealmManager.closeRealm(myRealm);
        super.onDestroy();
    }

    private void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.list_shop_member));
    }
}
