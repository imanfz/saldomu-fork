package com.sgo.saldomu.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.faber.circlestepview.CircleStepView;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.BBSComm;
import com.sgo.saldomu.Beans.CashInHistoryModel;
import com.sgo.saldomu.Beans.CashOutHistoryModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.TutorialActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.entityRealm.List_BBS_City;
import com.sgo.saldomu.widgets.CustomAutoCompleteTextView;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.entityRealm.BBSBankModel;
import com.sgo.saldomu.entityRealm.BBSCommModel;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

/**
 * Created by thinkpad on 4/20/2017.
 */

public class BBSTransaksiAmount extends Fragment {
    public final static String TAG = "com.sgo.saldomu.fragments.BBSTransaksiAmount";

    private View v, inputForm, emptyLayout, cityLayout, nameLayout, emptyCashoutBenefLayout;
    private ProgressDialog progdialog;
    private TextView tvTitle;
    private EditText etAmount;
    private String transaksi, comm_code, member_code, benef_product_type, api_key,
            callback_url, comm_id, userID, accessKey, comm_benef_atc, type, defaultAmount, noHpPengirim, benef_product_code, source_product_code,
            source_product_type, source_product_h2h;
    private Activity act;
    private Button btnProses, btnBack;
    private Realm realm, realmBBS;
    private String CTA = "CTA";
    private String ATC = "ATC";
    private String SOURCE = "SOURCE";
    private String BENEF = "BENEF";
    private CustomAutoCompleteTextView actv_rekening_member;
    private List<HashMap<String,String>> aListMember;
    private SimpleAdapter adapterMember;
    private List<BBSBankModel> listbankSource, listbankBenef;
    private ArrayList<BBSComm> listDataComm;
    private EditText etNoAcct, etNameAcct;
    private TextView tvEgNo;
    private ImageView spinwheelCity;
    private AutoCompleteTextView spBenefCity;
    private Animation frameAnimation;
    private ArrayList<List_BBS_City> list_bbs_cities;
    private ArrayList<String> list_name_bbs_cities;
    private Integer CityAutocompletePos = -1;
    private boolean isBack = false;
    SecurePreferences sp;
    CashInHistoryModel cashInHistoryModel;
    CashOutHistoryModel cashOutHistoryModel;
    BBSCommModel comm;

    public boolean isBack() {
        return isBack;
    }

