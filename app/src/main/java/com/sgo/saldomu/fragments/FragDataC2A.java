package com.sgo.saldomu.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.BBSCommBenef;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.entityRealm.List_BBS_Birth_Place;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.widgets.BaseFragment;
import com.sgo.saldomu.widgets.ProgressRequestBody;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import timber.log.Timber;

public class FragDataC2A extends BaseFragment {
    public final static String TAG = "com.sgo.saldomu.fragments.FragDataC2A";
    View v;
    SecurePreferences sp;
    Bundle bundle;
    Button btn_submit, btn_cancel;
    EditText et_name, et_address, et_noID, et_noHp, et_sumberdana;
    Spinner sp_sumberdana;
    TextView tv_dob;
    String tx_id;
    private com.wdullaer.materialdatetimepicker.date.DatePickerDialog dpd;
    private DateFormat fromFormat;
    private DateFormat toFormat2;
    private String dateNow;
    private String dedate;
    private String date_dob;
    private String sumberdana;
    ProgressDialog progressDialog;
    protected String memberIDLogin, userPhoneID;
    private String comm_code, tx_product_code, source_product_type,
            benef_city, source_product_h2h, api_key, callback_url, tx_bank_code, tx_bank_name, tx_product_name,
            comm_id, benef_product_name, name_benef, no_benef,
            no_hp_benef, remark, source_product_name, transaksi, benef_product_code,
            benef_product_type, max_resend, custIDtypes;
    LinearLayout layout_sender, layout_pob, layout_dob;
    private Boolean TCASHValidation = false, MandiriLKDValidation = false, code_success = false, isOwner = false;
    private Activity act;
    private Realm realm;
    private List<String> list_name_bbs_birth_place;
    AutoCompleteTextView city_textview_autocomplete;
    SignaturePad signaturePad;
    ImageButton ibRefresh;
    File photoFile;
    MultipartBody.Part photoFilePart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_data_mandirilkd, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        act = getActivity();
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        realm = Realm.getInstance(RealmManager.BBSConfiguration);

        memberIDLogin = sp.getString(DefineValue.MEMBER_ID, "");
        userPhoneID = sp.getString(DefineValue.USERID_PHONE, "");

        custIDtypes = DefineValue.KTP;

        btn_submit = v.findViewById(R.id.btn_submit_data_mandirilkd);
        btn_cancel = v.findViewById(R.id.btn_cancel_mandirilkd);
        et_name = v.findViewById(R.id.name_mandiriLKD);
        et_address = v.findViewById(R.id.address_value);
        et_noID = v.findViewById(R.id.socialId_value);
        et_noHp = v.findViewById(R.id.noHP_value);
        sp_sumberdana = v.findViewById(R.id.datalkd_sumber_dana);
        tv_dob = v.findViewById(R.id.dob_value);
        layout_sender = v.findViewById(R.id.layout_name);
        layout_pob = v.findViewById(R.id.layout_tempat_lahir);
        layout_dob = v.findViewById(R.id.layout_tanggal_lahir);
        city_textview_autocomplete = v.findViewById(R.id.mandiriLKD_pob);
        et_sumberdana = v.findViewById(R.id.et_sumber_dana);
        signaturePad = v.findViewById(R.id.signature_pad);
        ibRefresh = v.findViewById(R.id.ib_refresh);

