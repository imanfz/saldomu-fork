package com.sgo.saldomu.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.google.gson.Gson;
import com.mlsdev.rximagepicker.RxImageConverters;
import com.mlsdev.rximagepicker.RxImagePicker;
import com.mlsdev.rximagepicker.Sources;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ForwardScope;
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

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import me.shaohui.advancedluban.Luban;
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
    private String isDormant;
    private boolean is_agent = false;//saat antri untuk diverifikasi
    private boolean isUpgradeAgent = false; //saat antri untuk diverifikasi upgrade agent
    private boolean isRegisteredLevel = false;
    private ImageView custImage;
    private DateFormat fromFormat;
    private DateFormat dobFormat;
    private PickAndCameraUtil pickAndCameraUtil;
    private final int RESULT_GALERY = 100;
    private final int RESULT_CAMERA = 200;
    final int RC_CAMERA_STORAGE = 14;
    final int RESULT_OK = -1;
    Context context;
    Activity activity;

    // UI LAYOUT
    TextView tv_name, tv_phone_no, tv_lvl_member_value, tv_dormant_value, currencyLimit, limitValue, tv_email, tv_dob, tv_version;
    CardView btn_upgrade;
    ImageView imageQR;
    ProgressDialog progdialog;
    RelativeLayout lytUpgrade, lytDetail;
    LinearLayout llBalanceDetail;
    Runnable runnable;

    private File fileProfPic = null, compressFile = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            activity = (Activity) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickAndCameraUtil = new PickAndCameraUtil(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_profile_qr, container, false);
        tv_name = v.findViewById(R.id.tv_name);
        tv_phone_no = v.findViewById(R.id.tv_phone_no);
        tv_lvl_member_value = v.findViewById(R.id.tv_lvl_member_value);
        tv_dormant_value = v.findViewById(R.id.tv_dormant_value);
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
        reject_npwp = sp.getString(DefineValue.REJECT_NPWP, DefineValue.STRING_NO);
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

        isDormant = sp.getString(DefineValue.IS_DORMANT, DefineValue.STRING_NO);
        if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES))
            tv_dormant_value.setVisibility(View.VISIBLE);
        else
            tv_dormant_value.setVisibility(View.GONE);

        currencyLimit.setText(sp.getString(DefineValue.BALANCE_CCYID, ""));
        limitValue.setText(CurrencyFormat.format(sp.getString(DefineValue.BALANCE_REMAIN_LIMIT, "")));
//        setImageProfPic();

        viewOnProggressUpgrade();
        if (!isAgent) {
            llBalanceDetail.setVisibility(View.VISIBLE);
        } else {
            llBalanceDetail.setVisibility(View.GONE);
        }

        if (!levelClass.isLevel1QAC()) {
            lytUpgrade.setVisibility(View.GONE);
            btn_upgrade.setVisibility(View.GONE);
        }

        btn_upgrade.setOnClickListener(v -> {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES))
                dialogDormant();
            else
                checkIsLv1();
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
//                            ((MainPage) getActivity()).pickAndCameraUtil.chooseGallery(RESULT_GALERY);
                            chooseGallery();
