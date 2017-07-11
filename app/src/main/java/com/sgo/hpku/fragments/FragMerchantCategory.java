package com.sgo.hpku.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.BuildConfig;
import com.sgo.hpku.R;
import com.sgo.hpku.activities.BbsMerchantCategoryActivity;
import com.sgo.hpku.activities.BbsMerchantCommunityList;
import com.sgo.hpku.activities.BbsMerchantSetupHourActivity;
import com.sgo.hpku.activities.BbsSetupOpenHourActivity;
import com.sgo.hpku.coreclass.CustomSecurePref;
import com.sgo.hpku.coreclass.DateTimeFormat;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.HashMessage;
import com.sgo.hpku.coreclass.InetHandler;
import com.sgo.hpku.coreclass.MyApiClient;
import com.sgo.hpku.coreclass.WebParams;
import com.sgo.hpku.dialogs.AlertDialogLogout;
import com.sgo.hpku.dialogs.DefinedDialog;
import com.sgo.hpku.entityRealm.MerchantCommunityList;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import io.realm.Realm;
import timber.log.Timber;

/**
 * Created by thinkpad on 7/14/2016.
 */
public class FragMerchantCategory extends Fragment {
    View v;
    SecurePreferences sp;
    EditText etHpNo, etAmount, etMessage;
    Button btnProses;
    ProgressDialog progdialog;
    String userID, accessKey, memberID;
    String memberId,shopId, flagApprove, setupOpenHour;
    JSONArray categories;
    ArrayList categoryIds;

    Realm myRealm;
    MerchantCommunityList memberDetail;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        myRealm     = Realm.getDefaultInstance();
        sp          = CustomSecurePref.getInstance().getmSecurePrefs();
        userID      = sp.getString(DefineValue.USERID_PHONE, "");
        memberID    = sp.getString(DefineValue.MEMBER_ID,"");

        shopId      = getActivity().getIntent().getExtras().getString("shopId");
        memberId    = getActivity().getIntent().getExtras().getString("memberId");
        flagApprove = getActivity().getIntent().getExtras().getString("flagApprove");
        setupOpenHour   = getActivity().getIntent().getExtras().getString("setupOpenHour");
        categoryIds     = new ArrayList();

        btnProses = (Button) v.findViewById(R.id.btn_proses);
        btnProses.setOnClickListener(btnProsesListener);

        progdialog              = DefinedDialog.CreateProgressDialog(getActivity(), "");
        progdialog.show();

        RequestParams params    = new RequestParams();
        UUID rcUUID             = UUID.randomUUID();
        String  dtime           = DateTimeFormat.getCurrentDateTime();

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.AppIDHpku);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID );
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID );
        //params.put(WebParams.SHOP_ID, shopId);

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.AppIDHpku));

        params.put(WebParams.SIGNATURE, signature);

        MyApiClient.getCategoryList(getActivity(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progdialog.dismiss();

                try {

                    String code = response.getString(WebParams.ERROR_CODE);
                    if (code.equals(WebParams.SUCCESS_CODE)) {
                        LinearLayout ll = (LinearLayout) getActivity().findViewById(R.id.formMerchantCategory);

                        categories = response.getJSONArray("category");

                        for(int i =0; i < categories.length(); i++) {
                            CheckBox cb = new CheckBox(getActivity());
                            JSONObject object = categories.getJSONObject(i);
                            cb.setText(object.getString("category_name"));
                            cb.setId(i);
                            ll.addView(cb);
                        }
                    } else {
                        Toast.makeText(getActivity(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_LONG);
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
                Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                progdialog.dismiss();
                Timber.w("Error Koneksi login:" + throwable.toString());

            }

        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_merchant_category, container, false);
        return v;
    }

    Button.OnClickListener btnProsesListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                LinearLayout container = (LinearLayout) getActivity().findViewById(R.id.formMerchantCategory);
                int countChecked = 0;
                categoryIds = new ArrayList();
                for (int i = 0; i < container.getChildCount(); i++) {
                    View view2 = container.getChildAt(i);
                    if (view2 instanceof CheckBox) {
                        CheckBox cb = (CheckBox) view2;


                        if ( cb.isChecked() ) {


                            try {
                                if ( cb.isChecked() ) {
                                    int idx = cb.getId();
                                    JSONObject object = categories.getJSONObject(idx);
                                    categoryIds.add(object.getString("category_id"));
                                    countChecked++;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                }

                if ( countChecked == 0 ){
                    Toast.makeText(getActivity(), R.string.err_empty_categories, Toast.LENGTH_LONG).show();
                } else {



                    progdialog.show();
                    RequestParams params    = new RequestParams();
                    UUID rcUUID             = UUID.randomUUID();
                    String  dtime           = DateTimeFormat.getCurrentDateTime();
                    String categoryJSON     = new Gson().toJson(categoryIds);

                    params.put(WebParams.RC_UUID, rcUUID);
                    params.put(WebParams.RC_DATETIME, dtime);
                    params.put(WebParams.APP_ID, BuildConfig.AppID);
                    params.put(WebParams.SENDER_ID, DefineValue.SENDER_ID);
                    params.put(WebParams.RECEIVER_ID, DefineValue.RECEIVER_ID);
                    params.put(WebParams.SHOP_ID, shopId);
                    params.put(WebParams.MEMBER_ID, memberId);
                    params.put(WebParams.CATEGORY, categoryJSON);

                    String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.SENDER_ID + DefineValue.RECEIVER_ID + memberId.toUpperCase() + shopId.toUpperCase() + BuildConfig.AppID));

                    params.put(WebParams.SIGNATURE, signature);

                    MyApiClient.registerCategoryShop(getActivity(), params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            progdialog.dismiss();

                            try {

                                String code = response.getString(WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {

                                    if ( setupOpenHour.equals(DefineValue.STRING_YES) ){
//                                        Bundle args = new Bundle();
//                                        args.putString(DefineValue.SHOP_ID, memberDetail.getShopId());
//                                        args.putString(DefineValue.MEMBER_ID, memberDetail.getMemberId());
//
//                                        Fragment newFrag = new FragSetupOpenHour();
//                                        newFrag.setArguments(args);
//                                        switchFragment(newFrag, getString(R.string.toolbar_title_setup_open_hour), true);

                                        Intent intent=new Intent(getActivity(), BbsSetupOpenHourActivity.class);
                                        intent.putExtra("memberId", memberId);
                                        intent.putExtra("shopId", shopId);
                                        intent.putExtra("flagApprove", flagApprove);
                                        startActivity(intent);

                                    } else {
                                        Toast.makeText(getActivity(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_LONG);

                                        Intent intent=new Intent(getActivity(),BbsMerchantCommunityList.class);
                                        startActivity(intent);
                                    }
                                } else {
                                    Toast.makeText(getActivity(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_LONG);
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
                            Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                            progdialog.dismiss();
                            Timber.w("Error Koneksi login:" + throwable.toString());

                        }

                    });
                }


            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchFragment(Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        BbsMerchantCategoryActivity fca = (BbsMerchantCategoryActivity ) getActivity();
        fca.switchContent(i,name,isBackstack);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(getString(R.string.setup_merchant_category));
    }

    private void setTitle(String _title){
        if (getActivity() == null)
            return;

        BbsMerchantCategoryActivity fca = (BbsMerchantCategoryActivity) getActivity();
        fca.setTitleFragment(_title);
    }
}
