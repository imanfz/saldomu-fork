package com.sgo.saldomu.fragments;


import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Rfc822Tokenizer;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.PayFriendsConfirmTokenActivity;
import com.sgo.saldomu.activities.PaymentQRActivity;
import com.sgo.saldomu.activities.TopUpActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.EasyPermissionInit;
import com.sgo.saldomu.coreclass.ErrorDefinition;
import com.sgo.saldomu.coreclass.GlideManager;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.RoundImageTransformation;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogFrag;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.InformationDialog;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

import static com.sgo.saldomu.activities.MainPage.RESULT_NOTIF;
import static com.sgo.saldomu.activities.PaymentQRActivity.RESULT_PAYFRIEND_QR;

/*
  Created by thinkpad on 3/11/2015.
 */
public class FragPayFriends extends Fragment implements InformationDialog.OnDialogOkCallback, EasyPermissions.PermissionCallbacks  {

    private boolean isNotification = false;
    private InformationDialog dialogI;
    ImageView imgProfile, imgRecipients;
    TextView txtName,txtNumberRecipients, txtNameRecipient;
    Spinner sp_privacy;
    RecipientEditTextView phoneRetv;
    Button btnGetOTP;
    EditText etAmount, etMessage;
    String _memberId,userID,accessKey;
    View layoutDesc;
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

        dialogI = InformationDialog.newInstance(this, 5);

        imgProfile = (ImageView) v.findViewById(R.id.img_profile);
        imgRecipients = (ImageView) v.findViewById(R.id.img_recipients);
        txtName = (TextView) v.findViewById(R.id.txtName);
        phoneRetv = (RecipientEditTextView) v.findViewById(R.id.phone_retv);

