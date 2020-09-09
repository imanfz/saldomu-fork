package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BbsMerchantCategoryActivity;
import com.sgo.saldomu.activities.BbsMerchantCommunityList;
import com.sgo.saldomu.activities.BbsSetupOpenHourActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.entityRealm.MerchantCommunityList;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.jsonModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
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

        btnProses = v.findViewById(R.id.btn_proses);
        btnProses.setOnClickListener(btnProsesListener);

        progdialog              = DefinedDialog.CreateProgressDialog(getActivity(), "");
        progdialog.show();

        HashMap<String, Object> params = new HashMap<>();
        UUID rcUUID             = UUID.randomUUID();
        String  dtime           = DateTimeFormat.getCurrentDateTime();

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID );
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID );
        //params.put(WebParams.SHOP_ID, shopId);

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime +
                DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.APP_ID));

        params.put(WebParams.SIGNATURE, signature);

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CATEGORY_LIST, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {

                            String code = response.getString(WebParams.ERROR_CODE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                LinearLayout ll = getActivity().findViewById(R.id.formMerchantCategory);

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
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {
                        progdialog.dismiss();
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
                LinearLayout container = getActivity().findViewById(R.id.formMerchantCategory);
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
                    HashMap<String, Object> params = new HashMap<>();
                    UUID rcUUID             = UUID.randomUUID();
                    String  dtime           = DateTimeFormat.getCurrentDateTime();
                    String categoryJSON     = new Gson().toJson(categoryIds);

                    params.put(WebParams.RC_UUID, rcUUID);
                    params.put(WebParams.RC_DATETIME, dtime);
                    params.put(WebParams.APP_ID, BuildConfig.APP_ID);
                    params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
                    params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
                    params.put(WebParams.SHOP_ID, shopId);
                    params.put(WebParams.MEMBER_ID, memberId);
                    params.put(WebParams.CATEGORY, categoryJSON);

                    String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + memberId.toUpperCase() + shopId.toUpperCase() + BuildConfig.APP_ID));

                    params.put(WebParams.SIGNATURE, signature);


                    RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_BBS_BANK_ACCOUNT_DELETE, params,
                            new ObjListeners() {
                                @Override
                                public void onResponses(JSONObject response) {
                                    try {
                                        Gson gson = new Gson();
                                        jsonModel model = gson.fromJson(response.toString(), jsonModel.class);

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
                                                Toast.makeText(getActivity(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_LONG).show();

                                                Intent intent=new Intent(getActivity(),BbsMerchantCommunityList.class);
                                                startActivity(intent);
                                            }
                                        }else if (code.equals(DefineValue.ERROR_9333)) {
                                            Timber.d("isi response app data:" + model.getApp_data());
                                            final AppDataModel appModel = model.getApp_data();
                                            AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                            alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                        } else if (code.equals(DefineValue.ERROR_0066)) {
                                            Timber.d("isi response maintenance:" + response.toString());
                                            AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                            alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                                        } else {
                                            Toast.makeText(getActivity(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_LONG).show();
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
