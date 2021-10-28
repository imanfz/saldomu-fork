package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.sgo.saldomu.Beans.BBSCommBenef;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.TutorialActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.entityRealm.BBSCommModel;
import com.sgo.saldomu.entityRealm.List_BBS_City;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.BBSRegAcctModel;
import com.sgo.saldomu.models.retrofit.BBSRetrieveBankModel;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;


public class BBSRegisterAcct extends BaseFragment {

    public final static String TAG = "com.sgo.saldomu.fragments.BBSRegisterAcct";
    private final static String TYPE_ACCT = "ACCT";

    private View v;
    private BBSCommModel dataComm;
    private ArrayList<BBSCommBenef> listDataBank;
    private ArrayList<String> list_name_bbs_cities;
    AutoCompleteTextView city_textview_autocomplete;
    private Realm realm;
    private View cityLayout;
    private ProgressDialog progdialog;
    private Spinner spSourceAcct, spComm;
    private ProgressBar progBarBank;
    private EditText etNoBenefAcct, etNameBenefAcct;
    private Integer CityAutocompletePos = -1;
    private ArrayAdapter<String> adapterDataComm, adapterDataBank;
    private ActionListener actionListener;
    private ArrayList<List_BBS_City> list_bbs_cities;
    public Boolean isUpdate = false;
    private TextView tvEgNo;

    public interface ActionListener {
        void OnSuccessReqAcct(Bundle data);

        void OnEmptyCommunity();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.information, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_information:
                showTutorial();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();

        listDataBank = new ArrayList<>();
        progdialog = DefinedDialog.CreateProgressDialog(getContext(), "");
        progdialog.dismiss();
        Bundle bundle = getArguments();
        if (bundle.containsKey(DefineValue.IS_UPDATE))
            isUpdate = bundle.getBoolean(DefineValue.IS_UPDATE, false);

        dataComm = new BBSCommModel();
        dataComm.setComm_code(bundle.getString(DefineValue.COMMUNITY_CODE));
        dataComm.setComm_id(bundle.getString(DefineValue.COMMUNITY_ID));
        dataComm.setComm_name(bundle.getString(DefineValue.COMMUNITY_NAME));
        dataComm.setMember_code(bundle.getString(DefineValue.MEMBER_CODE));

        if (isUpdate) {
            BBSCommBenef bbsCommBenef = new BBSCommBenef();
            bbsCommBenef.setProduct_type(bundle.getString(DefineValue.PRODUCT_TYPE));
            bbsCommBenef.setProduct_name(bundle.getString(DefineValue.PRODUCT_NAME));
            bbsCommBenef.setProduct_code(bundle.getString(DefineValue.PRODUCT_CODE));
            listDataBank.add(bbsCommBenef);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_bbs_register_acct_input, container, false);
        spComm = v.findViewById(R.id.spinner_community);
        spSourceAcct = v.findViewById(R.id.bbsregistacct_value_bank_benef);
        progBarBank = v.findViewById(R.id.loading_progres_bank_benef);
        etNoBenefAcct = v.findViewById(R.id.bbsregistacct_value_no_acct_benef);
        etNameBenefAcct = v.findViewById(R.id.bbsregistacct_value_name_acct_benef);
        cityLayout = v.findViewById(R.id.bbsregistacct_city_layout);
        Button btnSave = v.findViewById(R.id.btn_save);
        city_textview_autocomplete = v.findViewById(R.id.bbsregistacct_value_city_benef2);
        tvEgNo = v.findViewById(R.id.tv_eg_no);

        btnSave.setOnClickListener(saveListener);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Bundle bundle = getArguments();

        final ArrayList<String> spinDataComm = new ArrayList<>();
        adapterDataComm = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, spinDataComm);
        adapterDataComm.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spComm.setAdapter(adapterDataComm);

