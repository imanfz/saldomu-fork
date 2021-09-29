package com.sgo.saldomu.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.BbsMerchantCommunityListAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.entityRealm.MerchantCommunityList;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.widgets.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;

import static io.realm.Realm.getDefaultInstance;

public class BbsMerchantCommunityList extends BaseActivity {

    private BbsMerchantCommunityListAdapter bbsMerchantCommunityListAdapter;
    private ArrayList<MerchantCommunityList> merchantCommunityListModel;
    private Realm myRealm;
    SecurePreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myRealm = getDefaultInstance();

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        initializeToolbar();

        ListView lvCommunityList = (ListView) findViewById(R.id.lvCommunityList);

        lvCommunityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        merchantCommunityListModel = new ArrayList<>();
        bbsMerchantCommunityListAdapter = new BbsMerchantCommunityListAdapter(BbsMerchantCommunityList.this, merchantCommunityListModel);
        lvCommunityList.setAdapter(bbsMerchantCommunityListAdapter);

        lvCommunityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(BbsMerchantCommunityList.this, BbsMemberLocationActivity.class);
                intent.putExtra(DefineValue.MEMBER_ID, merchantCommunityListModel.get(position).getMemberId());
                intent.putExtra(DefineValue.SHOP_ID, merchantCommunityListModel.get(position).getShopId());
                startActivity(intent);
            }
        });

        String extraSignature = DefineValue.STRING_YES;
        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_MEMBER_SHOP_LIST, extraSignature);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.CUSTOMER_ID, userPhoneID);
        params.put(WebParams.FLAG_APPROVE, DefineValue.STRING_YES);
        params.put(WebParams.USER_ID, userPhoneID);

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_MEMBER_SHOP_LIST, params, new ObjListeners() {

            @Override
            public void onResponses(JSONObject response) {
                try {

                    String code = response.getString(WebParams.ERROR_CODE);
                    if (code.equals(WebParams.SUCCESS_CODE)) {
                        JSONArray members = response.getJSONArray(WebParams.MEMBER);

                        merchantCommunityListModel.clear();

                        myRealm.beginTransaction();
                        for (int i = 0; i < members.length(); i++) {
                            JSONObject object = members.getJSONObject(i);

                            myRealm.where(MerchantCommunityList.class).equalTo(DefineValue.MEMBER_ID, object.getString(WebParams.MEMBER_ID))
                                    .findAll().deleteFirstFromRealm();

                            MerchantCommunityList agentDetailModel = myRealm.createObject(MerchantCommunityList.class, object.getString(WebParams.MEMBER_ID));
                            agentDetailModel.setMemberName(object.getString(WebParams.MEMBER_NAME));
                            agentDetailModel.setMemberCode(object.getString(WebParams.MEMBER_CODE));
                            agentDetailModel.setMemberType(object.getString(WebParams.MEMBER_TYPE));
                            agentDetailModel.setCommName(object.getString(WebParams.COMM_NAME));
                            agentDetailModel.setCommCode(object.getString(WebParams.COMM_CODE));
                            agentDetailModel.setShopId(object.getString(WebParams.SHOP_ID));
                            agentDetailModel.setShopName(object.getString(WebParams.SHOP_NAME));
                            agentDetailModel.setAddress1(object.getString(WebParams.ADDRESS1));
                            agentDetailModel.setDistrict(object.getString(WebParams.DISTRICT));
                            agentDetailModel.setProvince(object.getString(WebParams.PROVINCE));
                            agentDetailModel.setCountry(object.getString(WebParams.COUNTRY));
                            agentDetailModel.setMemberCust(sp.getString(DefineValue.USERID_PHONE, ""));
                            merchantCommunityListModel.add(myRealm.copyFromRealm(agentDetailModel));


                        }
                        myRealm.commitTransaction();

                        if (members.length() == 1) {

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
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

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