//                            pickAndCameraUtil.chooseGallery(RESULT_GALERY);
                        } else if (which == 1) {
                            chooseCamera();
                        }
                    }
            );
            a.create();
            a.show();
        });
        tv_version.setText(getString(R.string.appname) + " " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
    }

    private void chooseGallery() {
        PermissionX.init(this).permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .onForwardToSettings(new ForwardToSettingsCallback() {
                    @Override
                    public void onForwardToSettings(ForwardScope scope, List<String> deniedList) {
                        String message = "Please allow following permissions in settings";
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                })
                .request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                        if (allGranted) {
                            pickAndCameraUtil.chooseGallery(RESULT_GALERY);
                        }
                    }
                });
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

        Timber.wtf("url prof pic:%s", _url_profpic);

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

    RelativeLayout.OnClickListener detailOnClick = v -> showDialogMessage();

    private void showDialogMessage() {
//        final Dialog dialognya = DefinedDialog.MessageDialog(getActivity(), this.getString(R.string.upgrade_dialog_finish_title),
//                this.getString(R.string.level_dialog_finish_message) + "\n" + listAddress + "\n" +
//                        this.getString(R.string.level_dialog_finish_message_2) + "\n" + listContactPhone,
        final Dialog dialognya = DefinedDialog.MessageDialog(getActivity(), this.getString(R.string.upgrade_dialog_finish_title),
                this.getString(R.string.level_dialog_waiting),
                () -> {

                }
        );

        dialognya.setCanceledOnTouchOutside(false);
        dialognya.setCancelable(false);

        dialognya.show();
    }

    private void checkIsLv1() {
        if (levelClass.isLevel1QAC()) {
            androidx.appcompat.app.AlertDialog.Builder builder1 = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
            builder1.setTitle(R.string.upgrade_member);
            builder1.setMessage(R.string.message_upgrade_member);
            builder1.setCancelable(true);
            builder1.setPositiveButton(
                    R.string.yes,
                    (dialog, id) -> switchViewUpgradeVerified());

            builder1.setNegativeButton(
                    R.string.no,
                    (dialog, id) -> dialog.dismiss());

            androidx.appcompat.app.AlertDialog alert11 = builder1.create();
            alert11.show();

        }
    }

    private void switchViewUpgradeVerified() {
        Intent i = new Intent(getActivity(), UpgradeMemberActivity.class);
        startActivity(i);
    }

    @AfterPermissionGranted(RC_CAMERA_STORAGE)
    private void chooseCamera() {
//        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
//        if (EasyPermissions.hasPermissions(context, perms)) {
//            ((MainPage) getActivity()).pickAndCameraUtil.runCamera(RESULT_CAMERA);
//        } else {
//            EasyPermissions.requestPermissions(this, getString(R.string.rationale_camera_and_storage),
//                    RC_CAMERA_STORAGE, perms);
//        }
        PermissionX.init(this).permissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .onForwardToSettings(new ForwardToSettingsCallback() {
                    @Override
                    public void onForwardToSettings(ForwardScope scope, List<String> deniedList) {
                        String message = "Please allow following permissions in settings";
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                })
                .request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                        if (allGranted) {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                                RxImagePicker.with(getContext()).requestImage(Sources.CAMERA)
                                        .flatMap(new Function<Uri, ObservableSource<File>>() {
                                            @Override
                                            public ObservableSource<File> apply(@NonNull Uri uri) throws Exception {
                                                return RxImageConverters.uriToFile(getContext(), uri, prepareUploadFileTemp());
                                            }
                                        }).subscribe(new Consumer<File>() {
                                    @Override
                                    public void accept(@NonNull File file) throws Exception {
                                        // Do something with your file copy
                                        fileProfPic = file;
                                        convertImage();
                                    }
                                });
                            } else {
                                ((MainPage) getActivity()).pickAndCameraUtil.runCamera(RESULT_CAMERA);
                            }
                        }
                    }
                });
    }

    private void convertImage() {
        int fileSize = Integer.parseInt(String.valueOf(fileProfPic.length() / 1024));
        Log.e("TAG", "size: " + fileSize);
        if (fileSize > 500) {
            Luban.compress(getContext(), fileProfPic)
                    .setMaxSize(500)
                    .putGear(Luban.CUSTOM_GEAR)
                    .asObservable()
                    .subscribe(new Observer<File>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(File file) {
                            compressFile = file;
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            uploadFileToServer(compressFile);
                        }
                    });
        } else {
            compressFile = fileProfPic;
            uploadFileToServer(compressFile);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Handler handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
//                uploadNewProfilePicture();
                Log.e("TAG", "call: " );
                convertImage();
//                hidePbar();
                handler.removeCallbacks(this);
            }
        };

        switch (requestCode) {
            case RESULT_GALERY:
//                if (resultCode == RESULT_OK) {
//                    new ImageCompressionAsyncTask().execute(((MainPage) getActivity()).pickAndCameraUtil.getRealPathFromURI(data.getDataString()));
//                }
//                break;
                if (resultCode == RESULT_OK) {
                    try {
                        RxImageConverters.uriToFile(getContext(), data.getData(), prepareUploadFileTemp())
                                .subscribe(new Consumer<File>() {
                                    @Override
                                    public void accept(@NotNull File file) throws Exception {
                                        Log.e("TAG", "accept: " );
                                        fileProfPic = file;
                                        handler.postDelayed(runnable, 2000);
                                    }
                                });
                    } catch (IOException e) {
                        Log.e("TAG", "err" );
                        e.printStackTrace();
                    }

//                    new ImageCompressionAsyncTask().execute(pickAndCameraUtil.getRealPathFromURI(data.getDataString()));
//                    handler.postDelayed(runnable, 2000);
                }
                break;
            case RESULT_CAMERA:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    new ImageCompressionAsyncTask().execute(((MainPage) getActivity()).pickAndCameraUtil.getRealPathFromURI(((MainPage) getActivity()).pickAndCameraUtil.getCaptureImageUri()));
                } else {
                    new ImageCompressionAsyncTask().execute(((MainPage) getActivity()).pickAndCameraUtil.getCurrentPhotoPath());
                }
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

                    Timber.tag("sse : ").e(model.getImg_url());

                    String error_code = model.getError_code();
                    String error_message = model.getError_message();
                    if (error_code.equalsIgnoreCase("0000")) {
                        SecurePreferences.Editor mEditor = sp.edit();

                        mEditor.putString(DefineValue.IMG_URL, model.getImg_url());
                        mEditor.putString(DefineValue.IMG_SMALL_URL, model.getImg_small_url());
                        mEditor.putString(DefineValue.IMG_MEDIUM_URL, model.getImg_medium_url());
                        mEditor.putString(DefineValue.IMG_LARGE_URL, model.getImg_large_url());

                        mEditor.commit();

                        activity.runOnUiThread(() -> setImageProfPic());
                    } else if (error_code.equals(WebParams.LOGOUT_CODE)) {
                        AlertDialogLogout.getInstance().showDialoginActivity(getActivity(), error_message);
                    } else if (error_code.equals(DefineValue.ERROR_9333)) {
                        Timber.d("isi response app data:%s", model.getApp_data());
                        final AppDataModel appModel = model.getApp_data();
                        AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                    } else if (error_code.equals(DefineValue.ERROR_0066)) {
                        Timber.d("isi response maintenance:%s", object.toString());
                        AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
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
        Timber.tag("okhttp").d("percentage :%s", percentage);
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

        return isUpgradeAgent && !is_agent;
    }

    private void hideOnProgUpgrade() {
        btn_upgrade.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
        btn_upgrade.setEnabled(true);
        lytUpgrade.setVisibility(View.GONE);
    }

    private void dialogDormant() {
        Dialog dialognya = DefinedDialog.MessageDialog(getActivity(), getActivity().getString(R.string.title_dialog_dormant),
                getActivity().getString(R.string.message_dialog_dormant),
                () -> {
                }
        );
        dialognya.show();
    }

    public static String prepareFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        return imageFileName;
    }

    private static File createImageFile() throws IOException {
        String imageFileName = prepareFileName();
//        epassBookFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), currentDate + "_" +fileName);
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), BuildConfig.APP_ID + "Image.JPEG");
        storageDir.mkdirs();

        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                return null;
            }
        }
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpeg",         /* suffix */
                storageDir      /* directory */
        );
    }

    public static File prepareUploadFileTemp() throws IOException {

        return createImageFile();
    }
}
