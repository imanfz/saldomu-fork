package com.sgo.orimakardaya.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.BillerActivity;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.NoHPFormat;
import com.sgo.orimakardaya.coreclass.WebParams;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;

/*
  Created by Administrator on 3/4/2015.
 */
public class BillerInput extends Fragment {

    public String[] billerType = {  "PLS",  //pulsa  0
                                    "TKN",  //voucher listrik  1
                                    "CC",   //Kartu Kredit   2
                                    "CCL",  //Cicilan  3
                                    "TLP",  //Telkom / telpon 4
                                    "PLN",  // PLN listrik  5
                                    "AIR",  //PAM  6
                                    "HP",   //handphone  7
                                    "PST",  //Tiket pesawat  8
                                    "KA",   //Tiket Kereta Api  9
                                    "TV",   //TV vable   10
                                    "ASU",  //Asuransi   11
                                    "INT",  //Internet   12
                                    "PBB",  //PBB   13
                                    "NON",  // PLN Nont-Taglis  14
                                    "SPP",  // SPP   15
                                    "RMH"}; //Perumahan  16

    View v, layout_denom;
    TextView tv_denom,tv_payment_remark;
    EditText et_payment_remark;
    Spinner spin_denom;
    Button btn_submit;
    ImageView spinWheelDenom;
    ProgressDialog progdialog;
    Animation frameAnimation;
    String denom_data,biller_type, biller_comm_id,biller_merchant_name, biller_comm_code, biller_api_key, item_id,
            final_payment_remark, buy_type,callback_url;
    int buy_code;
    private HashMap<String,String> mDenomData;
    String[] _denomData;
    Boolean isToken;
    Spinner sp_privacy;
    int privacy;
    String digitsListener ="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_biller_input, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        denom_data = args.getString(DefineValue.DENOM_DATA);
        biller_type = args.getString(DefineValue.BILLER_TYPE);
        biller_comm_id = args.getString(DefineValue.BILLER_COMM_ID);
        biller_comm_code = args.getString(DefineValue.BILLER_COMM_CODE);
        biller_api_key = args.getString(DefineValue.BILLER_API_KEY);
        callback_url = args.getString(DefineValue.CALLBACK_URL);
        biller_merchant_name = args.getString(DefineValue.BILLER_NAME);

        isToken = false;

        spin_denom = (Spinner) v.findViewById(R.id.spinner_billerinput_denom);
        tv_denom = (TextView) v.findViewById(R.id.billerinput_text_denom);
        tv_payment_remark = (TextView) v.findViewById(R.id.billerinput_text_payment_remark);
        et_payment_remark = (EditText) v.findViewById(R.id.payment_remark_billerinput_value);
        spinWheelDenom = (ImageView) v.findViewById(R.id.spinning_wheel_billerinput_denom);
        btn_submit = (Button) v.findViewById(R.id.btn_submit_billerinput);
        layout_denom = v.findViewById(R.id.billerinput_layout_denom);
        sp_privacy = (Spinner) v.findViewById(R.id.privacy_spinner);

        layout_denom.setVisibility(View.VISIBLE);

        frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
        frameAnimation.setRepeatCount(Animation.INFINITE);

