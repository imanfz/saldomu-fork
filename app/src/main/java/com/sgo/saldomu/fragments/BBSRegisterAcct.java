package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.BBSComm;
import com.sgo.saldomu.Beans.BBSCommBenef;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.entityRealm.List_BBS_City;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import timber.log.Timber;


public class BBSRegisterAcct extends Fragment {

    public final static String TAG = "com.sgo.saldomu.fragments.BBSRegisterAcct";
    private final static String TYPE_ACCT = "ACCT";

    private View v;
    private ArrayList<BBSComm> listDataComm;
    private ArrayList<BBSCommBenef> listDataBank;
    private ArrayList<String> list_name_bbs_cities;
    AutoCompleteTextView city_textview_autocomplete;
    private Realm realm;
    private View cityLayout;
    private ProgressDialog progdialog;
    private Spinner spSourceAcct, spComm;
    private ProgressBar progBarComm;
    private ProgressBar progBarBank;
    private EditText etNoBenefAcct, etNameBenefAcct;
    private String userID, accessKey;
    private Integer CityAutocompletePos = -1;
    private ArrayAdapter<String> adapterDataComm, adapterDataBank, adapterDataCity;
    private ActionListener actionListener;
    private ArrayList<List_BBS_City> list_bbs_cities;
    public Boolean isUpdate = false;
    private TextView tvEgNo;

    public interface ActionListener{
        void OnSuccessReqAcct(Bundle data);
        void OnEmptyCommunity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");
        listDataComm = new ArrayList<>();
        listDataBank = new ArrayList<>();
        progdialog = DefinedDialog.CreateProgressDialog(getContext(),"");
        progdialog.dismiss();
        Bundle bundle = getArguments();
        if(bundle.containsKey(DefineValue.IS_UPDATE))
            isUpdate = bundle.getBoolean(DefineValue.IS_UPDATE,false);

