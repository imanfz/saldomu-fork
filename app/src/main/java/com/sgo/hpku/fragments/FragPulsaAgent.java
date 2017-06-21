package com.sgo.hpku.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.R;
import com.sgo.hpku.activities.MainPage;
import com.sgo.hpku.activities.PulsaAgentActivity;
import com.sgo.hpku.coreclass.CustomSecurePref;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.MyApiClient;
import com.sgo.hpku.coreclass.PrefixOperatorValidator;
import com.sgo.hpku.coreclass.WebParams;
import com.sgo.hpku.dialogs.AlertDialogLogout;
import com.sgo.hpku.dialogs.DefinedDialog;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by thinkpad on 9/11/2015.
 */
public class FragPulsaAgent extends Fragment{
    private View v;
    private View layout_denom;
    private View layout_member;
    private View layout_remark;
    private View layout_nominal;
    private Activity act;
    private SecurePreferences sp;
    private String member_dap;
    private String userID;
    private String accessKey;

    private TextView tv_denom;
    private TextView tv_payment_remark;
    private EditText et_payment_remark;
    private Spinner spin_denom;
    private Spinner spin_member;
    private Spinner spin_nominal;
    private Button btn_submit;
    private ImageView spinWheelDenom;
    private ImageView spinWheelMember;
    private ImageView spinWheelNominal;
    private ProgressDialog out;
    private Animation frameAnimation;
    private Spinner sp_privacy;

    private String member_id;
    private String item_id;
    private String item_name;
    private String final_payment_remark;
    private String member_code;
    private String catalog_id = "";
    private String operator_id;
    private String operator_name;
    private String[] _denomData;
    private String[] _memberData;
    private String[] _catalogData;
    private String[] _nominalData;
    private HashMap<String,String> mDenomData, mMemberData, mNominalData;
    private int privacy;
    private boolean flagDenom = true;
    private ArrayAdapter<String> adapterDenom;
    private ArrayAdapter<String> adapterNominal;
    private Boolean firstTimeSpinner = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        flagDenom = true;
//        setDefault();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);



        act = getActivity();
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        member_dap = sp.getString(DefineValue.MEMBER_DAP,"");
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        layout_member = v.findViewById(R.id.pulsainput_layout_member);
        layout_denom = v.findViewById(R.id.pulsainput_layout_denom);
        layout_remark = v.findViewById(R.id.pulsainput_layout_remark);
        layout_nominal = v.findViewById(R.id.pulsainput_layout_nominal);
        spin_member = (Spinner) v.findViewById(R.id.spinner_pulsainput_member);
        spin_denom = (Spinner) v.findViewById(R.id.spinner_pulsainput_denom);
        spin_nominal = (Spinner) v.findViewById(R.id.spinner_pulsainput_nominal);
        tv_denom = (TextView) v.findViewById(R.id.pulsainput_text_denom);
        tv_payment_remark = (TextView) v.findViewById(R.id.pulsainput_text_payment_remark);
        et_payment_remark = (EditText) v.findViewById(R.id.payment_remark_pulsainput_value);
        spinWheelDenom = (ImageView) v.findViewById(R.id.spinning_wheel_pulsainput_denom);
        spinWheelMember = (ImageView) v.findViewById(R.id.spinning_wheel_pulsainput_member);
        spinWheelNominal = (ImageView) v.findViewById(R.id.spinning_wheel_pulsainput_nominal);
        btn_submit = (Button) v.findViewById(R.id.btn_submit_pulsainput);
        sp_privacy = (Spinner) v.findViewById(R.id.privacy_spinner);

        Bundle bundle = getArguments();
        if(bundle!=null)
        {
            String no_hp;
            no_hp=bundle.getString(DefineValue.PHONE_NUMBER);
            et_payment_remark.setText(no_hp);
        }

        frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
        frameAnimation.setRepeatCount(Animation.INFINITE);