        initializeLayout();
    }

    private void initializeLayout(){

        String[] _buy_type = getResources().getStringArray(R.array.buy_vpi_title);

        if(biller_type.equals(billerType[0])){
            buy_type = _buy_type[0];
            buy_code = BillerActivity.PURCHASE_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_Pulsa));
            et_payment_remark.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
            et_payment_remark.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        else if(biller_type.equals(billerType[1])){
            buy_type = _buy_type[0];
            buy_code = BillerActivity.PURCHASE_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_Listrik));
            et_payment_remark.setKeyListener(DigitsKeyListener.getInstance(digitsListener));
        }
        else if(biller_type.equals(billerType[2])){
            buy_type = _buy_type[1];
            buy_code = BillerActivity.PAYMENT_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_CC));
            et_payment_remark.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        else if(biller_type.equals(billerType[8])){
            buy_type = _buy_type[1];
            buy_code = BillerActivity.PAYMENT_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_PST));
            et_payment_remark.setInputType(InputType.TYPE_CLASS_TEXT);
            et_payment_remark.setKeyListener(DigitsKeyListener.getInstance(digitsListener));
        }
        else if(biller_type.equals(billerType[16])){
            buy_type = _buy_type[1];
            buy_code = BillerActivity.PAYMENT_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_RMH));
            et_payment_remark.setInputType(InputType.TYPE_CLASS_TEXT);
            et_payment_remark.setKeyListener(DigitsKeyListener.getInstance(digitsListener));
        }
        else {
            buy_type = _buy_type[1];
            buy_code = BillerActivity.PAYMENT_TYPE;
            et_payment_remark.setInputType(InputType.TYPE_CLASS_TEXT);
            et_payment_remark.setKeyListener(DigitsKeyListener.getInstance(digitsListener));
        }

        btn_submit.setOnClickListener(submitInputListener);

        if(denom_data != null){
            JSONArray mArray;
            try {
                mArray = new JSONArray(denom_data);
                _denomData = new String[mArray.length()];
                final ArrayAdapter<String> adapterDenom = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,_denomData);
                adapterDenom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spin_denom.setAdapter(adapterDenom);
                spin_denom.setOnItemSelectedListener(spinnerDenomListener);

                spin_denom.setVisibility(View.GONE);
                spinWheelDenom.setVisibility(View.VISIBLE);
                spinWheelDenom.startAnimation(frameAnimation);

                final JSONArray finalMArray = mArray;
                mDenomData = new HashMap<String, String>();
                Thread deproses = new Thread(){
                    @Override
                    public void run() {
                        try {
                            for (int i = 0 ;i< finalMArray.length();i++){
                                //Timber.d("Json array isi", finalMArray.getJSONObject(i).getString(WebParams.DENOM_ITEM_NAME)+"/"+finalMArray.getJSONObject(i).getString(WebParams.DENOM_ITEM_ID));
                                _denomData[i] = finalMArray.getJSONObject(i).getString(WebParams.DENOM_ITEM_NAME);
                                mDenomData.put(finalMArray.getJSONObject(i).getString(WebParams.DENOM_ITEM_NAME),
                                        finalMArray.getJSONObject(i).getString(WebParams.DENOM_ITEM_ID));

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                spinWheelDenom.clearAnimation();
                                spinWheelDenom.setVisibility(View.GONE);
                                spin_denom.setVisibility(View.VISIBLE);
                                adapterDenom.notifyDataSetChanged();
                            }
                        });
                    }
                };
                deproses.run();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            layout_denom.setVisibility(View.GONE);
            item_id = getArguments().getString(DefineValue.BILLER_ITEM_ID);
        }

        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.privacy_list, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_privacy.setAdapter(spinAdapter);
        sp_privacy.setOnItemSelectedListener(spinnerPrivacy);

    }


    Spinner.OnItemSelectedListener spinnerPrivacy = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            privacy = i+1;
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    Spinner.OnItemSelectedListener spinnerDenomListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Object item = adapterView.getItemAtPosition(i);
            item_id = mDenomData.get(item.toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    Button.OnClickListener submitInputListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(inputValidation()){
                if(biller_type.equals(billerType[0]))final_payment_remark = NoHPFormat.editNoHP(String.valueOf(et_payment_remark.getText()));
                else final_payment_remark = String.valueOf(et_payment_remark.getText());
                showDialog(biller_merchant_name,final_payment_remark,item_id);
                //showDialog("jaijdijaij","aksjflak","IDR","212138","asjfals","PST","N","Y","{\"ORDER ID\":\"1880103994376\",\"AIRLINE CODE\":\"09\",\"AIRLINE CODE2\":\"0002\",\"TOTAL FLIGHT\":\"3\",\"PASSENGER NAME\":\"YUUDDISTRIA IASDJIFAJD\",\"PNR CODE\":\"NENCLK\",\"NUMBER OF PASSENGERS\":\"01\",\"CARRIER\":\"JT\",\"CLASS\":\"N\",\"FROM\":\"CGK\",\"TO\":\"PLW\",\"FLIGHT NUMBER\":\"720\",\"DEPART DATE\":\"2404\",\"DEPART TIME\":\"0500\"}");
            }
        }
    };

    void showDialog(final String _biller_name, String _payment_remark,String _item_id) {


        Bundle mArgs = getArguments();
        mArgs.putString(DefineValue.BILLER_NAME,_biller_name);
        mArgs.putString(DefineValue.CUST_ID, _payment_remark);
        mArgs.putString(DefineValue.ITEM_ID, _item_id);
        mArgs.putInt(DefineValue.BUY_TYPE, buy_code);
        mArgs.putString(DefineValue.SHARE_TYPE, String.valueOf(privacy));

        Fragment mFrag = new BillerDesciption();
        mFrag.setArguments(mArgs);
        switchFragment(mFrag, BillerActivity.FRAG_BIL_INPUT, null, true);

    }

    private void switchFragment(android.support.v4.app.Fragment i, String name,String next_name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity ) getActivity();
        fca.switchContent(i,name,next_name,isBackstack);
        et_payment_remark.setText("");
        spin_denom.setSelection(0);
    }

    public boolean inputValidation(){
        if(et_payment_remark.getText().toString().length()==0){
            et_payment_remark.requestFocus();
            if(biller_type.equals(billerType[0]))et_payment_remark.setError(this.getString(R.string.regist1_validation_nohp));
            else et_payment_remark.setError(this.getString(R.string.billerinput_validation_payment_remark));
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(getFragmentManager().getBackStackEntryCount()>0)
                    getFragmentManager().popBackStack();
                else
                    getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

}