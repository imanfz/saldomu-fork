package com.sgo.saldomu.fragments;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.PayFriendsActivity;
import com.sgo.saldomu.activities.ScanQRActivity;
import com.sgo.saldomu.activities.TopUpActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.ErrorDefinition;
import com.sgo.saldomu.coreclass.GlideManager;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.RoundImageTransformation;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogFrag;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.InformationDialog;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.QrModel;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.PayfriendDataTrfModel;
import com.sgo.saldomu.models.retrofit.SentDataPayfriendModel;
import com.sgo.saldomu.utils.NumberTextWatcherForThousand;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/*
  Created by thinkpad on 3/11/2015.
 */
public class FragPayFriends extends BaseFragment {

    private boolean isNotification = false;
    private InformationDialog dialogI;
    private ImageView imgProfile;
    private ImageView imgRecipients;
    private TextView txtName;
    private TextView txtNumberRecipients;
    //    private Spinner sp_privacy;
    private RecipientEditTextView phoneRetv;
    private Button btnGetOTP;
    private EditText etAmount;
    private EditText etMessage;
    private ImageButton btnScanQR;
    private List<String> listName;

    private static final int RC_READ_CONTACTS = 14;

    private int max_member_trans;

    private Bundle bundle;
    private DrawableRecipientChip[] chips;

    private ProgressDialog progdialog;

    private View v;

    private String authType;


    public static final int REQUEST_QR_FROM_PAY_FRIENDS = 1001;
    private QrModel qrObj = new QrModel();
    private BaseRecipientAdapter adapter;
    private List<String> phoneNumberList;
    private String phoneNumberString;
    private JSONArray phoneNumberJsonArr = new JSONArray();

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
        if (item.getItemId() == R.id.action_information) {
            if (!dialogI.isAdded())
                dialogI.show(getActivity().getSupportFragmentManager(), InformationDialog.TAG);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if ((ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)) {

        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS},
                    RC_READ_CONTACTS);
        }

        max_member_trans = sp.getInt(DefineValue.MAX_MEMBER_TRANS, 5);
        authType = sp.getString(DefineValue.AUTHENTICATION_TYPE, "");

        dialogI = InformationDialog.newInstance(5);
        dialogI.setTargetFragment(this, 0);

        imgProfile = v.findViewById(R.id.img_profile);
        imgRecipients = v.findViewById(R.id.img_recipients);
        txtName = v.findViewById(R.id.txtName);
        phoneRetv = v.findViewById(R.id.phone_retv);
        etAmount = v.findViewById(R.id.payfriends_value_amount);
        etAmount.addTextChangedListener(new NumberTextWatcherForThousand(etAmount));
        etMessage = v.findViewById(R.id.payfriends_value_message);
        txtNumberRecipients = v.findViewById(R.id.payfriends_value_number_recipients);
        btnGetOTP = v.findViewById(R.id.btn_get_otp);
        btnScanQR = v.findViewById(R.id.btnScanQr);

        if (authType.equalsIgnoreCase("PIN")) {
            btnGetOTP.setText(R.string.next);
        } else if (authType.equalsIgnoreCase("OTP")) {

            btnGetOTP.setText(R.string.submit);
        }

//        sp_privacy = v.findViewById(R.id.payfriend_privacy_spinner);