        initializeLayout();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_pulsa_agent, container, false);
        return v;
    }

    private void initializeLayout() {
        btn_submit.setOnClickListener(submitInputListener);

        try {
            if (!member_dap.equals("") && member_dap!=null && !member_dap.equals("null")) {
                JSONArray array_member_dap = new JSONArray(member_dap);

                _memberData = new String[array_member_dap.length()];
                final ArrayAdapter<String> adapterMember = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, _memberData);
                adapterMember.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spin_member.setAdapter(adapterMember);
                spin_member.setOnItemSelectedListener(spinnerMemberListener);

                spin_member.setVisibility(View.GONE);
                spinWheelMember.setVisibility(View.VISIBLE);
                spinWheelMember.startAnimation(frameAnimation);

                final JSONArray finalMArray = array_member_dap;
                if(finalMArray.length() == 1) {
                    layout_member.setVisibility(View.GONE);
                    member_id = finalMArray.getJSONObject(0).getString(WebParams.MEMBER_ID);
                    member_code = finalMArray.getJSONObject(0).getString(WebParams.MEMBER_CODE);
                    getDenomDAP();
                }
                else {
                    layout_member.setVisibility(View.VISIBLE);
                    mMemberData = new HashMap<>();
                    Thread deproses = new Thread() {
                        @Override
                        public void run() {
                            try {
                                for (int i = 0; i < finalMArray.length(); i++) {
                                    _memberData[i] = finalMArray.getJSONObject(i).getString(WebParams.MEMBER_CODE);
                                    mMemberData.put(finalMArray.getJSONObject(i).getString(WebParams.MEMBER_CODE),
                                            finalMArray.getJSONObject(i).getString(WebParams.MEMBER_ID));

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    spinWheelMember.clearAnimation();
                                    spinWheelMember.setVisibility(View.GONE);
                                    spin_member.setVisibility(View.VISIBLE);
                                    adapterMember.notifyDataSetChanged();
                                }
                            });
                        }
                    };
                    deproses.run();
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(act);
                builder.setTitle("Alert").setMessage("Member DAP is Empty")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initializeDenom(String denom_data){
        if(!denom_data.equals("") && denom_data!=null && !denom_data.equals("null")){
            layout_denom.setVisibility(View.VISIBLE);
            layout_remark.setVisibility(View.VISIBLE);
            btn_submit.setVisibility(View.VISIBLE);
            JSONArray mArray;
            try {
                mArray = new JSONArray(denom_data);
                _denomData = new String[mArray.length()+1];
                _catalogData = new String[mArray.length()];
                adapterDenom = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, _denomData);
                adapterDenom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spin_denom.setAdapter(adapterDenom);
                spin_denom.setOnItemSelectedListener(spinnerDenomListener);

                spin_denom.setVisibility(View.GONE);
                spinWheelDenom.setVisibility(View.VISIBLE);
                spinWheelDenom.startAnimation(frameAnimation);

                final JSONArray finalMArray = mArray;
                mDenomData = new HashMap<>();
                Thread deproses = new Thread(){
                    @Override
                    public void run() {
                        try {
                            _denomData[0] = getString(R.string.pulsaagent_spinner_choose_text);
                            for (int i = 0 ;i< finalMArray.length();i++){
//                                Timber.d("Json array isi", finalMArray.getJSONObject(i).getString(WebParams.DENOM_ITEM_NAME) + "/" + finalMArray.getJSONObject(i).getString(WebParams.DENOM_ITEM_ID));
                                _denomData[i+1] = finalMArray.getJSONObject(i).getString(WebParams.DENOM_ITEM_NAME);
                                mDenomData.put(finalMArray.getJSONObject(i).getString(WebParams.DENOM_ITEM_NAME),
                                        finalMArray.getJSONObject(i).getString(WebParams.DENOM_ITEM_ID));
                                _catalogData[i] = finalMArray.getJSONObject(i).getString(WebParams.CATALOG_ID);

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
            layout_remark.setVisibility(View.GONE);
            btn_submit.setVisibility(View.GONE);

        }

        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.privacy_list, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_privacy.setAdapter(spinAdapter);
        sp_privacy.setOnItemSelectedListener(spinnerPrivacy);

        out.dismiss();

        String noHp = et_payment_remark.getText().toString();
        if(!noHp.isEmpty()) {
            PrefixOperatorValidator.OperatorModel operatorModel =
                    PrefixOperatorValidator.validation(getActivity(), noHp);

            if(operatorModel != null) {
                for (int k = 0; k < _denomData.length; k++) {
                    if (operatorModel.prefix_name.equalsIgnoreCase(_denomData[k])){
                        spin_denom.setSelection(k);
                    }
                }

            }
        }
    }


    private void initializeNominal(String denom_data){
        if(!denom_data.equals("") && denom_data!=null && !denom_data.equals("null")){
            layout_denom.setVisibility(View.VISIBLE);
            layout_remark.setVisibility(View.VISIBLE);
            layout_nominal.setVisibility(View.VISIBLE);
            btn_submit.setVisibility(View.VISIBLE);
            JSONArray mArray;
            try {
                mArray = new JSONArray(denom_data);
                _nominalData = new String[mArray.length()];
                adapterNominal = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, _nominalData);
                adapterNominal.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spin_nominal.setAdapter(adapterNominal);
                spin_nominal.setOnItemSelectedListener(spinnerNominalListener);

                spin_nominal.setVisibility(View.GONE);
                spinWheelNominal.setVisibility(View.VISIBLE);
                spinWheelNominal.startAnimation(frameAnimation);

                final JSONArray finalMArray = mArray;
                mNominalData = new HashMap<>();
                Thread deproses = new Thread(){
                    @Override
                    public void run() {
                        try {
                            for (int i = 0 ;i< finalMArray.length();i++){
//                                Timber.d("Json array isi", finalMArray.getJSONObject(i).getString(WebParams.DENOM_ITEM_NAME) + "/" + finalMArray.getJSONObject(i).getString(WebParams.DENOM_ITEM_ID));
                                _nominalData[i] = finalMArray.getJSONObject(i).getString(WebParams.DENOM_ITEM_NAME);
                                mNominalData.put(finalMArray.getJSONObject(i).getString(WebParams.DENOM_ITEM_NAME),
                                        finalMArray.getJSONObject(i).getString(WebParams.DENOM_ITEM_ID));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                spinWheelNominal.clearAnimation();
                                spinWheelNominal.setVisibility(View.GONE);
                                spin_nominal.setVisibility(View.VISIBLE);
                                adapterNominal.notifyDataSetChanged();
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
            layout_nominal.setVisibility(View.GONE);
            layout_remark.setVisibility(View.GONE);
            btn_submit.setVisibility(View.GONE);
        }

        out.dismiss();
    }

    private void getDenomDAP(){
        try{
            out = DefinedDialog.CreateProgressDialog(getActivity(), null);

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_DENOM_DAP,
                    userID,accessKey);
            params.put(WebParams.MEMBER_ID, member_id);
            params.put(WebParams.CATALOG_ID, catalog_id);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);

            Timber.d("isi params sent Denom DAP"+ params.toString());

            MyApiClient.getDenomDAP(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("Isi response Denom DAP"+ response.toString());
                            String denom_data = response.getString(WebParams.DENOM_DATA);
                            if(catalog_id.equals("")) initializeDenom(denom_data);
                            else initializeNominal(denom_data);
                        } else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout"+ response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginMain(getActivity(),message);
                        }else {
                            out.dismiss();
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
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
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    if(out.isShowing())
                        out.dismiss();
                    Timber.w("Error Koneksi getDenomDAP desc:" + throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient"+ e.getMessage());
        }
    }

    private Spinner.OnItemSelectedListener spinnerPrivacy = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            privacy = i+1;
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private Spinner.OnItemSelectedListener spinnerMemberListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Object item = adapterView.getItemAtPosition(i);
            member_id = mMemberData.get(item.toString());
            member_code = _memberData[i];
            if(flagDenom) getDenomDAP();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private Spinner.OnItemSelectedListener spinnerDenomListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if(firstTimeSpinner) {
                Object item = adapterView.getItemAtPosition(i);
                if (i > 0) {
                    operator_id = mDenomData.get(item.toString());
                    operator_name = _denomData[i];
                    catalog_id = _catalogData[i - 1];
                    if (flagDenom) getDenomDAP();
                }
                else {
                    layout_nominal.setVisibility(View.GONE);
                    btn_submit.setEnabled(false);
                }
            }
            else {
                firstTimeSpinner = true;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private Spinner.OnItemSelectedListener spinnerNominalListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Object item = adapterView.getItemAtPosition(i);
            item_id = mNominalData.get(item.toString());
            item_name = _nominalData[i];
            btn_submit.setEnabled(true);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private Button.OnClickListener submitInputListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(inputValidation()){
                final_payment_remark = String.valueOf(et_payment_remark.getText());
                showDialog(final_payment_remark, item_id, item_name, operator_id, operator_name);
            }
        }
    };

    private boolean inputValidation(){
        if(et_payment_remark.getText().toString().length()==0){
            et_payment_remark.requestFocus();
            et_payment_remark.setError(this.getString(R.string.regist1_validation_nohp));
            return false;
        }
        return true;
    }

    private void showDialog(String _payment_remark, String _item_id, String _item_name, String _operator_id, String _operator_name) {
        Intent i = new Intent(act, PulsaAgentActivity.class);
        i.putExtra(DefineValue.MEMBER_ID, member_id);
        i.putExtra(DefineValue.DENOM_ITEM_ID, _item_id);
        i.putExtra(DefineValue.DENOM_ITEM_NAME, _item_name);
        i.putExtra(DefineValue.PHONE_NUMBER, _payment_remark);
        i.putExtra(DefineValue.SHARE_TYPE, String.valueOf(privacy));
        i.putExtra(DefineValue.OPERATOR_ID, _operator_id);
        i.putExtra(DefineValue.OPERATOR_NAME, _operator_name);
        switchActivity(i);
    }

    private void setDefault(){
        catalog_id = "";
        if(layout_member.getVisibility() == View.VISIBLE) {
            spin_member.setSelection(0);
        }
        spin_denom.setSelection(0);
        et_payment_remark.setText("");
    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }
}
