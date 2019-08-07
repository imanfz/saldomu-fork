package com.sgo.saldomu.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.net.http.RequestQueue;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.JsonObject;
import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.commentModel;
import com.sgo.saldomu.Beans.likeModel;
import com.sgo.saldomu.Beans.listHistoryModel;
import com.sgo.saldomu.Beans.listTimeLineModel;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.BBSDataManager;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.FabInstance;
import com.sgo.saldomu.coreclass.JobScheduleManager;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.NotificationActionView;
import com.sgo.saldomu.coreclass.NotificationHandler;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.RootUtil;
import com.sgo.saldomu.coreclass.SMSclass;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.coreclass.UserProfileHandler;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.fcm.FCMManager;
import com.sgo.saldomu.fcm.FCMWebServiceLoader;
import com.sgo.saldomu.fcm.GooglePlayUtils;
import com.sgo.saldomu.fragments.FragMainPage;
import com.sgo.saldomu.fragments.FragTagihInput;
import com.sgo.saldomu.fragments.MyHistory;
import com.sgo.saldomu.fragments.NavigationDrawMenu;
import com.sgo.saldomu.fragments.RightSideDrawMenu;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.models.retrofit.GetMemberModel;
import com.sgo.saldomu.models.retrofit.MemberDataModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.Md5;
import com.sgo.saldomu.services.AgentShopService;
import com.sgo.saldomu.services.AppInfoService;
import com.sgo.saldomu.services.BalanceService;
import com.sgo.saldomu.services.UpdateBBSBirthPlace;
import com.sgo.saldomu.services.UpdateBBSCity;
import com.sgo.saldomu.services.UserProfileService;
import com.sgo.saldomu.widgets.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import timber.log.Timber;

import static java.security.AccessController.getContext;

/**
 * Created by Administrator on 7/11/2014.
 */
public class MainPage extends BaseActivity {

    public static final int REQUEST_FINISH = 0;//untuk Request Code dari child activity ke parent activity
    private static final int RESULT_ERROR = -1;//untuk Result Code dari child activity ke parent activity kalau error (aplikasi exit)
    public static final int RESULT_LOGOUT = 1;//untuk Result Code dari child activity ke parent activity kalau sukses (aplikasi auto logout)
    public static final int RESULT_NORMAL = 2;//untuk Result Code dari child activity ke parent activity kalau normal (aplikasi close ke parent activity)
    public static final int RESULT_BALANCE = 3;
    public static final int RESULT_NOTIF = 5;
    public static final int RESULT_BILLER = 7;
    public static final int RESULT_REFRESH_NAVDRAW = 8;
    public static final int RESULT_FIRST_TIME = 9;
    public static final int RESULT_BBS = 11;
    public static final int RESULT_BBS_MEMBER_OTP = 12;
    public static final int RESULT_BBS_STATUS = 13;
    public static final int RESULT_RETRY = 14;
    public static final int RESULT_BALANCE_COLLECTOR = 14;

    public static final int RESULT_FINISH = 99;
    public static final int ACTIVITY_RESULT = 1;

    private final static int FIRST_SCREEN_LOGIN = 1;
    private final static int FIRST_SCREEN_INTRO = 2;
    private final static int REQCODE_PLAY_SERVICE = 312;


    public static String RESULT_HOME_BALANCE = "refresh_home_balance";
    public static String HOME_BALANCE_ANIMATE = "refresh_home_btn_animate";

    private static int AmountNotif = 0;

    private String flagLogin = DefineValue.STRING_NO;
    private Fragment mContent;
    private NavigationDrawMenu mNavDrawer;
    FragTagihInput fragTagihInput;
    private DrawerLayout mDrawerLayout;
    public ActionBarDrawerToggle mDrawerToggle;
    private ProgressDialog progdialog;
    private LinearLayout mOuterRelativeContent;
    private FrameLayout mLeftDrawerRelativeLayout;
    private FrameLayout mRightDrawerRelativeLayout;
    private float lastTranslate = 0.0f;
    //    private BroadcastReceiver mRegistrationBroadcastReceiver; // gcm
    private BalanceService serviceReferenceBalance;
    private AppInfoService serviceAppInfoReference;
    private UserProfileService serviceUserProfileReference;
    private boolean isBound, isBoundAppInfo, isBoundUserProfile, agent, isForeground = false;
    public MaterialSheetFab materialSheetFab;
    AlertDialog devRootedDeviceAlertDialog;
    private Bundle savedInstanceState;
    private SMSclass smSclass;
    private String isDormant, userNameLogin, fcm_id, fcmId_encrypted;
    private BottomNavigationView bottomNavigationView;

