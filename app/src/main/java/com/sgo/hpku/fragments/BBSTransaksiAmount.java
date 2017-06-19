package com.sgo.hpku.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.faber.circlestepview.CircleStepView;
import com.sgo.hpku.R;
import com.sgo.hpku.activities.BBSActivity;
import com.sgo.hpku.widgets.CustomAutoCompleteTextView;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.RealmManager;
import com.sgo.hpku.coreclass.ToggleKeyboard;
import com.sgo.hpku.coreclass.WebParams;
import com.sgo.hpku.entityRealm.BBSBankModel;
import com.sgo.hpku.entityRealm.BBSCommModel;
import com.sgo.hpku.fragments.BBSTransaksiInformasi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;

/**
 * Created by thinkpad on 4/20/2017.
 */

public class BBSTransaksiAmount extends Fragment {
    public final static String TAG = "com.sgo.hpku.fragments.BBSTransaksiAmount";

    private View v, inputForm, emptyLayout;
    private TextView tvTitle;
    private EditText etAmount;
    private String transaksi, comm_code, member_code, source_product_code, source_product_type,
            benef_product_code, benef_product_name, benef_product_type, source_product_h2h,
            api_key, callback_url, source_product_name, comm_id;
    private Activity act;
    private Button btnProses, btnBack;
    private Realm realm;
    private String CTA = "CTA";
    private String ATC = "ATC";
    private String SOURCE = "SOURCE";
    private String BENEF = "BENEF";
    private CustomAutoCompleteTextView actv_rekening_agent, actv_rekening_member;
    private List<HashMap<String,String>> aListAgent, aListMember;
    private SimpleAdapter adapterAgent, adapterMember;
    private List<BBSBankModel> listbankSource, listbankBenef;

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

        act = getActivity();
        realm = Realm.getInstance(RealmManager.BBSConfiguration);