        ArrayList<String> spinDataBank = new ArrayList<>();
        adapterDataBank = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, spinDataBank);
        adapterDataBank.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSourceAcct.setAdapter(adapterDataBank);
        spSourceAcct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String productType = listDataBank.get(position).getProduct_type();
                if (productType.equalsIgnoreCase(TYPE_ACCT)) {
                    cityLayout.setVisibility(View.VISIBLE);
                    tvEgNo.setText(getString(R.string.eg_no_acct));
                } else {
                    cityLayout.setVisibility(View.GONE);
                    tvEgNo.setText(getString(R.string.eg_no_hp));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        RealmResults<List_BBS_City> results = realm.where(List_BBS_City.class).findAllAsync();
        list_name_bbs_cities = new ArrayList<>();
        list_bbs_cities = new ArrayList<>();
        results.addChangeListener(element -> {
            if (getActivity() != null && !getActivity().isFinishing()) {
                for (List_BBS_City bbsCity : element) {
                    list_bbs_cities.add(bbsCity);
                    list_name_bbs_cities.add(bbsCity.getCity_name());
                }

                ArrayAdapter<String> city_adapter = new ArrayAdapter<String>
                        (getActivity(), android.R.layout.simple_selectable_list_item, list_name_bbs_cities);

                city_textview_autocomplete.setText("KOTA JAKARTA");
//                    city_textview_autocomplete.setThreshold(1);
                city_textview_autocomplete.setAdapter(city_adapter);

                if (bundle.containsKey(DefineValue.BENEF_CITY)) {
                    city_textview_autocomplete.setText(bundle.getString(DefineValue.BENEF_CITY));
                }
            }
        });

        if (isUpdate) {
            etNoBenefAcct.setText(bundle.getString(DefineValue.NO_BENEF));
            etNameBenefAcct.setText(bundle.getString(DefineValue.NAME_BENEF));
            spSourceAcct.setEnabled(false);
            spComm.setEnabled(false);

            adapterDataBank.add(listDataBank.get(0).getProduct_name());
        } else {
            spComm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                    retreiveBank(listDataComm.get(position).getCommCode());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        CommunityUIRefresh();
        validasiTutorial();
    }

    private void validasiTutorial() {
        if (sp.contains(DefineValue.TUTORIAL_TAMBAH_REKENING)) {
            Boolean is_first_time = sp.getBoolean(DefineValue.TUTORIAL_TAMBAH_REKENING, false);
            if (is_first_time)
                showTutorial();
        } else {
            showTutorial();
        }
    }

    private void showTutorial() {
        Intent intent = new Intent(getActivity(), TutorialActivity.class);
        intent.putExtra(DefineValue.TYPE, TutorialActivity.tutorial_tambahRekening);
        startActivity(intent);
    }

    Button.OnClickListener saveListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (inputValidation()) {
                String benefAcctType = listDataBank.get(spSourceAcct.getSelectedItemPosition()).getProduct_type();
                String city_id = "";
                if (benefAcctType.equalsIgnoreCase(TYPE_ACCT))
                    city_id = list_bbs_cities.get(CityAutocompletePos).getCity_id();
                sentReqAcct(dataComm.getComm_code(),
                        dataComm.getMember_code(),
                        benefAcctType,
                        listDataBank.get(spSourceAcct.getSelectedItemPosition()).getProduct_code(),
                        etNoBenefAcct.getText().toString(),
                        city_id,
                        etNameBenefAcct.getText().toString());
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getTargetFragment() instanceof ActionListener) {
            actionListener = (ActionListener) getTargetFragment();
        } else {
            if (context instanceof ActionListener) {
                actionListener = (ActionListener) context;
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement ActionListener RegisterAcct");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        actionListener = null;
    }


    public boolean inputValidation() {
        if (progBarBank.getVisibility() == View.VISIBLE)
            return false;

        if (listDataBank.size() == 0)
            return false;

        if (etNoBenefAcct.getText().toString().length() == 0) {
            etNoBenefAcct.requestFocus();
            etNoBenefAcct.setError(getString(R.string.bbsreg_et_error_rekbenef));
            return false;
        }
        if (etNameBenefAcct.getText().toString().length() == 0) {
            etNameBenefAcct.requestFocus();
            etNameBenefAcct.setError(getString(R.string.bbsreg_et_error_namebenef));
            return false;
        }

        if (cityLayout.getVisibility() == View.VISIBLE) {
            String autocomplete_text = city_textview_autocomplete.getText().toString();

            if (autocomplete_text.equals("")) {
                city_textview_autocomplete.requestFocus();
                city_textview_autocomplete.setError("Kota tujuan harus diisi!");
                return false;
            } else if (!list_name_bbs_cities.contains(autocomplete_text)) {

                city_textview_autocomplete.requestFocus();
                city_textview_autocomplete.setError("Nama kota tidak ditemukan!");
                return false;
            } else {
                CityAutocompletePos = list_name_bbs_cities.indexOf(autocomplete_text);
                city_textview_autocomplete.setError(null);
            }
        }

        return listDataBank.size() != 0;
    }

    private void CommunityUIRefresh() {
        if (dataComm == null) {
            Toast.makeText(getActivity(), R.string.joinagentbbs_toast_empty_comm, Toast.LENGTH_LONG).show();
            actionListener.OnEmptyCommunity();
        } else {
            TextView tvCommName = v.findViewById(R.id.tv_comm_value);
            tvCommName.setText(dataComm.getComm_name());
            tvCommName.setVisibility(View.VISIBLE);
            spComm.setVisibility(View.INVISIBLE);
            if (isUpdate)
                BankUIRefresh();
            else
                retreiveBank(dataComm.getComm_code());
        }
    }

    private void BankUIRefresh() {
        if (listDataBank.size() == 1) {
            TextView tvBankName = v.findViewById(R.id.tv_bank_value);
            tvBankName.setText(listDataBank.get(0).getProduct_name());
            tvBankName.setVisibility(View.VISIBLE);
            spSourceAcct.setVisibility(View.INVISIBLE);
        } else {
            spSourceAcct.setVisibility(View.VISIBLE);
        }
    }

    private void retreiveBank(String comm_code) {
        try {
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_BBS_BANK_REG_ACCT, comm_code);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userPhoneID);
            Timber.d("isi params retreive bank:%s", params.toString());

            spSourceAcct.setVisibility(View.GONE);
            progBarBank.setVisibility(View.VISIBLE);

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_BBS_BANK_REG_ACCT, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            try {

                                BBSRetrieveBankModel model = getGson().fromJson(object, BBSRetrieveBankModel.class);

                                String code = model.getError_code();
                                String message = model.getError_message();
                                listDataBank.clear();
                                adapterDataBank.clear();
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    JSONArray bank = new JSONArray(getGson().toJson(model.getBank_data()));

                                    if (bank != null && bank.length() > 0) {
                                        BBSCommBenef bbsCommBenef;
                                        for (int i = 0; i < bank.length(); i++) {
                                            bbsCommBenef = new BBSCommBenef(bank.getJSONObject(i).optString(WebParams.PRODUCT_CODE),
                                                    bank.getJSONObject(i).optString(WebParams.PRODUCT_NAME),
                                                    bank.getJSONObject(i).optString(WebParams.PRODUCT_TYPE));
                                            listDataBank.add(bbsCommBenef);
                                            adapterDataBank.add(bbsCommBenef.getProduct_name());
                                        }
                                    }
                                }else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:%s", model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:%s", object.toString());
                                    AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                                } else {
                                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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
                            adapterDataBank.notifyDataSetChanged();
                            spSourceAcct.setVisibility(View.VISIBLE);
                            progBarBank.setVisibility(View.GONE);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient: %s", e.getMessage());
        }
    }

    private void sentReqAcct(final String commCode, final String memberCode, final String benefAcctType, final String benefBankCode,
                             final String benefAcctNo, final String benefAcctCity, final String benefAcctName) {
        try {
            extraSignature = commCode + memberCode + benefAcctType + benefBankCode + benefAcctNo;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_BBS_REQ_ACCT, extraSignature);
            params.put(WebParams.COMM_CODE, commCode);
            params.put(WebParams.MEMBER_CODE, memberCode);
            params.put(WebParams.BENEF_ACCT_TYPE, benefAcctType);
            params.put(WebParams.BENEF_BANK_CODE, benefBankCode);
            params.put(WebParams.BENEF_ACCT_NO, benefAcctNo);
            params.put(WebParams.BENEF_ACCT_NAME, benefAcctName);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userPhoneID);

            if (benefAcctType.equalsIgnoreCase(TYPE_ACCT))
                params.put(WebParams.BENEF_ACCT_CITY, benefAcctCity);
            Timber.d("isi params sentReqAcct:%s", params.toString());

            progdialog.show();

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_BBS_REQ_ACCT, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            BBSRegAcctModel response = getGson().fromJson(object, BBSRegAcctModel.class);

                            String code = response.getError_code();
                            String message = response.getError_message();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                Bundle bundle = new Bundle();
                                bundle.putString(DefineValue.COMMUNITY_NAME, dataComm.getComm_name());
                                bundle.putString(DefineValue.COMMUNITY_CODE, response.getComm_code());
                                bundle.putString(DefineValue.COMMUNITY_ID, dataComm.getComm_id());
                                bundle.putString(DefineValue.MEMBER_CODE, response.getMember_code());
                                bundle.putString(DefineValue.ACCT_TYPE, response.getBenef_acct_type());
                                bundle.putString(DefineValue.BANK_CODE, response.getBenef_bank_code());
                                bundle.putString(DefineValue.BANK_NAME, response.getBenef_bank_name());
                                bundle.putString(DefineValue.ACCT_NO, response.getBenef_acct_no());
                                bundle.putString(DefineValue.ACCT_NAME, response.getBenef_acct_name());
                                bundle.putString(DefineValue.ACCT_CITY_NAME, response.getBenef_city_name());
                                bundle.putString(DefineValue.ACCT_CITY_CODE, response.getBenef_city_code());
                                bundle.putString(DefineValue.ACCT_NO_CURRENT, getArguments().getString(DefineValue.NO_BENEF));
                                bundle.putString(DefineValue.TX_ID, response.getTx_id());

                                actionListener.OnSuccessReqAcct(bundle);
                            }else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:%s", response.getApp_data());
                                final AppDataModel appModel = response.getApp_data();
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:%s", response.toString());
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                            } else {
                                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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
        } catch (Exception e) {
            Timber.d("httpclient: %s", e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        RetrofitService.dispose();
        RealmManager.closeRealm(realm);
        super.onDestroy();
    }
}