    private LevelClass levelClass;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        SecurePreferences.Editor mEditor = sp.edit();
                        mEditor.putString(DefineValue.FCM_ID, token);
                        mEditor.putString(DefineValue.FCM_ENCRYPTED, Md5.hashMd5(token));
                        mEditor.apply();
                    }
                });

        this.savedInstanceState = savedInstanceState;
        bottomNavigationView = findViewById(R.id.home_bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(drawerListener);

        isDormant = sp.getString(DefineValue.IS_DORMANT, "N");
        userNameLogin = sp.getString(DefineValue.USER_NAME, "");
        if (isHasAppPermission())
            InitializeApp();
    }

    BottomNavigationView.OnNavigationItemSelectedListener drawerListener = item -> {
        Intent i;
        switch (item.getItemId()) {
            case R.id.menu_home:
                Fragment newFragment = new FragMainPage();
//                switchContent(newFragment, getString(R.string.appname).toUpperCase());
//                switchContent(newFragment, setGreetings());
                switchContent(newFragment, userNameLogin);
                return true;
            case R.id.menu_transfer:
                if (isDormant.equalsIgnoreCase("Y")) {
                    dialogDormant();
                } else {
                    if (levelClass.isLevel1QAC()) {
                        levelClass.showDialogLevel();
                    } else {
                        i = new Intent(MainPage.this, PayFriendsActivity.class);
                        switchActivity(i, MainPage.ACTIVITY_RESULT);
                    }
                }
                return true;
            case R.id.menu_help:
                i = new Intent(MainPage.this, ContactActivity.class);
                switchActivity(i, MainPage.ACTIVITY_RESULT);
                return true;
            case R.id.menu_profile:
                i = new Intent(MainPage.this, ActivityProfileQr.class);
                startActivity(i);
                return true;
        }
        return false;
    };

    private void dialogDormant() {
        Dialog dialognya = DefinedDialog.MessageDialog(this, getString(R.string.title_dialog_dormant),
                getString(R.string.message_dialog_dormant_),
                (v, isLongClick) -> {
                    Intent i = new Intent(MainPage.this, TopUpActivity.class);
                    i.putExtra(DefineValue.IS_ACTIVITY_FULL, true);
                    switchActivity(i, MainPage.ACTIVITY_RESULT);
                }
        );

        dialognya.show();
    }

    private void InitializeApp() {
        if (GooglePlayUtils.isGooglePlayServicesAvailable(this)) {
            if (RootUtil.isDeviceRooted()) {
                if (BuildConfig.FLAVOR.equals("production")) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainPage.this);
                    builder.setMessage("Apakah anda ingin melewati pengecekan device?")
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    initializeDashboard();
                                }
                            });
                    builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switchErrorActivity(ErrorActivity.DEVICE_ROOTED);
                        }
                    });
                    builder.setCancelable(false);
                    devRootedDeviceAlertDialog = builder.create();
                    if (!isFinishing())
                        devRootedDeviceAlertDialog.show();
                } else {
                    switchErrorActivity(ErrorActivity.DEVICE_ROOTED);
//                    initializeDashboard();
                }
            } else {
                initializeDashboard();
            }
        } else {
            switchErrorActivity(ErrorActivity.GOOGLE_SERVICE_TYPE);
        }
    }

    private void startLocationService() {
        JobScheduleManager.getInstance(this).scheduleUploadLocationService();
    }

    private void initializeDashboard() {
        if (isHasAppPermission()) {
            if (checkNotificationNotif()) {
                int type = Integer.valueOf(getIntent().getExtras().getString("type_notif"));

                FCMManager fcmManager = new FCMManager(this);
                Intent intent = fcmManager.checkingAction(type);
                startActivity(intent);
            } else if (checkNotificationAction()) {
                int type = Integer.valueOf(getIntent().getExtras().getString("type"));

                Map<String, String> msgMap = new HashMap<String, String>();
                Intent intentData = getIntent();
                if (intentData.hasExtra("model_notif")) {
                    msgMap.put("model_notif", intentData.getStringExtra("model_notif"));
                }
                if (intentData.hasExtra("options")) {
                    msgMap.put("options", intentData.getStringExtra("options"));
                }
                Timber.d("testing :" + msgMap.toString());

                FCMManager fcmManager = new FCMManager(this);
                Intent intent = fcmManager.checkingAction(type, msgMap);
            }

            if (!isLogin()) {
                Bundle bundle = getIntent().getExtras();
                if (bundle != null) {
                    if (bundle.getString(DefineValue.MODEL_NOTIF) != null) {
                        int modelNotif = Integer.valueOf(bundle.getString(DefineValue.MODEL_NOTIF));
                        if (modelNotif == 2) {
                            SecurePreferences.Editor mEditor = sp.edit();
                            mEditor.putString(DefineValue.MODEL_NOTIF, Integer.toString(modelNotif));
                            mEditor.apply();
                        }
                    }
                }
                openFirstScreen(FIRST_SCREEN_INTRO);
            } else {
                initializeLogin();
            }
        }
    }

    @Override
    public void onGranted() {
        super.onGranted();
        InitializeApp();
    }

    @Override
    public void onDeny() {
        super.onDeny();
        Toast.makeText(this, getString(R.string.cancel_permission), Toast.LENGTH_SHORT).show();
        this.finish();
    }

    void initializeLogin() {
//        Boolean isSimSame = true;
//        if (BuildConfig.FLAVOR.equals("production")){
//            if(smSclass == null)
//                smSclass = new SMSclass(this);
//
//            isSimSame = smSclass.isSimSameSP();
//        }
//
//        if(isSimSame) {
        showProgLoading(getString(R.string.initialize), false);

        startLocationService();

//            if (savedInstanceState != null)
//                mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");

        levelClass = new LevelClass(this, sp);
        isForeground = true;
        agent = sp.getBoolean(DefineValue.IS_AGENT, false);
        UtilsLoader utilsLoader = new UtilsLoader(this, sp);
        utilsLoader.getAppVersion();
        ActiveAndroid.initialize(this);
        InitializeNavDrawer();
        setupFab();
        FCMWebServiceLoader.getInstance(this).sentTokenAtLogin(false, userPhoneID, sp.getString(DefineValue.PROFILE_EMAIL, ""));

        AlertDialogLogout.getInstance();    //inisialisasi alertdialoglogout


        if (checkNotificationAction()) {
            int type = Integer.valueOf(getIntent().getExtras().getString("type"));

            Map<String, String> msgMap = new HashMap<String, String>();
            Intent intentData = getIntent();
            if (intentData.hasExtra("model_notif")) {
                msgMap.put("model_notif", intentData.getStringExtra("model_notif"));
            }
            if (intentData.hasExtra("options")) {
                msgMap.put("options", intentData.getStringExtra("options"));
            }
            Timber.d("testing :" + msgMap.toString());

            FCMManager fcmManager = new FCMManager(this);
            Intent intent = fcmManager.checkingAction(type, msgMap);
            if (intent != null) {
                startActivity(intent);
            }
            //this.finish();
        } else {
            String sp_model_notif = sp.getString(DefineValue.MODEL_NOTIF, "");
            if (!sp_model_notif.equals("")) {
                if (sp_model_notif.equals("2")) {
                    Intent i = new Intent(this, MyProfileNewActivity.class);
                    startActivity(i);
                }
                sp.edit().remove(DefineValue.MODEL_NOTIF).apply();
            }
        }

        String notifDataNextLogin = sp.getString(DefineValue.NOTIF_DATA_NEXT_LOGIN, "");
        if (!notifDataNextLogin.equals("")) {

            changeActivityNextLogin(notifDataNextLogin);

        }
//        }
//        else {
//            Logout(FIRST_SCREEN_INTRO);
//        }
    }

    void changeActivityNextLogin(String jsonStr) {

        Intent i;
        Bundle bundle = new Bundle();
        try {
            JSONObject jsonObj = new JSONObject(jsonStr);

            int modelNotif = jsonObj.getInt("model_notif");

            if (modelNotif != FCMManager.SHOP_ACCEPT_TRX && modelNotif != FCMManager.MEMBER_RATING_TRX
                    && modelNotif != FCMManager.AGENT_LOCATION_MEMBER_REQ_TRX_TO_AGENT
                    && modelNotif != FCMManager.AGENT_LOCATION_KEY_ACCEPT_TRANSACTION
                    && modelNotif != FCMManager.SHOP_NOTIF_TRANSACTION
                    && modelNotif != FCMManager.SHOP_ACCEPT_TRX
                    && modelNotif != FCMManager.MEMBER_CONFIRM_CASHOUT_TRANSACTION) {
                sp.edit().remove(DefineValue.NOTIF_DATA_NEXT_LOGIN).commit();
            }

            switch (modelNotif) {
                case FCMManager.AGENT_LOCATION_SET_SHOP_LOCATION:
                    i = new Intent(this, BbsMemberLocationActivity.class);

                    bundle.putString("memberId", jsonObj.getString("memberId"));
                    bundle.putString("shopId", jsonObj.getString("shopId"));
                    bundle.putString("shopName", jsonObj.getString("shopName"));
                    bundle.putString("memberType", jsonObj.getString("memberType"));
                    bundle.putString("memberName", jsonObj.getString("memberName"));
                    bundle.putString("commName", jsonObj.getString("commName"));

                    bundle.putString("province", jsonObj.getString("province"));
                    bundle.putString("district", jsonObj.getString("district"));
                    bundle.putString("address", jsonObj.getString("address"));
                    bundle.putString("category", jsonObj.getString("category"));
                    bundle.putString("isMobility", jsonObj.getString("isMobility"));
                    i.putExtras(bundle);
                    break;
                case FCMManager.AGENT_LOCATION_MEMBER_REQ_TRX_TO_AGENT:
                    i = new Intent(this, BBSActivity.class);
                    i.putExtra(DefineValue.INDEX, BBSActivity.BBSTRXAGENT);
                    break;
                case FCMManager.AGENT_LOCATION_KEY_REJECT_TRANSACTION:
                    i = new Intent(this, BbsSearchAgentActivity.class);

                    bundle.putString(DefineValue.CATEGORY_ID, jsonObj.getString(DefineValue.CATEGORY_ID));
                    bundle.putString(DefineValue.CATEGORY_NAME, jsonObj.getString(DefineValue.CATEGORY_NAME));
                    bundle.putString(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_NO);
                    bundle.putString(DefineValue.AMOUNT, jsonObj.getString(DefineValue.AMOUNT));
                    bundle.putString(DefineValue.IS_AUTOSEARCH, DefineValue.STRING_YES);
                    bundle.putDouble(DefineValue.LAST_CURRENT_LATITUDE, jsonObj.getDouble(DefineValue.LAST_CURRENT_LATITUDE));
                    bundle.putDouble(DefineValue.LAST_CURRENT_LONGITUDE, jsonObj.getDouble(DefineValue.LAST_CURRENT_LONGITUDE));

                    i.putExtras(bundle);

                    break;
                case FCMManager.AGENT_LOCATION_SHOP_REJECT_TRANSACTION:
                    i = new Intent(this, MainPage.class);
                    break;
                case FCMManager.MEMBER_CONFIRM_CASHOUT_TRANSACTION:
                    i = new Intent(this, BBSActivity.class);
                    bundle.putInt(DefineValue.INDEX, BBSActivity.CONFIRMCASHOUT);
                    i.putExtras(bundle);
                    break;
                case FCMManager.SHOP_ACCEPT_TRX:
                    i = new Intent(this, BbsMapViewByMemberActivity.class);

                    bundle.putString(DefineValue.BBS_TX_ID, jsonObj.getString(DefineValue.BBS_TX_ID));
                    bundle.putString(DefineValue.CATEGORY_NAME, jsonObj.getString(DefineValue.CATEGORY_NAME));
                    bundle.putString(DefineValue.AMOUNT, jsonObj.getString(DefineValue.AMOUNT));


                    i.putExtras(bundle);

                    break;
                case FCMManager.SHOP_NOTIF_TRANSACTION:
                    i = new Intent(this, BBSActivity.class);

                    bundle.putInt(DefineValue.INDEX, BBSActivity.TRANSACTION);
                    bundle.putString(DefineValue.TYPE, jsonObj.getString(DefineValue.TYPE));
                    bundle.putString(DefineValue.AMOUNT, jsonObj.getString(DefineValue.AMOUNT));
                    bundle.putString(DefineValue.KEY_CODE, jsonObj.getString(DefineValue.KEY_CODE));

                    i.putExtras(bundle);

                    break;
                case FCMManager.AGENT_LOCATION_KEY_ACCEPT_TRANSACTION:
                    i = new Intent(this, BBSActivity.class);

                    bundle.putInt(DefineValue.INDEX, BBSActivity.TRANSACTION);
                    bundle.putString(DefineValue.TYPE, jsonObj.getString(DefineValue.TYPE));
                    bundle.putString(DefineValue.AMOUNT, jsonObj.getString(DefineValue.AMOUNT));
                    bundle.putString(DefineValue.KEY_CODE, jsonObj.getString(DefineValue.KEY_CODE));

                    i.putExtras(bundle);


                    break;
                case FCMManager.MEMBER_RATING_TRX:
                    i = new Intent(this, BBSActivity.class);
                    bundle.putInt(DefineValue.INDEX, BBSActivity.BBSRATINGBYMEMBER);
                    bundle.putString(DefineValue.BBS_TX_ID, jsonObj.getString(WebParams.TX_ID));
                    bundle.putString(DefineValue.CATEGORY_NAME, jsonObj.getString(WebParams.CATEGORY_NAME));
                    bundle.putString(DefineValue.AMOUNT, jsonObj.getString(WebParams.AMOUNT));
                    bundle.putString(DefineValue.URL_PROFILE_PICTURE, jsonObj.getString(WebParams.PROFILE_PICTURE));
                    bundle.putString(DefineValue.BBS_SHOP_NAME, jsonObj.getString(WebParams.SHOP_NAME));
                    bundle.putString(DefineValue.BBS_MAXIMUM_RATING, jsonObj.getString(WebParams.MAXIMUM_RATING));
                    bundle.putString(DefineValue.BBS_DEFAULT_RATING, jsonObj.getString(WebParams.DEFAULT_RATING));
                    i.putExtras(bundle);
                    break;
                case FCMManager.BLAST_INFO:
                    i = new Intent(this, MainPage.class);
                    break;
                default:
                    i = new Intent(this, MainPage.class);
                    break;
            }

            startActivity(i);

        } catch (JSONException e) {
            Timber.d("Json parsing error: " + e.getMessage());
        }


    }

    void switchErrorActivity(int type) {
        Intent i = new Intent(this, ErrorActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        i.putExtra(DefineValue.TYPE, type);
        startActivity(i);
    }

    boolean checkNotificationAction() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Timber.d("masuk check notification " + extras.toString());
            if (extras.containsKey("type")) {
                return true;
            }
        }
        return false;
    }

    boolean checkNotificationNotif() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Timber.d("masuk check notification " + extras.toString());
            if (extras.containsKey("type_notif")) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main_page;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    private ServiceConnection myServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // called when the connection with the service has been
            // established. gives us the service object to use so we can
            // interact with the service.we have bound to a explicit
            // service that we know is running in our own process, so we can
            // cast its IBinder to a concrete class and directly access it.
            Timber.i("Main page service connection Balance Bound service connected");
            serviceReferenceBalance = ((BalanceService.MyLocalBinder) service).getService();
            serviceReferenceBalance.setMainPageContext(MainPage.this);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // called when the connection with the service has been
            // unexpectedly disconnected -- its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            Timber.i("Main Page service connection Balance Problem: bound service disconnected");
            serviceReferenceBalance = null;
            isBound = false;
        }
    };

    private ServiceConnection AppInfoServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // called when the connection with the service has been
            // established. gives us the service object to use so we can
            // interact with the service.we have bound to a explicit
            // service that we know is running in our own process, so we can
            // cast its IBinder to a concrete class and directly access it.
            Timber.i("Main page service connection AppVersion Bound service connected");
            serviceAppInfoReference = ((AppInfoService.MyLocalBinder) service).getService();
            serviceAppInfoReference.setMainPageContext(MainPage.this);
            isBoundAppInfo = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // called when the connection with the service has been
            // unexpectedly disconnected -- its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            Timber.i("Main Page service connection AppVersion Problem: bound service disconnected");
            serviceAppInfoReference = null;
            isBoundAppInfo = false;
        }
    };

    private ServiceConnection UserProfileServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // called when the connection with the service has been
            // established. gives us the service object to use so we can
            // interact with the service.we have bound to a explicit
            // service that we know is running in our own process, so we can
            // cast its IBinder to a concrete class and directly access it.
            Timber.i("Main page service connection UserProfile Bound service connected");
            serviceUserProfileReference = ((UserProfileService.MyLocalBinder) service).getService();
            serviceUserProfileReference.setMainPageContext(MainPage.this);
            isBoundUserProfile = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // called when the connection with the service has been
            // unexpectedly disconnected -- its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            Timber.i("Main Page service connection UserProfile Problem: bound service disconnected");
            serviceUserProfileReference = null;
            isBoundUserProfile = false;
        }
    };

    private void doUnbindService() {
        Timber.i("Main Page service connection Unbind ........");
        unbindService(myServiceConnection);
        isBound = false;
    }

    //    bind to the service
    private void doBindToService() {
        Timber.i("Main Page service connection Binding ........");
        if (!isBound) {
            Timber.i("Main Page service connection Masuk binding");
            Intent bindIntent = new Intent(this, BalanceService.class);
            isBound = bindService(bindIntent, myServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    private void doUnbindAppInfoService() {
        Timber.i("Main Page service connection AppInfo Unbind ........");
        unbindService(AppInfoServiceConnection);
        isBoundAppInfo = false;
    }

    //    bind to the service
    private void doBindToAppInfoService() {
        Timber.i("Main Page service connection AppInfo Binding ........");
        if (!isBoundAppInfo) {
            Timber.i("Main Page service connection AppInfo Masuk binding");
            Intent bindIntent = new Intent(this, AppInfoService.class);
            isBoundAppInfo = bindService(bindIntent, AppInfoServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }


    //    bind to the service
    private void doBindToUserProfileService() {
        Timber.i("Main Page service connection UserProfile Binding ........");
        if (!isBoundUserProfile) {
            Timber.i("Main Page service connection UserProfile Masuk binding");
            Intent bindIntent = new Intent(this, UserProfileService.class);
            isBoundUserProfile = bindService(bindIntent, UserProfileServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void doUnbindUserProfileService() {
        Timber.i("Main Page service connection UserProfile Unbind ........");
        unbindService(UserProfileServiceConnection);
        isBoundUserProfile = false;
    }

    BroadcastReceiver btnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mNavDrawer.getBalance(true);
        }
    };

    private void InitializeNavDrawer() {
        mDrawerLayout = findViewById(R.id.main_drawer);
        mLeftDrawerRelativeLayout = findViewById(R.id.left_drawer);
//        mRightDrawerRelativeLayout = findViewById(R.id.right_drawer);
        mDrawerLayout.setScrimColor(getResources().getColor(R.color.transparent));
        mOuterRelativeContent = findViewById(R.id.outer_layout_content);
//        findViewById(R.id.layout_include_fab).setVisibility(View.VISIBLE);
        //buat lock gesture slide
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x - 150;
        int height = size.y;

//        mRightDrawerRelativeLayout.getLayoutParams().width = width;
//        mRightDrawerRelativeLayout.getLayoutParams().height = height;


        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, getToolbar(), R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                ToggleKeyboard.hide_keyboard(MainPage.this);

//                if (drawerView == mLeftDrawerRelativeLayout)
//                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mRightDrawerRelativeLayout);
//                else
//                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mLeftDrawerRelativeLayout);
            }

            @Override
            public boolean onOptionsItemSelected(MenuItem item) {
                return super.onOptionsItemSelected(item);
            }

            @Override
            public void setToolbarNavigationClickListener(View.OnClickListener onToolbarNavigationClickListener) {
                super.setToolbarNavigationClickListener(onToolbarNavigationClickListener);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                float moveFactor = (drawerView.getWidth() * slideOffset);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    if (drawerView == mLeftDrawerRelativeLayout)
                        mOuterRelativeContent.setTranslationX(moveFactor);
                    else if (drawerView == mRightDrawerRelativeLayout)
                        mOuterRelativeContent.setTranslationX(-moveFactor);
                } else {
                    TranslateAnimation anim = null;

                    if (drawerView == mLeftDrawerRelativeLayout)
                        anim = new TranslateAnimation(lastTranslate, moveFactor, 0.0f, 0.0f);
//                    else if (drawerView == mRightDrawerRelativeLayout)
//                        anim = new TranslateAnimation(lastTranslate, -moveFactor, 0.0f, 0.0f);

                    if (anim != null) {
                        anim.setDuration(0);
                        anim.setFillAfter(true);
                        mOuterRelativeContent.startAnimation(anim);
                    }

                    lastTranslate = moveFactor;
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
//                if (drawerView == mLeftDrawerRelativeLayout)
//                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, mRightDrawerRelativeLayout);
//                else
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, mLeftDrawerRelativeLayout);
            }
        };
        mDrawerToggle.syncState();
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerLayout.setActivated(false);
        disableHomeIcon();
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        getDataListMember();
        mNavDrawer = new NavigationDrawMenu();
        getSupportFragmentManager().beginTransaction().replace(R.id.left_menu_layout, mNavDrawer, NavigationDrawMenu.TAG).commitAllowingStateLoss();
    }


//    private void refreshPromo() {
//        RightSideDrawMenu mRightDrawer = (RightSideDrawMenu) getSupportFragmentManager().findFragmentById(R.id.main_list_menu_fragment_right);
//        if (mRightDrawer != null)
//            mRightDrawer.autoRefreshList();
//    }

    private void initializeNavDrawer() {
        if (mNavDrawer != null) {
            mNavDrawer.initializeNavDrawer();
            mNavDrawer.getBalance(true);
        }
    }

    private void getDataListMember() {

        if (sp.getString(DefineValue.BBS_MODULE, "").equals(DefineValue.BBS_REVIEW)) {
            //do validate before redirect to rating
            Intent tempIntent = new Intent(getApplicationContext(), BBSActivity.class);
            Bundle tempBundle = new Bundle();
            tempBundle.putInt(DefineValue.INDEX, BBSActivity.BBSRATINGBYMEMBER);
            tempBundle.putString(DefineValue.BBS_TX_ID, sp.getString(DefineValue.BBS_TX_ID, ""));
            tempBundle.putString(DefineValue.CATEGORY_NAME, sp.getString(DefineValue.CATEGORY_NAME, ""));
            tempBundle.putString(DefineValue.AMOUNT, sp.getString(DefineValue.AMOUNT, ""));
            tempBundle.putString(DefineValue.URL_PROFILE_PICTURE, sp.getString(DefineValue.URL_PROFILE_PICTURE, ""));
            tempBundle.putString(DefineValue.BBS_SHOP_NAME, sp.getString(DefineValue.BBS_SHOP_NAME, ""));
            tempBundle.putString(DefineValue.BBS_MAXIMUM_RATING, sp.getString(DefineValue.BBS_MAXIMUM_RATING, ""));
            tempBundle.putString(DefineValue.BBS_DEFAULT_RATING, sp.getString(DefineValue.BBS_DEFAULT_RATING, ""));
            tempIntent.putExtras(tempBundle);
            switchActivity(tempIntent, ACTIVITY_RESULT);
        }

        try {

            String cust_id = sp.getString(DefineValue.CUST_ID, "");

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_LIST_MEMBER, MyApiClient.COMM_ID_PULSA);
            params.put(WebParams.COMM_ID, commIDLogin);
            params.put(WebParams.CUST_ID, cust_id);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID_PULSA, MyApiClient.COMM_ID_PULSA);
            params.put(WebParams.ACCESS_KEY, sp.getString(DefineValue.ACCESS_KEY, ""));

            Timber.d("isi params listmember mainpage:" + params.toString());

            RetrofitService.getInstance().PostObjectRequestDebounce(MyApiClient.LINK_LIST_MEMBER, params
                    , new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            GetMemberModel model = RetrofitService.getInstance().getGson().fromJson(object, GetMemberModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                if (!model.getMemberData().isEmpty()) {

                                    MemberDataModel memberModel = model.getMemberData().get(0);

                                    SecurePreferences.Editor mEditor = sp.edit();
                                    mEditor.putString(DefineValue.MEMBER_CODE, memberModel.getMember_code());
                                    mEditor.putString(DefineValue.MEMBER_ID, memberModel.getMember_id());
                                    mEditor.putString(DefineValue.MEMBER_NAME, memberModel.getMember_name());
                                    mEditor.apply();

                                    if (mNavDrawer != null && serviceReferenceBalance != null)
                                        serviceReferenceBalance.runBalance();

                                    initializeNavDrawer();
                                    CheckNotification();

                                    String is_new_bulk = sp.getString(DefineValue.IS_NEW_BULK, "N");

                                    if (is_new_bulk.equalsIgnoreCase(DefineValue.STRING_YES)) {
                                        UserProfileHandler mBH = new UserProfileHandler(getApplication());
                                        mBH.sentUserProfile(new OnLoadDataListener() {
                                            @Override
                                            public void onSuccess(Object deData) {

                                                checkField();
                                            }

                                            @Override
                                            public void onFail(Bundle message) {

                                            }

                                            @Override
                                            public void onFailure(String message) {

                                            }
                                        }, is_new_bulk);
                                    } else {

                                        checkField();
                                    }

                                    setupBBSData();
                                    try {
                                        JSONObject shopAgentObject = new JSONObject(sp.getString(DefineValue.SHOP_AGENT_DATA, ""));

                                        if (!shopAgentObject.optString("member_id", "").equalsIgnoreCase("")
                                                && sp.getString(DefineValue.IS_AGENT_SET_LOCATION, "").equals(DefineValue.STRING_NO)) {
                                            Intent intent = new Intent(MainPage.this, BbsMemberLocationActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            intent.putExtra("memberId", shopAgentObject.optString("member_id", ""));
                                            intent.putExtra("shopId", shopAgentObject.optString("shop_id", ""));
                                            intent.putExtra("shopName", shopAgentObject.optString("shop_name", ""));
                                            intent.putExtra("memberType", shopAgentObject.optString("member_type", ""));
                                            intent.putExtra("memberName", shopAgentObject.optString("member_name", ""));
                                            intent.putExtra("commName", shopAgentObject.optString("comm_name", ""));
                                            intent.putExtra("province", shopAgentObject.optString("province", ""));
                                            intent.putExtra("district", shopAgentObject.optString("district", ""));
                                            intent.putExtra("address", shopAgentObject.optString("address1", ""));
                                            intent.putExtra("category", "");
                                            intent.putExtra("isMobility", shopAgentObject.optString("is_mobility", ""));
                                            switchActivity(intent, ACTIVITY_RESULT);
                                        } else if (!shopAgentObject.optString("member_id", "").equalsIgnoreCase("")
                                                && sp.getString(DefineValue.IS_AGENT_SET_OPENHOUR, "").equals(DefineValue.STRING_NO)) {
                                            Bundle bundle = new Bundle();
                                            bundle.putInt(DefineValue.INDEX, BBSActivity.BBSWAKTUBEROPERASI);

                                            Intent intent = new Intent(MainPage.this, BBSActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            intent.putExtras(bundle);
                                            startActivityForResult(intent, MainPage.RESULT_REFRESH_NAVDRAW);
                                            finish();
                                        }


                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
//                                    }
//                                    else if (!sp.getString(DefineValue.SHOP_AGENT_DATA, "").equals("") && sp.getString(DefineValue.IS_AGENT_SET_OPENHOUR, "").equals(DefineValue.STRING_NO)) {
//                                        try {
//
//
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                    }

                                } else {

                                    Toast.makeText(MainPage.this, "List Member is Empty", Toast.LENGTH_LONG).show();

                                }

                                String member_dap = RetrofitService.getInstance().getGson().toJson(model.getMember_dap());
                                SecurePreferences.Editor mEditor = sp.edit();
                                mEditor.putString(DefineValue.MEMBER_DAP, member_dap);
                                mEditor.apply();


                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                Timber.d("isi response autologout:" + model.getError_message());

                                String message = model.getError_message();

                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginMain(MainPage.this, message);
                            } else {
                                Timber.d("Error ListMember comlist:" + model.getError_message());
                                code = model.getError_message();
                                Toast.makeText(MainPage.this, code, Toast.LENGTH_LONG).show();

                            }

                            hideProgLoading();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            sentLogout();
//                            getDataListMember();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });


        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    void setupBBSData() {
        boolean isAgent = sp.getBoolean(DefineValue.IS_AGENT, false);
        if (isAgent) {
            RealmManager.getInstance().setBbsRealm(RealmManager.getRealmBBS());
            RealmManager.getInstance().setRealm(Realm.getDefaultInstance());
            callBBSCityService();
            checkAndRunServiceBBS();
            callAgentShopService();
            callBBSBirthPlaceService();
        } else {

        }
    }

    /**
     * Check jika bisa menjalankan ServiceUpdateData langsung
     * Check jika MustUpdate, IsSameUser, dan IsUpdated
     */
    void checkAndRunServiceBBS() {
        BBSDataManager bbsDataManager = new BBSDataManager();
        if (bbsDataManager.isValidToUpdate()) {
            bbsDataManager.runServiceUpdateData(this);
            Timber.d("Run Service update data BBS");
        }
    }

    private void CheckNotification() {
        Thread mth = new Thread() {
            @Override
            public void run() {
                NotificationHandler mNoHand = new NotificationHandler(MainPage.this, sp);
                mNoHand.sentRetrieveNotif();
            }
        };
        mth.start();
    }

    private void callBBSCityService() {
        Timber.d("Panggil service BBS City");
        UpdateBBSCity.startUpdateBBSCity(MainPage.this);
    }

    private void callBBSBirthPlaceService() {
        Timber.d("Panggil service BBS Birth Place");
        UpdateBBSBirthPlace.startUpdateBBSBirthPlace(MainPage.this);
    }

    private void showChangePassword() {
        Intent i = new Intent(this, ChangePassword.class);
        i.putExtra(DefineValue.IS_FIRST, DefineValue.YES);
        switchActivity(i, ACTIVITY_RESULT);
    }

    private void showMyProfile() {
        Intent i = new Intent(this, MyProfileNewActivity.class);
//        i.putExtra(DefineValue.IS_FIRST, DefineValue.YES);
        switchActivity(i, ACTIVITY_RESULT);
    }

    private void showCreatePin() {
        Intent i = new Intent(this, CreatePIN.class);
        switchActivity(i, MainPage.ACTIVITY_RESULT);

    }

    private void checkField() {
        if (sp.getString(DefineValue.IS_CHANGED_PASS, "").equals(DefineValue.STRING_NO)) {
            showChangePassword();
        } else if (sp.getString(DefineValue.IS_HAVE_PIN, "").equalsIgnoreCase(DefineValue.STRING_NO)) {
            showCreatePin();
        } else if (levelClass.isLevel1QAC() && sp.getString(DefineValue.IS_FIRST, "").equalsIgnoreCase(DefineValue.YES)) {
            showMyProfile();
        }
//        else if(sp.getString(DefineValue.IS_NEW_BULK,"N").equalsIgnoreCase(DefineValue.STRING_YES)){
//            showValidasiEmail();
//        }
    }


//----------------------------------------------------------------------------------------------------------------


    private void openFirstScreen(int index) {
        Intent i;
        switch (index) {
            case FIRST_SCREEN_LOGIN:
                i = new Intent(this, LoginActivity.class);
                break;
            case FIRST_SCREEN_INTRO:
                i = new Intent(this, Introduction.class);
                break;
            default:
                i = new Intent(this, LoginActivity.class);
                break;
        }
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        this.finish();
    }


    public void setNotifAmount(String _amount) {
        AmountNotif = Integer.parseInt(_amount);
        invalidateOptionsMenu();
    }

    public void switchContent(Fragment mFragment, String fragName) {
        mContent = mFragment;

//        materialSheetFab.showFab();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_page_content, mContent, fragName)
                .commitAllowingStateLoss();
        setActionBarTitle(fragName);
        if (mDrawerLayout != null) mDrawerLayout.closeDrawer(mLeftDrawerRelativeLayout);
    }

    public void switchContent(Fragment mFragment, String fragName, Boolean isBackstack) {

        if (isBackstack) {
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_page_content, mFragment, fragName)
                    .addToBackStack(null)
                    .commit();
        } else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_page_content, mFragment, fragName)
                    .commit();

        }
        setActionBarTitle(fragName);
        if (mDrawerLayout != null) mDrawerLayout.closeDrawer(mLeftDrawerRelativeLayout);
    }

    public void switchActivity(Intent mIntent, int activity_type) {
        switch (activity_type) {
            case ACTIVITY_RESULT:
                startActivityForResult(mIntent, REQUEST_FINISH);
                break;
            case 2:
                break;
        }
    }

    public void switchMenu(int IdxItemMenu, Bundle data) {
        mNavDrawer.selectItem(IdxItemMenu, data);
    }

    public void switchLogout() {
        sentLogout();
    }

    private void Logout(int logoutTo) {

        String balance = sp.getString(DefineValue.BALANCE_AMOUNT, "");
        String contact_first_time = sp.getString(DefineValue.CONTACT_FIRST_TIME, "");
        deleteData();
        SecurePreferences.Editor mEditor = sp.edit();
        mEditor.putString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
        if (sp.getString(DefineValue.IS_POS, "N").equals(DefineValue.N)) {
            mEditor.putString(DefineValue.PREVIOUS_LOGIN_USER_ID, userPhoneID);
        }else
            mEditor.putString(DefineValue.PREVIOUS_LOGIN_USER_ID, "");
        mEditor.putString(DefineValue.PREVIOUS_BALANCE, balance);
        mEditor.putString(DefineValue.PREVIOUS_CONTACT_FIRST_TIME, contact_first_time);

        mEditor.putString(DefineValue.IS_AGENT_APPROVE, "");
        mEditor.putString(DefineValue.AGENT_NAME, "");
        mEditor.putString(DefineValue.AGENT_SHOP_CLOSED, "");
        mEditor.putString(DefineValue.BBS_MEMBER_ID, "");
        mEditor.putString(DefineValue.BBS_SHOP_ID, "");
        mEditor.putString(DefineValue.IS_AGENT_SET_LOCATION, "");
        mEditor.putString(DefineValue.IS_AGENT_SET_OPENHOUR, "");
        mEditor.putString(DefineValue.SHOP_AGENT_DATA, "");
        mEditor.putString(DefineValue.IS_MEMBER_SHOP_DGI, "");
        mEditor.putString(DefineValue.IS_POS, "");
        mEditor.remove(DefineValue.IS_DORMANT);
        mEditor.remove(DefineValue.IS_REGISTERED_LEVEL);
        mEditor.remove(DefineValue.CATEGORY);
        mEditor.remove(DefineValue.SAME_BANNER);
        mEditor.remove(DefineValue.DATA_BANNER);
        mEditor.remove(DefineValue.IS_POS);

        //di commit bukan apply, biar yakin udah ke di write datanya
        mEditor.commit();
        openFirstScreen(logoutTo);
    }

    private void sentLogout() {
        try {
            showProgLoading("", true);

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_LOGOUT);
//            RequestParams params = MyApiClient.getInstance().getSignatureWithParams(MyApiClient.LINK_LOGOUT);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userPhoneID);

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_LOGOUT, params
                    , new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            jsonModel model = RetrofitService.getInstance().getGson().fromJson(object, jsonModel.class);

                            if (model.getError_code().equals(WebParams.SUCCESS_CODE)) {
                                //stopService(new Intent(MainPage.this, UpdateLocationService.class));
                                Logout(FIRST_SCREEN_INTRO);

                            } else {
                                Toast.makeText(MainPage.this, model.getError_message(), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            MainPage.this.finish();
                        }

                        @Override
                        public void onComplete() {
                            hideProgLoading();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("isi request code:" + String.valueOf(requestCode));
        Timber.d("isi result Code:" + String.valueOf(resultCode));
        if (requestCode == REQUEST_FINISH) {
            if (resultCode == RESULT_LOGOUT) {
                switchLogout();
            }
            if (resultCode == RESULT_ERROR) {
                this.finish();
            }
            if (resultCode == RESULT_BALANCE) {
                Timber.w("Masuk result Balance");
                mNavDrawer.getBalance(true);
            }
            if (resultCode == RESULT_BALANCE_COLLECTOR) {
                Timber.w("Masuk result Balance collector");
                fragTagihInput.getBalanceCollector();
            }
            if (resultCode == RESULT_NOTIF) {
                Timber.w("Masuk result notif");
                CheckNotification();
                invalidateOptionsMenu();
                if (data != null) {
                    int _type = data.getIntExtra(DefineValue.NOTIF_TYPE, 0);
                    switch (_type) {
                        case NotificationActivity.TYPE_TRANSFER:
                            Bundle dataBundle = new Bundle();
                            dataBundle.putString(DefineValue.AMOUNT, data.getStringExtra(DefineValue.AMOUNT));
                            dataBundle.putString(DefineValue.CUST_NAME, data.getStringExtra(DefineValue.CUST_NAME));
                            dataBundle.putString(DefineValue.MESSAGE, data.getStringExtra(DefineValue.MESSAGE));
                            dataBundle.putString(DefineValue.USERID_PHONE, data.getStringExtra(DefineValue.USERID_PHONE));
                            dataBundle.putString(DefineValue.TRX, data.getStringExtra(DefineValue.TRX));
                            dataBundle.putString(DefineValue.REQUEST_ID, data.getStringExtra(DefineValue.REQUEST_ID));
                            dataBundle.putBoolean(DefineValue.CONFIRM_PAYFRIEND, true);

                            mNavDrawer.selectItem(NavigationDrawMenu.MPAYFRIENDS, dataBundle);
                            break;
                        case NotificationActivity.TYPE_LIKE:
                        case NotificationActivity.TYPE_COMMENT:
                            int _post_id = Integer.valueOf(data.getExtras().getString(DefineValue.POST_ID, "0"));
                            if (mContent instanceof FragMainPage) {
                                FragMainPage mFrag = (FragMainPage) mContent;
                                if (mFrag.getFragment(0) instanceof MyHistory) {
                                    MyHistory _history = (MyHistory) mFrag.getFragment(0);
                                    _history.ScrolltoItem(_post_id);
                                }
                            }
                            Intent i = new Intent(this, HistoryDetailActivity.class);
                            i.putExtras(data);
                            switchActivity(i, ACTIVITY_RESULT);
                            break;
                        case NotificationActivity.REJECTED_KTP:
                            Intent e = new Intent(this, MyProfileNewActivity.class);
                            switchActivity(e, ACTIVITY_RESULT);
                            break;
                        case NotificationActivity.REJECTED_SIUP_NPWP:
                            Intent a = new Intent(this, UpgradeAgentActivity.class);
                            switchActivity(a, ACTIVITY_RESULT);
                            break;
                        case NotificationActivity.SOURCE_OF_FUND:
                            Intent s = new Intent(this, SourceOfFundActivity.class);
                            s.putExtras(data);
                            switchActivity(s, ACTIVITY_RESULT);
                            break;
                    }
                }
            }
            if (resultCode == RESULT_REFRESH_NAVDRAW) {
//                Timber.d("masuukk result refesh navdraw");
                mNavDrawer.refreshUINavDrawer();
                mNavDrawer.refreshDataNavDrawer();

            }
            if (resultCode == RESULT_FIRST_TIME) {
                checkField();
                mNavDrawer.refreshUINavDrawer();
                mNavDrawer.refreshDataNavDrawer();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private Boolean isLogin() {
        flagLogin = sp.getString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
        if (flagLogin == null)
            flagLogin = DefineValue.STRING_NO;
        Timber.d("isLoginMainPage");
        return flagLogin.equals(DefineValue.STRING_YES);
    }

    private void showLogoutDialog() {
        AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
        alertbox.setTitle("Warning");
        alertbox.setMessage("Exit Application?");
        alertbox.setPositiveButton("OK", (arg0, arg1) -> switchLogout());
        alertbox.setNegativeButton(getString(R.string.cancel), (arg0, arg1) -> {
        });
        alertbox.show();
    }
//---------------------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ab_notification, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemData = menu.findItem(R.id.notifications);

        itemData.setActionView(R.layout.ab_notification);
        NotificationActionView actionView = (NotificationActionView) itemData.getActionView();
        actionView.setItemData(menu, itemData);
        actionView.setCount(AmountNotif); // initial value
        if (AmountNotif == 0) actionView.hideView();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        invalidateOptionsMenu();
        if (item.getItemId() == R.id.notifications) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                //NotificationActionView.setCountDelta(this, 3);
                //FragNotification fragNotification = new FragNotification();
                //switchContent(fragNotification, "Notification",true);
                Intent i = new Intent(this, NotificationActivity.class);
                switchActivity(i, ACTIVITY_RESULT);
            } else getSupportFragmentManager().popBackStack();
            return true;
        }
//        else if (item.getItemId() == R.id.right_drawer_menu) {
//            refreshPromo();
//            if (mDrawerLayout.isDrawerOpen(mRightDrawerRelativeLayout)) {
//                mDrawerLayout.closeDrawer(mRightDrawerRelativeLayout);
//            }
//            mDrawerLayout.openDrawer(mRightDrawerRelativeLayout);
//            return true;
//        }
        else if (item.getItemId() == R.id.settings) {
            Intent i = new Intent(this, ActivityListSettings.class);
            switchActivity(i, ACTIVITY_RESULT);
        }
        invalidateOptionsMenu();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null)
            mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        if (mDrawerToggle != null)
            mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        Timber.w("get Back Stack Entry Count:" + String.valueOf(getSupportFragmentManager().getBackStackEntryCount()));
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            showLogoutDialog();
        } else super.onBackPressed();

    }

    @Override
    protected void onStart() {
        super.onStart();

        doBindToService();
        doBindToAppInfoService();
        doBindToUserProfileService();

    }


    public int getDefaultSimmm(Context context) {

        Object tm = context.getSystemService(Context.TELEPHONY_SERVICE);
        Method method_getDefaultSim;
        int defaultSimm = -1;
        try {
            method_getDefaultSim = tm.getClass().getDeclaredMethod("getDefaultSim");
            method_getDefaultSim.setAccessible(true);
            defaultSimm = (Integer) method_getDefaultSim.invoke(tm);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Method method_getSmsDefaultSim;
        int smsDefaultSim = -1;
        try {
            method_getSmsDefaultSim = tm.getClass().getDeclaredMethod("getSmsDefaultSim");
            smsDefaultSim = (Integer) method_getSmsDefaultSim.invoke(tm);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return smsDefaultSim;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isForeground) {
            if (serviceReferenceBalance != null) {
                serviceReferenceBalance.StartCallBalance();
            }
            if (serviceAppInfoReference != null) {
                serviceAppInfoReference.StartCallAppInfo();
            }

//        if(mBH !=null)
//            mBH.getDataBalance();

//        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
//                new IntentFilter(DefineValue.BR_REGISTRATION_COMPLETE));
        }
        bottomNavigationView.setSelectedItemId(R.id.menu_home);

        LocalBroadcastManager.getInstance(this).registerReceiver(btnReceiver, new IntentFilter(MainPage.RESULT_HOME_BALANCE));

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isForeground) {
            if (serviceReferenceBalance != null)
                serviceReferenceBalance.StopCallBalance();
            if (serviceAppInfoReference != null)
                serviceAppInfoReference.StopCallAppInfo();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(btnReceiver);
        if (progdialog != null)
            progdialog.dismiss();
    }

    @Override
    protected void onStop() {
        super.onStop();
        doUnbindService();
        doUnbindAppInfoService();
        doUnbindUserProfileService();
    }

    void showProgLoading(String msg, boolean islogout) {
        if (islogout) {
            progdialog = DefinedDialog.CreateProgressDialog(this, msg);
        } else {
            if (progdialog == null)
//            {
                progdialog = DefinedDialog.CreateProgressDialog(this, msg);
//            } else
            progdialog.show();
        }
    }

    void hideProgLoading() {
        if (progdialog != null) {
            if (progdialog.isShowing())
                progdialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        Timber.w("destroy main page");
        /*serviceReferenceBalance.StopCallBalance();
        doUnbindService();*/

        hideProgLoading();

        JobScheduleManager.getInstance(this).cancelAll();
        RetrofitService.dispose();
        super.onDestroy();
    }

    private void deleteData() {
//        sp.edit().clear().apply();
        CustomSecurePref.getInstance().ClearAllCustomData();
        listTimeLineModel.deleteAll();
        listHistoryModel.deleteAll();
        commentModel.deleteAll();
        likeModel.deleteAll();
    }


    private void setupFab() {
        materialSheetFab = FabInstance.newInstance(this, new FabInstance.OnBtnListener() {
            @Override
            public void OnClickItemFAB(int idx) {
                switch (idx) {
                    case FabInstance.ITEM_FAB_ASK4MONEY:
                        switchMenu(NavigationDrawMenu.MASK4MONEY, null);
                        break;
                    case FabInstance.ITEM_FAB_PAYFRIENDS:
                        switchMenu(NavigationDrawMenu.MPAYFRIENDS, null);
                        break;
                }
            }
        });

    }

    private void showValidasiEmail() {
        Intent i = new Intent(this, ValidasiEmailActivity.class);
        i.putExtra(DefineValue.IS_FIRST, DefineValue.YES);
        switchActivity(i, ACTIVITY_RESULT);
    }

    private void callAgentShopService() {
        AgentShopService.getAgentShop(MainPage.this);
    }
}