    public void setBack(boolean back) {
        isBack = back;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        act = getActivity();
        realm = Realm.getDefaultInstance();
        realmBBS = Realm.getInstance(RealmManager.BBSConfiguration);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");

        Bundle bundle = getArguments();
        if (bundle != null) {
            transaksi = bundle.getString(DefineValue.TRANSACTION);
            type = bundle.getString(DefineValue.TYPE,"");
            defaultAmount = bundle.getString(DefineValue.AMOUNT,"");
            noHpPengirim = bundle.getString(DefineValue.KEY_CODE,"");


            if(transaksi.equalsIgnoreCase(getString(R.string.cash_in)))
            {
                String cashIn = sp.getString("cashin_history_temp", "");
                Gson gson = new Gson();
                cashInHistoryModel = gson.fromJson(cashIn, CashInHistoryModel.class);

                if (!cashIn.equalsIgnoreCase("") && cashIn!=null) {
                    benef_product_code = cashInHistoryModel.getBenef_product_code();
                    benef_product_type= cashInHistoryModel.getBenef_product_type();
                }
            }
            else if (transaksi.equalsIgnoreCase(getString(R.string.cash_out))){
                String cashOut = sp.getString("cashout_history_temp", "");
                Gson gson1 = new Gson();
                cashOutHistoryModel = gson1.fromJson(cashOut, CashOutHistoryModel.class);

                if (!cashOut.equalsIgnoreCase("") && cashOut!=null) {
                    source_product_code = cashOutHistoryModel.getSource_product_code();
                    source_product_type= cashOutHistoryModel.getSource_product_type();
                    source_product_h2h = cashOutHistoryModel.getSource_product_h2h();
                }
            }
        } else {
            getFragmentManager().popBackStack();
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v =  inflater.inflate(R.layout.bbs_transaksi_amount, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CircleStepView mCircleStepView = ((CircleStepView) v.findViewById(R.id.circle_step_view));
        mCircleStepView.setTextBelowCircle(getString(R.string.transaction), getString(R.string.informasi), getString(R.string.konfirmasi));
        mCircleStepView.setCurrentCircleIndex(0, false);

        tvTitle = (TextView) v.findViewById(R.id.tv_title);
        inputForm = v.findViewById(R.id.bbs_amount_form);
        emptyLayout = v.findViewById(R.id.empty_layout);
        emptyCashoutBenefLayout = v.findViewById(R.id.empty_cashout_benef_layout);
        etAmount = (EditText) v.findViewById(R.id.jumlah_transfer_edit);
        btnProses = (Button) v.findViewById(R.id.proses_btn);
        btnBack = (Button) v.findViewById(R.id.back_btn);
        ViewStub stub = (ViewStub) v.findViewById(R.id.transaksi_stub);
        tvTitle.setText(transaksi);
        emptyLayout.setVisibility(View.GONE);

        if(!isBack) listDataComm = new ArrayList<>();

        if (transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
            if(type.equalsIgnoreCase(DefineValue.BBS_CASHIN)){
                if(!defaultAmount.equals(""))
                {
                    etAmount.setText(defaultAmount);
                }
                else
                {
                    if (cashInHistoryModel!=null)
                    {
                        etAmount.setText(cashInHistoryModel.getAmount());
                    }
                }
            }
            else if (cashInHistoryModel!=null)
            {
                etAmount.setText(cashInHistoryModel.getAmount());
            }

            stub.setLayoutResource(R.layout.bbs_cashin_amount);
            View cashin_layout = stub.inflate();

            nameLayout = cashin_layout.findViewById(R.id.bbs_cashin_name_layout);
            actv_rekening_member = (CustomAutoCompleteTextView) cashin_layout.findViewById(R.id.rekening_member_value);
            etNoAcct = (EditText) cashin_layout.findViewById(R.id.no_tujuan_value);
            tvEgNo = (TextView) cashin_layout.findViewById(R.id.tv_eg_no);
            etNameAcct = (EditText) cashin_layout.findViewById(R.id.name_value);
            cityLayout = cashin_layout.findViewById(R.id.bbscashin_city_layout);
            spBenefCity = (AutoCompleteTextView) cashin_layout.findViewById(R.id.bbscashin_value_city_benef);
            spinwheelCity = (ImageView) cashin_layout.findViewById(R.id.spinning_wheel_bbscashin_city);
            frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
            frameAnimation.setRepeatCount(Animation.INFINITE);

            if (cashInHistoryModel!=null)
            {
                actv_rekening_member.setText(cashInHistoryModel.getBenef_product_name());
                etNoAcct.setText(cashInHistoryModel.getBenef_product_value_code());
            }


            // Keys used in Hashmap
            String[] from = {"flag", "txt"};

            // Ids of views in listview_layout
            int[] to = {R.id.flag, R.id.txt};

            if(!isBack) aListMember = new ArrayList<>();
            // Instantiating an adapter to store each items
            // R.layout.listview_layout defines the layout of each item
            adapterMember = new SimpleAdapter(getActivity().getBaseContext(), aListMember, R.layout.bbs_autocomplete_layout, from, to);

            comm = realmBBS.where(BBSCommModel.class)
                    .equalTo(WebParams.SCHEME_CODE, CTA).findFirst();
            listbankBenef = realmBBS.where(BBSBankModel.class)
                    .equalTo(WebParams.SCHEME_CODE, CTA)
                    .equalTo(WebParams.COMM_TYPE, BENEF).findAll();
            setBBSCity();
            setMember(listbankBenef);
        } else if (transaksi.equalsIgnoreCase(getString(R.string.cash_out))){
            if(type.equalsIgnoreCase(DefineValue.BBS_CASHOUT)) {
                if(!defaultAmount.equals("")) {
                    etAmount.setText(defaultAmount);
                }
                else
                {
                    if (cashOutHistoryModel!=null)
                    {
                        etAmount.setText(cashOutHistoryModel.getAmount());
                    }
                }
            }
            else if (cashOutHistoryModel!=null)
            {
                etAmount.setText(cashOutHistoryModel.getAmount());
            }


            stub.setLayoutResource(R.layout.bbs_cashout_amount);
            View cashout_layout = stub.inflate();

            actv_rekening_member = (CustomAutoCompleteTextView) cashout_layout.findViewById(R.id.rekening_member_value);
            etNoAcct = (EditText) cashout_layout.findViewById(R.id.no_tujuan_value);

            if (cashOutHistoryModel!=null)
            {
                actv_rekening_member.setText(cashOutHistoryModel.getSource_product_name());
                etNoAcct.setText(cashOutHistoryModel.getMember_shop_phone());
            }

            // Keys used in Hashmap
            String[] from = {"flag", "txt"};

            // Ids of views in listview_layout
            int[] to = {R.id.flag, R.id.txt};

            if(!isBack) aListMember = new ArrayList<>();
            // Instantiating an adapter to store each items
            // R.layout.listview_layout defines the layout of each item
            adapterMember = new SimpleAdapter(getActivity().getBaseContext(), aListMember, R.layout.bbs_autocomplete_layout, from, to);

            comm = realmBBS.where(BBSCommModel.class)
                    .equalTo(WebParams.SCHEME_CODE, ATC).findFirst();
            if(isBack) setMember(listbankSource);
        }

        actv_rekening_member.addTextChangedListener(textWatcher);
        btnBack.setOnClickListener(backListener);
        btnProses.setOnClickListener(prosesListener);

        if(!isBack) {
            comm_id = comm.getComm_id();
            comm_code = comm.getComm_code();
            callback_url = comm.getCallback_url();
            api_key = comm.getApi_key();

            retrieveComm();
        }

        if(transaksi.equalsIgnoreCase(getString(R.string.cash_in)))
        {
            validasiTutorialCashIn();
        }
        else if (transaksi.equalsIgnoreCase(getString(R.string.cash_out)))
        {
            validasiTutorialCashOut();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_information:
                if(transaksi.equalsIgnoreCase(getString(R.string.cash_in)))
                {
                    showTutorialCashIn();
                }
                else if (transaksi.equalsIgnoreCase(getString(R.string.cash_out)))
                {
                    showTutorialCashOut();
                }
                return true;
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void validasiTutorialCashIn()
    {
        if(transaksi.equalsIgnoreCase(getString(R.string.cash_in)))
        {
            if(sp.contains(DefineValue.TUTORIAL_CASHIN))
            {
                Boolean is_first_time = sp.getBoolean(DefineValue.TUTORIAL_CASHIN,false);
                if(is_first_time) {
                    showTutorialCashIn();
                }
            }
            else {
                showTutorialCashIn();
            }
        }
    }


    private void validasiTutorialCashOut()
    {
        if(transaksi.equalsIgnoreCase(getString(R.string.cash_out)))
        {
            if (sp.contains(DefineValue.TUTORIAL_CASHOUT))
            {
                Boolean is_first_time = sp.getBoolean(DefineValue.TUTORIAL_CASHOUT,false);
                if(is_first_time)
                    showTutorialCashOut();
            }
        }
        else {
            showTutorialCashOut();
        }
    }

    private void showTutorialCashIn()
    {
        Intent intent = new Intent(getActivity(), TutorialActivity.class);
        intent.putExtra(DefineValue.TYPE, TutorialActivity.tutorial_cash_in);
        startActivity(intent);
    }

    private void showTutorialCashOut()
    {
        Intent intent = new Intent(getActivity(), TutorialActivity.class);
        intent.putExtra(DefineValue.TYPE, TutorialActivity.tutorial_cash_out);
        startActivity(intent);
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            int position;
            String nameAcct = actv_rekening_member.getText().toString();
            for(int i = 0 ; i < aListMember.size() ; i++) {
                if(nameAcct.equalsIgnoreCase(aListMember.get(i).get("txt"))) {
                    position = i;
                    if (transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
                        benef_product_type = listbankBenef.get(position).getProduct_type();
                        if (benef_product_type.equalsIgnoreCase(DefineValue.EMO)) {
                            cityLayout.setVisibility(View.GONE);
                            etNoAcct.setHint(R.string.number_hp_destination_hint);
                            tvEgNo.setText(getString(R.string.eg_no_hp));
                        } else {
                            cityLayout.setVisibility(View.VISIBLE);
                            etNoAcct.setHint(R.string.number_destination_hint);
                            tvEgNo.setText(getString(R.string.eg_no_acct));
                        }

                        if(listbankBenef.get(position).getBank_gateway().equalsIgnoreCase(DefineValue.STRING_YES))
                            nameLayout.setVisibility(View.GONE);
                        else
                            nameLayout.setVisibility(View.VISIBLE);
                    } else {
                        if(listbankSource.get(position).getBank_gateway() != null) {
                            if (listbankSource.get(position).getBank_gateway().equalsIgnoreCase(DefineValue.STRING_YES))
                                etNoAcct.setHint(getString(R.string.user_id) + " " + getString(R.string.appname));
                            else
                                etNoAcct.setHint(getString(R.string.user_id) + " " + listbankSource.get(position).getProduct_name());
                        }
                    }
                    break;
                }
            }

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    Button.OnClickListener backListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            getActivity().finish();
        }
    };

    public Button.OnClickListener prosesListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(inputValidation()) {
                int position =-1;
                String nameAcct = actv_rekening_member.getText().toString();
                for(int i = 0 ; i < aListMember.size() ; i++) {
                    if(nameAcct.equalsIgnoreCase(aListMember.get(i).get("txt")))
                        position = i;
                }

                if(position != -1) {
                    Fragment newFrag = new BBSTransaksiInformasi();
                    Bundle args = new Bundle();
                    args.putString(DefineValue.TRANSACTION, transaksi);
                    args.putString(DefineValue.AMOUNT, etAmount.getText().toString());
                    args.putString(DefineValue.COMMUNITY_ID, comm_id);
                    args.putString(DefineValue.COMMUNITY_CODE, comm_code);
                    args.putString(DefineValue.MEMBER_CODE, member_code);
                    args.putString(DefineValue.CALLBACK_URL, callback_url);
                    args.putString(DefineValue.API_KEY, api_key);

                    if (transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
                        args.putString(DefineValue.BENEF_PRODUCT_CODE, listbankBenef.get(position).getProduct_code());
                        args.putString(DefineValue.BENEF_PRODUCT_TYPE, listbankBenef.get(position).getProduct_type());
                        args.putString(DefineValue.BENEF_PRODUCT_NAME, listbankBenef.get(position).getProduct_name());
                        args.putString(DefineValue.NO_BENEF, etNoAcct.getText().toString());
                        args.putString(DefineValue.NAME_BENEF, etNameAcct.getText().toString());;
                        if (benef_product_type.equalsIgnoreCase(DefineValue.ACCT)) {
                            String city_id = list_bbs_cities.get(CityAutocompletePos).getCity_id();
                            String city_name = spBenefCity.getText().toString();
                            args.putString(DefineValue.ACCT_CITY_CODE, city_id);
                            args.putString(DefineValue.ACCT_CITY_NAME, city_name);
                        }
                        if(type.equalsIgnoreCase(DefineValue.BBS_CASHIN)) {
                            if (!noHpPengirim.equals(""))
                                args.putString(DefineValue.KEY_CODE, noHpPengirim);
                        }
                    } else if (transaksi.equalsIgnoreCase(getString(R.string.cash_out)))
                    {
                        args.putString(DefineValue.SOURCE_PRODUCT_CODE, listbankSource.get(position).getProduct_code());
                        args.putString(DefineValue.SOURCE_PRODUCT_TYPE, listbankSource.get(position).getProduct_type());
                        args.putString(DefineValue.SOURCE_PRODUCT_NAME, listbankSource.get(position).getProduct_name());
                        args.putString(DefineValue.SOURCE_PRODUCT_H2H, listbankSource.get(position).getProduct_h2h());
                        args.putString(DefineValue.SOURCE_ACCT_NO, etNoAcct.getText().toString());
                        args.putString(DefineValue.BBS_COMM_ATC, comm_benef_atc);
                    }
                    newFrag.setArguments(args);



                    getFragmentManager().beginTransaction().replace(R.id.bbsTransaksiFragmentContent, newFrag, BBSTransaksiInformasi.TAG)
                            .addToBackStack(TAG).commit();
                    ToggleKeyboard.hide_keyboard(act);
                }
                else {
                    Toast.makeText(act, getString(R.string.no_match_member_acct_message), Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private void setMember(List<BBSBankModel> bankMember) {
        aListMember.clear();

        for(int i=0;i<bankMember.size();i++){
            HashMap<String, String> hm = new HashMap<>();
            hm.put("txt", bankMember.get(i).getProduct_name());

            if(bankMember.get(i).getProduct_name().toLowerCase().contains("mandiri"))
                hm.put("flag", Integer.toString(R.drawable.logo_mandiri_bank_small));
            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("bri"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_bri_small));
            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("permata"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_permata_small));
            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("uob"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_uob_small));
            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("maspion"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_maspion_rev1_small));
            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("bii"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_bii_small));
            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("jatim"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_jatim_small));
            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("bca"))
                hm.put("flag", Integer.toString(R.drawable.logo_bca_bank_small));
            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("nobu"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_nobu));
            else
                hm.put("flag", Integer.toString(R.drawable.ic_square_gate_one));
            aListMember.add(hm);
        }

        actv_rekening_member.setAdapter(adapterMember);
    }

    private void setBBSCity() {
        spBenefCity.setVisibility(View.GONE);
        spinwheelCity.setVisibility(View.VISIBLE);
        spinwheelCity.startAnimation(frameAnimation);

        Thread proses = new Thread(){

            @Override
            public void run() {
                RealmResults results = realm.where(List_BBS_City.class).findAll();
                list_bbs_cities = new ArrayList<>(results);
                list_name_bbs_cities = new ArrayList<>();
                if(list_bbs_cities.size() > 0) {
                    for (int i = 0; i < list_bbs_cities.size(); i++) {
                        list_name_bbs_cities.add(list_bbs_cities.get(i).getCity_name());
                    }
                }
                else {
//                    UpdateBBSCity.startUpdateBBSCity(getActivity());
                }

                final ArrayAdapter<String> city_adapter = new ArrayAdapter<String>
                        (getActivity(),android.R.layout.select_dialog_item, list_name_bbs_cities);

                spBenefCity.setThreshold(1);
                spBenefCity.setAdapter(city_adapter);

                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        spinwheelCity.clearAnimation();
                        spinwheelCity.setVisibility(View.GONE);
                        spBenefCity.setVisibility(View.VISIBLE);
//                        adapter.notifyDataSetChanged();
                        city_adapter.notifyDataSetChanged();

                        String defaultValue = "KOTA JAKARTA";
                        spBenefCity.setText(defaultValue);
                        CityAutocompletePos = list_name_bbs_cities.indexOf(defaultValue);
                    }
                });
            }
        };
        proses.run();
    }

    private void retrieveComm(){
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "Ambil data komunitas dan bank...");
            progdialog.show();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID, MyApiClient.LINK_GLOBAL_BBS_COMM,
                    userID, accessKey);
            params.put(WebParams.CUSTOMER_ID, userID);
            if(transaksi.equalsIgnoreCase(getString(R.string.cash_in)))
                params.put(WebParams.SCHEME_CODE, DefineValue.CTA);
            else
                params.put(WebParams.SCHEME_CODE, DefineValue.ATC);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);
            Timber.d("isi params retrieveComm:" + params.toString());

            MyApiClient.getGlobalBBSComm(getActivity(), TAG, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("Isi response retrieveComm: " + response.toString());
                        listDataComm.clear();
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            if(transaksi.equalsIgnoreCase(getString(R.string.cash_in))) progdialog.dismiss();
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
                                }
                                getMemberCode();
                            }
                        } else {
                            progdialog.dismiss();
                            inputForm.setVisibility(View.GONE);
                            emptyLayout.setVisibility(View.VISIBLE);
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progdialog.dismiss();
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
                    inputForm.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.VISIBLE);
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    Timber.w("Error Koneksi retrieveComm:" + throwable.toString());
                    progdialog.dismiss();
                }
            });
        } catch (Exception e) {
            Timber.d("httpclient: " + e.getMessage());
            progdialog.dismiss();
        }
    }

    private void getBankList(String _comm_code, String _member_code) {
        try {

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID, MyApiClient.LINK_GLOBAL_BBS_BANK_A2C,
                    userID, accessKey);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.MEMBER_CODE, _member_code);
            params.put(WebParams.COMM_CODE, _comm_code);

            Log.d("params bbs list", params.toString());
            MyApiClient.getGlobalBBSBankA2C(getActivity(),params, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progdialog.dismiss();
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi response get bbs list:"+response.toString());

                            comm_benef_atc = response.getString(WebParams.COMM_BENEF);   //rekening agen
                            if(!comm_benef_atc.equals("")) {
                                emptyCashoutBenefLayout.setVisibility(View.GONE);
                                String comm_source = response.getString(WebParams.COMM_SOURCE); //rekening member
                                if(!comm_source.equals("")) {
                                    inputForm.setVisibility(View.VISIBLE);
                                    emptyLayout.setVisibility(View.GONE);
                                    JSONArray arr = new JSONArray(comm_source);
                                    setBankDataSource(arr);
                                }
                                else {
                                    Toast.makeText(getActivity(), getString(R.string.no_source_list_message), Toast.LENGTH_LONG).show();
                                    emptyLayout.setVisibility(View.VISIBLE);
                                    inputForm.setVisibility(View.GONE);
                                }
                            }
                            else {
                                inputForm.setVisibility(View.GONE);
                                emptyLayout.setVisibility(View.GONE);
                                emptyCashoutBenefLayout.setVisibility(View.VISIBLE);
                            }

                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            Timber.d("isi response autologout:" + response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(), message);
                        }else {
                            Timber.d("isi error get bbs list:"+response.toString());
                            String code_msg = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code_msg, Toast.LENGTH_LONG).show();
                            emptyLayout.setVisibility(View.VISIBLE);
                            inputForm.setVisibility(View.GONE);
                            emptyCashoutBenefLayout.setVisibility(View.GONE);
                        }
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

                private void failure(Throwable throwable){
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(act, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(act, throwable.toString(), Toast.LENGTH_SHORT).show();

                    Timber.w("Error Koneksi get bbs list:"+throwable.toString());
                    progdialog.dismiss();
                    emptyLayout.setVisibility(View.VISIBLE);
                    inputForm.setVisibility(View.GONE);
                    emptyCashoutBenefLayout.setVisibility(View.GONE);
                }

            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
            progdialog.dismiss();
            emptyLayout.setVisibility(View.VISIBLE);
            inputForm.setVisibility(View.GONE);
            emptyCashoutBenefLayout.setVisibility(View.GONE);
        }
    }


    private void setBankDataSource(JSONArray _data){
        listbankSource = new ArrayList<>();
        for(int i = 0 ; i < _data.length() ; i++) {
            BBSBankModel bbsBankModel =  new BBSBankModel();
            try {
                bbsBankModel.setProduct_code(_data.getJSONObject(i).getString(WebParams.PRODUCT_CODE));
                bbsBankModel.setProduct_name(_data.getJSONObject(i).getString(WebParams.PRODUCT_NAME));
                bbsBankModel.setProduct_type(_data.getJSONObject(i).getString(WebParams.PRODUCT_TYPE));
                bbsBankModel.setProduct_h2h(_data.getJSONObject(i).getString(WebParams.PRODUCT_H2H));
                bbsBankModel.setBank_gateway(_data.getJSONObject(i).getString(WebParams.BANK_GATEWAY));
                listbankSource.add(bbsBankModel);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        setMember(listbankSource);
    }


    private void getMemberCode(){
        boolean commIdFound = false;
        for(int i = 0 ; i < listDataComm.size() ; i++) {
            if (comm_id.equalsIgnoreCase(listDataComm.get(i).getCommId())) {
                commIdFound = true;
                member_code = listDataComm.get(i).getMemberCode();
                break;
            }
        }
        if(commIdFound) {
            if(transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
                inputForm.setVisibility(View.VISIBLE);
                emptyLayout.setVisibility(View.GONE);
            }
            if(transaksi.equalsIgnoreCase(getString(R.string.cash_out)))
                getBankList(comm_code, member_code);
        }
        else {
            progdialog.dismiss();
            inputForm.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(), "Belum terdaftar di komunitas", Toast.LENGTH_LONG).show();;
        }

    }

    private boolean inputValidation() {
        if(etAmount.getText().toString().length()==0){
            etAmount.requestFocus();
            etAmount.setError(getString(R.string.payfriends_amount_validation));
            return false;
        }
        else if(Long.parseLong(etAmount.getText().toString()) < 1){
            etAmount.requestFocus();
            etAmount.setError(getString(R.string.payfriends_amount_zero));
            return false;
        }
        if(actv_rekening_member.getText().toString().length()==0){
            actv_rekening_member.requestFocus();
            actv_rekening_member.setError(getString(R.string.rekening_member_error_message));
            return false;
        }
        if(transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
            if (benef_product_type.equalsIgnoreCase(DefineValue.EMO)) {
                if (etNoAcct.getText().toString().length() == 0) {
                    etNoAcct.requestFocus();
                    etNoAcct.setError(getString(R.string.no_member_validation));
                    return false;
                } else if (etNoAcct.getText().toString().length() < 7) {
                    etNoAcct.requestFocus();
                    etNoAcct.setError(getString(R.string.no_member_validation));
                    return false;
                }
            }
            else {
                if (etNoAcct.getText().toString().length() == 0) {
                    etNoAcct.requestFocus();
                    etNoAcct.setError(getString(R.string.no_rekening_validation));
                    return false;
                } else if (etNoAcct.getText().toString().length() < 7) {
                    etNoAcct.requestFocus();
                    etNoAcct.setError(getString(R.string.no_rekening_validation));
                    return false;
                }
            }

            if (nameLayout.getVisibility() == View.VISIBLE) {
                if (etNameAcct.getText().toString().length() == 0) {
                    etNameAcct.requestFocus();
                    etNameAcct.setError(getString(R.string.nama_rekening_validation));
                    return false;
                }
            }

            if(cityLayout.getVisibility() == View.VISIBLE) {
                String autocomplete_text = spBenefCity.getText().toString();

                if (autocomplete_text.equals("")){
                    spBenefCity.requestFocus();
                    spBenefCity.setError(getString(R.string.city_empty_message));
                    return false;
                }else if (!list_name_bbs_cities.contains(autocomplete_text)){

                    spBenefCity.requestFocus();
                    spBenefCity.setError(getString(R.string.city_not_found_message));
                    return false;
                }else {
                    CityAutocompletePos = list_name_bbs_cities.indexOf(autocomplete_text);
                    spBenefCity.setError(null);
                }
            }
        }
        else {
            if(etNoAcct.getText().toString().length()==0){
                etNoAcct.requestFocus();
                etNoAcct.setError(getString(R.string.user_id_validation));
                return false;
            }
        }


        return true;
    }

//    private void switchFragment(Fragment i, String name, Boolean isBackstack){
//        if (getActivity() == null)
//            return;
//
//        BBSActivity fca = (BBSActivity ) getActivity();
//        fca.switchContent(i,name,isBackstack);
//    }
}