//        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(getActivity(),
//                R.array.privacy_list, android.R.layout.simple_spinner_item);
//        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        sp_privacy.setAdapter(spinAdapter);
//        sp_privacy.setOnItemSelectedListener(spinnerPrivacy);


        Bitmap bmRecipients = BitmapFactory.decodeResource(getResources(), R.drawable.grey_background);
        RoundImageTransformation roundedImageRecipients = new RoundImageTransformation(bmRecipients);
        imgRecipients.setImageDrawable(roundedImageRecipients);


        setImageProfPic();

        txtName.setText(sp.getString(DefineValue.USER_NAME, ""));

        phoneRetv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        adapter = new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, getActivity().getApplicationContext());
        phoneRetv.setAdapter(adapter);
        phoneRetv.dismissDropDownOnItemSelected(true);
        phoneRetv.setThreshold(1);


        btnGetOTP.setOnClickListener(btnGetOTPListener);
        btnScanQR.setOnClickListener(scanQRListener);

        etAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    setNumberRecipients();
                }
            }
        });

        etMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    setNumberRecipients();
                }
            }
        });


        phoneRetv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Timber.d("before Text Change:" + s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Timber.d("on Text Change:" + s.toString());
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
                Timber.d("after Text Change:" + s.toString());
            }
        });

        bundle = this.getArguments();
        if (bundle != null) {
            final String name, phone;

            if (bundle.containsKey(DefineValue.AMOUNT) && !bundle.getString(DefineValue.AMOUNT, null).isEmpty()) {
                name = bundle.getString(DefineValue.CUST_NAME);
                phone = bundle.getString(DefineValue.USERID_PHONE);
                String amount = bundle.getString(DefineValue.AMOUNT);
                etAmount.setText(amount);
                etAmount.setEnabled(false);

                phoneRetv.setEnabled(false);
                isNotification = true;
            } else {
                name = bundle.getString("name");
                phone = bundle.getString("phone");

            }

            if (bundle.containsKey(DefineValue.FAVORITE_CUSTOMER_ID) && bundle.getString(DefineValue.FAVORITE_CUSTOMER_ID, null) != null) {
                phoneRetv.setText(bundle.getString(DefineValue.FAVORITE_CUSTOMER_ID));
                phoneRetv.requestFocus();
            }

            if (bundle.containsKey(DefineValue.QR_OBJ) && bundle.getParcelable(DefineValue.QR_OBJ) != null) {
                qrObj = bundle.getParcelable(DefineValue.QR_OBJ);
                setBundleViewQR();
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

    private void setNumberRecipients() {

        if (phoneRetv.getSortedRecipients().length == 0) {
            txtNumberRecipients.setTextColor(getResources().getColor(R.color.colorSecondaryDark));
        } else {
            txtNumberRecipients.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        if (phoneRetv.length() == 0)
            txtNumberRecipients.setText(String.valueOf(phoneRetv.getSortedRecipients().length));
        else
            txtNumberRecipients.setText(String.valueOf(phoneRetv.getRecipients().length));

        Timber.d("isi length recipients:" + String.valueOf(phoneRetv.getRecipients().length));
    }

    private class TempObjectData {

        private String member_code_to;
        private String ccy_id;
        private String amount;
        private String name;

        public TempObjectData(String _member_code_to, String _ccy_id, String _amount, String _name) {
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

    private Button.OnClickListener scanQRListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {

            switchViewToScanQR();
        }

    };


    private Button.OnClickListener btnGetOTPListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                if (inputValidation()) {

                    Timber.d("isi length recipients button:" + String.valueOf(phoneRetv.getRecipients().length));
                    Timber.d("isi length sort recipients button:" + String.valueOf(phoneRetv.getSortedRecipients().length));
                    phoneRetv.requestFocus();
                    String amount = NumberTextWatcherForThousand.trimCommaOfString(etAmount.getText().toString());
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
                    phoneNumberList = new ArrayList<>();
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
                            Log.e("finalNumber : ", finalNumber);
                            listName.add(chip.getEntry().getDisplayName());
                            phoneNumberList.add(finalName);
                            phoneNumberJsonArr.put(finalNumber);
                            mTempObjectDataList.add(new TempObjectData(finalNumber, DefineValue.IDR, amount, finalName));
                        }

                        if (recipientValidation) {
                            final GsonBuilder gsonBuilder = new GsonBuilder();
//                            gsonBuilder.setPrettyPrinting();
                            final Gson gson = gsonBuilder.create();
                            String testJson = gson.toJson(mTempObjectDataList);
                            String nameJson = gson.toJson(listName);
//                            phoneNumberString = gson.toJson(phoneNumberJsonArr);
                            Log.e("phoneNumberJsonArr ", phoneNumberJsonArr.toString());
                            phoneNumberString = phoneNumberJsonArr.toString();
                            Log.e("phoneNumberJsonArr 2 ", phoneNumberString);
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
            } else
                DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    private boolean isAlpha(String name) {
        Pattern p = Pattern.compile("[a-zA-Z]");
        Matcher m = p.matcher(name);
        return m.find();
    }

    private void sentData(String _message, String _data, final String _nameJson) {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();


            HashMap<String, Object> params;
            String url;
            if (isNotification) {
//                params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_REQ_TOKEN_P2P_NOTIF,
//                        userPhoneID,accessKey, memberIDLogin);
                params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_P2P_NOTIF, memberIDLogin);
                url = MyApiClient.LINK_REQ_TOKEN_P2P_NOTIF;
            } else {
                extraSignature = memberIDLogin;
//                params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_REQ_TOKEN_P2P,
//                        userPhoneID,accessKey, extraSignature);
                params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_P2P, extraSignature);
                url = MyApiClient.LINK_REQ_TOKEN_P2P;
            }

            params.put(WebParams.MEMBER_ID, memberIDLogin);
            params.put(WebParams.MEMBER_REMARK, _message);
            params.put(WebParams.DATA, _data);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.PRIVACY, "");
            if (isNotification) {
                params.put(WebParams.REQUEST_ID, bundle.getString(DefineValue.REQUEST_ID));
                params.put(WebParams.TRX_ID, bundle.getString(DefineValue.TRX));
            }

            Timber.d("isi params sent req token p2p:%s", params.toString());

            RetrofitService.getInstance().PostObjectRequest(url, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            try {
                                Timber.d("isi response sent req token p2p:%s", object.toString());
                                SentDataPayfriendModel model = getGson().fromJson(object, SentDataPayfriendModel.class);

                                String code = model.getError_code();
                                String message = model.getError_message();
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    int isFailed = 0;
                                    String msg = "";

                                    for (PayfriendDataTrfModel obj : model.getData_transfer()) {
                                        if (obj.getMember_status().equals(DefineValue.FAILED)) {
                                            isFailed++;
                                            msg = obj.getMember_remark();
                                        }
                                    }
                                    if (isFailed != model.getData_transfer().size()) {
                                        String dataTransfer = getGson().toJson(model.getData_transfer());
                                        showDialog(dataTransfer, _nameJson, model.getMessage(), getGson().toJson(model.getData_mapper()));
                                    } else
                                        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    AlertDialogLogout.getInstance().showDialoginActivity(getActivity(), message);
                                } else if (code.equals(ErrorDefinition.WRONG_PIN_P2P)) {
                                    showDialogError(message);
                                } else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:%s", model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:%s", object.toString());
                                    AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                                } else {
                                    if (code.equals(ErrorDefinition.ERROR_CODE_DUPLICATED_RECIPIENT)) {
                                        phoneRetv.requestFocus();
                                        phoneRetv.setError(getString(R.string.payfriends_recipients_duplicate_validation));
                                    } else if (code.equals(ErrorDefinition.ERROR_CODE_LESS_BALANCE)) {

                                        String message_dialog = "\"" + message + "\" \n" + getString(R.string.dialog_message_less_balance, getString(R.string.appname));

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
                                        dialog_frag.setTargetFragment(FragPayFriends.this, 0);
                                        dialog_frag.show(getActivity().getSupportFragmentManager(), AlertDialogFrag.TAG);
                                    } else {
                                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                                    }

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            phoneNumberJsonArr = new JSONArray();
                            if (progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:%s", e.getMessage());
        }
    }

    private void showDialogError(String message) {
        Dialog mdialog = DefinedDialog.MessageDialog(getActivity(), getString(R.string.blocked_pin_title), message,
                () -> {  });
        mdialog.show();
    }

    private void showDialog(final String _data_transfer, final String _nameJson, final String _message, final String _data_mapper) {
        phoneRetv.setText(null);
        if (authType.equalsIgnoreCase("OTP")) {
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
            Title.setText(getString(R.string.payfriends_dialog_validation_title));
            Message.setText(getString(R.string.appname) + " " + getString(R.string.dialog_token_message_sms));

            //clear data in edit text


            btnDialogOTP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();

                    Intent i = new Intent(getActivity(), PayFriendsActivity.class);

                    i.putExtra(WebParams.DATA_TRANSFER, _data_transfer);
                    i.putExtra(WebParams.DATA, _nameJson);
                    i.putExtra(WebParams.MESSAGE, _message);
                    i.putExtra(DefineValue.TRANSACTION_TYPE, isNotification);
                    i.putExtra(DefineValue.CONFIRM_PAYFRIEND, true);
                    i.putExtra(WebParams.DATA_MAPPER, _data_mapper);
//                    i.putExtra(WebParams.CUSTOMER_ID, phoneNumberList.toString());
                    i.putExtra(WebParams.CUSTOMER_ID, phoneNumberString);

                    switchActivity(i);
                }
            });

            dialog.show();
        } else if (authType.equalsIgnoreCase("PIN")) {
            //clear data in edit text

            Intent i = new Intent(getActivity(), PayFriendsActivity.class);
            i.putExtra(WebParams.DATA_TRANSFER, _data_transfer);
            i.putExtra(WebParams.DATA, _nameJson);
            i.putExtra(WebParams.MESSAGE, _message);
            i.putExtra(DefineValue.TRANSACTION_TYPE, isNotification);
            i.putExtra(DefineValue.CONFIRM_PAYFRIEND, true);
            i.putExtra(WebParams.DATA_MAPPER, _data_mapper);
//            i.putExtra(WebParams.CUSTOMER_ID, phoneNumberList.toString());
            i.putExtra(WebParams.CUSTOMER_ID, phoneNumberString);

            switchActivity(i);
        }

        phoneRetv.setEnabled(true);
        phoneRetv.clearListSelection();
        phoneRetv.clearComposingText();
        phoneRetv.setText("");
        phoneRetv.removeMoreChip();
        etAmount.setText("");
        etMessage.setText("");