        Bundle bundle = getArguments();
        if(bundle!= null) {
            transaksi = bundle.getString(DefineValue.TRANSACTION);

            CircleStepView mCircleStepView = ((CircleStepView) v.findViewById(R.id.circle_step_view));
            mCircleStepView.setTextBelowCircle(getString(R.string.jumlah), getString(R.string.informasi), getString(R.string.konfirmasi));
            mCircleStepView.setCurrentCircleIndex(0, false);

            tvTitle = (TextView) v.findViewById(R.id.tv_title);
            inputForm = v.findViewById(R.id.bbstransaksi_form);
            emptyLayout = v.findViewById(R.id.empty_layout);
            etAmount = (EditText) v.findViewById(R.id.jumlah_transfer_edit);
            btnProses = (Button) v.findViewById(R.id.proses_btn);
            btnBack = (Button) v.findViewById(R.id.back_btn);

            tvTitle.setText(transaksi);
            emptyLayout.setVisibility(View.GONE);

            //Getting the instance of AutoCompleteTextView
            actv_rekening_agent = (CustomAutoCompleteTextView) v.findViewById(R.id.rekening_agen_value);
            //Getting the instance of AutoCompleteTextView
            actv_rekening_member = (CustomAutoCompleteTextView) v.findViewById(R.id.rekening_member_value);

            // Keys used in Hashmap
            String[] from = {"flag", "txt"};

            // Ids of views in listview_layout
            int[] to = {R.id.flag, R.id.txt};

            aListAgent = new ArrayList<>();
            aListMember = new ArrayList<>();
            // Instantiating an adapter to store each items
            // R.layout.listview_layout defines the layout of each item
            adapterAgent = new SimpleAdapter(getActivity().getBaseContext(), aListAgent, R.layout.bbs_autocomplete_layout, from, to);
            adapterMember = new SimpleAdapter(getActivity().getBaseContext(), aListMember, R.layout.bbs_autocomplete_layout, from, to);

            BBSCommModel comm;
            if (transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
                comm = realm.where(BBSCommModel.class)
                        .equalTo(WebParams.SCHEME_CODE, CTA).findFirst();
                listbankSource = realm.where(BBSBankModel.class)
                        .equalTo(WebParams.SCHEME_CODE, CTA)
                        .equalTo(WebParams.COMM_TYPE, SOURCE).findAll();
                listbankBenef = realm.where(BBSBankModel.class)
                        .equalTo(WebParams.SCHEME_CODE, CTA)
                        .equalTo(WebParams.COMM_TYPE, BENEF).findAll();
                setSourceBenef(listbankSource, listbankBenef);
            } else {
                comm = realm.where(BBSCommModel.class)
                        .equalTo(WebParams.SCHEME_CODE, ATC).findFirst();
                listbankSource = realm.where(BBSBankModel.class)
                        .equalTo(WebParams.SCHEME_CODE, ATC)
                        .equalTo(WebParams.COMM_TYPE, SOURCE).findAll();
                listbankBenef = realm.where(BBSBankModel.class)
                        .equalTo(WebParams.SCHEME_CODE, ATC)
                        .equalTo(WebParams.COMM_TYPE, BENEF).findAll();
                setSourceBenef(listbankBenef, listbankSource);
            }

            comm_id = comm.getComm_id();
            comm_code = comm.getComm_code();
            member_code = comm.getMember_code();
            callback_url = comm.getCallback_url();
            api_key = comm.getApi_key();

            btnBack.setOnClickListener(backListener);
            btnProses.setOnClickListener(prosesListener);
        }
        else {
            getFragmentManager().popBackStack();
        }
    }

    Button.OnClickListener backListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            getActivity().finish();
        }
    };

    Button.OnClickListener prosesListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(inputValidation()) {
                Fragment newFrag = new BBSTransaksiInformasi();
                Bundle args = new Bundle();
                args.putString(DefineValue.TRANSACTION, transaksi);
                args.putString(DefineValue.AMOUNT, etAmount.getText().toString());
                args.putString(DefineValue.COMMUNITY_ID, comm_id);
                args.putString(DefineValue.COMMUNITY_CODE, comm_code);
                args.putString(DefineValue.MEMBER_CODE, member_code);
                args.putString(DefineValue.CALLBACK_URL, callback_url);
                args.putString(DefineValue.API_KEY, api_key);

                int sourcePos = 0, benefPos = 0;
                if(transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
                    String sourceName = actv_rekening_agent.getText().toString();
                    for(int i = 0 ; i < aListAgent.size() ; i++) {
                        if(sourceName.equalsIgnoreCase(aListAgent.get(i).get("txt")))
                            sourcePos = i;
                    }
                    String benefName = actv_rekening_member.getText().toString();
                    for(int i = 0 ; i < aListMember.size() ; i++) {
                        if(benefName.equalsIgnoreCase(aListMember.get(i).get("txt")))
                            benefPos = i;
                    }
                }
                else {
                    String sourceName = actv_rekening_member.getText().toString();
                    for(int i = 0 ; i < aListMember.size() ; i++) {
                        if(sourceName.equalsIgnoreCase(aListMember.get(i).get("txt")))
                            sourcePos = i;
                    }
                    String benefName = actv_rekening_agent.getText().toString();
                    for(int i = 0 ; i < aListAgent.size() ; i++) {
                        if(benefName.equalsIgnoreCase(aListAgent.get(i).get("txt")))
                            benefPos = i;
                    }
                }

                args.putString(DefineValue.SOURCE_PRODUCT_CODE, listbankSource.get(sourcePos).getProduct_code());
                args.putString(DefineValue.SOURCE_PRODUCT_TYPE, listbankSource.get(sourcePos).getProduct_type());
                args.putString(DefineValue.SOURCE_PRODUCT_NAME, listbankSource.get(sourcePos).getProduct_name());
                args.putString(DefineValue.SOURCE_PRODUCT_H2H, listbankSource.get(sourcePos).getProduct_h2h());
                args.putString(DefineValue.BENEF_PRODUCT_CODE, listbankBenef.get(benefPos).getProduct_code());
                args.putString(DefineValue.BENEF_PRODUCT_TYPE, listbankBenef.get(benefPos).getProduct_type());
                args.putString(DefineValue.BENEF_PRODUCT_NAME, listbankBenef.get(benefPos).getProduct_name());
                newFrag.setArguments(args);

                getFragmentManager().beginTransaction().replace(R.id.bbsTransaksiFragmentContent , newFrag, BBSTransaksiInformasi.TAG)
                        .addToBackStack(TAG).commit();
                ToggleKeyboard.hide_keyboard(act);
            }
        }
    };

    private void setSourceBenef(List<BBSBankModel> bankAgen, List<BBSBankModel> bankMember) {
        aListMember.clear();
        aListAgent.clear();

        for(int i=0;i<bankAgen.size();i++){
            HashMap<String, String> hm = new HashMap<>();
            hm.put("txt", bankAgen.get(i).getProduct_name());

            if(bankAgen.get(i).getProduct_name().toLowerCase().contains("mandiri"))
                hm.put("flag", Integer.toString(R.drawable.logo_mandiri_bank_small));
            else if(bankAgen.get(i).getProduct_name().toLowerCase().contains("bri"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_bri_small));
            else if(bankAgen.get(i).getProduct_name().toLowerCase().contains("permata"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_permata_small));
            else if(bankAgen.get(i).getProduct_name().toLowerCase().contains("uob"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_uob_small));
            else if(bankAgen.get(i).getProduct_name().toLowerCase().contains("maspion"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_maspion_rev1_small));
            else if(bankAgen.get(i).getProduct_name().toLowerCase().contains("bii"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_bii_small));
            else if(bankAgen.get(i).getProduct_name().toLowerCase().contains("jatim"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_jatim_small));
            else if(bankAgen.get(i).getProduct_name().toLowerCase().contains("bca"))
                hm.put("flag", Integer.toString(R.drawable.logo_bca_bank_small));
            else if(bankAgen.get(i).getProduct_name().toLowerCase().contains("nobu"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_nobu));
            else
                hm.put("flag", Integer.toString(R.drawable.ic_square_gate_one));
            aListAgent.add(hm);
        }
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

        actv_rekening_agent.setAdapter(adapterAgent);
        actv_rekening_member.setAdapter(adapterMember);
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
        if(actv_rekening_agent.getText().toString().length()==0){
            actv_rekening_agent.requestFocus();
            actv_rekening_agent.setError(getString(R.string.rekening_agent_error_message));
            return false;
        }
        if(actv_rekening_member.getText().toString().length()==0){
            actv_rekening_member.requestFocus();
            actv_rekening_member.setError(getString(R.string.rekening_member_error_message));
            return false;
        }
        return true;
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.bbs_reg_acct, menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;

//            case R.id.action_reg_acct:
//                Fragment mFrag = new ListAccountBBS();
//                switchFragment(mFrag, ListAccountBBS.TAG, true);
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchFragment(Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        BBSActivity fca = (BBSActivity ) getActivity();
        fca.switchContent(i,name,isBackstack);
    }
}