        bundle = getArguments();
        if (bundle != null) {
            transaksi = bundle.getString(DefineValue.TRANSACTION);
            tx_id = bundle.getString(DefineValue.TX_ID, "");
            if (bundle.containsKey(DefineValue.BENEF_CITY)) {
                benef_city = bundle.getString(DefineValue.BENEF_CITY);
            }
            source_product_h2h = bundle.getString(DefineValue.PRODUCT_H2H);
            source_product_type = bundle.getString(DefineValue.PRODUCT_TYPE);
            tx_product_code = bundle.getString(DefineValue.PRODUCT_CODE);
            tx_bank_code = bundle.getString(DefineValue.BANK_CODE);
            tx_bank_name = bundle.getString(DefineValue.BANK_NAME);
            tx_product_name = bundle.getString(DefineValue.PRODUCT_NAME);
            comm_code = bundle.getString(DefineValue.COMMUNITY_CODE);
            tx_id = bundle.getString(DefineValue.TX_ID);
            callback_url = bundle.getString(DefineValue.CALLBACK_URL);
            api_key = bundle.getString(DefineValue.API_KEY);
            comm_id = bundle.getString(DefineValue.COMMUNITY_ID);
            benef_product_name = bundle.getString(DefineValue.BANK_BENEF);
            benef_product_code = bundle.getString(DefineValue.BENEF_PRODUCT_CODE);
            name_benef = bundle.getString(DefineValue.NAME_BENEF);
            no_benef = bundle.getString(DefineValue.NO_BENEF);
            no_hp_benef = bundle.getString(DefineValue.NO_HP_BENEF);
            remark = bundle.getString(DefineValue.REMARK);
            max_resend = bundle.getString(DefineValue.MAX_RESEND);
            source_product_name = bundle.getString(DefineValue.SOURCE_ACCT);
            TCASHValidation = bundle.getBoolean(DefineValue.TCASH_HP_VALIDATION, false);
            MandiriLKDValidation = bundle.getBoolean(DefineValue.MANDIRI_LKD_VALIDATION, false);
            code_success = bundle.getBoolean(DefineValue.CODE_SUCCESS);
            benef_product_type = bundle.getString(DefineValue.TYPE_BENEF, "");
            isOwner = bundle.getBoolean(DefineValue.IS_OWNER, false);
        }

        RealmResults<List_BBS_Birth_Place> results = realm.where(List_BBS_Birth_Place.class).findAll();

        Timber.d("REALM RESULTS:%s", results.toString());

        list_name_bbs_birth_place = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {

            if (results.get(i).getBirthPlace_city() != null || !results.get(i).getBirthPlace_city().equalsIgnoreCase("")) {
                list_name_bbs_birth_place.add(results.get(i).getBirthPlace_city());
            }
        }

        Timber.d("Size of List name Birth place:%s", list_name_bbs_birth_place.size());
        ArrayAdapter<String> city_adapter = new ArrayAdapter<String>
                (getActivity(), android.R.layout.simple_selectable_list_item, list_name_bbs_birth_place);
        city_textview_autocomplete.setThreshold(1);
        city_textview_autocomplete.setAdapter(city_adapter);

        initializeLayout();

        fromFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("ID", "INDONESIA"));

        toFormat2 = new SimpleDateFormat("dd-M-yyyy", new Locale("ID", "INDONESIA"));

        Calendar c = Calendar.getInstance();
        dateNow = fromFormat.format(c.getTime());
        Timber.d("date now profile:%s", dateNow);

        dpd = DatePickerDialog.newInstance(
                dobPickerSetListener,
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        tv_dob.setOnClickListener(view -> {
            if (getFragmentManager() != null) {
                dpd.show(getFragmentManager(), "asd");
            }
        });

        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.sumber_dana_type, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_sumberdana.setAdapter(spinAdapter);
        sp_sumberdana.setOnItemSelectedListener(spinnerSumberDana);

        ibRefresh.setOnClickListener(v -> signaturePad.clear());
        btn_submit.setOnClickListener(submitlistener);
        btn_cancel.setOnClickListener(cancellistener);

    }

    public void initializeLayout() {
        if (!isOwner) {
            layout_dob.setVisibility(View.VISIBLE);
            layout_pob.setVisibility(View.VISIBLE);

        }
    }

    private Spinner.OnItemSelectedListener spinnerSumberDana = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            sumberdana = sp_sumberdana.getItemAtPosition(i).toString();
            if (sumberdana.equalsIgnoreCase("LAINNYA")) {
                et_sumberdana.setVisibility(View.VISIBLE);
            } else et_sumberdana.setVisibility(View.GONE);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private Button.OnClickListener submitlistener = view -> {
        if (inputValidation()) {
//                birthplace_id = list_bbs_birth_place.get(CityAutocompletePos).getBirthPlace_id();
            setSignaturePhoto();
        }
    };

    private void setSignaturePhoto() {
        Bitmap signatureBitmap = signaturePad.getSignatureBitmap();
        if (addJpgSignatureToGallery(signatureBitmap)) {
            sendData();
        } else {
            Toast.makeText(getActivity(), "Unable to store the signature", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean addJpgSignatureToGallery(Bitmap signature) {
        try {
            File photo = new File(getAlbumStorageDir("SignaturePad"),
                    String.format("Signature_%d.jpg", System.currentTimeMillis()));
            saveBitmapToJPG(signature, photo);
            scanMediaFile(photo);
            photoFile = photo;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void scanMediaFile(File photo) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(photo);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    private void saveBitmapToJPG(Bitmap bitmap, File photo) {
        try {
            OutputStream stream = new FileOutputStream(photo);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getAlbumStorageDir(String albumName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
        ), albumName);
        if (!file.mkdirs()) {
            Timber.e("Directory not created");
        }
        return file;
    }

    public void sendData() {
        progressDialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
        progressDialog.show();
        try {
            extraSignature = tx_id + sp.getString(DefineValue.MEMBER_ID, "") + custIDtypes;
            HashMap<String, RequestBody> params = RetrofitService.getInstance().getSignature2(MyApiClient.LINK_BBS_SEND_DATA_LKD, extraSignature);

            String transferTo;
            if (isOwner) {
                transferTo = "S";
            } else {
                transferTo = "O";
                RequestBody reqCustBirthPlace = RequestBody.create(MediaType.parse("text/plain"),
                        city_textview_autocomplete.getText().toString());
                RequestBody reqCustBirthDate = RequestBody.create(MediaType.parse("text/plain"),
                        date_dob);
                params.put(WebParams.CUST_BIRTH_PLACE, reqCustBirthPlace);
                params.put(WebParams.CUST_BIRTH_DATE, reqCustBirthDate);
            }

            if (sumberdana.equalsIgnoreCase("LAINNYA"))
                sumberdana = et_sumberdana.getText().toString();

            RequestBody reqTransferTo = RequestBody.create(MediaType.parse("text/plain"),
                    transferTo);
            RequestBody reqCustName = RequestBody.create(MediaType.parse("text/plain"),
                    et_name.getText().toString());
            RequestBody reqUserId = RequestBody.create(MediaType.parse("text/plain"),
                    userPhoneID);
            RequestBody reqTxId = RequestBody.create(MediaType.parse("text/plain"),
                    tx_id);
            RequestBody reqMemberId = RequestBody.create(MediaType.parse("text/plain"),
                    memberIDLogin);
            RequestBody reqCustPhone = RequestBody.create(MediaType.parse("text/plain"),
                    et_noHp.getText().toString());
            RequestBody reqCustAddress = RequestBody.create(MediaType.parse("text/plain"),
                    et_address.getText().toString());
            RequestBody reqCustIdType = RequestBody.create(MediaType.parse("text/plain"),
                    custIDtypes);
            RequestBody reqCustIdNumber = RequestBody.create(MediaType.parse("text/plain"),
                    et_noID.getText().toString());
            RequestBody reqSourceOfFund = RequestBody.create(MediaType.parse("text/plain"),
                    sumberdana);

            params.put(WebParams.TRANSFER_TO, reqTransferTo);
            params.put(WebParams.CUST_NAME, reqCustName);
            params.put(WebParams.USER_ID, reqUserId);
            params.put(WebParams.TX_ID, reqTxId);
            params.put(WebParams.MEMBER_ID, reqMemberId);
            params.put(WebParams.CUST_PHONE, reqCustPhone);
            params.put(WebParams.CUST_ADDRESS, reqCustAddress);
            params.put(WebParams.CUST_ID_TYPE, reqCustIdType);
            params.put(WebParams.CUST_ID_NUMBER, reqCustIdNumber);
            params.put(WebParams.SOURCE_OF_FUND, reqSourceOfFund);

            RequestBody reqFile = new ProgressRequestBody(photoFile, percentage -> Timber.d("Percentage : %s", percentage));
            photoFilePart = MultipartBody.Part.createFormData(WebParams.SIGN, photoFile.getName(), reqFile);

            Timber.d("params bbs send data : %s", params.toString());

            RetrofitService.getInstance().MultiPartRequest(MyApiClient.LINK_BBS_SEND_DATA_LKD, params, photoFilePart,
                    response -> {
                        try {
                            jsonModel model = getGson().fromJson(String.valueOf(response), jsonModel.class);
                            JSONObject jsonObject = new JSONObject(response.toString());
                            String code = jsonObject.getString(WebParams.ERROR_CODE);
                            String message = jsonObject.getString(WebParams.ERROR_MESSAGE);
                            Timber.d("response bbs send data : %s", jsonObject.toString());
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                changeToBBSCashInConfirm(jsonObject.getString(WebParams.ADMIN_FEE), jsonObject.getString(WebParams.AMOUNT), jsonObject.getString(WebParams.TOTAL_AMOUNT));
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                Timber.d("isi response autologout:%s", response.toString());
                                AlertDialogLogout.getInstance().showDialoginActivity(getActivity(), message);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:%s", model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:%s", response.toString());
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                            } else {
                                Timber.d("isi error bbs send data:%s", response.toString());
                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                            }
                            btn_submit.setEnabled(true);
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:%s", e.getMessage());
        }

    }

    private void changeToBBSCashInConfirm(String fee, String amount, String total_amount) {

        Bundle mArgs = new Bundle();
        if (benef_product_type.equalsIgnoreCase(DefineValue.ACCT)) {
            mArgs.putString(DefineValue.BENEF_CITY, benef_city);
        }
        mArgs.putString(DefineValue.PRODUCT_H2H, source_product_h2h);
        mArgs.putString(DefineValue.PRODUCT_TYPE, source_product_type);
        mArgs.putString(DefineValue.PRODUCT_CODE, tx_product_code);
        mArgs.putString(DefineValue.BANK_CODE, tx_bank_code);
        mArgs.putString(DefineValue.BANK_NAME, tx_bank_name);
        mArgs.putString(DefineValue.PRODUCT_NAME, tx_product_name);
        mArgs.putString(DefineValue.FEE, fee);
        mArgs.putString(DefineValue.COMMUNITY_CODE, comm_code);
        mArgs.putString(DefineValue.TX_ID, tx_id);
        mArgs.putString(DefineValue.AMOUNT, amount);
        mArgs.putString(DefineValue.TOTAL_AMOUNT, total_amount);
        mArgs.putString(DefineValue.SHARE_TYPE, "1");
        mArgs.putString(DefineValue.CALLBACK_URL, callback_url);
        mArgs.putString(DefineValue.API_KEY, api_key);
        mArgs.putString(DefineValue.COMMUNITY_ID, comm_id);
        mArgs.putString(DefineValue.BANK_BENEF, benef_product_name);
        mArgs.putString(DefineValue.NAME_BENEF, name_benef);
        mArgs.putString(DefineValue.NO_BENEF, no_benef);
        mArgs.putString(DefineValue.TYPE_BENEF, benef_product_type);
        mArgs.putString(DefineValue.NO_HP_BENEF, no_hp_benef);
        mArgs.putString(DefineValue.REMARK, remark);
        mArgs.putString(DefineValue.SOURCE_ACCT, source_product_name);
        mArgs.putString(DefineValue.MAX_RESEND, max_resend);
        mArgs.putString(DefineValue.TRANSACTION, transaksi);
        mArgs.putString(DefineValue.BENEF_PRODUCT_CODE, benef_product_code);
//        mArgs.putString(DefineValue.ADDITIONAL_FEE, additional_fee);
        if (TCASHValidation != null)
            mArgs.putBoolean(DefineValue.TCASH_HP_VALIDATION, TCASHValidation);
        if (MandiriLKDValidation != null)
            mArgs.putBoolean(DefineValue.MANDIRI_LKD_VALIDATION, MandiriLKDValidation);
        if (code_success != null)
            mArgs.putBoolean(DefineValue.CODE_SUCCESS, code_success);
        btn_submit.setEnabled(true);

        Fragment mFrag = new BBSCashInConfirm();
        mFrag.setArguments(mArgs);

        getFragmentManager().beginTransaction().addToBackStack(TAG)
                .replace(R.id.bbs_content, mFrag, BBSCashInConfirm.TAG).commit();
        ToggleKeyboard.hide_keyboard(act);
//        switchFragment(mFrag, getString(R.string.cash_in), true);
    }

    Button.OnClickListener cancellistener = view -> {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else
            getActivity().finish();
    };


    private boolean inputValidation() {
        int compare = 100;
        if (date_dob != null) {
            Date dob = null;
            Date now = null;
            try {
                dob = fromFormat.parse(date_dob);
                now = fromFormat.parse(dateNow);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (dob != null) {
                if (now != null) {
                    compare = dob.compareTo(now);
                }
            }
            Timber.d("compare date:%s", Integer.toString(compare));
        }
        if (et_noID.getText().toString().length() == 0) {
            et_noID.requestFocus();
            et_noID.setError("NIK dibutuhkan!");
            return false;
        } else if (et_noID.getText().toString().length() < 16) {
            et_noID.requestFocus();
            et_noID.setError("NIK Anda kurang lengkap!");
            return false;
        } else if (layout_sender.getVisibility() == View.VISIBLE) {
            if (et_name.getText().toString().length() == 0) {
                et_name.requestFocus();
                et_name.setError("Nama dibutuhkan!");
                return false;
            } else if (et_name.getText().toString().length() < 3) {
                et_name.requestFocus();
                et_name.setError("Nama minimal 3 karakter!");
                return false;
            }
        } else if (et_address.getText().toString().length() == 0) {
            et_address.requestFocus();
            et_address.setError("Alamat dibutuhkan!");
            return false;
        } else if (et_address.getText().toString().length() < 10) {
            et_address.requestFocus();
            et_address.setError("Alamat Anda kurang lengkap!");
            return false;
        } else if (layout_pob.getVisibility() == View.VISIBLE && layout_dob.getVisibility() == View.VISIBLE) {
            if (city_textview_autocomplete.getText().toString().trim().length() == 0) {
                city_textview_autocomplete.requestFocus();
                city_textview_autocomplete.setError("Tempat Lahir dibutuhkan!");
                return false;
            } else if (!list_name_bbs_birth_place.contains(city_textview_autocomplete.getText().toString())) {

                city_textview_autocomplete.requestFocus();
                city_textview_autocomplete.setError("Nama kota tidak ditemukan!");
                return false;
            } else if (compare == 100 || tv_dob.getText().toString().equalsIgnoreCase(getString(R.string.myprofile_text_date_click))) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Alert")
                        .setMessage(getString(R.string.myprofile_validation_date_empty))
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            } else if (compare >= 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Alert")
                        .setMessage(getString(R.string.myprofile_validation_date))
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            }
        }

        if (et_noHp.getText().toString().length() == 0) {
            et_noHp.requestFocus();
            et_noHp.setError("No. Handphone dibutuhkan!");
            return false;
        } else if (et_noHp.getText().toString().length() < 10) {
            et_noHp.requestFocus();
            et_noHp.setError("No. Handphone minimal 10 karakter!");
            return false;
        } else if (et_sumberdana.getVisibility() == View.VISIBLE && et_sumberdana.getText().toString().length() == 0) {
            et_sumberdana.requestFocus();
            et_sumberdana.setError("Sumber Dana dibutuhkan!");
            return false;
        } else if (signaturePad.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.put_a_signature), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private DatePickerDialog.OnDateSetListener dobPickerSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            dedate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
            Timber.d("masuk date picker dob");
            try {
                date_dob = fromFormat.format(toFormat2.parse(dedate));
                Timber.d("masuk date picker dob masuk tanggal : %s", date_dob);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tv_dob.setText(dedate);
        }
    };
}
