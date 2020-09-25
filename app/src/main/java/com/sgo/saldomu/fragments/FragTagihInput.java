package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.TagihModel;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.TagihActivity;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.TagihCommunityModel;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import timber.log.Timber;

public class FragTagihInput extends BaseFragment {
    Spinner sp_mitra, sp_communtiy;
    LinearLayout ll_komunitas;
    SecurePreferences sp;
    EditText et_memberCode;
    Button btn_submit, btn_regShop;
    Boolean is_search = false, isAgentLKD = false;
    View v;
    TextView tv_saldo_collector;
    private ArrayAdapter<String> mitraAdapter;
    private ArrayList<String> mitraNameArrayList = new ArrayList<>();
    private ArrayList<TagihModel> mitraNameData = new ArrayList<>();
    private ArrayAdapter<String> communityAdapter;
    private ArrayList<String> communityNameArrayList = new ArrayList<>();
    String commCodeTagih, balanceCollector, commNamePG, commCodePG, anchorNamePG, memberCode, txIdPG;
    ProgressDialog progdialog;
    private ArrayList<TagihModel> anchorDataList = new ArrayList<>();
    private ArrayList<TagihCommunityModel> communityDataList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_tagih_input, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            is_search = bundle.getBoolean(DefineValue.IS_SEARCH_DGI, false);
            Timber.d("is_search : ", is_search.toString());
            if (bundle.containsKey(DefineValue.ANCHOR_NAME_PG)) {
                commCodePG = bundle.getString(DefineValue.COMM_CODE_PG, "");
                commNamePG = bundle.getString(DefineValue.COMM_NAME_PG, "");
                anchorNamePG = bundle.getString(DefineValue.ANCHOR_NAME_PG, "");
                memberCode = bundle.getString(DefineValue.MEMBER_CODE_PG, "");
                txIdPG = bundle.getString(DefineValue.TXID_PG, "");
            }
        }

        sp = CustomSecurePref.getInstance().getSecurePrefsInstance();

        isAgentLKD = sp.getString(DefineValue.COMPANY_TYPE, "").equalsIgnoreCase(getString(R.string.LKD));

        if (sp.getString(DefineValue.USE_DEPOSIT_COL, "").equals("LIMIT")) {
            getBalanceCollector();
        } else if (sp.getString(DefineValue.USE_DEPOSIT_COL, "").equals("REG")) {
            balanceCollector = sp.getString(DefineValue.BALANCE_AMOUNT, "0");
            tv_saldo_collector.setText(CurrencyFormat.format(balanceCollector));
        }

        anchorDataList.clear();
        getAnchor();
        initializeView();


        btn_submit.setOnClickListener(submitListener);
        btn_regShop.setOnClickListener(registrationListener);
    }

    private void getAnchor() {
        try {
            showProgressDialog();
            params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_ANCHOR_COMMUNITIES);
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("param LINK_GET_ANCHOR_COMMUNITIES : " + params);
            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_ANCHOR_COMMUNITIES, params, new ObjListeners() {
                @Override
                public void onResponses(JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            JSONArray anchors = response.getJSONArray("anchors");
                            if (anchors.length() > 0) {
                                for (int i = 0; i < anchors.length(); i++) {
                                    JSONObject jsonObjectAnchor = anchors.getJSONObject(i);
                                    TagihModel tagihModel = new TagihModel();
                                    tagihModel.setId(jsonObjectAnchor.getString("anchor_id"));
                                    tagihModel.setAnchor_cust(jsonObjectAnchor.getString("anchor_cust"));
                                    tagihModel.setAnchor_name(jsonObjectAnchor.getString("anchor_name"));

                                    anchorDataList.add(tagihModel);
                                    JSONArray communities = jsonObjectAnchor.getJSONArray("communities");
                                    ArrayList<TagihCommunityModel> comList = new ArrayList<>();
                                    for (int j = 0; j < communities.length(); j++) {
                                        JSONObject jsonObjectCommunities = communities.getJSONObject(j);
                                        TagihCommunityModel tagihCommunityModel = new TagihCommunityModel();
                                        tagihCommunityModel.setId(jsonObjectCommunities.getString("comm_id"));
                                        tagihCommunityModel.setComm_code(jsonObjectCommunities.getString("comm_code"));
                                        tagihCommunityModel.setComm_name(jsonObjectCommunities.getString("comm_name"));
                                        comList.add(tagihCommunityModel);
                                    }

                                    tagihModel.setListCommunity(comList);
                                }
                            }
                            JSONArray rejectReasonArray = response.getJSONArray("reject_reason_codes");
                            if (rejectReasonArray.length() > 0) {
                                sp.edit().putString(DefineValue.REJECT_REASON, rejectReasonArray.toString()).commit();
                            }
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
                    initializeData();
                    dismissProgressDialog();
                }
            });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    Button.OnClickListener submitListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (inputValidation()) {
                sendDataTagih();
            }
        }
    };

    Button.OnClickListener registrationListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (inputValidation()) {
                Fragment newFrag = new FragShopLocation();
                Bundle bundle = new Bundle();
                bundle.putString(DefineValue.MEMBER_CODE, et_memberCode.getText().toString());
                bundle.putString(DefineValue.COMMUNITY_CODE, commCodeTagih);
                bundle.putString(DefineValue.COMMUNITY_NAME, communityNameArrayList.get(sp_communtiy.getSelectedItemPosition()));

                newFrag.setArguments(bundle);
                if (getActivity() == null) {
                    return;
                }
                TagihActivity ftf = (TagihActivity) getActivity();
                ftf.switchContent(newFrag, "Registrasi Alamat Toko", true);
            }
        }
    };


    private void initializeView() {
        sp_mitra = v.findViewById(R.id.sp_mitra);
        sp_communtiy = v.findViewById(R.id.sp_community);
        et_memberCode = v.findViewById(R.id.et_memberCode);
        btn_submit = v.findViewById(R.id.btn_submit);
        btn_regShop = v.findViewById(R.id.bt_registTokoDGI);
        tv_saldo_collector = v.findViewById(R.id.tv_saldoCollector);
        ll_komunitas = v.findViewById(R.id.ll_komunitas);

        if (is_search) {
            Timber.d("is_search initialize");
            et_memberCode.setText(memberCode);
        }

        if (isAgentLKD) {
            btn_regShop.setVisibility(View.GONE);
        }

        mitraNameArrayList.clear();

        mitraAdapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, mitraNameArrayList);
        sp_mitra.setAdapter(mitraAdapter);
    }

    public void initializeData() {
//        Realm _realm = RealmManager.getRealmTagih();
//        RealmResults<TagihModel> list = _realm.where(TagihModel.class).findAll();
//        mitraNameData.addAll(anchorDataList);
        mitraNameArrayList.add(getString(R.string.mitra_default));
        for (int i = 0; i < anchorDataList.size(); i++) {
            mitraNameArrayList.add(anchorDataList.get(i).getAnchor_name());
        }
        mitraAdapter.notifyDataSetChanged();

        communityAdapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, communityNameArrayList);
        sp_communtiy.setAdapter(communityAdapter);

        if (anchorNamePG != null) {
            int spinnerPosition = mitraAdapter.getPosition(anchorNamePG);
            sp_mitra.setSelection(spinnerPosition);
        }

        sp_mitra.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    ll_komunitas.setVisibility(View.VISIBLE);
                    initializeCommunity(position - 1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    public void initializeCommunity(int pos) {
//        Realm _realm = RealmManager.getRealmTagih();
//        final ArrayList<TagihCommunityModel> listTagih = new ArrayList<>();
        communityDataList.clear();
        communityDataList.addAll(anchorDataList.get(pos).getListCommunity());
//                _realm.where(TagihCommunityModel.class).findAll();
        Log.d("mainpage", "id : " + communityDataList.get(0).getId());
        communityNameArrayList.clear();
        communityNameArrayList.add(getString(R.string.community_default));
        for (int i = 0; i < communityDataList.size(); i++) {
            communityNameArrayList.add(communityDataList.get(i).getComm_name());
            Timber.d("comm code tagih : " + communityDataList.get(i).getComm_code());
        }
        communityAdapter.notifyDataSetChanged();

        if (communityDataList != null && communityDataList.size() > 0) {
            commCodeTagih = communityDataList.get(0).getComm_code();
        } else
            commCodeTagih = "";

        if (commNamePG != null) {
            int spinnerPosition = communityAdapter.getPosition(commNamePG);
            sp_communtiy.setSelection(spinnerPosition);
        }


        sp_communtiy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position != 0) {
                    position -= 1;
                    commCodeTagih = communityDataList.get(position).getComm_code();
                } else
                    commCodeTagih = "";
                Timber.d("comm code tagih selected: " + communityDataList.get(position).getComm_code() + " pos:" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
    }

    public Boolean inputValidation() {
        if (et_memberCode.getText().toString().equalsIgnoreCase("")) {
            et_memberCode.requestFocus();
            et_memberCode.setError(getString(R.string.error_input_tagih));
            return false;
        }
        if (commCodeTagih == null || commCodeTagih.equals("")) {
            sp_communtiy.requestFocus();
            Toast.makeText(getActivity(), getString(R.string.error_input_community), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void getBalanceCollector() {
        try {
            showProgressDialog();
            if (!memberIDLogin.isEmpty()) {

                extraSignature = memberIDLogin;

                params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_SALDO_COLLECTOR, extraSignature);
                params.put(WebParams.MEMBER_ID, memberIDLogin);
                params.put(WebParams.USER_ID, userPhoneID);
                params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
                params.put(WebParams.IS_AUTO, "Y");

                if (!memberIDLogin.isEmpty()) {

                    RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_SALDO_COLLECTOR, params,
                            new ObjListeners() {
                                @Override
                                public void onResponses(JSONObject response) {
                                    try {
                                        jsonModel model = getGson().fromJson(String.valueOf(response), jsonModel.class);
                                        String code = response.getString(WebParams.ERROR_CODE);
                                        if (code.equals(WebParams.SUCCESS_CODE)) {
                                            balanceCollector = response.getString(WebParams.AMOUNT);
                                            tv_saldo_collector.setText(CurrencyFormat.format(balanceCollector));
                                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                            String message = response.getString(WebParams.ERROR_MESSAGE);
                                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                                            test.showDialoginMain(getActivity(), message);
                                        } else if (code.equals(DefineValue.ERROR_9333)) {
                                            Timber.d("isi response app data:" + model.getApp_data());
                                            final AppDataModel appModel = model.getApp_data();
                                            AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                            alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                        } else if (code.equals(DefineValue.ERROR_0066)) {
                                            Timber.d("isi response maintenance:" + response.toString());
                                            AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                            alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                                        } else {
                                            code = response.getString(WebParams.ERROR_MESSAGE);
                                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
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
                                    dismissProgressDialog();
                                }
                            });
                }
            }
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    public void sendDataTagih() {
        showProgressDialog();

        String extraSignature = commCodeTagih + et_memberCode.getText().toString();
        params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_LIST_INVOICE_DGI, extraSignature);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.MEMBER_CODE, et_memberCode.getText().toString());
        params.put(WebParams.COMM_CODE, commCodeTagih);
        params.put(WebParams.USER_ID, userPhoneID);
        Timber.d("params list invoice DGI : " + params.toString());

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_LIST_INVOICE_DGI, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            dismissProgressDialog();
                            jsonModel model = getGson().fromJson(String.valueOf(response), jsonModel.class);
                            Timber.d("response list invoice DGI : " + response.toString());
                            String code = response.getString(WebParams.ERROR_CODE);
                            String error_message = response.getString(WebParams.ERROR_MESSAGE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                String responseListInvoice = response.toString();
                                Fragment newFrag = new FragListInvoiceTagih();
                                Bundle bundle = new Bundle();
                                bundle.putString(DefineValue.MEMBER_CODE, et_memberCode.getText().toString());
                                bundle.putString(DefineValue.COMMUNITY_CODE, commCodeTagih);
                                bundle.putString(DefineValue.RESPONSE, responseListInvoice);
                                bundle.putString(DefineValue.TXID_PG, txIdPG);

                                SecurePreferences.Editor mEditor = sp.edit();
                                mEditor.putString(DefineValue.COMM_CODE_DGI, response.getString(WebParams.COMM_CODE_DGI));
                                mEditor.apply();

                                newFrag.setArguments(bundle);
                                if (getActivity() == null) {
                                    return;
                                }
                                TagihActivity ftf = (TagihActivity) getActivity();
                                ftf.switchContent(newFrag, "List Invoice", true);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + response.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                            } else {
                                Toast.makeText(getActivity(), error_message, Toast.LENGTH_LONG).show();
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


}
