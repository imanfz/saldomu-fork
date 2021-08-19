package com.sgo.saldomu.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.faber.circlestepview.CircleStepView;
import com.google.gson.Gson;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.CashInHistoryModel;
import com.sgo.saldomu.Beans.CashOutHistoryModel;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.activities.TutorialActivity;
import com.sgo.saldomu.coreclass.BBSDataManager;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.entityRealm.BBSBankModel;
import com.sgo.saldomu.entityRealm.BBSCommModel;
import com.sgo.saldomu.entityRealm.List_BBS_City;
import com.sgo.saldomu.utils.BbsUtil;
import com.sgo.saldomu.widgets.CustomAutoCompleteTextViewWithIcon;

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
    public final static String TAG = "BBSTransaksiAmount";

    private View v, inputForm, emptyLayout, cityLayout, nameLayout;
    private TextView tvTitle;
    private AutoCompleteTextView etAmount;
    private String transaksi, benef_product_type, type, defaultAmount, noHpPengirim,
            benef_product_code, source_product_code, defaultProductCode, noHpMemberLocation = "", enabledAdditionalFee;
    private Activity act;
    private Button btnProses, btnBack;
    private Realm realm, realmBBS;
    private String CTA = "CTA";
    private String ATC = "ATC";
    private String SOURCE = "SOURCE";
    private String BENEF = "BENEF";
    private CustomAutoCompleteTextViewWithIcon actv_rekening_member;
    private List<HashMap<String, String>> aListMember;
    private SimpleAdapter adapterMember;
    private List<BBSBankModel> listbankSource, listbankBenef;
    private EditText etNoAcct, etNameAcct, etNoOTPC2A;
    private TextView tvEgNo, tvDestination, tvBankExample, tvSource;
    private ImageView spinwheelCity;
    private AutoCompleteTextView spBenefCity;
    private Animation frameAnimation;
    private ArrayList<List_BBS_City> list_bbs_cities;
    private ArrayList<String> list_name_bbs_cities;
    private Integer CityAutocompletePos = -1;
    BBSCommModel comm;
    SecurePreferences sp;
    CashInHistoryModel cashInHistoryModel;
    CashOutHistoryModel cashOutHistoryModel;
    String denom[] = {"10000", "20000", "50000", "100000", "150000", "200000"};
    private LinearLayout layoutBankBenefCTA;
    private Boolean isAgentLKD = false;
    BBSBankModel bbsBankModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        act = getActivity();
        realm = Realm.getDefaultInstance();
        realmBBS = Realm.getInstance(RealmManager.BBSConfiguration);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        Bundle bundle = getArguments();
        if (bundle != null) {
            transaksi = bundle.getString(DefineValue.TRANSACTION);
            type = bundle.getString(DefineValue.TYPE, "");
            defaultAmount = bundle.getString(DefineValue.AMOUNT, "");
            noHpPengirim = bundle.getString(DefineValue.KEY_CODE, "");
            noHpMemberLocation = bundle.getString(DefineValue.KEY_CODE, "");

            defaultProductCode = "";
            if (bundle.containsKey(DefineValue.PRODUCT_CODE)) {
                defaultProductCode = bundle.getString(DefineValue.PRODUCT_CODE, "");
            }


            if (transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
                String cashIn = sp.getString(DefineValue.CASH_IN_HISTORY_TEMP, "");
                Gson gson = new Gson();
                cashInHistoryModel = gson.fromJson(cashIn, CashInHistoryModel.class);

            } else if (transaksi.equalsIgnoreCase(getString(R.string.cash_out))) {
                String cashOut = sp.getString(DefineValue.CASH_OUT_HISTORY_TEMP, "");
                Gson gson1 = new Gson();
                cashOutHistoryModel = gson1.fromJson(cashOut, CashOutHistoryModel.class);
            }


        } else {
            getFragmentManager().popBackStack();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.bbs_transaksi_amount, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CircleStepView mCircleStepView = v.findViewById(R.id.circle_step_view);
        mCircleStepView.setTextBelowCircle(getString(R.string.informasi_pelanggan), "", "");
//        mCircleStepView.setTextBelowCircle(getString(R.string.informasi_pelanggan), getString(R.string.informasi), getString(R.string.konfirmasi));
        mCircleStepView.setCurrentCircleIndex(0, false);

        tvTitle = v.findViewById(R.id.tv_title);
        inputForm = v.findViewById(R.id.bbs_amount_form);
        emptyLayout = v.findViewById(R.id.empty_layout);
        etAmount = v.findViewById(R.id.jumlah_transfer_edit);

        ArrayAdapter adapterDenom = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, denom);

        etAmount.setAdapter(adapterDenom);
        etAmount.setThreshold(1);
        etAmount.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                etAmount.showDropDown();
                return false;
            }
        });

        btnProses = v.findViewById(R.id.proses_btn);
        btnBack = v.findViewById(R.id.back_btn);
        ViewStub stub = v.findViewById(R.id.transaksi_stub);
        tvTitle.setText(transaksi);
        emptyLayout.setVisibility(View.GONE);
        isAgentLKD = sp.getString(DefineValue.COMPANY_TYPE, "").equalsIgnoreCase(getString(R.string.LKD));
        if (transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
            if (type.equalsIgnoreCase(DefineValue.BBS_CASHIN)) {
                if (!defaultAmount.equals("")) {
                    etAmount.setText(defaultAmount);
                } else {
                    if (cashInHistoryModel != null) {
                        etAmount.setText(cashInHistoryModel.getAmount());
                    }
                }
            } else if (cashInHistoryModel != null) {
                etAmount.setText(cashInHistoryModel.getAmount());
            }

            stub.setLayoutResource(R.layout.bbs_cashin_amount);
            View cashin_layout = stub.inflate();

            nameLayout = cashin_layout.findViewById(R.id.bbs_cashin_name_layout);
            actv_rekening_member = cashin_layout.findViewById(R.id.rekening_member_value);
            etNoOTPC2A = cashin_layout.findViewById(R.id.no_OTP);
            etNoAcct = cashin_layout.findViewById(R.id.no_tujuan_value);
            tvEgNo = cashin_layout.findViewById(R.id.tv_eg_no);
            etNameAcct = cashin_layout.findViewById(R.id.name_value);
            cityLayout = cashin_layout.findViewById(R.id.bbscashin_city_layout);
            spBenefCity = cashin_layout.findViewById(R.id.bbscashin_value_city_benef);
            spinwheelCity = cashin_layout.findViewById(R.id.spinning_wheel_bbscashin_city);
            layoutBankBenefCTA = cashin_layout.findViewById(R.id.layout_bank_benef_cashin);
            tvDestination = cashin_layout.findViewById(R.id.tv_destination);
            tvBankExample = cashin_layout.findViewById(R.id.textView7);
            frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
            frameAnimation.setRepeatCount(Animation.INFINITE);

            etNoAcct.setText(getArguments().getString(DefineValue.FAVORITE_CUSTOMER_ID, ""));

            if (isAgentLKD) {
                tvDestination.setText(getString(R.string.label_bank_transfer_ke_emoney));
                tvBankExample.setVisibility(View.INVISIBLE);
                actv_rekening_member.setHint(getString(R.string.label_bank_pelangggan_lkd));
                etNoAcct.setHint(R.string.number_hp_destination_hint);
            }

            // Keys used in Hashmap
            String[] from = {"flag", "txt"};

            // Ids of views in listview_layout
            int[] to = {R.id.flag, R.id.txt};

            aListMember = new ArrayList<>();
            // Instantiating an adapter to store each items
            // R.layout.listview_layout defines the layout of each item
            adapterMember = new SimpleAdapter(getActivity().getBaseContext(), aListMember, R.layout.bbs_autocomplete_layout, from, to);

            initializeDataBBS(CTA);
        } else {
            if (type.equalsIgnoreCase(DefineValue.BBS_CASHOUT)) {
                if (!defaultAmount.equals("")) {
                    etAmount.setText(defaultAmount);
                } else {
                    if (cashOutHistoryModel != null) {
                        etAmount.setText(cashOutHistoryModel.getAmount());
                    }
                }
            } else if (cashOutHistoryModel != null) {
                etAmount.setText(cashOutHistoryModel.getAmount());
            }


            stub.setLayoutResource(R.layout.bbs_cashout_amount);
            View cashout_layout = stub.inflate();

            actv_rekening_member = cashout_layout.findViewById(R.id.rekening_member_value);
            etNoAcct = cashout_layout.findViewById(R.id.no_tujuan_value);
            tvSource = cashout_layout.findViewById(R.id.tv_source);
            tvBankExample = cashout_layout.findViewById(R.id.tv_bank_example);

            etNoAcct.setText(getArguments().getString(DefineValue.FAVORITE_CUSTOMER_ID, ""));

            if (isAgentLKD) {
                tvSource.setText(getString(R.string.label_transfer_dari_member_atc_lkd));
                tvBankExample.setVisibility(View.INVISIBLE);
                actv_rekening_member.setHint(getString(R.string.label_bank_pelangggan_lkd));
            }


            // Keys used in Hashmap
            String[] from = {"flag", "txt"};

            // Ids of views in listview_layout
            int[] to = {R.id.flag, R.id.txt};

            aListMember = new ArrayList<>();
            // Instantiating an adapter to store each items
            // R.layout.listview_layout defines the layout of each item
            adapterMember = new SimpleAdapter(getActivity().getBaseContext(), aListMember, R.layout.bbs_autocomplete_layout, from, to);

            initializeDataBBS(ATC);
        }

