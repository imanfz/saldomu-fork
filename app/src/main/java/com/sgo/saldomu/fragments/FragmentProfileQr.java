package com.sgo.saldomu.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.MyQRActivity;
import com.sgo.saldomu.activities.UpgradeAgentActivity;
import com.sgo.saldomu.activities.UpgradeMemberActivity;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlideManager;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.RoundImageTransformation;
import com.sgo.saldomu.coreclass.ScanQRUtils;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.UploadPPModel;
import com.sgo.saldomu.utils.PickAndCameraUtil;
import com.sgo.saldomu.widgets.BaseFragment;
import com.sgo.saldomu.widgets.ProgressRequestBody;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class FragmentProfileQr extends BaseFragment implements ProgressRequestBody.UploadCallbacks {

    private View v;
    // DATA
    String sourceAcct = "", sourceAcctName = "";
    private LevelClass levelClass;
    private String reject_npwp;
    private String userID;
    private boolean is_agent = false;//saat antri untuk diverifikasi
    private boolean isUpgradeAgent = false; //saat antri untuk diverifikasi upgrade agent
    private boolean isRegisteredLevel = false;
    private ImageView custImage;
    private DateFormat fromFormat;
    private DateFormat dobFormat;
    //    private PickAndCameraUtil pickAndCameraUtil;
    private final int RESULT_GALERY = 100;
    private final int RESULT_CAMERA = 200;
    final int RC_CAMERA_STORAGE = 14;
    final int RESULT_OK = -1;
    Context context;
    Activity activity;

    // UI LAYOUT
    TextView tv_name, tv_phone_no, tv_lvl_member_value, currencyLimit, limitValue, tv_email, tv_dob, tv_version;
    CardView btn_upgrade;
    ImageView imageQR;
    ProgressDialog progdialog;
    RelativeLayout lytUpgrade, lytDetail;
    LinearLayout llBalanceDetail;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            activity = (Activity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_profile_qr, container, false);
        tv_name = v.findViewById(R.id.tv_name);
        tv_phone_no = v.findViewById(R.id.tv_phone_no);
        tv_lvl_member_value = v.findViewById(R.id.tv_lvl_member_value);
        tv_email = v.findViewById(R.id.tv_current_email);
        tv_dob = v.findViewById(R.id.tv_dob);
        tv_version = v.findViewById(R.id.tv_version);
        btn_upgrade = v.findViewById(R.id.btn_upgrade);
        imageQR = v.findViewById(R.id.iv_qr);
        lytUpgrade = v.findViewById(R.id.lyt_upgrade_detail);
        lytDetail = v.findViewById(R.id.lyt_detail);
        custImage = v.findViewById(R.id.cust_image);
        llBalanceDetail = v.findViewById(R.id.llBalanceDetail);
        currencyLimit = v.findViewById(R.id.currency_limit_value);
        limitValue = v.findViewById(R.id.limit_value);
        context = container.getContext();

        setImageProfPic();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        levelClass = new LevelClass(getActivity(), sp);
        fromFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("ID", "INDONESIA"));
        dobFormat = new SimpleDateFormat("dd-MM-yyyy", new Locale("ID", "INDONESIA"));


        initData();
        initLayout();
        checkAgent();
        setView();

        imageQR.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, MyQRActivity.class);
            intent.putExtra("sourceAcct", sourceAcct);
            intent.putExtra("sourceAcctName", sourceAcctName);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainPage) getActivity()).pickAndCameraUtil = new PickAndCameraUtil(getActivity());
    }

    private void initData() {
        if (getActivity().getIntent() != null) {
            sourceAcct = NoHPFormat.formatTo08(sp.getString(DefineValue.USERID_PHONE, ""));
            sourceAcctName = sp.getString(DefineValue.CUST_NAME, "");
//            contactCenter = sp.getString(DefineValue.LIST_CONTACT_CENTER, "");
        }

        isRegisteredLevel = sp.getBoolean(DefineValue.IS_REGISTERED_LEVEL, false);
        isUpgradeAgent = sp.getBoolean(DefineValue.IS_UPGRADE_AGENT, false);
        is_agent = sp.getBoolean(DefineValue.IS_AGENT, false);
        reject_npwp = sp.getString(DefineValue.REJECT_NPWP, "N");
    }

    private void initLayout() {
        tv_name.setText(sourceAcctName);
        tv_phone_no.setText(sourceAcct);
        tv_email.setText(sp.getString(DefineValue.PROFILE_EMAIL, ""));
        try {
            tv_dob.setText(dobFormat.format(fromFormat.parse(sp.getString(DefineValue.PROFILE_DOB, ""))));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        boolean isAgent = sp.getBoolean(DefineValue.IS_AGENT, false);
        String agentType = sp.getString(DefineValue.COMPANY_TYPE, "");
        if (isAgent) {
            if (agentType.equalsIgnoreCase(getString(R.string.LP))) {
                SpannableString content = new SpannableString(getString(R.string.lbl_member_lvl_agent));
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                tv_lvl_member_value.setText(content);
            } else {
                tv_lvl_member_value.setText(getString(R.string.lbl_member_lvl_agent));
            }
        } else
            tv_lvl_member_value.setText(getLvl());
        currencyLimit.setText(sp.getString(DefineValue.BALANCE_CCYID, ""));
        limitValue.setText(CurrencyFormat.format(sp.getString(DefineValue.BALANCE_REMAIN_LIMIT, "")));
//        setImageProfPic();


        viewOnProggressUpgrade();
        if (!sp.getBoolean(DefineValue.IS_AGENT, false)) {
            llBalanceDetail.setVisibility(View.VISIBLE);
        } else {
            llBalanceDetail.setVisibility(View.GONE);
        }

        if (!levelClass.isLevel1QAC()) {
            lytUpgrade.setVisibility(View.GONE);
            btn_upgrade.setVisibility(View.GONE);
        }


        btn_upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIsLv1();
            }
        });
        custImage.setOnClickListener(v -> {
            final String[] items = {"Choose from Gallery", "Take a Photo"};

            AlertDialog.Builder a = new AlertDialog.Builder(getActivity());
            a.setCancelable(true);
            a.setTitle("Choose Profile Picture");
            a.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, items),
                    (dialog, which) -> {
                        if (which == 0) {
                            Timber.wtf("masuk gallery");
                            ((MainPage) getActivity()).pickAndCameraUtil.chooseGallery(RESULT_GALERY);
//                            pickAndCameraUtil.chooseGallery(RESULT_GALERY);
                        } else if (which == 1) {
                            chooseCamera();
                        }
                    }
            );
            a.create();
            a.show();
        });
        tv_version.setText(getString(R.string.appname) + " " + BuildConfig.VERSION_NAME+ " (" +BuildConfig.VERSION_CODE +")");
    }

    private String getLvl() {
        int tempLvl = sp.getInt(DefineValue.LEVEL_VALUE, 1);

        if (tempLvl == 1) {
            return getString(R.string.lbl_member_lvl_silver);
        } else if (tempLvl == 2) {
            return getString(R.string.lbl_member_lvl_gold);
        }

        return "";
    }

    private void setImageProfPic() {
        String _url_profpic;
        _url_profpic = sp.getString(DefineValue.IMG_URL, null);

        Timber.wtf("url prof pic:" + _url_profpic);

        Bitmap bm = BitmapFactory.decodeResource(Objects.requireNonNull(context).getResources(), R.drawable.user_unknown_menu);
        RoundImageTransformation roundedImage = new RoundImageTransformation(bm);

        if (_url_profpic != null && _url_profpic.isEmpty()) {
            GlideManager.sharedInstance().initializeGlide(context, R.drawable.user_unknown_menu, roundedImage, custImage);
        } else {
            GlideManager.sharedInstance().initializeGlide(context, _url_profpic, roundedImage, custImage);
        }

    }

    private void viewOnProggressUpgrade() {
        btn_upgrade.setCardBackgroundColor(getResources().getColor(R.color.colorSecondaryWhiteFixed));
        btn_upgrade.setEnabled(false);

        lytUpgrade.setVisibility(View.VISIBLE);
        lytDetail.setOnClickListener(detailOnClick);
    }

    RelativeLayout.OnClickListener detailOnClick = new RelativeLayout.OnClickListener() {
        @Override
        public void onClick(View v) {
            showDialogMessage();
        }
    };

    private void showDialogMessage() {
//        final Dialog dialognya = DefinedDialog.MessageDialog(getActivity(), this.getString(R.string.upgrade_dialog_finish_title),
//                this.getString(R.string.level_dialog_finish_message) + "\n" + listAddress + "\n" +
//                        this.getString(R.string.level_dialog_finish_message_2) + "\n" + listContactPhone,
        final Dialog dialognya = DefinedDialog.MessageDialog(getActivity(), this.getString(R.string.upgrade_dialog_finish_title),
                this.getString(R.string.level_dialog_waiting),
                (v, isLongClick) -> {

                }
        );

        dialognya.setCanceledOnTouchOutside(false);
        dialognya.setCancelable(false);

        dialognya.show();
    }

    private void checkIsLv1() {
        if (levelClass.isLevel1QAC()) {
            android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(getActivity());
            builder1.setTitle(R.string.upgrade_member);
            builder1.setMessage(R.string.message_upgrade_member);
            builder1.setCancelable(true);
            builder1.setPositiveButton(
                    "Yes",
                    (dialog, id) -> switchViewUpgradeVerified());

            builder1.setNegativeButton(
                    "No",
                    (dialog, id) -> dialog.dismiss());

            android.support.v7.app.AlertDialog alert11 = builder1.create();
            alert11.show();

        }
    }

    private void switchViewUpgradeVerified() {
        Intent i = new Intent(getActivity(), UpgradeMemberActivity.class);
        startActivity(i);
    }

    @AfterPermissionGranted(RC_CAMERA_STORAGE)
    private void chooseCamera() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(context, perms)) {
            ((MainPage) getActivity()).pickAndCameraUtil.runCamera(RESULT_CAMERA);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_camera_and_storage),
                    RC_CAMERA_STORAGE, perms);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_GALERY:
                if (resultCode == RESULT_OK) {
                    new ImageCompressionAsyncTask().execute(((MainPage) getActivity()).pickAndCameraUtil.getRealPathFromURI(data.getDataString()));
                }
                break;
            case RESULT_CAMERA:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    new ImageCompressionAsyncTask().execute(((MainPage) getActivity()).pickAndCameraUtil.getRealPathFromURI(((MainPage) getActivity()).pickAndCameraUtil.getCaptureImageUri()));
                } else {
                    new ImageCompressionAsyncTask().execute(((MainPage) getActivity()).pickAndCameraUtil.getCurrentPhotoPath());
                }

