package com.sgo.orimakardaya.fragments;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.*;
import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.activities.PayFriendsConfirmTokenActivity;
import com.sgo.orimakardaya.activities.TopUpActivity;
import com.sgo.orimakardaya.coreclass.*;
import com.sgo.orimakardaya.dialogs.AlertDialogFrag;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import com.sgo.orimakardaya.dialogs.InformationDialog;
import com.squareup.picasso.Picasso;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/*
  Created by thinkpad on 3/11/2015.
 */
public class FragPayFriends extends Fragment implements InformationDialog.OnDialogOkCallback {

    private boolean isNotification = false;
    private InformationDialog dialogI;
    ImageView imgProfile, imgRecipients;
    TextView txtName,txtNumberRecipients;
    Spinner sp_privacy;
    RecipientEditTextView phoneRetv;
    Button btnGetOTP;
    EditText etAmount, etMessage;
    String _memberId,userID,accessKey;
    List<String> listName;

    int privacy,max_member_trans;

    SecurePreferences sp;
    Bundle bundle;
    DrawableRecipientChip[] chips;

    ProgressDialog progdialog;

    View v;

    String authType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_payfriends, container, false);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.information, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_information:
                if(!dialogI.isAdded())
                    dialogI.show(getActivity().getSupportFragmentManager(), InformationDialog.TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");
        max_member_trans = sp.getInt(DefineValue.MAX_MEMBER_TRANS, 5);
        authType = sp.getString(DefineValue.AUTHENTICATION_TYPE, "");

        dialogI = InformationDialog.newInstance(this,5);

        imgProfile = (ImageView) v.findViewById(R.id.img_profile);
        imgRecipients = (ImageView) v.findViewById(R.id.img_recipients);
        txtName = (TextView) v.findViewById(R.id.txtName);
        phoneRetv = (RecipientEditTextView) v.findViewById(R.id.phone_retv);
        etAmount = (EditText) v.findViewById(R.id.payfriends_value_amount);
        etAmount.addTextChangedListener(jumlahChangeListener);
        etMessage = (EditText) v.findViewById(R.id.payfriends_value_message);
        txtNumberRecipients = (TextView) v.findViewById(R.id.payfriends_value_number_recipients);
        btnGetOTP = (Button) v.findViewById(R.id.btn_get_otp);

        if(authType.equalsIgnoreCase("PIN")) {
            btnGetOTP.setText(R.string.next);
        }
        else if(authType.equalsIgnoreCase("OTP")) {

            btnGetOTP.setText(R.string.submit);
        }

        sp_privacy = (Spinner) v.findViewById(R.id.payfriend_privacy_spinner);

        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.privacy_list, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_privacy.setAdapter(spinAdapter);
        sp_privacy.setOnItemSelectedListener(spinnerPrivacy);


        Bitmap bmRecipients = BitmapFactory.decodeResource(getResources(), R.drawable.grey_background);
        RoundImageTransformation roundedImageRecipients = new RoundImageTransformation(bmRecipients);
        imgRecipients.setImageDrawable(roundedImageRecipients);

        _memberId = sp.getString(DefineValue.MEMBER_ID,"");
        setImageProfPic();

        txtName.setText(sp.getString(DefineValue.USER_NAME, ""));

        phoneRetv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        final BaseRecipientAdapter adapter = new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, getActivity().getApplicationContext());
        phoneRetv.setAdapter(adapter);
        phoneRetv.dismissDropDownOnItemSelected(true);
        phoneRetv.setThreshold(1);

        btnGetOTP.setOnClickListener(btnGetOTPListener);

        etAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    setNumberRecipients();
                }
            }
        });

        etMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    setNumberRecipients();
                }
            }
        });


        phoneRetv.addTextChangedListener(new TextWatcher() {
            boolean mToggle = false;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Timber.d("before Text Change:"+s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Timber.d("on Text Change:"+s.toString());
                if (phoneRetv.hasFocus()) {
                    if (phoneRetv.getSortedRecipients().length == 0) {
                        txtNumberRecipients.setTextColor(getResources().getColor(R.color.colorSecondaryDark));
                    } else {
                        txtNumberRecipients.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    }
                    txtNumberRecipients.setText(String.valueOf(phoneRetv.getSortedRecipients().length));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                Timber.d("after Text Change:"+s.toString());

            }
        });

        bundle = this.getArguments();
        if(bundle != null) {
            final String name,phone;

            if(bundle.containsKey(DefineValue.AMOUNT) && !bundle.getString(DefineValue.AMOUNT,null).isEmpty()){
                name = bundle.getString(DefineValue.CUST_NAME);
                phone = bundle.getString(DefineValue.USERID_PHONE);
                String amount = bundle.getString(DefineValue.AMOUNT);
                etAmount.setText(amount);
                etAmount.setEnabled(false);

                phoneRetv.setEnabled(false);
                isNotification = true;
            }
            else {
                name = bundle.getString("name");
                phone = bundle.getString("phone");

            }

            phoneRetv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    phoneRetv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    phoneRetv.submitItem(name, phone);
                }

            });
            txtNumberRecipients.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            txtNumberRecipients.setText(String.valueOf(phoneRetv.getSortedRecipients().length));
        }
    }

    public void setNumberRecipients(){

        if (phoneRetv.getSortedRecipients().length == 0) {
            txtNumberRecipients.setTextColor(getResources().getColor(R.color.colorSecondaryDark));
        } else {
            txtNumberRecipients.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        if(phoneRetv.length() == 0)
            txtNumberRecipients.setText(String.valueOf(phoneRetv.getSortedRecipients().length));
        else
            txtNumberRecipients.setText(String.valueOf(phoneRetv.getRecipients().length));

        Timber.d("isi length recipients:"+String.valueOf(phoneRetv.getRecipients().length));
    }


    TextWatcher jumlahChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.toString().equals("0"))etAmount.setText("");
            if(s.length() > 0 && s.charAt(0) == '0'){
                int i = 0;
                for (; i < s.length(); i++){
                    if(s.charAt(i) != '0')break;
                }
                etAmount.setText(s.toString().substring(i));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    Spinner.OnItemSelectedListener spinnerPrivacy = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            privacy = i+1;
            if(phoneRetv.hasFocus()) {
                phoneRetv.clearFocus();

            }
            setNumberRecipients();

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

            if(phoneRetv.hasFocus()) {
                phoneRetv.clearFocus();
            }
            setNumberRecipients();
        }
    };

    @Override
    public void onOkButton() {

    }

    private class TempObjectData{

        private String member_code_to;
        private String ccy_id;
        private String amount;
        private String name;

        public TempObjectData(String _member_code_to, String _ccy_id, String _amount, String _name){
            this.setMember_code_to(_member_code_to);
            this.setCcy_id(_ccy_id);
            this.setAmount(_amount);
            this.setName(_name);
        }

        public String getMember_code_to() {
            return member_code_to;
        }

        public void setMember_code_to(String member_code_to) {
            this.member_code_to = member_code_to;
        }

        public String getCcy_id() {
            return ccy_id;
        }

        public void setCcy_id(String ccy_id) {
            this.ccy_id = ccy_id;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    Button.OnClickListener btnGetOTPListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(InetHandler.isNetworkAvailable(getActivity())) {
                if (inputValidation()) {

                    Timber.d("isi length recipients button:" + String.valueOf(phoneRetv.getRecipients().length));
                    Timber.d("isi length sort recipients button:" + String.valueOf(phoneRetv.getSortedRecipients().length));
                    phoneRetv.requestFocus();
                    String amount = etAmount.getText().toString();
                    String message = etMessage.getText().toString();
                    Boolean recipientValidation = true;
                    ArrayList<TempObjectData> mTempObjectDataList = new ArrayList<TempObjectData>();

                    String finalNumber, finalName;

                    String check = phoneRetv.getText().toString();
                    if ((!check.isEmpty()) && check.substring(check.length() - 1).equals(","))
                        phoneRetv.setText(check.substring(0, check.length() - 1));

                    chips = new DrawableRecipientChip[phoneRetv.getSortedRecipients().length];
                    chips = phoneRetv.getSortedRecipients();
                    listName = new ArrayList<String>();
                    phoneRetv.clearFocus();
                    if (chips.length <= max_member_trans) {
                        for (DrawableRecipientChip chip : chips) {
                            Timber.v("DrawableChip:" + chip.getEntry().getDisplayName() + " " + chip.getEntry().getDestination());
                            finalName = chip.getEntry().getDisplayName();
                            finalNumber = chip.getEntry().getDestination();
                            if (isAlpha(finalNumber) || finalNumber.length() < getResources().getInteger(R.integer.lenght_phone_number)) {
                                recipientValidation = false;
                                break;
                            }

                            finalNumber = NoHPFormat.editNoHP(chip.getEntry().getDestination());
                            listName.add(chip.getEntry().getDisplayName());
                            mTempObjectDataList.add(new TempObjectData(finalNumber, DefineValue.IDR, amount, finalName));
                        }

                        if (recipientValidation) {
                            final GsonBuilder gsonBuilder = new GsonBuilder();
                            gsonBuilder.setPrettyPrinting();
                            final Gson gson = gsonBuilder.create();
                            String testJson = gson.toJson(mTempObjectDataList);
                            String nameJson = gson.toJson(listName);
                            //  Timber.v("isi json build", testJson + numberJson);
                            sentData(message, testJson, nameJson);
                        } else {
                            phoneRetv.requestFocus();
                            phoneRetv.setError(getString(R.string.payfriends_recipients_alpha_validation));
                        }
                    } else {
                        phoneRetv.requestFocus();
                        phoneRetv.setError(getString(R.string.payfriends_recipients_max_validation1) + " " + max_member_trans + " " + getString(R.string.payfriends_recipients_max_validation2));
                    }
                }
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    public boolean isAlpha(String name) {
        Pattern p = Pattern.compile("[a-zA-Z]");
        Matcher m = p.matcher(name);
        return m.find();
    }

    public void sentData(String _message, String _data, final String _nameJson){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();


            RequestParams params;
            if(isNotification) {
                params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_REQ_TOKEN_P2P_NOTIF,
                        userID,accessKey);
            }
            else
                params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_REQ_TOKEN_P2P,
                        userID,accessKey);


            params.put(WebParams.MEMBER_ID, _memberId);
            params.put(WebParams.MEMBER_REMARK, _message);
            params.put(WebParams.DATA, _data);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.PRIVACY, privacy);
            if(isNotification){
                params.put(WebParams.REQUEST_ID, bundle.getString(DefineValue.REQUEST_ID));
                params.put(WebParams.TRX_ID, bundle.getString(DefineValue.TRX));
            }

            Timber.d("isi params sent req token p2p:"+params.toString());

            JsonHttpResponseHandler myHandler = new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progdialog.dismiss();

                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi response req token p2p:"+response.toString());
                            JSONArray mArrayData = new JSONArray(response.getString(WebParams.DATA_TRANSFER));
                            int isFailed=0 ;
                            String msg = "";
                            for(int i = 0 ; i < mArrayData.length() ; i++) {
                                if(mArrayData.getJSONObject(i).getString(WebParams.MEMBER_STATUS).equals(DefineValue.FAILED)){
                                    isFailed++ ;
                                    msg = mArrayData.getJSONObject(i).getString(WebParams.MEMBER_REMARK);
                                }
                            }
                            if(isFailed != mArrayData.length()){
                                String dataTransfer = response.getString(WebParams.DATA_TRANSFER);
                                showDialog(dataTransfer, _nameJson, response.getString(WebParams.MESSAGE), response.getString(WebParams.DATA_MAPPER));
                            }
                            else Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginMain(getActivity(),message);
                        }
                        else if(code.equals(ErrorDefinition.WRONG_PIN_P2P)){
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            showDialogError(code);
                        }
                        else {
                            Timber.d("isi error req token p2p:"+response.toString());
                            String code_msg = response.getString(WebParams.ERROR_MESSAGE);
                            if(code.equals(ErrorDefinition.ERROR_CODE_DUPLICATED_RECIPIENT)){
                                phoneRetv.requestFocus();
                                phoneRetv.setError(getString(R.string.payfriends_recipients_duplicate_validation));
                            }
                            else if(code.equals(ErrorDefinition.ERROR_CODE_LESS_BALANCE)){

                                String message_dialog = "\""+code_msg+"\" \n"+getString(R.string.dialog_message_less_balance);

                                AlertDialogFrag dialog_frag = AlertDialogFrag.newInstance(getString(R.string.dialog_title_less_balance),
                                        message_dialog,getString(R.string.ok),getString(R.string.cancel),false);
                                dialog_frag.setOkListener(new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent mI = new Intent(getActivity(),TopUpActivity.class);
                                        mI.putExtra(DefineValue.IS_ACTIVITY_FULL,true);
                                        getActivity().startActivityForResult(mI,MainPage.ACTIVITY_RESULT);
                                    }
                                });
                                dialog_frag.setTargetFragment(FragPayFriends.this, 0);
                                dialog_frag.show(getActivity().getSupportFragmentManager(), AlertDialogFrag.TAG);
                            }
                            else {
                                Toast.makeText(getActivity(), code_msg, Toast.LENGTH_LONG).show();
                            }

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
                    if (progdialog.isShowing())
                        progdialog.dismiss();

                    Timber.w("Error Koneksi req token p2p:"+throwable.toString());
                }
            };

            if(isNotification) {
                Timber.d("masuk ke reqTokenP2P notif");
                MyApiClient.sentReqTokenP2PNotif(getActivity(),params, myHandler);
            }
            else
                MyApiClient.sentReqTokenP2P(getActivity(),params, myHandler );
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void showDialogError(String message){
        Dialog mdialog = DefinedDialog.MessageDialog(getActivity(), getString(R.string.blocked_pin_title), message,
                new DefinedDialog.DialogButtonListener() {
                    @Override
                    public void onClickButton(View v, boolean isLongClick) {

                    }
                });
        mdialog.show();
    }

    void showDialog(final String _data_transfer, final String _nameJson, final String _message, final String _data_mapper) {
        phoneRetv.setText(null);
        if(authType.equalsIgnoreCase("OTP")) {
            // Create custom dialog object
            final Dialog dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(false);
            // Include dialog.xml file
            dialog.setContentView(R.layout.dialog_notification);

            // set values for custom dialog components - text, image and button
            Button btnDialogOTP = (Button) dialog.findViewById(R.id.btn_dialog_notification_ok);
            TextView Title = (TextView) dialog.findViewById(R.id.title_dialog);
            TextView Message = (TextView) dialog.findViewById(R.id.message_dialog);

            Message.setVisibility(View.VISIBLE);
            Title.setText(getString(R.string.payfriends_dialog_validation_title));
            Message.setText(getString(R.string.appname)+" "+getString(R.string.dialog_token_message_sms));

            //clear data in edit text


            btnDialogOTP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    Intent i = new Intent(getActivity(), PayFriendsConfirmTokenActivity.class);
                    i.putExtra(WebParams.DATA_TRANSFER, _data_transfer);
                    i.putExtra(WebParams.DATA, _nameJson);
                    i.putExtra(WebParams.MESSAGE, _message);
                    i.putExtra(DefineValue.TRANSACTION_TYPE, isNotification);
                    i.putExtra(WebParams.DATA_MAPPER, _data_mapper);

                    switchActivity(i);


                }
            });

            dialog.show();
        }
        else if(authType.equalsIgnoreCase("PIN")) {
            //clear data in edit text

            Intent i = new Intent(getActivity(), PayFriendsConfirmTokenActivity.class);
            i.putExtra(WebParams.DATA_TRANSFER, _data_transfer);
            i.putExtra(WebParams.DATA, _nameJson);
            i.putExtra(WebParams.MESSAGE, _message);
            i.putExtra(DefineValue.TRANSACTION_TYPE, isNotification);
            i.putExtra(WebParams.DATA_MAPPER, _data_mapper);

            switchActivity(i);
        }

        phoneRetv.setEnabled(true);
        phoneRetv.clearListSelection();
        phoneRetv.clearComposingText();
        phoneRetv.setText("");
        phoneRetv.removeMoreChip();
        etAmount.setText("");
        etMessage.setText("");
        sp_privacy.setSelection(0);
        phoneRetv.requestFocus();
        phoneRetv.clearFocus();
        etAmount.setEnabled(true);
        //txtNumberRecipients.setText(getString(R.string.Zero));
        setNumberRecipients();
        txtNumberRecipients.setText(String.valueOf(phoneRetv.getSortedRecipients().length));
    }


    public boolean inputValidation(){
        if(phoneRetv.getText().toString().length()==0){
            phoneRetv.requestFocus();
            phoneRetv.setError(getString(R.string.payfriends_recipients_validation));
            return false;
        }
        if(phoneRetv.isFocused()){
            phoneRetv.clearFocus();
        }
        if(phoneRetv.getText().toString().charAt(0) == ' '){
            phoneRetv.requestFocus();
            phoneRetv.setError(getString(R.string.payfriends_recipients_validation));
            return false;
        }
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
        return true;
    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        /*MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent,MainPage.ACTIVITY_RESULT);*/
        getActivity().startActivityForResult(mIntent,MainPage.REQUEST_FINISH);
    }

    public void setImageProfPic(){
        float density = getResources().getDisplayMetrics().density;
        String _url_profpic;

        if(density <= 1) _url_profpic = sp.getString(DefineValue.IMG_SMALL_URL, null);
        else if(density < 2) _url_profpic = sp.getString(DefineValue.IMG_MEDIUM_URL, null);
        else _url_profpic = sp.getString(DefineValue.IMG_LARGE_URL, null);

        Timber.wtf("url prof pic:"+_url_profpic);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.user_unknown_menu);
        RoundImageTransformation roundedImage = new RoundImageTransformation(bm);

        Picasso mPic;
        if(MyApiClient.PROD_FLAG_ADDRESS)
            mPic = MyPicasso.getImageLoader(getActivity());
        else
            mPic= Picasso.with(getActivity());

        if(_url_profpic != null && _url_profpic.isEmpty()){
            mPic.load(R.drawable.user_unknown_menu)
                .error(roundedImage)
                .fit().centerInside()
                .placeholder(R.anim.progress_animation)
                .transform(new RoundImageTransformation())
                .into(imgProfile);
        }
        else {
            mPic.load(_url_profpic)
                .error(roundedImage)
                .fit().centerInside()
                .placeholder(R.anim.progress_animation)
                .transform(new RoundImageTransformation())
                .into(imgProfile);
        }
    }

}
