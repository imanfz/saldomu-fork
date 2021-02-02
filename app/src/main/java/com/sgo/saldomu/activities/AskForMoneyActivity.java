package com.sgo.saldomu.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlideManager;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.RoundImageTransformation;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.InformationDialog;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.utils.NumberTextWatcherForThousand;
import com.sgo.saldomu.widgets.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 12/12/2017.
 */

public class AskForMoneyActivity extends BaseActivity {
    private View v;
    private ImageView imgProfile;
    private ImageView imgRecipients;
    private TextView txtName;
    private TextView txtNumberRecipients;
    private RecipientEditTextView phoneRetv;
    //    private Spinner sp_privacy;
    private Button btnRequestMoney;
    private EditText etAmount;
    private EditText etMessage;
    private String _memberId;
    private String _userid;
    private String accessKey;
    private ProgressDialog progdialog;

    private int privacy;
    private int max_member_trans;
    private InformationDialog dialogI;

    private SecurePreferences sp;
    private DrawableRecipientChip[] chips;
    private int memberLevel;
    private String amount;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_ask_for_money;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        max_member_trans = sp.getInt(DefineValue.MAX_MEMBER_TRANS, 5);
        memberLevel = sp.getInt(DefineValue.LEVEL_VALUE, 0);

        imgProfile = findViewById(R.id.img_profile);
        imgRecipients = findViewById(R.id.img_recipients);
        txtName = findViewById(R.id.txtName);
        phoneRetv = findViewById(R.id.phone_retv);
        etAmount = findViewById(R.id.askformoney_value_amount);
        etAmount.addTextChangedListener(new NumberTextWatcherForThousand(etAmount));
        etMessage = findViewById(R.id.askformoney_value_message);
        txtNumberRecipients = findViewById(R.id.askformoney_value_number_recipients);
        btnRequestMoney = findViewById(R.id.btn_request_money);
//        sp_privacy = (Spinner) findViewById(R.id.askformoney_privacy_spinner);
//
//        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(this,
//                R.array.privacy_list, android.R.layout.simple_spinner_item);
//        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        sp_privacy.setAdapter(spinAdapter);
//        sp_privacy.setOnItemSelectedListener(spinnerPrivacy);

        Bitmap bmRecipients = BitmapFactory.decodeResource(getResources(), R.drawable.grey_background);
        RoundImageTransformation roundedImageRecipients = new RoundImageTransformation(bmRecipients);
        imgRecipients.setImageDrawable(roundedImageRecipients);

        _memberId = sp.getString(DefineValue.MEMBER_ID, "");
        _userid = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");
        setImageProfPic();

        txtName.setText(sp.getString(DefineValue.USER_NAME, ""));

