package com.sgo.saldomu.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.faber.circlestepview.CircleStepView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.CashInHistoryModel;
import com.sgo.saldomu.Beans.CashOutHistoryModel;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.RegisterSMSBankingActivity;
import com.sgo.saldomu.activities.TopUpActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.SMSclass;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogFrag;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.ConfirmationDialog;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.SMSDialog;
import com.sgo.saldomu.entityRealm.BBSAccountACTModel;
import com.sgo.saldomu.entityRealm.BBSBankModel;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.BBSTransModel;
import com.sgo.saldomu.widgets.BaseFragment;
import com.sgo.saldomu.widgets.CustomAutoCompleteTextViewWithIcon;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/**
 * Created by thinkpad on 4/21/2017.
 */

public class BBSTransaksiInformasi extends BaseFragment implements EasyPermissions.PermissionCallbacks
        , ConfirmationDialog.clickListener {
    public final static String TAG = "com.sgo.saldomu.fragments.BBSTransaksiInformasi";
    private final String MANDIRISMS = "MANDIRISMS";
    private static final int RC_READ_PHONE_STATE = 122;
    private static final int RC_SEND_SMS = 123;
    private View v, bbs_informasi_form, emptyCashoutBenefLayout;

    private Activity act;
    private TextView tvTitle;
    private CustomAutoCompleteTextViewWithIcon actv_rekening_cta;
    private Spinner sp_rekening_act;
    private List<HashMap<String, String>> aListAgent;
    private SimpleAdapter adapterAgent;
    private List<BBSBankModel> listbankSource;
    private List<BBSAccountACTModel> listbankBenef;
    private String CTA = "CTA";
    private String ATC = "ATC";
    private String SOURCE = "SOURCE";
    private String BENEF = "BENEF";
    private EditText etNoHp, etRemark, etOTP;
    private Button btnNext, btnBack;
    private SMSclass smSclass;
    private SMSDialog smsDialog;
    ConfirmationDialog confirmationDialog;
    Dialog dialog;
    private Boolean isSMSBanking = false, isSimExist = false, isOwner = false;
    private BBSTransaksiInformasi.ActionListener actionListener;
    private String comm_code, member_code, source_product_code = "", source_product_type,
            benef_product_code, benef_product_name, benef_product_type, source_product_h2h,
            api_key, callback_url, source_product_name, productValue = "", comm_id, city_id, amount,
            transaksi, no_benef, name_benef, city_name, no_source, benef_product_value_token, source_product_value_token, key_code,
            noHPMemberLocation = "", message, lkd_product_code;
    Realm realmBBS;
    CashInHistoryModel cashInHistoryModel;
    CashOutHistoryModel cashOutHistoryModel;
    private Boolean TCASHValidation = false, MandiriLKDValidation = false, code_success = false;

    public interface ActionListener {
        void ChangeActivityFromCashInput(Intent data);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        realmBBS = Realm.getInstance(RealmManager.BBSConfiguration);
        realmBBS.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm element) {
                setBankDataBenef();
                if (adapterAgent != null)
                    adapterAgent.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDestroy() {
        RealmManager.closeRealm(realmBBS);
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getTargetFragment() instanceof BBSTransaksiInformasi.ActionListener) {
            actionListener = (BBSTransaksiInformasi.ActionListener) getTargetFragment();
        } else {
            if (context instanceof BBSTransaksiInformasi.ActionListener) {
                actionListener = (BBSTransaksiInformasi.ActionListener) context;
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement ActionListener");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        actionListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.bbs_transaksi_informasi, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        act = getActivity();

        Bundle bundle = getArguments();
        if (bundle != null) {
            transaksi = bundle.getString(DefineValue.TRANSACTION);
            amount = bundle.getString(DefineValue.AMOUNT);
            comm_id = bundle.getString(DefineValue.COMMUNITY_ID);
            comm_code = bundle.getString(DefineValue.COMMUNITY_CODE);
            member_code = bundle.getString(DefineValue.MEMBER_CODE);
            callback_url = bundle.getString(DefineValue.CALLBACK_URL);
            api_key = bundle.getString(DefineValue.API_KEY);

            if (bundle.containsKey(DefineValue.NO_HP_MEMBER_LOCATION)) {
                noHPMemberLocation = bundle.getString(DefineValue.NO_HP_MEMBER_LOCATION, "");
            }

            benef_product_code = bundle.getString(DefineValue.BENEF_PRODUCT_CODE, "");

            if (transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
                String cashIn = sp.getString(DefineValue.CASH_IN_HISTORY_TEMP, "");
                Gson gson = new Gson();
                cashInHistoryModel = gson.fromJson(cashIn, CashInHistoryModel.class);

                benef_product_name = bundle.getString(DefineValue.BENEF_PRODUCT_NAME, "");
                benef_product_type = bundle.getString(DefineValue.BENEF_PRODUCT_TYPE, "");
                benef_product_value_token = bundle.getString(DefineValue.BENEF_PRODUCT_VALUE_TOKEN, "");
                if (cashInHistoryModel != null) {
                    source_product_code = (cashInHistoryModel.getSource_product_code());
                    source_product_name = (cashInHistoryModel.getSource_product_name());
                    source_product_type = (cashInHistoryModel.getSource_product_type());
                    source_product_h2h = (cashInHistoryModel.getSource_product_h2h());
                }
                no_benef = bundle.getString(DefineValue.NO_BENEF);
                name_benef = bundle.getString(DefineValue.NAME_BENEF);
                setBankDataSourceCTA();
                if (benef_product_type.equalsIgnoreCase(DefineValue.ACCT)) {
                    city_id = bundle.getString(DefineValue.ACCT_CITY_CODE);
                    city_name = bundle.getString(DefineValue.ACCT_CITY_NAME);
                }
            } else {
                String cashOut = sp.getString(DefineValue.CASH_OUT_HISTORY_TEMP, "");
                Gson gson1 = new Gson();
                cashOutHistoryModel = gson1.fromJson(cashOut, CashOutHistoryModel.class);

                source_product_code = bundle.getString(DefineValue.SOURCE_PRODUCT_CODE, "");
                source_product_type = bundle.getString(DefineValue.SOURCE_PRODUCT_TYPE, "");
                source_product_h2h = bundle.getString(DefineValue.SOURCE_PRODUCT_H2H, "");
                source_product_name = bundle.getString(DefineValue.SOURCE_PRODUCT_NAME, "");
                source_product_value_token = bundle.getString(DefineValue.SOURCE_PRODUCT_VALUE_TOKEN, "");
                no_source = bundle.getString(DefineValue.SOURCE_ACCT_NO, "");

                setBankDataBenef();
                if (cashOutHistoryModel != null) {
                    benef_product_code = (cashOutHistoryModel.getBenef_product_code());
                    benef_product_type = (cashOutHistoryModel.getBenef_product_type());
                    benef_product_name = (cashOutHistoryModel.getBenef_product_name());
                }
            }

            CircleStepView mCircleStepView = v.findViewById(R.id.circle_step_view);
            mCircleStepView.setTextBelowCircle("", getString(R.string.informasi), "");
            mCircleStepView.setCurrentCircleIndex(1, false);

            tvTitle = v.findViewById(R.id.tv_title);
            btnNext = v.findViewById(R.id.proses_btn);
            btnBack = v.findViewById(R.id.back_btn);
            emptyCashoutBenefLayout = v.findViewById(R.id.empty_cashout_benef_layout);
            bbs_informasi_form = v.findViewById(R.id.bbinformasi_input_layout);
            ViewStub stub = v.findViewById(R.id.informasi_stub);

            key_code = bundle.getString(DefineValue.KEY_CODE, "");

            tvTitle.setText(transaksi);
            if (transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
                stub.setLayoutResource(R.layout.bbs_cashin_informasi);
                View cashin_layout = stub.inflate();
                actv_rekening_cta = cashin_layout.findViewById(R.id.rekening_agen_value);
                etNoHp = cashin_layout.findViewById(R.id.no_hp_pengirim_value);
                etRemark = cashin_layout.findViewById(R.id.message_value);// Keys used in Hashmap

                if (!key_code.equals("")) {
                    etNoHp.setText(key_code);
                } else {
                    if (cashInHistoryModel != null) {
                        etNoHp.setText(cashInHistoryModel.getMember_shop_phone());
                    }
                }
                if (cashInHistoryModel != null) {
                    actv_rekening_cta.setText(cashInHistoryModel.getSource_product_name());
                    etRemark.setText(cashInHistoryModel.getPesan());
                }

                String[] from = {"flag", "txt"};

                // Ids of views in listview_layout
                int[] to = {R.id.flag, R.id.txt};

                aListAgent = new ArrayList<>();
                // Instantiating an adapter to store each items
                // R.layout.listview_layout defines the layout of each item
                adapterAgent = new SimpleAdapter(getActivity().getBaseContext(), aListAgent, R.layout.bbs_autocomplete_layout, from, to);
                setAgent(listbankSource);
                actv_rekening_cta.setAdapter(adapterAgent);
                actv_rekening_cta.addTextChangedListener(textWatcher);
            } else {
                stub.setLayoutResource(R.layout.bbs_cashout_informasi);
                View cashout_layout = stub.inflate();
                sp_rekening_act = cashout_layout.findViewById(R.id.rekening_agen_value);
                etRemark = cashout_layout.findViewById(R.id.message_value);
                etOTP = cashout_layout.findViewById(R.id.no_OTP_cashout);


                String[] from = {"flag", "txt"};
                // Ids of views in listview_layout
                int[] to = {R.id.flag, R.id.txt};

                aListAgent = new ArrayList<>();
                // Instantiating an adapter to store each items
                // R.layout.listview_layout defines the layout of each item
                adapterAgent = new SimpleAdapter(getActivity().getBaseContext(), aListAgent, R.layout.bbs_autocomplete_layout, from, to);
                setAgentATC(listbankBenef);
                sp_rekening_act.setAdapter(adapterAgent);
                sp_rekening_act.setOnItemSelectedListener(spAgentListener);

                if (cashOutHistoryModel != null) {
                    for (int i = 0; i < aListAgent.size(); i++) {
                        if (aListAgent.get(i).get("txt").equalsIgnoreCase(benef_product_name)) {
                            sp_rekening_act.setSelection(i);
                            break;
                        }
                    }
                    etRemark.setText(cashOutHistoryModel.getPesan());
                }
                if (listbankBenef.size() == 0) {
                    bbs_informasi_form.setVisibility(View.GONE);
                    emptyCashoutBenefLayout.setVisibility(View.VISIBLE);
                }
            }

            btnBack.setOnClickListener(backListener);
            btnNext.setOnClickListener(nextListener);
        } else {
            getFragmentManager().popBackStack();
        }
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            source_product_code = "";
            source_product_name = "";
            source_product_type = "";
            source_product_h2h = "";
            int position;
            String nameAcct = actv_rekening_cta.getText().toString();
            for (int i = 0; i < aListAgent.size(); i++) {
                if (nameAcct.equalsIgnoreCase(aListAgent.get(i).get("txt"))) {
                    position = i;
                    source_product_code = listbankSource.get(position).getProduct_code();
                    source_product_type = listbankSource.get(position).getProduct_type();
                    source_product_h2h = listbankSource.get(position).getProduct_h2h();
                    source_product_name = listbankSource.get(position).getProduct_name();
                    break;
                }
            }

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    Spinner.OnItemSelectedListener spAgentListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            benef_product_code = listbankBenef.get(position).getProduct_code();
            benef_product_type = listbankBenef.get(position).getProduct_type();
            benef_product_name = listbankBenef.get(position).getProduct_name();

            if (benef_product_code.equalsIgnoreCase("tcash") || benef_product_code.equalsIgnoreCase("MANDIRILKD"))
                etOTP.setVisibility(View.VISIBLE);
            else etOTP.setVisibility(View.GONE);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    Button.OnClickListener backListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            } else
                getActivity().finish();
        }
    };

    Button.OnClickListener nextListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                isSMSBanking = source_product_code.equalsIgnoreCase(MANDIRISMS);

                extraSignature = comm_code + member_code + source_product_type + source_product_code + benef_product_type + benef_product_code
                        + MyApiClient.CCY_VALUE + amount;

                if (transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
//                    if (isSMSBanking) {
//                        if (EasyPermissions.hasPermissions(getActivity(), Manifest.permission.READ_PHONE_STATE)) {
//                            initializeSmsClass();
//                            if (isSimExist)
//                                SubmitAction(true);
//                        } else {
//                            // Ask for one permission
//                            EasyPermissions.requestPermissions(BBSTransaksiInformasi.this, getString(R.string.rationale_phone_state),
//                                    RC_READ_PHONE_STATE, Manifest.permission.READ_PHONE_STATE);
//                        }
//                    } else {
                        SubmitAction(true);
//                    }
                } else {
//                    btnNext.setEnabled(false);
                    if (inputValidation()) {
                        SubmitAction(false);
                    }
//                     btnNext.setEnabled(false);
                }
            } else
                DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    private void SubmitAction(boolean isCashin) {
        if (inputValidation()) {
            if (isCashin) {
                confirmationDialog = ConfirmationDialog.newDialog(this
                        , transaksi
                        , amount
                        , source_product_name
                        , benef_product_name
                        , no_benef
                        , etRemark.getText().toString(), name_benef,
                        etNoHp.getText().toString());
            } else {
                confirmationDialog = ConfirmationDialog.newDialog(this
                        , transaksi
                        , amount
                        , source_product_name
                        , benef_product_name
                        , no_source
                        , etRemark.getText().toString(), "", "");
            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//                getActivity().getFragmentManager().beginTransaction().add(dialog, "ConfirmationDialog").commit();
//            else
//                getChildFragmentManager().beginTransaction().add(dialog,"ConfirmationDialog").commit();
            confirmationDialog.show(getActivity().getSupportFragmentManager(), "ConfirmationDialog");
        }
    }

    private void setAgent(List<BBSBankModel> bankAgen) {
        aListAgent.clear();

        for (int i = 0; i < bankAgen.size(); i++) {
            HashMap<String, String> hm = new HashMap<>();
            hm.put("txt", bankAgen.get(i).getProduct_name());

            if (bankAgen.get(i).getProduct_name().toLowerCase().contains("mandiri"))
                hm.put("flag", Integer.toString(R.drawable.logo_mandiri_bank_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("bri"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_bri_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("permata"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_permata_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("uob"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_uob_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("maspion"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_maspion_rev1_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("bii"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_bii_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("jatim"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_jatim_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("bca"))
                hm.put("flag", Integer.toString(R.drawable.logo_bca_bank_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("nobu"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_nobu));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("saldomu"))
                hm.put("flag", Integer.toString(R.drawable.logo_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("linkaja"))
                hm.put("flag", Integer.toString(R.drawable.linkaja));
            else if (bankAgen.get(i).getProduct_code().toLowerCase().contains("emoedikk"))
                hm.put("flag", Integer.toString(R.drawable.dana_small));
            else if (bankAgen.get(i).getProduct_code().toLowerCase().contains("009"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_bni_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("akardaya"))
                hm.put("flag", Integer.toString(R.drawable.mad_small));
            else
                hm.put("flag", Integer.toString(R.drawable.ic_square_gate_one));
            aListAgent.add(hm);
        }
        adapterAgent.notifyDataSetChanged();
    }

    private void setAgentATC(List<BBSAccountACTModel> bankAgen) {
        aListAgent.clear();

        for (int i = 0; i < bankAgen.size(); i++) {
            HashMap<String, String> hm = new HashMap<>();
            hm.put("txt", bankAgen.get(i).getProduct_name());

            if (bankAgen.get(i).getProduct_name().toLowerCase().contains("mandiri"))
                hm.put("flag", Integer.toString(R.drawable.logo_mandiri_bank_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("bri"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_bri_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("permata"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_permata_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("uob"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_uob_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("maspion"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_maspion_rev1_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("bii"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_bii_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("jatim"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_jatim_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("bca"))
                hm.put("flag", Integer.toString(R.drawable.logo_bca_bank_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("nobu"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_nobu));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("saldomu"))
                hm.put("flag", Integer.toString(R.drawable.logo_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("linkaja"))
                hm.put("flag", Integer.toString(R.drawable.linkaja));
            else if (bankAgen.get(i).getProduct_code().toLowerCase().contains("emoedikk"))
                hm.put("flag", Integer.toString(R.drawable.dana_small));
            else if (bankAgen.get(i).getProduct_code().toLowerCase().contains("009"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_bni_small));
            else if (bankAgen.get(i).getProduct_name().toLowerCase().contains("akardaya"))
                hm.put("flag", Integer.toString(R.drawable.mad_small));
            else
                hm.put("flag", Integer.toString(R.drawable.ic_square_gate_one));
            aListAgent.add(hm);
        }
        adapterAgent.notifyDataSetChanged();
    }


    private void setBankDataBenef() {
        listbankBenef = realmBBS.where(BBSAccountACTModel.class).findAll();
    }

    private void setBankDataSourceCTA() {
        listbankSource = realmBBS.where(BBSBankModel.class)
                .equalTo(WebParams.SCHEME_CODE, CTA)
                .equalTo(WebParams.COMM_TYPE, SOURCE).findAll();
    }

    private void sentInsertC2A() {
        try {

            showProgressDialog();

            extraSignature = comm_code + member_code + source_product_type + source_product_code + benef_product_type + benef_product_code
                    + MyApiClient.CCY_VALUE + amount;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GLOBAL_BBS_INSERT_C2A, extraSignature);

            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.MEMBER_CODE, member_code);
            params.put(WebParams.SOURCE_PRODUCT_CODE, source_product_code);
            params.put(WebParams.SOURCE_PRODUCT_TYPE, source_product_type);
            params.put(WebParams.BENEF_PRODUCT_CODE, benef_product_code);
            params.put(WebParams.BENEF_PRODUCT_TYPE, benef_product_type);
            params.put(WebParams.BENEF_PRODUCT_VALUE_CODE, no_benef);
            params.put(WebParams.BENEF_PRODUCT_VALUE_NAME, name_benef);
            if (!key_code.equals("")) {
                params.put(WebParams.CUST_ID, key_code);
            }
            if (benef_product_code.equalsIgnoreCase("tcash")) {
                params.put(WebParams.BENEF_PRODUCT_VALUE_TOKEN, benef_product_value_token);
            }

            if (benef_product_type.equalsIgnoreCase(DefineValue.ACCT)) {
                params.put(WebParams.BENEF_PRODUCT_VALUE_CITY, city_id);
            }
            params.put(WebParams.CCY_ID, MyApiClient.CCY_VALUE);
            params.put(WebParams.AMOUNT, amount);
            params.put(WebParams.PAYMENT_REMARK, etRemark.getText().toString());
            params.put(WebParams.MEMBER_SHOP_PHONE, etNoHp.getText().toString());
            params.put(WebParams.USER_COMM_CODE, BuildConfig.COMM_CODE_BBS_ATC);

            String aodTxId = sp.getString(DefineValue.AOD_TX_ID, "");
            if (!aodTxId.equals("")) {
                params.put(WebParams.TX_ID, aodTxId);
            }

            Log.d("params insert c2a", params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GLOBAL_BBS_INSERT_C2A, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {

                            try {
                                final BBSTransModel model = getGson().fromJson(object, BBSTransModel.class);

                                String code = model.getError_code();

                                message = model.getError_message();
                                Log.d("response insert c2a: ", model.toString());

                                dismissProgressDialog();
                                if (code.equals(WebParams.SUCCESS_CODE) || code.equals("0282")) {

                                    SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                                    SecurePreferences.Editor mEditor = prefs.edit();
                                    mEditor.remove(DefineValue.AOD_TX_ID);
                                    mEditor.apply();

//                            Toast.makeText(getActivity(), "Kode " +code, Toast.LENGTH_LONG);
                                    if (code.equals("0282")) {
                                        if (source_product_code.equalsIgnoreCase("tcash")) {
                                            TCASHValidation = true;
                                        } else
                                            MandiriLKDValidation = true;
                                    } else code_success = true;


                                    if (isSMSBanking) {
                                        if (smsDialog == null) {
                                            smsDialog = new SMSDialog(getActivity(), null);
                                        }

                                        smsDialog.setListener(new SMSDialog.DialogButtonListener() {
                                            @Override
                                            public void onClickOkButton(View v, boolean isLongClick) {
                                                if (EasyPermissions.hasPermissions(getActivity(), Manifest.permission.CAMERA)) {
                                                    smsDialog.sentSms();
                                                    RegSimCardReceiver(true);
                                                } else {
                                                    EasyPermissions.requestPermissions(BBSTransaksiInformasi.this, getString(R.string.rationale_send_sms),
                                                            RC_SEND_SMS, Manifest.permission.CAMERA);
                                                }
                                            }

                                            @Override
                                            public void onClickCancelButton(View v, boolean isLongClick) {
                                                dismissProgressDialog();
                                            }

                                            @Override
                                            public void onSuccess(int user_is_new) {

                                            }

                                            @Override
                                            public void onSuccess(String product_value) {
                                                productValue = product_value;
                                                smsDialog.dismiss();
                                                smsDialog.reset();
                                                sentDataReqToken(model);
                                            }
                                        });


                                        if (isSimExist)
                                            smsDialog.show();
                                    } else if (source_product_h2h.equalsIgnoreCase("Y") && source_product_type.equalsIgnoreCase(DefineValue.EMO)) {
                                        if (code.equals(WebParams.SUCCESS_CODE) && !source_product_code.equalsIgnoreCase("tcash")
                                                && !source_product_code.equalsIgnoreCase("MANDIRILKD")) {
                                        sentDataReqToken(model);
//                                            changeToDataMandiriLKD(model.getTx_id(), model.getTx_product_code(), model.getTx_product_name(), model.getTx_bank_code(),
//                                                    model.getAmount(), model.getAdmin_fee(), model.getTotal_amount(), model.getTx_bank_name(),
//                                                    model.getMax_resend_token(), model.getBenef_acct_no(), model.getBenef_acct_name());
                                        }else
                                        {
//                                            changeToConfirmCashIn(model);
                                            sentDataReqToken(model);
                                        }
                                    } else {
//                                        changeToConfirmCashIn(model);
                                        sentDataReqToken(model);
                                        isOwner = true;
//                                        changeToDataMandiriLKD(model.getTx_id(), model.getTx_product_code(), model.getTx_product_name(), model.getTx_bank_code(),
//                                                model.getAmount(), model.getAdmin_fee(), model.getTotal_amount(), model.getTx_bank_name(),
//                                                model.getMax_resend_token(), model.getBenef_acct_no(), model.getBenef_acct_name(), model.getBenef_product_value_code());
                                    }

                                }else if (code.equals("0295")) {
                                    showDialogLimit();
                                } else if (code.equals("0296")) {
                                    lkd_product_code = model.getLkd_product_code();
                                    dialogJoinLKD();
                                }else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    String message = model.getError_message();
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), message);
                                } else {
                                    String code_msg = model.getError_message();
                                    Toast.makeText(getActivity(), code_msg, Toast.LENGTH_LONG).show();

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            dismissProgressDialog();
                        }

                        @Override
                        public void onComplete() {
                            btnNext.setEnabled(true);
                            confirmationDialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    public void showDialogLimit() {
        dialog = DefinedDialog.MessageDialog(getActivity(), this.getString(R.string.limit_dialog_title),
                message, new DefinedDialog.DialogButtonListener() {
                    @Override
                    public void onClickButton(View v, boolean isLongClick) {
                        dialog.dismiss();
                    }
                }
        );

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        dialog.show();
    }

    public void dialogJoinLKD() {
        android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(getActivity());
        builder1.setTitle(R.string.join_lkd);
        builder1.setMessage(message);
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        joinMemberLKD();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().finish();
                    }
                });

        android.support.v7.app.AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public void dialogBenefLKD(String _tx_id, String _product_code, String _product_name, String _bank_code,
                               String _amount, String _fee, String _totalAmount, String _bank_name, String _max_resend_token,
                               String _benef_acct_no, String _benef_acct_name, String no_benef) {
        android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(getActivity());
        builder1.setTitle(R.string.c2a_lkd);
        builder1.setMessage("Transfer ke : ");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Diri Sendiri",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        isOwner = true;
                        changeToDataMandiriLKD(_tx_id, _product_code, _product_name, _bank_code,
                                _amount, _fee, _totalAmount, _bank_name, _max_resend_token,
                                _benef_acct_no, _benef_acct_name, no_benef, isOwner);
                    }
                });

        builder1.setNegativeButton(
                "Orang Lain",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        isOwner = false;
                        changeToDataMandiriLKD(_tx_id, _product_code, _product_name, _bank_code,
                                _amount, _fee, _totalAmount, _bank_name, _max_resend_token,
                                _benef_acct_no, _benef_acct_name, no_benef, isOwner);
                    }
                });

        android.support.v7.app.AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void joinMemberLKD() {
        try {
            showProgressDialog();

            extraSignature = memberIDLogin + lkd_product_code;


            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_BBS_MANDIRI_LKD, extraSignature);
            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.MEMBER_ID, memberIDLogin);
            params.put(WebParams.PRODUCT_CODE, lkd_product_code);

            Timber.d("params send data member mandiri LKD:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_BBS_MANDIRI_LKD, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                Timber.d("isi response sent data member mandiri lkd:" + response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    if (transaksi.equalsIgnoreCase("Setor Tunai")) {
                                        sentInsertC2A();
                                    } else
                                        sentInsertA2C();
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:" + response.toString());
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), message);
                                } else {
                                    Timber.d("isi error send data member mandiri LKD:" + response.toString());
                                    String code_msg = response.getString(WebParams.ERROR_MESSAGE);
                                    Toast.makeText(getActivity(), code_msg, Toast.LENGTH_LONG).show();
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
                            btnNext.setEnabled(true);
                            showProgressDialog();
                        }
                    });

        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }


    private void sentInsertA2C() {
        try {
            showProgressDialog();

            extraSignature = comm_code + member_code + source_product_type + source_product_code + benef_product_type + benef_product_code
                    + MyApiClient.CCY_VALUE + amount;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GLOBAL_BBS_INSERT_A2C, extraSignature);
            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.MEMBER_CODE, member_code);
            params.put(WebParams.SOURCE_PRODUCT_CODE, source_product_code);
            params.put(WebParams.SOURCE_PRODUCT_TYPE, source_product_type);
            params.put(WebParams.SOURCE_PRODUCT_VALUE, no_source);
            params.put(WebParams.BENEF_PRODUCT_CODE, benef_product_code);
            if (benef_product_code.equalsIgnoreCase("tcash") || benef_product_code.equalsIgnoreCase("MANDIRILKD")) {
                params.put((WebParams.PRODUCT_VALUE), etOTP.getText().toString());
            }
            params.put(WebParams.BENEF_PRODUCT_TYPE, benef_product_type);
            params.put(WebParams.CCY_ID, MyApiClient.CCY_VALUE);
            params.put(WebParams.AMOUNT, amount);
            params.put(WebParams.PAYMENT_REMARK, etRemark.getText().toString());

            String aodTxId = sp.getString(DefineValue.AOD_TX_ID, "");
            if (!aodTxId.equals("")) {
                params.put(WebParams.TX_ID, aodTxId);
            }

            params.put(WebParams.CUSTOMER_ID, noHPMemberLocation);

            Log.d("params insert a2c", params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GLOBAL_BBS_INSERT_A2C, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            final BBSTransModel model = getGson().fromJson(object, BBSTransModel.class);

                            String code = model.getError_code();

                            dismissProgressDialog();
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                                SecurePreferences.Editor mEditor = prefs.edit();
                                mEditor.remove(DefineValue.AOD_TX_ID);
                                mEditor.apply();
                                mEditor.commit();

                                sentDataReqToken(model);

                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), model.getError_message());
                            }else if (code.equals("0296")) {
                                message = model.getError_message();
                                lkd_product_code = model.getLkd_product_code();
                                dialogJoinLKD();
                            } else {
                                Toast.makeText(getActivity(), model.getError_message(), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            dismissProgressDialog();
                        }

                        @Override
                        public void onComplete() {
                            btnNext.setEnabled(true);
                            confirmationDialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    public void sentDataReqToken(final BBSTransModel A2CModel) {
        try {
            showProgressDialog();

            extraSignature = A2CModel.getTx_id() + comm_code + A2CModel.getTx_product_code();

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_SGOL, extraSignature);

            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.TX_ID, A2CModel.getTx_id());
            params.put(WebParams.PRODUCT_CODE, A2CModel.getTx_product_code());
            if (source_product_code.equalsIgnoreCase("tcash") || source_product_code.equalsIgnoreCase("MANDIRILKD"))
                params.put(WebParams.PRODUCT_VALUE, "");
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, comm_id);

            if (isSMSBanking)
                params.put(WebParams.PRODUCT_VALUE, productValue);

            Timber.d("isi params regtoken Sgo+:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_REQ_TOKEN_SGOL, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            final BBSTransModel model = getGson().fromJson(object, BBSTransModel.class);

                            String code = model.getError_code();

                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                if (transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
                                    if (isSMSBanking)
                                        showDialog(model);
                                    else if (benef_product_code.equalsIgnoreCase("MANDIRILKD"))
                                    {
                                        dialogBenefLKD(A2CModel.getTx_id(), A2CModel.getTx_product_code(), A2CModel.getTx_product_name(), A2CModel.getTx_bank_code(),
                                                A2CModel.getAmount(), A2CModel.getAdmin_fee(), A2CModel.getTotal_amount(), A2CModel.getTx_bank_name(),
                                                A2CModel.getMax_resend_token(), A2CModel.getBenef_acct_no(), A2CModel.getBenef_product_value_name(), A2CModel.getBenef_product_value_code());
                                    }
                                    else {
//                                        changeToConfirmCashIn(A2CModel);
                                        isOwner = true;
                                        changeToDataMandiriLKD(A2CModel.getTx_id(), A2CModel.getTx_product_code(), A2CModel.getTx_product_name(), A2CModel.getTx_bank_code(),
                                                A2CModel.getAmount(), A2CModel.getAdmin_fee(), A2CModel.getTotal_amount(), A2CModel.getTx_bank_name(),
                                                A2CModel.getMax_resend_token(), A2CModel.getBenef_acct_no(), A2CModel.getBenef_product_value_name(), A2CModel.getBenef_product_value_code(), isOwner);
                                    }
                                } else {
                                    changeToConfirmCashout(A2CModel, A2CModel.getTx_product_code());
                                }
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), model.getError_message());
                            } else {
                                String code_msg = model.getError_code();
                                if (code.equals("0059") || code.equals("0164")) {
                                    showDialogErrorSMS(model.getTx_bank_name(), code, model.getError_message());
                                } else if (code.equals("0057")) {
                                    if (transaksi.equalsIgnoreCase(getString(R.string.cash_out))) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setTitle("Alert")
                                                .setMessage(getString(R.string.member_saldo_not_enough))
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        getActivity().finish();
                                                    }
                                                });
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    } else {
                                        String message_dialog = "\""+ "\" \n" + getString(R.string.dialog_message_less_balance, getString(R.string.appname));
                                        AlertDialogFrag dialog_frag = AlertDialogFrag.newInstance(getString(R.string.dialog_title_less_balance),
                                                message_dialog, getString(R.string.ok), getString(R.string.cancel), false);
                                        dialog_frag.setOkListener(new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent mI = new Intent(getActivity(), TopUpActivity.class);
                                                mI.putExtra(DefineValue.IS_ACTIVITY_FULL, true);
                                                getActivity().startActivityForResult(mI, MainPage.ACTIVITY_RESULT);
                                            }
                                        });
                                        dialog_frag.setTargetFragment(BBSTransaksiInformasi.this, 0);
                                        dialog_frag.show(getFragmentManager(), AlertDialogFrag.TAG);
                                    }
                                } else {
                                    code = model.getError_code() + " : " + model.getError_message();

                                    Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                                }
                            }
                            dismissProgressDialog();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            dismissProgressDialog();

                        }

                        @Override
                        public void onComplete() {
                            confirmationDialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void showDialog(final BBSTransModel model) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = dialog.findViewById(R.id.title_dialog);
        TextView Message = dialog.findViewById(R.id.message_dialog);

        Message.setVisibility(View.VISIBLE);
        Title.setText(getResources().getString(R.string.regist1_notif_title_verification));
        Message.setText(getString(R.string.appname) + " " + getString(R.string.dialog_token_message_sms));
        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeToConfirmCashIn(model);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void changeToConfirmCashIn(BBSTransModel model) {

        Bundle mArgs = new Bundle();
        if (benef_product_type.equalsIgnoreCase(DefineValue.ACCT)) {
            mArgs.putString(DefineValue.BENEF_CITY, city_name);
        }
        mArgs.putString(DefineValue.PRODUCT_H2H, source_product_h2h);
        mArgs.putString(DefineValue.PRODUCT_TYPE, source_product_type);
        mArgs.putString(DefineValue.PRODUCT_CODE, model.getTx_product_code());
        mArgs.putString(DefineValue.BANK_CODE, model.getTx_bank_code());
        mArgs.putString(DefineValue.BANK_NAME, model.getTx_bank_name());
        mArgs.putString(DefineValue.PRODUCT_NAME, model.getTx_product_name());
        mArgs.putString(DefineValue.FEE, model.getAdmin_fee());
        mArgs.putString(DefineValue.COMMUNITY_CODE, comm_code);
        mArgs.putString(DefineValue.TX_ID, model.getTx_id());
        mArgs.putString(DefineValue.AMOUNT, model.getAmount());
        mArgs.putString(DefineValue.TOTAL_AMOUNT, model.getTotal_amount());
        mArgs.putString(DefineValue.SHARE_TYPE, "1");
        mArgs.putString(DefineValue.CALLBACK_URL, callback_url);
        mArgs.putString(DefineValue.API_KEY, api_key);
        mArgs.putString(DefineValue.COMMUNITY_ID, comm_id);
        mArgs.putString(DefineValue.BANK_BENEF, benef_product_name);
        mArgs.putString(DefineValue.NAME_BENEF, model.getBenef_product_value_name());
        mArgs.putString(DefineValue.NO_BENEF, model.getBenef_product_value_code());
        mArgs.putString(DefineValue.TYPE_BENEF, benef_product_type);
        mArgs.putString(DefineValue.NO_HP_BENEF, etNoHp.getText().toString());
        mArgs.putString(DefineValue.REMARK, etRemark.getText().toString());
        mArgs.putString(DefineValue.SOURCE_ACCT, source_product_name);
        mArgs.putString(DefineValue.MAX_RESEND, model.getMax_resend_token());
        mArgs.putString(DefineValue.TRANSACTION, transaksi);
        mArgs.putString(DefineValue.BENEF_PRODUCT_CODE, benef_product_code);
        if (TCASHValidation != null)
            mArgs.putBoolean(DefineValue.TCASH_HP_VALIDATION, TCASHValidation);
        if (MandiriLKDValidation != null)
            mArgs.putBoolean(DefineValue.MANDIRI_LKD_VALIDATION, MandiriLKDValidation);
        if (code_success != null)
            mArgs.putBoolean(DefineValue.CODE_SUCCESS, code_success);
        btnNext.setEnabled(true);
        cashInHistory();

        Fragment mFrag = new BBSCashInConfirm();
        mFrag.setArguments(mArgs);

        getFragmentManager().beginTransaction().addToBackStack(TAG)
                .replace(R.id.bbsTransaksiFragmentContent, mFrag, BBSCashInConfirm.TAG).commit();
        ToggleKeyboard.hide_keyboard(act);
//        switchFragment(mFrag, getString(R.string.cash_in), true);
    }

    private void changeToDataMandiriLKD(String _tx_id, String _product_code, String _product_name, String _bank_code,
                                        String _amount, String fee, String totalAmount, String _bank_name, String _max_resend_token,
                                        String _benef_acct_no, String _benef_acct_name, String no_benef, Boolean isOwner) {

        Bundle mArgs = new Bundle();
        if (benef_product_type.equalsIgnoreCase(DefineValue.ACCT)) {
            mArgs.putString(DefineValue.BENEF_CITY, city_name);
        }
        mArgs.putString(DefineValue.PRODUCT_H2H, source_product_h2h);
        mArgs.putString(DefineValue.PRODUCT_TYPE, source_product_type);
        mArgs.putString(DefineValue.PRODUCT_CODE, _product_code);
        mArgs.putString(DefineValue.BANK_CODE, _bank_code);
        mArgs.putString(DefineValue.BANK_NAME, _bank_name);
        mArgs.putString(DefineValue.PRODUCT_NAME, _product_name);
        mArgs.putString(DefineValue.FEE, fee);
        mArgs.putString(DefineValue.COMMUNITY_CODE, comm_code);
        mArgs.putString(DefineValue.TX_ID, _tx_id);
        mArgs.putString(DefineValue.AMOUNT, _amount);
        mArgs.putString(DefineValue.TOTAL_AMOUNT, totalAmount);
        mArgs.putString(DefineValue.SHARE_TYPE, "1");
        mArgs.putString(DefineValue.CALLBACK_URL, callback_url);
        mArgs.putString(DefineValue.API_KEY, api_key);
        mArgs.putString(DefineValue.COMMUNITY_ID, comm_id);
        mArgs.putString(DefineValue.BANK_BENEF, benef_product_name);
        mArgs.putString(DefineValue.NAME_BENEF, _benef_acct_name);
        mArgs.putString(DefineValue.NO_BENEF, no_benef);
        mArgs.putString(DefineValue.TYPE_BENEF, benef_product_type);
        mArgs.putString(DefineValue.NO_HP_BENEF, etNoHp.getText().toString());
        mArgs.putString(DefineValue.REMARK, etRemark.getText().toString());
        mArgs.putString(DefineValue.SOURCE_ACCT, source_product_name);
        mArgs.putString(DefineValue.MAX_RESEND, _max_resend_token);
        mArgs.putString(DefineValue.TRANSACTION, transaksi);
        mArgs.putString(DefineValue.BENEF_PRODUCT_CODE, benef_product_code);
        mArgs.putBoolean(DefineValue.IS_OWNER, isOwner);
        if (TCASHValidation != null)
            mArgs.putBoolean(DefineValue.TCASH_HP_VALIDATION, TCASHValidation);
//        if (MandiriLKDValidation != null)
//            mArgs.putBoolean(DefineValue.MANDIRI_LKD_VALIDATION, MandiriLKDValidation);
        if (code_success != null)
            mArgs.putBoolean(DefineValue.CODE_SUCCESS, code_success);
        btnNext.setEnabled(true);
        cashInHistory();

        Fragment mFrag = new FragDataMandiriLKD();
        mFrag.setArguments(mArgs);

        getFragmentManager().beginTransaction().addToBackStack(TAG)
                .replace(R.id.bbsTransaksiFragmentContent, mFrag, FragDataMandiriLKD.TAG).commit();
        ToggleKeyboard.hide_keyboard(act);
    }

    private void cashInHistory() {
        if (cashInHistoryModel == null) {
            cashInHistoryModel = new CashInHistoryModel();
        }

        cashInHistoryModel.setAmount(amount);
        cashInHistoryModel.setBenef_product_code(benef_product_code);
        cashInHistoryModel.setBenef_product_name(benef_product_name);
        cashInHistoryModel.setBenef_product_type(benef_product_type);
        cashInHistoryModel.setBenef_product_value_code(no_benef);
        cashInHistoryModel.setSource_product_code(source_product_code);
        cashInHistoryModel.setSource_product_name(actv_rekening_cta.getText().toString());
        cashInHistoryModel.setSource_product_type(source_product_type);
        cashInHistoryModel.setSource_product_h2h(source_product_h2h);
        cashInHistoryModel.setMember_shop_phone(etNoHp.getText().toString());
        cashInHistoryModel.setPesan(etRemark.getText().toString());

        if (!benef_product_type.equalsIgnoreCase(DefineValue.EMO)) {
            cashInHistoryModel.setBenef_product_value_city(city_name);
        }

        Gson gson = new Gson();
        String jsonObject = gson.toJson(cashInHistoryModel, CashInHistoryModel.class);

        SecurePreferences.Editor editor = sp.edit();
        editor.putString(DefineValue.CASH_IN_HISTORY_TEMP, jsonObject);
        editor.apply();
    }

    private void changeToConfirmCashout(BBSTransModel model, String productcode) {
        Bundle mArgs = new Bundle();
        mArgs.putString(DefineValue.PRODUCT_H2H, source_product_h2h);
        mArgs.putString(DefineValue.PRODUCT_TYPE, source_product_type);
        mArgs.putString(DefineValue.PRODUCT_CODE, productcode);
        mArgs.putString(DefineValue.BANK_CODE, model.getTx_bank_code());
        mArgs.putString(DefineValue.BANK_NAME, model.getTx_bank_name());
        mArgs.putString(DefineValue.PRODUCT_NAME, model.getTx_product_name());
        mArgs.putString(DefineValue.COMMUNITY_CODE, comm_code);
        mArgs.putString(DefineValue.TX_ID, model.getTx_id());
        mArgs.putString(DefineValue.AMOUNT, model.getAmount());
        mArgs.putString(DefineValue.SHARE_TYPE, "1");
        mArgs.putString(DefineValue.CALLBACK_URL, callback_url);
        mArgs.putString(DefineValue.API_KEY, api_key);
        mArgs.putString(DefineValue.COMMUNITY_ID, comm_id);
        mArgs.putString(DefineValue.BANK_BENEF, benef_product_name);
        mArgs.putString(DefineValue.USER_ID, no_source);
        mArgs.putString(DefineValue.REMARK, etRemark.getText().toString());
        mArgs.putString(DefineValue.SOURCE_ACCT, source_product_name);
        mArgs.putString(DefineValue.TRANSACTION, transaksi);
        mArgs.putString(DefineValue.FEE, model.getAdmin_fee());
        mArgs.putString(DefineValue.TOTAL_AMOUNT, model.getTotal_amount());
        btnNext.setEnabled(true);
        cashOutHistory();

        Fragment mFrag = new CashOutBBS_confirm_agent();
        mFrag.setArguments(mArgs);
        getFragmentManager().beginTransaction().addToBackStack(TAG)
                .replace(R.id.bbsTransaksiFragmentContent, mFrag, CashOutBBS_confirm_agent.TAG).commit();
        ToggleKeyboard.hide_keyboard(act);
//        switchFragment(mFrag, getString(R.string.cash_out), true);
    }

    private void cashOutHistory() {

        if (cashOutHistoryModel == null) {
            cashOutHistoryModel = new CashOutHistoryModel();
        }

        cashOutHistoryModel.setAmount(amount);
        cashOutHistoryModel.setBenef_product_code(benef_product_code);
        cashOutHistoryModel.setBenef_product_name(benef_product_name);
        cashOutHistoryModel.setBenef_product_type(benef_product_type);
        cashOutHistoryModel.setSource_product_code(source_product_code);
        cashOutHistoryModel.setSource_product_name(source_product_name);
        cashOutHistoryModel.setSource_product_type(source_product_type);
        cashOutHistoryModel.setSource_product_h2h(source_product_h2h);
        cashOutHistoryModel.setMember_shop_phone(no_source);
        cashOutHistoryModel.setPesan(etRemark.getText().toString());

        Gson gson1 = new Gson();
        String jsonObject = gson1.toJson(cashOutHistoryModel, CashOutHistoryModel.class);

        SecurePreferences.Editor editor = sp.edit();
        editor.putString(DefineValue.CASH_OUT_HISTORY_TEMP, jsonObject);
        editor.apply();
    }

    void showDialogErrorSMS(final String _nama_bank, String error_code, String error_msg) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = dialog.findViewById(R.id.title_dialog);
        TextView Message = dialog.findViewById(R.id.message_dialog);

        Message.setVisibility(View.VISIBLE);
        Title.setText(getString(R.string.topup_dialog_not_registered));
        if (error_code.equals("0059")) {
            Message.setText(error_msg);
            btnDialogOTP.setText(getString(R.string.firstscreen_button_daftar));
            btnDialogOTP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent newIntent = new Intent(getActivity(), RegisterSMSBankingActivity.class);
                    newIntent.putExtra(DefineValue.BANK_NAME, _nama_bank);
                    actionListener.ChangeActivityFromCashInput(newIntent);
                    dialog.dismiss();
                }
            });
        } else if (error_code.equals("0164")) {
            Message.setText(error_msg);
            btnDialogOTP.setText(getString(R.string.close));
            btnDialogOTP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    getActivity().finish();
                }
            });
        }

        dialog.show();
    }

    private void initializeSmsClass() {
        if (smSclass == null)
            smSclass = new SMSclass(getActivity(), CustomSimcardListener);

        smSclass.isSimExists(new SMSclass.SMS_SIM_STATE() {
            @Override
            public void sim_state(Boolean isExist, String msg) {
                if (!isExist) {
                    isSimExist = false;
                    AlertDialog.Builder builder = new AlertDialog.Builder(act);
                    builder.setMessage(msg)
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else
                    isSimExist = true;
            }
        });
    }

    private BroadcastReceiver CustomSimcardListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase("android.intent.action.SIM_STATE_CHANGED")) {
                if (intent.getStringExtra("ss").equalsIgnoreCase("ABSENT")) {
                    if (smsDialog != null && smsDialog.isShowing()) {
                        Toast.makeText(getActivity(), R.string.smsclass_simcard_listener_absent_toast, Toast.LENGTH_LONG).show();
                        smsDialog.dismiss();
                        smsDialog.reset();
                    }
                }

            }
        }
    };

    private void RegSimCardReceiver(Boolean isReg) {
        if (isSMSBanking) {
            if (isReg) {
                try {
                    getActivity().unregisterReceiver(CustomSimcardListener);
                } catch (Exception ignored) {
                }
                getActivity().registerReceiver(CustomSimcardListener, SMSclass.simStateIntentFilter);
            } else {
                try {
                    getActivity().unregisterReceiver(CustomSimcardListener);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (requestCode == RC_READ_PHONE_STATE) {
            initializeSmsClass();
            if (isSimExist)
                SubmitAction(true);
        } else if (requestCode == RC_SEND_SMS) {
            smsDialog.sentSms();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Toast.makeText(getActivity(), getString(R.string.cancel_permission_read_contacts), Toast.LENGTH_SHORT).show();
        if (requestCode == RC_SEND_SMS) {
            dismissProgressDialog();
            if (smsDialog != null) {
                smsDialog.dismiss();
                smsDialog.reset();
            }
        }
    }

    private boolean inputValidation() {
        if (transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
            if (actv_rekening_cta.getText().toString().length() == 0) {
                actv_rekening_cta.requestFocus();
                actv_rekening_cta.setError(getString(R.string.rekening_agent_error_message));
                return false;
            } else actv_rekening_cta.setError(null);
            if (etNoHp.getText().toString().length() == 0) {
                etNoHp.requestFocus();
                etNoHp.setError(getString(R.string.no_hp_pengirim_validation));
                return false;
            }
            if (source_product_code.equals("")) {
                Toast.makeText(act, getString(R.string.no_match_agent_acct_message), Toast.LENGTH_LONG).show();
                return false;
            }
        } else {
            if (benef_product_code.equals("")) {
                Toast.makeText(act, getString(R.string.no_match_agent_acct_message), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    @Override
    public void onOK() {
        extraSignature = comm_code + member_code + source_product_type + source_product_code + benef_product_type + benef_product_code
                + MyApiClient.CCY_VALUE + amount;
        if (transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
            sentInsertC2A();
        } else
            sentInsertA2C();
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

    @Override
    public void onResume() {
        super.onResume();
        RegSimCardReceiver(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        RegSimCardReceiver(false);
    }
}