        if(isUpdate){
            BBSComm bbsComm = new BBSComm();
            bbsComm.setCommCode(bundle.getString(DefineValue.COMMUNITY_CODE));
            bbsComm.setCommId(bundle.getString(DefineValue.COMMUNITY_ID));
            bbsComm.setCommName(bundle.getString(DefineValue.COMMUNITY_NAME));
            bbsComm.setMemberCode(bundle.getString(DefineValue.MEMBER_CODE));
            listDataComm.add(bbsComm);

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
//        Button emptySpin = (Button) v.findViewById(R.id.empty_spin);
//        emptySpin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                retrieveComm();
//            }
//        });
        spComm = (Spinner) v.findViewById(R.id.spinner_community);
//        spComm.setEmptyView(emptySpin);
        progBarComm = (ProgressBar) v.findViewById(R.id.loading_progres_comm);
        spSourceAcct = (Spinner) v.findViewById(R.id.bbsregistacct_value_bank_benef);
        progBarBank = (ProgressBar) v.findViewById(R.id.loading_progres_bank_benef);
        etNoBenefAcct = (EditText) v.findViewById(R.id.bbsregistacct_value_no_acct_benef);
        etNameBenefAcct = (EditText) v.findViewById(R.id.bbsregistacct_value_name_acct_benef);
        cityLayout = v.findViewById(R.id.bbsregistacct_city_layout);
        Button btnSave = (Button) v.findViewById(R.id.btn_save);
        city_textview_autocomplete = (AutoCompleteTextView) v.findViewById(R.id.bbsregistacct_value_city_benef2);
        tvEgNo = (TextView) v.findViewById(R.id.tv_eg_no);

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
                if(productType.equalsIgnoreCase(TYPE_ACCT)) {
                    cityLayout.setVisibility(View.VISIBLE);
                    tvEgNo.setText(getString(R.string.eg_no_acct));
                }
                else {
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
        results.addChangeListener(new RealmChangeListener<RealmResults<List_BBS_City>>() {
            @Override
            public void onChange(RealmResults<List_BBS_City> element) {
                if(getActivity() != null && !getActivity().isFinishing()) {
                    for (List_BBS_City bbsCity : element) {
                        list_bbs_cities.add(bbsCity);
                        list_name_bbs_cities.add(bbsCity.getCity_name());
                    }

                    ArrayAdapter<String> city_adapter = new ArrayAdapter<String>
                            (getActivity(),android.R.layout.simple_selectable_list_item, list_name_bbs_cities);

                    city_textview_autocomplete.setThreshold(1);
                    city_textview_autocomplete.setAdapter(city_adapter);

                    if (bundle.containsKey(DefineValue.BENEF_CITY)) {
                        city_textview_autocomplete.setText(bundle.getString(DefineValue.BENEF_CITY));
                    }
                }
            }
        });

        if(isUpdate){
            etNoBenefAcct.setText(bundle.getString(DefineValue.NO_BENEF));
            etNameBenefAcct.setText(bundle.getString(DefineValue.NAME_BENEF));
            spSourceAcct.setEnabled(false);
            spComm.setEnabled(false);

            adapterDataComm.add(listDataComm.get(0).getCommName());
            adapterDataBank.add(listDataBank.get(0).getProduct_name());
            CommunityUIRefresh();
        }
        else {
            spComm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    retreiveBank(listDataComm.get(position).getCommCode());
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

    }

    Button.OnClickListener saveListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(inputValidation()) {
                String benefAcctType =  listDataBank.get(spSourceAcct.getSelectedItemPosition()).getProduct_type();
                String city_id = "" ;
                if(benefAcctType.equalsIgnoreCase(TYPE_ACCT))
                    city_id = list_bbs_cities.get(CityAutocompletePos).getCity_id();
                sentReqAcct(listDataComm.get(spComm.getSelectedItemPosition()).getCommCode(),
                        listDataComm.get(spComm.getSelectedItemPosition()).getMemberCode(),
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
            if(context instanceof ActionListener){
                actionListener = (ActionListener) context;
            }
            else {
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

    @Override
    public void onResume() {
        super.onResume();
        if(!isUpdate)
            retrieveComm();
    }

    public boolean inputValidation(){
        if(etNoBenefAcct.getText().toString().length()==0){
            etNoBenefAcct.requestFocus();
            etNoBenefAcct.setError(getString(R.string.bbsreg_et_error_rekbenef));
            return false;
        }
        if(etNameBenefAcct.getText().toString().length()==0){
            etNameBenefAcct.requestFocus();
            etNameBenefAcct.setError(getString(R.string.bbsreg_et_error_namebenef));
            return false;
        }

        if(cityLayout.getVisibility() == View.VISIBLE) {
            String autocomplete_text = city_textview_autocomplete.getText().toString();

            if (autocomplete_text.equals("")){
                city_textview_autocomplete.requestFocus();
                city_textview_autocomplete.setError("Kota tujuan harus diisi!");
                return false;
            }else if (!list_name_bbs_cities.contains(autocomplete_text)){

                city_textview_autocomplete.requestFocus();
                city_textview_autocomplete.setError("Nama kota tidak ditemukan!");
                return false;
            }else {
                CityAutocompletePos = list_name_bbs_cities.indexOf(autocomplete_text);
                city_textview_autocomplete.setError(null);
            }
        }

        if(listDataBank.size() == 0)
            return false;

        return true;
    }

    private void CommunityUIRefresh(){
        if(listDataComm.size() < 1) {
            Toast.makeText(getActivity(), R.string.joinagentbbs_toast_empty_comm, Toast.LENGTH_LONG).show();
            actionListener.OnEmptyCommunity();
        }

        if(listDataComm.size() == 1) {
            TextView tvCommName = (TextView) v.findViewById(R.id.tv_comm_value);
            tvCommName.setText(listDataComm.get(0).getCommName());
            tvCommName.setVisibility(View.VISIBLE);
            spComm.setVisibility(View.INVISIBLE);
            if(isUpdate)
                BankUIRefresh();
            else
                retreiveBank(listDataComm.get(0).getCommCode());
        }
        else {
            spComm.setVisibility(View.VISIBLE);
        }
    }

    private void BankUIRefresh(){
        if(listDataBank.size() == 1) {
            TextView tvBankName = (TextView) v.findViewById(R.id.tv_bank_value);
            tvBankName.setText(listDataBank.get(0).getProduct_name());
            tvBankName.setVisibility(View.VISIBLE);
            spSourceAcct.setVisibility(View.INVISIBLE);
        }
        else {
            spSourceAcct.setVisibility(View.VISIBLE);
        }
    }

    private void retrieveComm(){
        if(progBarComm.getVisibility() == View.GONE) {
            try {
                RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID, MyApiClient.LINK_GLOBAL_BBS_COMM,
                        userID, accessKey);
                params.put(WebParams.CUSTOMER_ID, userID);
                params.put(WebParams.SCHEME_CODE, DefineValue.ATC);
                params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
                params.put(WebParams.USER_ID, userID);
                Timber.d("isi params retreiveComm:" + params.toString());

                spComm.setVisibility(View.GONE);
                progBarComm.setVisibility(View.VISIBLE);
                MyApiClient.getGlobalBBSComm(getActivity(), TAG, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            String code = response.getString(WebParams.ERROR_CODE);
                            Timber.d("Isi response retreiveComm: " + response.toString());
                            listDataComm.clear();
                            adapterDataComm.clear();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                JSONArray comm = response.optJSONArray(WebParams.COMMUNITY);
                                if (comm != null && comm.length() > 0) {
                                    BBSComm bbsComm;
                                    for (int i = 0; i < comm.length(); i++) {
                                        bbsComm = new BBSComm(comm.getJSONObject(i).optString(WebParams.COMM_ID),
                                                comm.getJSONObject(i).optString(WebParams.COMM_CODE),
                                                comm.getJSONObject(i).optString(WebParams.COMM_NAME),
                                                comm.getJSONObject(i).optString(WebParams.API_KEY),
                                                comm.getJSONObject(i).optString(WebParams.MEMBER_CODE),
                                                comm.getJSONObject(i).optString(WebParams.CALLBACK_URL));
                                        listDataComm.add(bbsComm);
                                        adapterDataComm.add(bbsComm.getCommName());
                                    }
                                }
                            } else {
                                code = response.getString(WebParams.ERROR_MESSAGE);
                                Toast.makeText(getActivity(), code, Toast.LENGTH_SHORT).show();
                            }
                            adapterDataComm.notifyDataSetChanged();
                            CommunityUIRefresh();
                            progBarComm.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        failure(throwable);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        failure(throwable);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        failure(throwable);
                    }

                    private void failure(Throwable throwable) {
                        if (MyApiClient.PROD_FAILURE_FLAG)
                            Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                        Timber.w("Error Koneksi retreiveComm:" + throwable.toString());
                        progBarComm.setVisibility(View.GONE);
                        actionListener.OnEmptyCommunity();
                    }
                });
            } catch (Exception e) {
                Timber.d("httpclient: " + e.getMessage());
            }
        }
    }

    private void retreiveBank(String comm_code){
        try{
            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_BBS_BANK_REG_ACCT,
                    userID,accessKey);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);
            Timber.d("isi params retreive bank:" + params.toString());

            spSourceAcct.setVisibility(View.GONE);
            progBarBank.setVisibility(View.VISIBLE);
            MyApiClient.sentBBSBankRegAcct(getActivity(),TAG, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("Isi response retreive bank: "+response.toString());
                        listDataBank.clear();
                        adapterDataBank.clear();
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            JSONArray bank = response.optJSONArray(WebParams.BANK_DATA);
                            if(bank != null && bank.length() > 0) {
                                BBSCommBenef bbsCommBenef;
                                for (int i = 0; i < bank.length(); i++) {
                                    bbsCommBenef = new BBSCommBenef(bank.getJSONObject(i).optString(WebParams.PRODUCT_CODE),
                                            bank.getJSONObject(i).optString(WebParams.PRODUCT_NAME),
                                            bank.getJSONObject(i).optString(WebParams.PRODUCT_TYPE));
                                    listDataBank.add(bbsCommBenef);
                                    adapterDataBank.add(bbsCommBenef.getProduct_name());
                                }
                            }
                        }
                        else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(),code, Toast.LENGTH_SHORT).show();
                        }
                        adapterDataBank.notifyDataSetChanged();
                        spSourceAcct.setVisibility(View.VISIBLE);
                        progBarBank.setVisibility(View.GONE);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable) {
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    Timber.w("Error Koneksi retreive bank:"+throwable.toString());
                    spSourceAcct.setVisibility(View.VISIBLE);
                    progBarBank.setVisibility(View.GONE);
                }
            });
        }catch (Exception e){
            Timber.d("httpclient: "+e.getMessage());
        }
    }

    private void sentReqAcct(final String commCode, final String memberCode, final String benefAcctType, final String benefBankCode,
                             final String benefAcctNo, final String benefAcctCity, final String benefAcctName){
        try{
            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_BBS_REQ_ACCT,
                    userID,accessKey);
            params.put(WebParams.COMM_CODE, commCode);
            params.put(WebParams.MEMBER_CODE, memberCode);
            params.put(WebParams.BENEF_ACCT_TYPE, benefAcctType);
            params.put(WebParams.BENEF_BANK_CODE, benefBankCode);
            params.put(WebParams.BENEF_ACCT_NO, benefAcctNo);
            params.put(WebParams.BENEF_ACCT_NAME, benefAcctName);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);

            if(benefAcctType.equalsIgnoreCase(TYPE_ACCT))
                params.put(WebParams.BENEF_ACCT_CITY, benefAcctCity);
            Timber.d("isi params sentReqAcct:" + params.toString());

            progdialog.show();
            MyApiClient.sentBBSReqAcct(getActivity(),TAG, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("Isi response sentReqAcct: "+response.toString());

                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Bundle bundle = new Bundle();
                            bundle.putString(DefineValue.COMMUNITY_NAME,listDataComm.get(spComm.getSelectedItemPosition()).getCommName());
                            bundle.putString(DefineValue.COMMUNITY_CODE,response.getString(WebParams.COMM_CODE));
                            bundle.putString(DefineValue.COMMUNITY_ID,listDataComm.get(spComm.getSelectedItemPosition()).getCommId());
                            bundle.putString(DefineValue.MEMBER_CODE,response.getString(WebParams.MEMBER_CODE));
                            bundle.putString(DefineValue.ACCT_TYPE,response.getString(WebParams.BENEF_ACCT_TYPE));
                            bundle.putString(DefineValue.BANK_CODE,response.getString(WebParams.BENEF_BANK_CODE));
                            bundle.putString(DefineValue.BANK_NAME,response.getString(WebParams.BENEF_BANK_NAME));
                            bundle.putString(DefineValue.ACCT_NO,response.getString(WebParams.BENEF_ACCT_NO));
                            bundle.putString(DefineValue.ACCT_NAME,response.getString(WebParams.BENEF_ACCT_NAME));
                            bundle.putString(DefineValue.ACCT_CITY_NAME,response.optString(WebParams.BENEF_CITY_NAME,""));
                            bundle.putString(DefineValue.ACCT_CITY_CODE,response.optString(WebParams.BENEF_CITY_CODE,""));
                            bundle.putString(DefineValue.TX_ID,response.getString(WebParams.TX_ID));

                            actionListener.OnSuccessReqAcct(bundle);
                        }
                        else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(),code, Toast.LENGTH_SHORT).show();
                        }
                       progdialog.dismiss();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable) {
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    Timber.w("Error Koneksi sentReqAcct:"+throwable.toString());
                   progdialog.dismiss();
                }
            });
        }catch (Exception e){
            Timber.d("httpclient: "+e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        MyApiClient.CancelRequestWSByTag(TAG,true);
        super.onDestroy();
    }
}