        phoneRetv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        BaseRecipientAdapter adapter = new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, this.getApplicationContext());
        phoneRetv.setAdapter(adapter);
        phoneRetv.dismissDropDownOnItemSelected(true);

        btnRequestMoney.setOnClickListener(btnRequestMoneyListener);

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

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
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

            }
        });

        Intent intent = this.getIntent();
        if (intent != null) {
            final String name = intent.getStringExtra("name");
            final String phone = intent.getStringExtra("phone");

            //phoneRetv.submitItem(name, phone);
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

        dialogI = InformationDialog.newInstance(6);
//        dialogI.setTargetFragment(this,0);
    }

    public void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_title_ask_for_money));
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

        Timber.d("isi length recipients:" + phoneRetv.getRecipients().length);
    }

    private Spinner.OnItemSelectedListener spinnerPrivacy = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            privacy = i + 1;
            if (phoneRetv.hasFocus())
                phoneRetv.clearFocus();
            setNumberRecipients();

            if (phoneRetv.length() == 0)
                txtNumberRecipients.setText(String.valueOf(phoneRetv.getSortedRecipients().length));
            else
                txtNumberRecipients.setText(String.valueOf(phoneRetv.getRecipients().length));
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            if (phoneRetv.hasFocus())
                phoneRetv.clearFocus();
            setNumberRecipients();

            if (phoneRetv.length() == 0)
                txtNumberRecipients.setText(String.valueOf(phoneRetv.getSortedRecipients().length));
            else
                txtNumberRecipients.setText(String.valueOf(phoneRetv.getRecipients().length));
        }
    };

    private class TempObjectData {

        private String send_to;
        private String ccy_id;
        private String amount;
        private String recipient_name;

        public TempObjectData(String _send_to, String _ccy_id, String _amount, String _recipient_name) {
            this.send_to = _send_to;
            this.ccy_id = _ccy_id;
            this.amount = _amount;
            this.recipient_name = _recipient_name;
        }

    }

    private Button.OnClickListener btnRequestMoneyListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (InetHandler.isNetworkAvailable(AskForMoneyActivity.this)) {
                amount = NumberTextWatcherForThousand.trimCommaOfString(etAmount.getText().toString());
                if (inputValidation()) {
                    phoneRetv.requestFocus();
                    String finalNumber;
                    Boolean recipientValidation = true;
                    String message = etMessage.getText().toString();
                    ArrayList<TempObjectData> mTempObjectDataList = new ArrayList<>();

                    String check = phoneRetv.getText().toString();
                    if ((!check.isEmpty()) && check.substring(check.length() - 1).equals(","))
                        phoneRetv.setText(check.substring(0, check.length() - 1));

                    chips = new DrawableRecipientChip[phoneRetv.getSortedRecipients().length];
                    chips = phoneRetv.getSortedRecipients();
                    phoneRetv.clearFocus();
                    if (chips.length <= max_member_trans) {
                        for (DrawableRecipientChip chip : chips) {
                            Timber.v("DrawableChip:" + chip.getEntry().getDisplayName() + " " + chip.getEntry().getDestination());
                            finalNumber = chip.getEntry().getDestination();
                            if (isAlpha(finalNumber) || finalNumber.length() < getResources().getInteger(R.integer.lenght_phone_number)) {
                                recipientValidation = false;
                                break;
                            }
                            finalNumber = NoHPFormat.formatTo62(finalNumber);
                            Timber.v("final number:" + finalNumber);
                            mTempObjectDataList.add(new TempObjectData(finalNumber, DefineValue.IDR, amount, chip.getEntry().getDisplayName()));
                        }


                        if (recipientValidation) {
                            GsonBuilder gsonBuilder = new GsonBuilder();
                            gsonBuilder.setPrettyPrinting();
                            Gson gson = gsonBuilder.create();
                            String data = gson.toJson(mTempObjectDataList);
                            preDialog(message, data);
                        } else {
                            phoneRetv.requestFocus();
                            phoneRetv.setError(getString(R.string.payfriends_recipients_alpha_validation));
                        }
                    } else {
                        phoneRetv.requestFocus();
                        phoneRetv.setError(getString(R.string.payfriends_recipients_max_validation1) + " " +
                                max_member_trans + " " + getString(R.string.payfriends_recipients_max_validation2));
                    }

                }
            } else
                DefinedDialog.showErrorDialog(AskForMoneyActivity.this, getString(R.string.inethandler_dialog_message));
        }
    };

    private boolean isAlpha(String name) {
        Pattern p = Pattern.compile("[a-zA-Z]");
        Matcher m = p.matcher(name);
        return m.find();
    }


    private void sentData(final String _message, final String _data) {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(this, "");
            progdialog.show();

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_ASKFORMONEY_SUBMIT);
            params.put(WebParams.MEMBER_ID, _memberId);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.USER_ID, _userid);
            params.put(WebParams.DESC, _message);
            params.put(WebParams.DATA, _data);
            params.put(WebParams.PRIVACY, privacy);
            params.put(WebParams.MEMBER_LEVEL, memberLevel);

            Timber.d("isi params sent ask for money:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_ASKFORMONEY_SUBMIT, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            jsonModel model = getGson().fromJson(object, jsonModel.class);

                            String code = model.getError_code();


                            Log.wtf("asd", "code" + code);
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                JSONArray mArrayData;
                                String messageDialog = null, recipient = "", amount, recipient_name = "";
                                try {
                                    mArrayData = new JSONArray(_data);
                                    for (int i = 0; i < mArrayData.length(); i++) {
                                        recipient = recipient + mArrayData.getJSONObject(i).getString(WebParams.SEND_TO);
                                        recipient_name = recipient_name + mArrayData.getJSONObject(i).getString(WebParams.RECIPIENT_NAME);
                                        if ((i + 1) < mArrayData.length()) {
                                            recipient = recipient + ", ";
                                            recipient_name = recipient_name + ", ";
                                        }
                                    }
                                    amount = MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(mArrayData.getJSONObject(0).getString(WebParams.AMOUNT));
                                    messageDialog = getString(R.string.askfriends_dialog_text_recipient) + " : " + recipient_name + "\n" +
                                            getString(R.string.askfriends_dialog_text_amount) + " : " + amount + "\n" +
                                            getString(R.string.askfriends_dialog_text_desc) + " : " + _message + "\n";
                                } catch (JSONException e) {
                                    Timber.d("error" + e.toString());
                                    e.printStackTrace();
                                }
                                showDialog(messageDialog);
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity2(AskForMoneyActivity.this, message);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(AskForMoneyActivity.this, appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + object.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(AskForMoneyActivity.this, model.getError_message());
                            } else {
                                if (code.equals("0998")) {
                                    phoneRetv.requestFocus();
                                    phoneRetv.setError(getString(R.string.payfriends_recipients_duplicate_validation));
                                }
                                code = model.getError_message();

                                Toast.makeText(AskForMoneyActivity.this, code, Toast.LENGTH_LONG).show();

                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                            Log.wtf("asd2", "asd2");
                        }

                        @Override
                        public void onComplete() {
                            if (progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void preDialog(final String _message, final String _data) {
        String message = getString(R.string.askfriends_predialog_msg1) + " " + chips.length + " " + getString(R.string.askfriends_predialog_msg2);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.askfriends_predialog_title))
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sentData(_message, _data);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    private void showDialog(String messageDialog) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = dialog.findViewById(R.id.title_dialog);
        TextView Message = dialog.findViewById(R.id.message_dialog);
        TextView Message_Detail = dialog.findViewById(R.id.message_dialog3);

        //clear data in edit text
        phoneRetv.setText("");
        etAmount.setText("");
        etMessage.setText("");
//        sp_privacy.setSelection(0);
        txtNumberRecipients.setText(getString(R.string.Zero));
        setNumberRecipients();
        txtNumberRecipients.setText(String.valueOf(phoneRetv.getSortedRecipients().length));


        Message.setVisibility(View.VISIBLE);
        Title.setText(getString(R.string.askfriends_dialog_title));
        Message.setText(getResources().getString(R.string.askfriends_dialog_msg));
        Message_Detail.setVisibility(View.VISIBLE);
        Message_Detail.setGravity(Gravity.LEFT);
        Message_Detail.setText(messageDialog);

        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
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
        if (amount.length() == 0) {
            etAmount.requestFocus();
            etAmount.setError(getString(R.string.payfriends_amount_validation));
            return false;
        } else if (Long.parseLong(amount) < 1) {
            etAmount.requestFocus();
            etAmount.setError(getString(R.string.payfriends_amount_zero));
            return false;
        }
        return true;
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

//        Picasso mPic;
//        if(MyApiClient.PROD_FLAG_ADDRESS)
//            mPic = MyPicasso.getUnsafeImageLoader(getActivity());
//        else
//            mPic= Picasso.with(getActivity());

        if (_url_profpic != null && _url_profpic.isEmpty()) {
            GlideManager.sharedInstance().initializeGlide(this, R.drawable.user_unknown_menu, roundedImage, imgProfile);

        } else {
            GlideManager.sharedInstance().initializeGlide(this, _url_profpic, roundedImage, imgProfile);

        }
    }


    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_information:
                if (!dialogI.isAdded())
                    dialogI.show(this.getSupportFragmentManager(), InformationDialog.TAG);
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