        phoneRetv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                final int DRAWABLE_LEFT = 0;
//                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
//                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (phoneRetv.getRight() - phoneRetv.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        chooseCamera();
                    }
                }
                return false;
            }
        });

        etAmount = (EditText) v.findViewById(R.id.payfriends_value_amount);
        etAmount.addTextChangedListener(jumlahChangeListener);
        etMessage = (EditText) v.findViewById(R.id.payfriends_value_message);
        txtNumberRecipients = (TextView) v.findViewById(R.id.payfriends_value_number_recipients);
        txtNameRecipient = (TextView) v.findViewById(R.id.txtNumberRecipients);
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

        String[] perms = {Manifest.permission.READ_CONTACTS};
        if (EasyPermissionInit.sharedInstance().initEasyPermission(this, perms, DefineValue.PERMISSIONS_READ_CONTACTS
                , getString(R.string.cancel_permission_read_contacts)))
            setBaseRecipAdapter();

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
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Timber.d("before Text Change:"+s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Timber.d("on Text Change:"+s.toString());
                boolean retval = s.toString().contains("@");

                if(retval)
                {
                    phoneRetv.setTokenizer(new Rfc822Tokenizer());
                    final BaseRecipientAdapter adapter =new BaseRecipientAdapter(getActivity().getApplicationContext());
                    phoneRetv.setAdapter(adapter);
                    phoneRetv.dismissDropDownOnItemSelected(true);
                    phoneRetv.setThreshold(1);
                }
                else
                {
                    phoneRetv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                    final BaseRecipientAdapter adapter = new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, getActivity().getApplicationContext());
                    phoneRetv.setAdapter(adapter);
                    phoneRetv.dismissDropDownOnItemSelected(true);
                    phoneRetv.setThreshold(1);
                }

                if (phoneRetv.hasFocus()) {
                    if (phoneRetv.getSortedRecipients().length == 0) {
                        txtNumberRecipients.setTextColor(getResources().getColor(R.color.colorSecondaryDark));
                    } else {
                        txtNumberRecipients.setTextColor(getResources().getColor(R.color.grey_1000b));
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
            String name = bundle.getString(DefineValue.CUST_NAME,"");
            String phone = bundle.getString(DefineValue.USERID_PHONE,"");
            String amount = bundle.getString(DefineValue.AMOUNT,"");
            String msg = bundle.getString(DefineValue.MESSAGE,"");
            if(bundle.getInt(DefineValue.TYPE,0) == RESULT_NOTIF){
                phoneRetv.setEnabled(false);
                isNotification = true;
            }
            setDataToUI(name,phone,amount,msg);

        }
    }

    private void setBaseRecipAdapter(){
        BaseRecipientAdapter adapter = new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, getActivity().getApplicationContext());
        phoneRetv.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (requestCode ==  DefineValue.PERMISSIONS_REQ_WRITEEXTERNALSTORAGE ||
                requestCode == DefineValue.PERMISSIONS_REQ_CAMERA) {
            chooseCamera();
        }
        else if(requestCode == DefineValue.PERMISSIONS_READ_CONTACTS){
            BaseRecipientAdapter adapter = new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, getActivity().getApplicationContext());
            phoneRetv.setAdapter(adapter);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Toast.makeText(getActivity(), getString(R.string.cancel_permission_read_contacts), Toast.LENGTH_SHORT).show();
    }

    private void chooseCamera() {
        String[] perms = {Manifest.permission.CAMERA};

        if (EasyPermissionInit.sharedInstance().initEasyPermission(this, perms, DefineValue.PERMISSIONS_REQ_CAMERA
                , getString(R.string.cancel_permission_read_contacts)))
            runCamera();

    }

    private void runCamera() {
        Intent i = new Intent(getActivity(), PaymentQRActivity.class);
        switchActivityFromHere(i);
    }

    public void setNumberRecipients(){

        if (phoneRetv.getSortedRecipients().length == 0) {
            txtNumberRecipients.setTextColor(getResources().getColor(R.color.colorSecondaryDark));
        } else {
            txtNumberRecipients.setTextColor(getResources().getColor(R.color.grey_1000b));
        }

        if (phoneRetv.length() == 0)
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
                    ArrayList<TempObjectData> mTempObjectDataList = new ArrayList<>();

                    String finalNumber, finalName;

                    String check = phoneRetv.getText().toString();
                    if ((!check.isEmpty()) && check.substring(check.length() - 1).equals(","))
                        phoneRetv.setText(check.substring(0, check.length() - 1));

                    chips = new DrawableRecipientChip[phoneRetv.getSortedRecipients().length];
                    chips = phoneRetv.getSortedRecipients();
                    listName = new ArrayList<>();
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
                            finalNumber = NoHPFormat.formatTo62(chip.getEntry().getDestination());
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
//        layoutDesc.setVisibility(View.GONE);
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

    private void switchActivityFromHere(Intent mIntent){
        startActivityForResult(mIntent,MainPage.REQUEST_FINISH);
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

        if(_url_profpic != null && _url_profpic.isEmpty()){
            GlideManager.sharedInstance().initializeGlide(getActivity(), R.drawable.user_unknown_menu, roundedImage, imgProfile);
        }
        else {
            GlideManager.sharedInstance().initializeGlide(getActivity(), _url_profpic, roundedImage, imgProfile);
        }
    }

    private void setTitle(String _title){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.setTitleFragment(_title);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainPage.REQUEST_FINISH) {
            if (resultCode == RESULT_PAYFRIEND_QR) {
                try {
                    JSONArray _data = new JSONArray(data.getStringExtra(DefineValue.RECIPIENTS));
                    final String name = _data.getJSONObject(0).getString(WebParams.NAME);
                    final String phone = _data.getJSONObject(0).getString(WebParams.MEMBER_CODE_TO);
                    phoneRetv.setEnabled(false);
                    setDataToUI(name,phone,data.getStringExtra(DefineValue.AMOUNT),data.getStringExtra(DefineValue.MESSAGE));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setDataToUI(final String name, final String phone, String amount, String msg){
        if(amount != null && !amount.isEmpty()){
            etAmount.setText(amount);
            etAmount.setEnabled(false);
        }
//        if(msg != null && !msg.isEmpty()){
//            layoutDesc.setVisibility(View.VISIBLE);
//            ((TextView)layoutDesc.findViewById(R.id.payfriend_desc_value)).setText(msg);
//        }
        phoneRetv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                phoneRetv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                phoneRetv.submitItem(name, phone);
            }

        });
//        txtNumberRecipients.setTextColor(getResources().getColor(R.color.colorPrimaryBlueDark));
        int d = phoneRetv.getSortedRecipients().length;
//        txtNumberRecipients.setText(String.valueOf(phoneRetv.getSortedRecipients().length));
        setNumberRecipients();

    }

}