//        if (isAgentLKD) {
//            actv_rekening_member.setText(defaultProductCode);
//            actv_rekening_member.setEnabled(false);
//        } else {
//            actv_rekening_member.setText(defaultProductCode);
//            actv_rekening_member.requestFocus();
//        }

        actv_rekening_member.setAdapter(adapterMember);
        actv_rekening_member.addTextChangedListener(textWatcher);
        btnBack.setOnClickListener(backListener);
        btnProses.setOnClickListener(prosesListener);

        if (transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
            if (isAgentLKD)
            {
                if (BuildConfig.FLAVOR.equalsIgnoreCase("development")) {
                    defaultProductCode = getString(R.string.EMOSALDOMU);
                } else
                    defaultProductCode = getString(R.string.SALDOMU);
            }
            if (!defaultProductCode.equals("")) {

                bbsBankModel = realmBBS.where(BBSBankModel.class).
                        equalTo(BBSBankModel.SCHEME_CODE, DefineValue.CTA).
                        equalTo(BBSBankModel.PRODUCT_CODE, defaultProductCode)
                        .equalTo(BBSBankModel.COMM_TYPE, DefineValue.BENEF)
                        .findFirst();
//                }

                if (bbsBankModel != null) {
                    actv_rekening_member.setText(bbsBankModel.getProduct_name());

                    if (bbsBankModel.getProduct_display().equals(DefineValue.STRING_YES)) {


                        if (!noHpPengirim.equals("")) {
                            etNoAcct.setText(noHpPengirim);
                        }
                    }
                }
            } else if (cashInHistoryModel != null) {
                actv_rekening_member.setText(cashInHistoryModel.getBenef_product_name());
                etNoAcct.setText(cashInHistoryModel.getBenef_product_value_code());
            }
//            validasiTutorialCashIn();
        }

        if (transaksi.equalsIgnoreCase(getString(R.string.cash_out))) {
            if (isAgentLKD)
            {
                if (BuildConfig.FLAVOR.equalsIgnoreCase("development")) {
                    defaultProductCode = getString(R.string.EMOSALDOMU);
                } else
                    defaultProductCode = getString(R.string.SALDOMU);
            }
            if (!defaultProductCode.equals("")) {
                bbsBankModel = realmBBS.where(BBSBankModel.class).
                        equalTo(BBSBankModel.SCHEME_CODE, DefineValue.ATC).
                        equalTo(BBSBankModel.PRODUCT_CODE, defaultProductCode)
                        .equalTo(BBSBankModel.COMM_TYPE, DefineValue.SOURCE)
                        .findFirst();

                if (bbsBankModel != null) {
                    actv_rekening_member.setText(bbsBankModel.getProduct_name());
                    if (bbsBankModel.getProduct_display().equals(DefineValue.STRING_YES)) {


                        if (!noHpPengirim.equals("")) {
                            etNoAcct.setText(noHpPengirim);
                        }
                    }
                }
            } else if (cashOutHistoryModel != null) {
                actv_rekening_member.setText(cashOutHistoryModel.getSource_product_name());
                etNoAcct.setText(cashOutHistoryModel.getMember_shop_phone());
            }
//            validasiTutorialCashOut();
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
                if (transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
                    showTutorialCashIn();
                } else if (transaksi.equalsIgnoreCase(getString(R.string.cash_out))) {
                    showTutorialCashOut();
                }
                return true;
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void validasiTutorialCashIn() {
        if (transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
            if (sp.contains(DefineValue.TUTORIAL_CASHIN)) {
                Boolean is_first_time = sp.getBoolean(DefineValue.TUTORIAL_CASHIN, false);
                if (is_first_time) {
                    showTutorialCashIn();
                }
            } else {
                showTutorialCashIn();
            }
        }
    }


    private void validasiTutorialCashOut() {
        if (transaksi.equalsIgnoreCase(getString(R.string.cash_out))) {
            if (sp.contains(DefineValue.TUTORIAL_CASHOUT)) {
                Boolean is_first_time = sp.getBoolean(DefineValue.TUTORIAL_CASHOUT, false);
                if (is_first_time)
                    showTutorialCashOut();
            }
        } else {
            showTutorialCashOut();
        }
    }

    private void showTutorialCashIn() {
        Intent intent = new Intent(getActivity(), TutorialActivity.class);
        intent.putExtra(DefineValue.TYPE, TutorialActivity.tutorial_cash_in);
        startActivity(intent);
    }

    private void showTutorialCashOut() {
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
            for (int i = 0; i < aListMember.size(); i++) {
                if (nameAcct.equalsIgnoreCase(aListMember.get(i).get("txt"))) {
                    position = i;
                    if (transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
                        if (isAgentLKD) {
                            benef_product_type = listbankBenef.get(0).getProduct_type();
                            benef_product_code = listbankBenef.get(0).getProduct_code();
                            enabledAdditionalFee = listbankBenef.get(0).getEnabled_additional_fee();
                        } else {
                            benef_product_type = listbankBenef.get(position).getProduct_type();
                            benef_product_code = listbankBenef.get(position).getProduct_code();
                            enabledAdditionalFee = listbankBenef.get(position).getEnabled_additional_fee();
                        }
                        if ((benef_product_type.equalsIgnoreCase(DefineValue.EMO) && !benef_product_code.equalsIgnoreCase("MANDIRILKD"))) {
//                            cityLayout.setVisibility(View.GONE);
                            etNoAcct.setHint(R.string.number_hp_destination_hint);
                            tvEgNo.setText(getString(R.string.eg_no_hp));
                        } else {
                            if (benef_product_code.equalsIgnoreCase("MANDIRILKD")) {
                                etNoAcct.setHint(R.string.nomor_rekening);
                            } else {
                                etNoAcct.setHint(R.string.number_destination_hint);
                            }
//                            cityLayout.setVisibility(VISIBLE);
                            tvEgNo.setText(getString(R.string.eg_no_acct));
                        }
                        if (listbankBenef.get(position).getBank_gateway().equalsIgnoreCase(DefineValue.STRING_YES))
                            nameLayout.setVisibility(View.GONE);
                        else
                            nameLayout.setVisibility(View.VISIBLE);

//                        if(benef_product_code.equalsIgnoreCase("TCASH") || benef_product_code.equalsIgnoreCase("MANDIRILKD"))
                        if (benef_product_code.equalsIgnoreCase("tcash")) {
                            etNoOTPC2A.setVisibility(View.VISIBLE);
                        } else
                            etNoOTPC2A.setVisibility(View.GONE);

                    } else {
                        if (listbankSource.get(position).getBank_gateway() != null) {
                            if (isAgentLKD) {
                                source_product_code = listbankSource.get(0).getProduct_code();
                                enabledAdditionalFee = listbankSource.get(0).getEnabled_additional_fee();
                            } else {
                                source_product_code = listbankSource.get(position).getProduct_code();
                                enabledAdditionalFee = listbankSource.get(position).getEnabled_additional_fee();
                            }
//                            if (listbankSource.get(position).getProduct_type().equalsIgnoreCase(DefineValue.ACCT) || source_product_code.equalsIgnoreCase("MANDIRILKD"))
                            if (source_product_code.equalsIgnoreCase("MANDIRILKD")) {
                                etNoAcct.setHint(getString(R.string.nomor_rekening) + " " + listbankSource.get(position).getProduct_name());
                            } else
                                etNoAcct.setHint(getString(R.string.customer_mobile_number) + " " + getString(R.string.appname));
//                                else if (source_product_code.equalsIgnoreCase("MANDIRIIB"))
//                                {
//                                    etNoAcct.setHint(getString(R.string.user_id) + " " + listbankSource.get(position).getProduct_name());
//                                }else etNoAcct.setHint(getString(R.string.no_rekening_source_cashout) + " " + listbankSource.get(position).getProduct_name());

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

    Button.OnClickListener prosesListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (inputValidation()) {
                int position = -1;
                String nameAcct = actv_rekening_member.getText().toString();
                for (int i = 0; i < aListMember.size(); i++) {
                    if (nameAcct.equalsIgnoreCase(aListMember.get(i).get("txt")))
                        position = i;
                }

                if (position != -1) {
                    Fragment newFrag = new BBSTransaksiInformasi();
                    Bundle args = new Bundle();
                    args.putString(DefineValue.TRANSACTION, transaksi);
                    args.putString(DefineValue.AMOUNT, etAmount.getText().toString());
                    args.putString(DefineValue.COMMUNITY_ID, comm.getComm_id());
                    args.putString(DefineValue.COMMUNITY_CODE, comm.getComm_code());
                    args.putString(DefineValue.MEMBER_CODE, comm.getMember_code());
                    args.putString(DefineValue.CALLBACK_URL, comm.getCallback_url());
                    args.putString(DefineValue.API_KEY, comm.getApi_key());

                    if (transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
                        args.putString(DefineValue.BENEF_PRODUCT_CODE, listbankBenef.get(position).getProduct_code());
                        args.putString(DefineValue.BENEF_PRODUCT_TYPE, listbankBenef.get(position).getProduct_type());
                        args.putString(DefineValue.BENEF_PRODUCT_NAME, listbankBenef.get(position).getProduct_name());
                        args.putString(DefineValue.ENABLED_ADDITIONAL_FEE, listbankBenef.get(position).getEnabled_additional_fee());
                        args.putString(DefineValue.NO_BENEF, etNoAcct.getText().toString());
                        if (nameLayout.getVisibility() == View.VISIBLE) {
                            args.putString(DefineValue.NAME_BENEF, etNameAcct.getText().toString());
                        } else {
                            args.putString(DefineValue.NAME_BENEF, "");
                        }
                        if (benef_product_type.equalsIgnoreCase(DefineValue.ACCT)) {
                            String city_id = list_bbs_cities.get(CityAutocompletePos).getCity_id();
                            String city_name = spBenefCity.getText().toString();
                            args.putString(DefineValue.ACCT_CITY_CODE, city_id);
                            args.putString(DefineValue.ACCT_CITY_NAME, city_name);
                        }
                        if (benef_product_code.equalsIgnoreCase("tcash")) {
                            args.putString(DefineValue.BENEF_PRODUCT_VALUE_TOKEN, etNoOTPC2A.getText().toString());
                        }
                        if (type.equalsIgnoreCase(DefineValue.BBS_CASHIN)) {
                            if (!noHpPengirim.equals(""))
                                args.putString(DefineValue.KEY_CODE, noHpPengirim);
                        }
                    } else if (transaksi.equalsIgnoreCase(getString(R.string.cash_out))) {
                        args.putString(DefineValue.SOURCE_PRODUCT_CODE, listbankSource.get(position).getProduct_code());
                        args.putString(DefineValue.SOURCE_PRODUCT_TYPE, listbankSource.get(position).getProduct_type());
                        args.putString(DefineValue.SOURCE_PRODUCT_NAME, listbankSource.get(position).getProduct_name());
                        args.putString(DefineValue.SOURCE_PRODUCT_H2H, listbankSource.get(position).getProduct_h2h());
                        args.putString(DefineValue.ENABLED_ADDITIONAL_FEE, listbankSource.get(position).getEnabled_additional_fee());
                        args.putString(DefineValue.SOURCE_ACCT_NO, etNoAcct.getText().toString());
                        args.putString(DefineValue.NO_HP_MEMBER_LOCATION, noHpMemberLocation);
                    }
                    newFrag.setArguments(args);

                    getFragmentManager().beginTransaction().replace(R.id.bbsTransaksiFragmentContent, newFrag, BBSTransaksiInformasi.TAG)
                            .addToBackStack(TAG).commit();
//                    switchFragment(newFrag, "Tarik Tunai", true);
                    ToggleKeyboard.hide_keyboard(act);
                } else {
                    Toast.makeText(act, getString(R.string.no_match_member_acct_message), Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private void switchFragment(Fragment i, String name, Boolean isBackstack) {
        if (getActivity() == null)
            return;

        BBSActivity fca = (BBSActivity) getActivity();
        fca.switchContent(i, name, isBackstack);
    }

    private void setMember(List<BBSBankModel> bankMember) {
        aListMember.clear();

        aListMember.addAll(BbsUtil.mappingProductCodeIcons(bankMember));

//        for(int i=0;i<bankMember.size();i++){
//            HashMap<String, String> hm = new HashMap<>();
//            hm.put("txt", bankMember.get(i).getProduct_name());
//
//            if(bankMember.get(i).getProduct_name().toLowerCase().contains("mandiri"))
//                hm.put("flag", Integer.toString(R.drawable.logo_mandiri_bank_small));
//            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("bri"))
//                hm.put("flag", Integer.toString(R.drawable.logo_bank_bri_small));
//            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("permata"))
//                hm.put("flag", Integer.toString(R.drawable.logo_bank_permata_small));
//            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("uob"))
//                hm.put("flag", Integer.toString(R.drawable.logo_bank_uob_small));
//            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("maspion"))
//                hm.put("flag", Integer.toString(R.drawable.logo_bank_maspion_rev1_small));
//            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("bii"))
//                hm.put("flag", Integer.toString(R.drawable.logo_bank_bii_small));
//            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("jatim"))
//                hm.put("flag", Integer.toString(R.drawable.logo_bank_jatim_small));
//            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("bca"))
//                hm.put("flag", Integer.toString(R.drawable.logo_bca_bank_small));
//            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("nobu"))
//                hm.put("flag", Integer.toString(R.drawable.logo_bank_nobu));
//            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("saldomu"))
//                hm.put("flag", Integer.toString(R.drawable.logo_small));
//            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("telkomsel"))
//                hm.put("flag", Integer.toString(R.drawable.tcash_small));
//            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("unik"))
//                hm.put("flag", Integer.toString(R.drawable.unik_small));
//            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("akardaya"))
//                hm.put("flag", Integer.toString(R.drawable.mad_small));
//            else
//                hm.put("flag", Integer.toString(R.drawable.ic_square_gate_one));
//            aListMember.add(hm);
//        }
        adapterMember.notifyDataSetChanged();
    }

    private void setBBSCity() {
        spBenefCity.setVisibility(View.GONE);
        spinwheelCity.setVisibility(View.VISIBLE);
        spinwheelCity.startAnimation(frameAnimation);

        Thread proses = new Thread() {

            @Override
            public void run() {
                RealmResults results = realm.where(List_BBS_City.class).findAll();
                list_bbs_cities = new ArrayList<>(results);
                list_name_bbs_cities = new ArrayList<>();
                if (list_bbs_cities.size() > 0) {
                    for (int i = 0; i < list_bbs_cities.size(); i++) {
                        list_name_bbs_cities.add(list_bbs_cities.get(i).getCity_name());
                    }
                } else {
//                    UpdateBBSCity.startUpdateBBSCity(getActivity());
                }

                final ArrayAdapter<String> city_adapter = new ArrayAdapter<String>
                        (getActivity(), android.R.layout.select_dialog_item, list_name_bbs_cities);

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


    private void initializeDataBBS(String schemeCode) {
        comm = realmBBS.where(BBSCommModel.class)
                .equalTo(WebParams.SCHEME_CODE, schemeCode).findFirst();
        if (isAgentLKD)
        {
            if (BuildConfig.FLAVOR.equalsIgnoreCase("development"))
                defaultProductCode = "EMO SALDOMU";
            else
                defaultProductCode = getString(R.string.SALDOMU);
        }


        if (schemeCode.equalsIgnoreCase(CTA)) {
            if (isAgentLKD) {
                listbankBenef = realmBBS.where(BBSBankModel.class)
                        .equalTo(WebParams.SCHEME_CODE, CTA)
                        .equalTo(WebParams.COMM_TYPE, BENEF)
                        .equalTo(WebParams.PRODUCT_NAME, defaultProductCode).findAll();
            } else {
                listbankBenef = realmBBS.where(BBSBankModel.class)
                        .equalTo(WebParams.SCHEME_CODE, CTA)
                        .equalTo(WebParams.COMM_TYPE, BENEF).findAll();
            }
            setBBSCity();
            setMember(listbankBenef);
        } else {
            if (isAgentLKD) {
                listbankSource = realmBBS.where(BBSBankModel.class)
                        .equalTo(WebParams.SCHEME_CODE, ATC)
                        .equalTo(WebParams.COMM_TYPE, SOURCE)
                        .equalTo(WebParams.PRODUCT_NAME, defaultProductCode).findAll();
            } else {
                listbankSource = realmBBS.where(BBSBankModel.class)
                        .equalTo(WebParams.SCHEME_CODE, ATC)
                        .equalTo(WebParams.COMM_TYPE, SOURCE).findAll();
            }

            if (listbankSource == null) {
                Toast.makeText(getActivity(), getString(R.string.no_source_list_message), Toast.LENGTH_LONG).show();
                emptyLayout.setVisibility(View.VISIBLE);
                inputForm.setVisibility(View.GONE);
            }
            setMember(listbankSource);

        }

        if (comm == null) {
            inputForm.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
            if (schemeCode.equalsIgnoreCase(CTA))
                Toast.makeText(getActivity(), getString(R.string.bbstransaction_toast_not_registered,
                        getString(R.string.cash_in)), Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getActivity(), getString(R.string.bbstransaction_toast_not_registered,
                        getString(R.string.cash_out)), Toast.LENGTH_LONG).show();

            boolean isUpdatingData = sp.getBoolean(DefineValue.IS_UPDATING_BBS_DATA, false);
            if (!isUpdatingData)
                checkAndRunServiceBBS();
        }

    }

    void checkAndRunServiceBBS() {
        BBSDataManager bbsDataManager = new BBSDataManager();
        if (!bbsDataManager.isDataUpdated()) {
//            showProgressDialog();
            bbsDataManager.runServiceUpdateData(getContext());
            Timber.d("Run Service update data BBS");
        }
    }

    private boolean inputValidation() {
        if (etAmount.getText().toString().length() == 0) {
            etAmount.requestFocus();
            etAmount.setError(getString(R.string.payfriends_amount_validation));
            return false;
        }
//        else if(Integer.parseInt(etAmount.getText().toString()) < 1){
//            etAmount.requestFocus();
//            etAmount.setError(getString(R.string.payfriends_amount_zero));
//            return false;
//        }


        if (actv_rekening_member.getText().toString().length() == 0) {
            actv_rekening_member.requestFocus();
            actv_rekening_member.setError(getString(R.string.rekening_member_error_message));
            return false;
        } else actv_rekening_member.setError(null);
        if (transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
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
            } else {
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
            if (benef_product_code.equalsIgnoreCase("tcash")) {
                if (etNoOTPC2A.getText().toString().length() == 0) {
                    etNoOTPC2A.requestFocus();
                    etNoOTPC2A.setError("Kode OTP dibutuhkan!");
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

            if (cityLayout.getVisibility() == View.VISIBLE) {
                String autocomplete_text = spBenefCity.getText().toString();

                if (autocomplete_text.equals("")) {
                    spBenefCity.requestFocus();
                    spBenefCity.setError(getString(R.string.destination_city_empty_message));
                    return false;
                } else if (!list_name_bbs_cities.contains(autocomplete_text)) {

                    spBenefCity.requestFocus();
                    spBenefCity.setError(getString(R.string.city_not_found_message));
                    return false;
                } else {
                    CityAutocompletePos = list_name_bbs_cities.indexOf(autocomplete_text);
                    spBenefCity.setError(null);
                }
            }
        } else {
            if (etNoAcct.getText().toString().length() == 0) {
                etNoAcct.requestFocus();
                etNoAcct.setError(getString(R.string.customer_mobile_number_validation));
                return false;
            }
        }


        return true;
    }

    @Override
    public void onDestroy() {
        RealmManager.closeRealm(realm);
        RealmManager.closeRealm(realmBBS);
        super.onDestroy();
    }

    //    private void switchFragment(Fragment i, String name, Boolean isBackstack){
//        if (getActivity() == null)
//            return;
//
//        BBSActivity fca = (BBSActivity ) getActivity();
//        fca.switchContent(i,name,isBackstack);
//    }
}