//                if (resultCode == RESULT_OK && ((MainPage)getActivity()).pickAndCameraUtil.getCaptureImageUri() != null) {
//                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//                        new ImageCompressionAsyncTask().execute(((MainPage)getActivity()).pickAndCameraUtil.getRealPathFromURI(((MainPage)getActivity()).pickAndCameraUtil.getCaptureImageUri()));
//                    } else {
//                        new ImageCompressionAsyncTask().execute(((MainPage)getActivity()).pickAndCameraUtil.getCurrentPhotoPath());
//                    }
//                } else {
//                    Toast.makeText(getActivity(), "Try Again", Toast.LENGTH_LONG).show();
//                }
                break;
            default:
                break;
        }
    }

    private class ImageCompressionAsyncTask extends AsyncTask<String, Void, File> {
        @Override
        protected File doInBackground(String... params) {
            if (((MainPage) getActivity()).pickAndCameraUtil == null) {
                ((MainPage) getActivity()).pickAndCameraUtil = new PickAndCameraUtil(getActivity());
            }
            return ((MainPage) getActivity()).pickAndCameraUtil.compressImage(params[0]);
        }

        @Override
        protected void onPostExecute(File file) {
            uploadFileToServer(file);
        }
    }

    private void uploadFileToServer(File photoFile) {
        progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

        if (accessKey == null)
            accessKey = sp.getString(DefineValue.ACCESS_KEY, "");

        if (userID == null)
            userID = sp.getString(DefineValue.USERID_PHONE, "");

        HashMap<String, RequestBody> params2 = RetrofitService.getInstance()
                .getSignature2(MyApiClient.LINK_UPLOAD_PROFILE_PIC, "");

        RequestBody requestFile =
                new ProgressRequestBody(photoFile, this);

        MultipartBody.Part filePart = MultipartBody.Part.createFormData(WebParams.USER_FILE, photoFile.getName(),
                requestFile);
        RequestBody req1 = RequestBody.create(MediaType.parse("text/plain"),
                sp.getString(DefineValue.CUST_ID, ""));
        RequestBody req2 = RequestBody.create(MediaType.parse("text/plain"),
                MyApiClient.COMM_ID);

        params2.put(WebParams.USER_ID, req1);
        params2.put(WebParams.COMM_ID, req2);

        RetrofitService.getInstance().MultiPartRequest(MyApiClient.LINK_UPLOAD_PROFILE_PIC, params2, filePart,
                object -> {
                    Gson gson2 = new Gson();
                    UploadPPModel model = gson2.fromJson(object, UploadPPModel.class);

                    Log.e("sse : ", model.getImg_url());

                    String error_code = model.getError_code();
                    String error_message = model.getError_message();
                    if (error_code.equalsIgnoreCase("0000")) {
                        SecurePreferences.Editor mEditor = sp.edit();

                        mEditor.putString(DefineValue.IMG_URL, model.getImg_url());
                        mEditor.putString(DefineValue.IMG_SMALL_URL, model.getImg_small_url());
                        mEditor.putString(DefineValue.IMG_MEDIUM_URL, model.getImg_medium_url());
                        mEditor.putString(DefineValue.IMG_LARGE_URL, model.getImg_large_url());

                        mEditor.commit();

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setImageProfPic();
                            }
                        });
                    } else if (error_code.equals(WebParams.LOGOUT_CODE)) {
                        AlertDialogLogout test = AlertDialogLogout.getInstance();
                        test.showDialoginActivity(getActivity(), error_message);
                    } else if (error_code.equals(DefineValue.ERROR_9333)) {
                        Timber.d("isi response app data:" + model.getApp_data());
                        final AppDataModel appModel = model.getApp_data();
                        AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                        alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                    } else if (error_code.equals(DefineValue.ERROR_0066)) {
                        Timber.d("isi response maintenance:" + object.toString());
                        AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                        alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                    } else {
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("Upload Image");
                        alert.setMessage("Upload Image : " + error_message);
                        alert.setPositiveButton("OK", null);
                        alert.show();

                    }
                    progdialog.dismiss();
                });
    }

    @Override
    public void onProgressUpdate(int percentage) {
        Log.d("okhttp", "percentage :" + percentage);
        if (progdialog == null) {
            progdialog = DefinedDialog.CreateProgressDialog(context, "");
        }
        if (progdialog.isShowing())
            progdialog.setProgress(percentage);
    }

    private void checkAgent() {
        if (is_agent && !levelClass.isLevel1QAC() && !isUpgradeAgent) {
            lytUpgrade.setVisibility(View.GONE);
            btn_upgrade.setVisibility(View.GONE);
        } else if (is_agent && !reject_npwp.isEmpty()) {
            Intent intent1 = new Intent(getActivity(), UpgradeAgentActivity.class);
            startActivity(intent1);
        }
    }

    private void setView() {
        imageQR.setImageBitmap(ScanQRUtils.getInstance(getActivity()).generateQRCode(DefineValue.QR_TYPE_FROM_DEFAULT_ACCOUNT, sourceAcct, sourceAcctName));
        if (isShowUpgradeStatus()) {
            viewOnProggressUpgrade();
        } else {
            hideOnProgUpgrade();
        }
    }

    private boolean isShowUpgradeStatus() {
        if (levelClass.isLevel1QAC() && isRegisteredLevel) {
            return true;
        }

        if (isUpgradeAgent && !is_agent) {
            return true;
        }
        return false;
    }

    private void hideOnProgUpgrade() {
        btn_upgrade.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
        btn_upgrade.setEnabled(true);
        lytUpgrade.setVisibility(View.GONE);
    }
}
