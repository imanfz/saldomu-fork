package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.InformationDialog;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/*
 * Created by thinkpad on 3/17/2015.
 */
public class FragAskForMoney extends BaseFragment {

    private View v;
    private ImageView imgProfile;
    private ImageView imgRecipients;
    private TextView txtName;
    private TextView txtNumberRecipients;
    private RecipientEditTextView phoneRetv;
    private Spinner sp_privacy;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_ask_for_money, container, false);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.information, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_information:
                if (!dialogI.isAdded())
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
        max_member_trans = sp.getInt(DefineValue.MAX_MEMBER_TRANS, 5);
        memberLevel = sp.getInt(DefineValue.LEVEL_VALUE, 0);

        imgProfile = v.findViewById(R.id.img_profile);
        imgRecipients = v.findViewById(R.id.img_recipients);
        txtName = v.findViewById(R.id.txtName);
        phoneRetv = v.findViewById(R.id.phone_retv);
        etAmount = v.findViewById(R.id.askformoney_value_amount);
        etAmount.addTextChangedListener(jumlahChangeListener);
        etMessage = v.findViewById(R.id.askformoney_value_message);
        txtNumberRecipients = v.findViewById(R.id.askformoney_value_number_recipients);
        btnRequestMoney = v.findViewById(R.id.btn_request_money);
        sp_privacy = v.findViewById(R.id.askformoney_privacy_spinner);

        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.privacy_list, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_privacy.setAdapter(spinAdapter);
        sp_privacy.setOnItemSelectedListener(spinnerPrivacy);

        Bitmap bmRecipients = BitmapFactory.decodeResource(getResources(), R.drawable.grey_background);
        RoundImageTransformation roundedImageRecipients = new RoundImageTransformation(bmRecipients);
        imgRecipients.setImageDrawable(roundedImageRecipients);

        _memberId = sp.getString(DefineValue.MEMBER_ID, "");
        _userid = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");
        setImageProfPic();

        txtName.setText(sp.getString(DefineValue.USER_NAME, ""));

        phoneRetv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        BaseRecipientAdapter adapter = new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, getActivity().getApplicationContext());
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

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            final String name = bundle.getString("name");
            final String phone = bundle.getString("phone");

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
        dialogI.setTargetFragment(this, 0);
    }


    private TextWatcher jumlahChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().equals("0")) etAmount.setText("");
            if (s.length() > 0 && s.charAt(0) == '0') {
                int i = 0;
                for (; i < s.length(); i++) {
                    if (s.charAt(i) != '0') break;
                }
                etAmount.setText(s.toString().substring(i));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

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
            if (InetHandler.isNetworkAvailable(getActivity())) {
                if (inputValidation()) {
                    phoneRetv.requestFocus();
                    String amount = etAmount.getText().toString();
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
                DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    private boolean isAlpha(String name) {
        Pattern p = Pattern.compile("[a-zA-Z]");
        Matcher m = p.matcher(name);
        return m.find();
    }


    private void sentData(final String _message, final String _data) {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
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
                                    e.printStackTrace();
                                }
                                showDialog(messageDialog);
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginMain(getActivity(), message);
                            } else {
                                if (code.equals("0998")) {
                                    phoneRetv.requestFocus();
                                    phoneRetv.setError(getString(R.string.payfriends_recipients_duplicate_validation));
                                }
                                code = model.getError_message();

                                Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();

                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

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
        new AlertDialog.Builder(getActivity())
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
        final Dialog dialog = new Dialog(getActivity());
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
        sp_privacy.setSelection(0);
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
        if (etAmount.getText().toString().length() == 0) {
            etAmount.requestFocus();
            etAmount.setError(getString(R.string.payfriends_amount_validation));
            return false;
        } else if (Long.parseLong(etAmount.getText().toString()) < 1) {
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
            GlideManager.sharedInstance().initializeGlide(getActivity(), R.drawable.user_unknown_menu, roundedImage, imgProfile);

        } else {
            GlideManager.sharedInstance().initializeGlide(getActivity(), _url_profpic, roundedImage, imgProfile);

        }
    }

}