//        sp_privacy.setSelection(0);
        phoneRetv.requestFocus();
        phoneRetv.clearFocus();
        etAmount.setEnabled(true);
        //txtNumberRecipients.setText(getString(R.string.Zero));
        setNumberRecipients();
        txtNumberRecipients.setText(String.valueOf(phoneRetv.getSortedRecipients().length));
    }


    private boolean inputValidation() {
        if (phoneRetv.getText().toString().length() == 0) {
            phoneRetv.requestFocus();
            phoneRetv.setError(getString(R.string.payfriends_recipients_validation));
            return false;
        }
        if (phoneRetv.isFocused()) {
            phoneRetv.clearFocus();
        }
        if (phoneRetv.getText().toString().charAt(0) == ' ') {
            phoneRetv.requestFocus();
            phoneRetv.setError(getString(R.string.payfriends_recipients_validation));
            return false;
        }
        if (etAmount.getText().toString().length() == 0) {
            etAmount.requestFocus();
            etAmount.setError(getString(R.string.payfriends_amount_validation));
            return false;
        }

        return true;
    }

    private void switchActivity(Intent mIntent) {
        if (getActivity() == null)
            return;

        /*MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent,MainPage.ACTIVITY_RESULT);*/
        getActivity().startActivityForResult(mIntent, MainPage.REQUEST_FINISH);
    }

    private void setImageProfPic() {
        float density = getResources().getDisplayMetrics().density;
        String _url_profpic;

        if (density <= 1) _url_profpic = sp.getString(DefineValue.IMG_SMALL_URL, null);
        else if (density < 2) _url_profpic = sp.getString(DefineValue.IMG_MEDIUM_URL, null);
        else _url_profpic = sp.getString(DefineValue.IMG_LARGE_URL, null);

        Timber.wtf("url prof pic:" + _url_profpic);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.user_unknown_menu);
        RoundImageTransformation roundedImage = new RoundImageTransformation(bm);

        if (_url_profpic != null && _url_profpic.isEmpty()) {
            GlideManager.sharedInstance().initializeGlide(getActivity(), R.drawable.user_unknown_menu, roundedImage, imgProfile);
        } else {
            GlideManager.sharedInstance().initializeGlide(getActivity(), _url_profpic, roundedImage, imgProfile);
        }
    }


    private void switchViewToScanQR() {
        Intent intent = new Intent(getActivity(), ScanQRActivity.class);
        intent.putExtra(DefineValue.TYPE, DefineValue.QR_FROM_PAY_FRIENDS);
        startActivityForResult(intent, REQUEST_QR_FROM_PAY_FRIENDS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_QR_FROM_PAY_FRIENDS) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    qrObj = data.getParcelableExtra(DefineValue.QR_OBJ);
                    setBundleViewQR();
                }
            }
        }

    }

    private void setBundleViewQR() {
        Timber.d("Isi qrOBJ name:" + qrObj.getSourceName() + " id:" + qrObj.getSourceAcct() + " type:" + qrObj.getQrType());
        phoneRetv.append("+" + NoHPFormat.formatTo62(qrObj.getSourceAcct()));
        phoneRetv.requestFocus(phoneRetv.getText().length());
    }
}
