package com.sgo.saldomu.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.commentModel;
import com.sgo.saldomu.Beans.likeModel;
import com.sgo.saldomu.Beans.listHistoryModel;
import com.sgo.saldomu.Beans.listTimeLineModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.BBSDataManager;
import com.sgo.saldomu.coreclass.BaseActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.FabInstance;
import com.sgo.saldomu.coreclass.JobScheduleManager;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.NotificationActionView;
import com.sgo.saldomu.coreclass.NotificationHandler;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.fcm.FCMManager;
import com.sgo.saldomu.fcm.FCMWebServiceLoader;
import com.sgo.saldomu.fcm.GooglePlayUtils;
import com.sgo.saldomu.fragments.FragMainPage;
import com.sgo.saldomu.fragments.MyHistory;
import com.sgo.saldomu.fragments.NavigationDrawMenu;
import com.sgo.saldomu.fragments.RightSideDrawMenu;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.services.AgentShopService;
import com.sgo.saldomu.services.AppInfoService;
import com.sgo.saldomu.services.BalanceService;
import com.sgo.saldomu.services.UpdateBBSCity;
import com.sgo.saldomu.services.UpdateLocationService;
import com.sgo.saldomu.services.UserProfileService;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 Created by Administrator on 7/11/2014.
 */
public class MainPage extends BaseActivity{

    public static final int REQUEST_FINISH = 0 ;//untuk Request Code dari child activity ke parent activity
    private static final int RESULT_ERROR = -1 ;//untuk Result Code dari child activity ke parent activity kalau error (aplikasi exit)
    public static final int RESULT_LOGOUT = 1;//untuk Result Code dari child activity ke parent activity kalau sukses (aplikasi auto logout)
    public static final int RESULT_NORMAL = 2 ;//untuk Result Code dari child activity ke parent activity kalau normal (aplikasi close ke parent activity)
    public static final int RESULT_BALANCE = 3;
    public static final int RESULT_NOTIF = 5;
    public static final int RESULT_BILLER = 7 ;
	public static final int RESULT_REFRESH_NAVDRAW= 8;
    public static final int RESULT_FIRST_TIME = 9;
    public static final int RESULT_BBS = 11;
    public static final int RESULT_BBS_MEMBER_OTP = 12;
    public static final int RESULT_BBS_STATUS= 13;

    public static final int RESULT_FINISH = 99;
    public static final int ACTIVITY_RESULT = 1;

    private final static int FIRST_SCREEN_LOGIN = 1;
    private final static int FIRST_SCREEN_INTRO = 2;
    private final static int REQCODE_PLAY_SERVICE = 312;

    private static int AmountNotif = 0;

    private String flagLogin = DefineValue.STRING_NO;
    private String userID;
    private String accessKey;
    private SecurePreferences sp;
    private Fragment mContent;
    private NavigationDrawMenu mNavDrawer;
    private FragmentManager mFragmentManager;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ProgressDialog progdialog;
    private RelativeLayout mOuterRelativeContent;
    private FrameLayout mLeftDrawerRelativeLayout;
    private FrameLayout mRightDrawerRelativeLayout;
    private float lastTranslate = 0.0f;
//    private BroadcastReceiver mRegistrationBroadcastReceiver; // gcm
    private BalanceService serviceReferenceBalance;
    private AppInfoService serviceAppInfoReference;
    private UserProfileService serviceUserProfileReference;
    private boolean isBound, isBoundAppInfo, isBoundUserProfile, agent, isForeground = false;
    private UtilsLoader utilsLoader;
    public MaterialSheetFab materialSheetFab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain the FirebaseAnalytics instance.
//        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        if (savedInstanceState != null)
            mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");

        if(GooglePlayUtils.isGooglePlayServicesAvailable(this)) {
            if(checkNotification()){
                int type = Integer.valueOf(getIntent().getExtras().getString("type"));

                FCMManager fcmManager = new FCMManager(this);
                Intent intent = fcmManager.checkingAction(type);
                startActivity(intent);
                this.finish();
            }
            else {
                if (!isLogin()) {
                    openFirstScreen(FIRST_SCREEN_INTRO);
                } else {
                    isForeground = true;
                    agent = sp.getBoolean(DefineValue.IS_AGENT, false);
                    utilsLoader = new UtilsLoader(this, sp);
                    utilsLoader.getAppVersion();
                    ActiveAndroid.initialize(this);
                    progdialog = DefinedDialog.CreateProgressDialog(this, getString(R.string.initialize));
                    progdialog.show();
                    InitializeNavDrawer();
                    setupFab();
                    AlertDialogLogout.getInstance();    //inisialisasi alertdialoglogout
                    startService(new Intent(this, UpdateLocationService.class));
                }
            }
        }
        else {
            switchErrorActivity(ErrorActivity.GOOGLE_SERVICE_TYPE);
        }
    }

    void switchErrorActivity(int type){
        Intent i = new Intent(this, ErrorActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        i.putExtra(DefineValue.TYPE,type);
        startActivity(i);
    }

    boolean checkNotification(){
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            Timber.d("masuk check notification " +extras.toString());
            if (extras.containsKey("type")) {
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
            isBound=true;
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
            isBoundAppInfo=true;
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
            isBoundUserProfile=true;
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
            isBoundAppInfo = bindService(bindIntent, AppInfoServiceConnection,Context.BIND_AUTO_CREATE);
        }
    }


    //    bind to the service
    private void doBindToUserProfileService() {
        Timber.i("Main Page service connection UserProfile Binding ........");
        if (!isBoundUserProfile) {
            Timber.i("Main Page service connection UserProfile Masuk binding");
            Intent bindIntent = new Intent(this, UserProfileService.class);
            isBoundUserProfile = bindService(bindIntent, UserProfileServiceConnection,Context.BIND_AUTO_CREATE);
        }
    }


    private void doUnbindUserProfileService() {
        Timber.i("Main Page service connection UserProfile Unbind ........");
        unbindService(UserProfileServiceConnection);
        isBoundUserProfile = false;
    }

    private void InitializeNavDrawer(){
        mFragmentManager = getSupportFragmentManager();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer);
        mLeftDrawerRelativeLayout = (FrameLayout) findViewById(R.id.left_drawer);
        mRightDrawerRelativeLayout = (FrameLayout) findViewById(R.id.right_drawer);
        mDrawerLayout.setScrimColor(getResources().getColor(R.color.transparent));
        mOuterRelativeContent = (RelativeLayout) findViewById(R.id.outer_layout_content);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x - 150;
        int height = size.y;

        mRightDrawerRelativeLayout.getLayoutParams().width = width;
        mRightDrawerRelativeLayout.getLayoutParams().height = height;

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, getToolbar(), R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                ToggleKeyboard.hide_keyboard(MainPage.this);

                if(drawerView == mLeftDrawerRelativeLayout)
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED,mRightDrawerRelativeLayout);
                else
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED,mLeftDrawerRelativeLayout);
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                {
                    if(drawerView == mLeftDrawerRelativeLayout)
                        mOuterRelativeContent.setTranslationX(moveFactor);
                    else if(drawerView == mRightDrawerRelativeLayout)
                        mOuterRelativeContent.setTranslationX(-moveFactor);
                }
                else
                {
                    TranslateAnimation anim = null;

                    if(drawerView == mLeftDrawerRelativeLayout)
                        anim = new TranslateAnimation(lastTranslate, moveFactor, 0.0f, 0.0f);
                    else if(drawerView == mRightDrawerRelativeLayout)
                        anim = new TranslateAnimation(lastTranslate, -moveFactor, 0.0f, 0.0f);

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
                if(drawerView == mLeftDrawerRelativeLayout)
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,mRightDrawerRelativeLayout);
                else
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,mLeftDrawerRelativeLayout);
            }
        };
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        getDataListMember();
        mNavDrawer = (NavigationDrawMenu) getSupportFragmentManager().findFragmentById(R.id.main_list_menu_fragment);
    }


    private void refreshPromo(){
        RightSideDrawMenu mRightDrawer = (RightSideDrawMenu) getSupportFragmentManager().findFragmentById(R.id.main_list_menu_fragment_right);
        if(mRightDrawer != null)
            mRightDrawer.autoRefreshList();
    }

    private void initializeNavDrawer(){
        if(mNavDrawer != null) {
            mNavDrawer.initializeNavDrawer();
            mNavDrawer.getBalance(true);
        }
    }

    private void getDataListMember(){
        try{

            String comm_id = sp.getString(DefineValue.COMMUNITY_ID,"");
            String cust_id = sp.getString(DefineValue.CUST_ID,"");

            RequestParams params = MyApiClient.getSignatureWithParams(comm_id,MyApiClient.LINK_LIST_MEMBER,
                    userID,accessKey);
            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.CUST_ID, cust_id);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_ID_PULSA, MyApiClient.COMM_ID_PULSA);

            Timber.d("isi params listmember mainpage:" + params.toString());

            MyApiClient.sentDataListMember(this,params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            String arraynya = response.getString(WebParams.MEMBER_DATA);
                            Timber.d("Isi response listmember:" + response.toString());
                            if (!arraynya.isEmpty()) {
                                JSONArray arrayJson = new JSONArray(arraynya);
                                JSONObject objectJson = arrayJson.getJSONObject(0);

                                SecurePreferences.Editor mEditor = sp.edit();
                                mEditor.putString(DefineValue.MEMBER_CODE, objectJson.optString(WebParams.MEMBER_CODE, ""));
                                mEditor.putString(DefineValue.MEMBER_ID, objectJson.optString(WebParams.MEMBER_ID, ""));
                                mEditor.putString(DefineValue.MEMBER_NAME, objectJson.optString(WebParams.MEMBER_NAME, ""));
                                mEditor.apply();

                                if(mNavDrawer != null && serviceReferenceBalance != null)
                                    serviceReferenceBalance.runBalance();
//                                TurnOnGCM();
//                                getBalance(true);
                                initializeNavDrawer();
                                CheckNotification();

                                if (progdialog.isShowing())
                                    progdialog.dismiss();
                                checkField();
                                setupBBSData();

                                if ( !sp.getString(DefineValue.SHOP_AGENT_DATA, "").equals("") && sp.getString(DefineValue.IS_AGENT_SET_LOCATION, "").equals(DefineValue.STRING_NO) ) {
                                    try{
                                        JSONObject shopAgentObject = new JSONObject(sp.getString(DefineValue.SHOP_AGENT_DATA, ""));
                                        Intent intent = new Intent(MainPage.this, BbsMemberLocationActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putExtra("memberId", shopAgentObject.getString("member_id"));
                                        intent.putExtra("shopId", shopAgentObject.getString("shop_id"));
                                        intent.putExtra("shopName", shopAgentObject.getString("shop_name"));
                                        intent.putExtra("memberType", shopAgentObject.getString("member_type"));
                                        intent.putExtra("memberName", shopAgentObject.getString("member_name"));
                                        intent.putExtra("commName", shopAgentObject.getString("comm_name"));
                                        intent.putExtra("province", shopAgentObject.getString("province"));
                                        intent.putExtra("district", shopAgentObject.getString("district"));
                                        intent.putExtra("address", shopAgentObject.getString("address1"));
                                        intent.putExtra("category", "");
                                        intent.putExtra("isMobility", shopAgentObject.getString("is_mobility"));
                                        switchActivity(intent, ACTIVITY_RESULT);
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                } else if ( !sp.getString(DefineValue.SHOP_AGENT_DATA, "").equals("") && sp.getString(DefineValue.IS_AGENT_SET_OPENHOUR, "").equals(DefineValue.STRING_NO) ) {
                                    try{
                                        Bundle bundle = new Bundle();
                                        bundle.putInt(DefineValue.INDEX, BBSActivity.BBSWAKTUBEROPERASI);

                                        Intent intent = new Intent(MainPage.this, BBSActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putExtras(bundle);
                                        startActivityForResult(intent, MainPage.RESULT_REFRESH_NAVDRAW);
                                        finish();

                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                }

                            } else
                                Toast.makeText(MainPage.this, "List Member is Empty", Toast.LENGTH_LONG).show();

                            String member_dap = response.getString(WebParams.MEMBER_DAP);
                            SecurePreferences.Editor mEditor = sp.edit();
                            mEditor.putString(DefineValue.MEMBER_DAP, member_dap);
                            mEditor.apply();

                            if (progdialog.isShowing())
                                progdialog.dismiss();

                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            Timber.d("isi response autologout:" + response.toString());
                            progdialog.dismiss();
                            String message = response.getString(WebParams.ERROR_MESSAGE);

                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginMain(MainPage.this, message);
                        } else {
                            Timber.d("Error ListMember comlist:" + response.toString());
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            progdialog.dismiss();
                            Toast.makeText(MainPage.this, code, Toast.LENGTH_LONG).show();
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
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(MainPage.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(MainPage.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                    if(progdialog.isShowing())
                        progdialog.dismiss();
                    sentLogout();
//                    finish();
                    Timber.w("Error Koneksi List member comlist:" + throwable.getMessage());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    void setupBBSData(){
        boolean isAgent = sp.getBoolean(DefineValue.IS_AGENT,false);
        if(isAgent){
            callBBSCityService();
            checkAndRunServiceBBS();
            callAgentShopService();
        }
    }

    void checkAndRunServiceBBS(){
        BBSDataManager.checkAndRunService(this);
    }

    private void CheckNotification(){
        Thread mth = new Thread(){
            @Override
            public void run() {
                NotificationHandler mNoHand = new NotificationHandler(MainPage.this,sp);
                mNoHand.sentRetrieveNotif();
            }
        };
        mth.start();
    }

    private void callBBSCityService(){
        Timber.d("Panggil service BBS City");
        UpdateBBSCity.startUpdateBBSCity(MainPage.this);
    }

    private void showChangePassword(){
            Intent i = new Intent(this, ChangePassword.class);
            i.putExtra(DefineValue.IS_FIRST, DefineValue.YES);
            switchActivity(i, ACTIVITY_RESULT);
    }

    private void showMyProfile(){
        Intent i = new Intent(this, MyProfileActivity.class);
        i.putExtra(DefineValue.IS_FIRST, DefineValue.YES);
        switchActivity(i, ACTIVITY_RESULT);
    }

    private void showCreatePin() {
        Intent i = new Intent(this, CreatePIN.class);
        switchActivity(i, MainPage.ACTIVITY_RESULT);

    }

    private void checkField(){
        String bom_value = sp.getString(DefineValue.PROFILE_BOM, "");
        if (bom_value.isEmpty()) {
            showMyProfile();
        } else if (sp.getString(DefineValue.IS_CHANGED_PASS, "").equals(DefineValue.STRING_NO)) {
            showChangePassword();
        } else if (sp.getString(DefineValue.IS_HAVE_PIN, "").equalsIgnoreCase(DefineValue.STRING_NO)) {
            showCreatePin();
        }
    }


//----------------------------------------------------------------------------------------------------------------


    private void openFirstScreen(int index){
        Intent i;
        switch(index){
            case FIRST_SCREEN_LOGIN :
                i = new Intent(this,LoginActivity.class);
                break;
            case FIRST_SCREEN_INTRO :
                i = new Intent(this,Introduction.class);
                break;
            default:
                i = new Intent(this,LoginActivity.class);
                break;
        }
        startActivity(i);
        this.finish();
    }


    public void setNotifAmount(String _amount) {
        AmountNotif = Integer.parseInt(_amount);
        invalidateOptionsMenu();
    }

    public void switchContent(Fragment mFragment,String fragName) {
        mContent = mFragment;

        materialSheetFab.showFab();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_page_content, mContent, fragName)
                .commitAllowingStateLoss();
        setActionBarTitle(fragName);
        if(mDrawerLayout !=null)mDrawerLayout.closeDrawer(mLeftDrawerRelativeLayout);
    }

    public void switchContent(Fragment mFragment,String fragName,Boolean isBackstack) {

        if(isBackstack){
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_page_content, mFragment, fragName)
                    .addToBackStack(null)
                    .commit();
        }
        else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_page_content, mFragment, fragName)
                    .commit();

        }
        setActionBarTitle(fragName);
        if(mDrawerLayout !=null)mDrawerLayout.closeDrawer(mLeftDrawerRelativeLayout);
    }

    public void switchActivity(Intent mIntent, int activity_type) {
        switch (activity_type){
            case ACTIVITY_RESULT:
                startActivityForResult(mIntent, REQUEST_FINISH);
                break;
            case 2:
                break;
        }
    }

    public void switchMenu( int IdxItemMenu,Bundle data) {
        mNavDrawer.selectItem(IdxItemMenu, data);
    }

    public void switchLogout() {
        sentLogout();
    }
    private void Logout() {

        String balance = sp.getString(DefineValue.BALANCE_AMOUNT, "");
        String contact_first_time = sp.getString(DefineValue.CONTACT_FIRST_TIME,"");
        deleteData();
        SecurePreferences.Editor mEditor = sp.edit();
        mEditor.putString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
        mEditor.putString(DefineValue.PREVIOUS_LOGIN_USER_ID,userID);
        mEditor.putString(DefineValue.PREVIOUS_BALANCE,balance);
        mEditor.putString(DefineValue.PREVIOUS_CONTACT_FIRST_TIME,contact_first_time);

        mEditor.putString(DefineValue.IS_AGENT_APPROVE, "");
        mEditor.putString(DefineValue.AGENT_NAME, "");
        mEditor.putString(DefineValue.AGENT_SHOP_CLOSED, "");
        mEditor.putString(DefineValue.BBS_MEMBER_ID, "");
        mEditor.putString(DefineValue.BBS_SHOP_ID, "");
        mEditor.putString(DefineValue.IS_AGENT_SET_LOCATION, "");
        mEditor.putString(DefineValue.IS_AGENT_SET_OPENHOUR, "");
        mEditor.putString(DefineValue.SHOP_AGENT_DATA, "");

        //di commit bukan apply, biar yakin udah ke di write datanya
        mEditor.commit();
        openFirstScreen(FIRST_SCREEN_LOGIN);
    }
	
	private void sentLogout(){
        try{
            if(progdialog != null && !progdialog.isShowing()) {
                progdialog = DefinedDialog.CreateProgressDialog(this, "");
                progdialog.show();
            }

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_LOGOUT,
                    userID,accessKey);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);


            Timber.d("isi params logout:"+params.toString());

            MyApiClient.sentLogout(this, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String message = response.getString(WebParams.ERROR_MESSAGE);
                        progdialog.dismiss();
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("logout:"+response.toString());
                            stopService(new Intent(MainPage.this, UpdateLocationService.class));
                            Logout();

                        } else {
                            progdialog.dismiss();
                            Timber.d("isi error logout:"+response.toString());
                            Toast.makeText(MainPage.this, message, Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MainPage.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(MainPage.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                    if(progdialog.isShowing())
                        progdialog.dismiss();
                    MainPage.this.finish();
                    Timber.w("Error Koneksi logout mainpage:"+throwable.toString());
                }
            });
        }
        catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
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
            if(resultCode == RESULT_BALANCE){
                Timber.w("Masuk result Balance");
                mNavDrawer.getBalance(true);
            }
            if(resultCode == RESULT_NOTIF){
                Timber.w("Masuk result notif");
                CheckNotification();
                invalidateOptionsMenu();
                if(data != null){
                    int _type = data.getIntExtra(DefineValue.NOTIF_TYPE,0);
                    if( _type == NotificationActivity.TYPE_TRANSFER){
                        Bundle dataBundle = new Bundle();
                        dataBundle.putString(DefineValue.AMOUNT,data.getStringExtra(DefineValue.AMOUNT));
                        dataBundle.putString(DefineValue.CUST_NAME,data.getStringExtra(DefineValue.CUST_NAME));
                        dataBundle.putString(DefineValue.MESSAGE,data.getStringExtra(DefineValue.MESSAGE));
                        dataBundle.putString(DefineValue.USERID_PHONE,data.getStringExtra(DefineValue.USERID_PHONE));
                        dataBundle.putString(DefineValue.TRX,data.getStringExtra(DefineValue.TRX));
                        dataBundle.putString(DefineValue.REQUEST_ID,data.getStringExtra(DefineValue.REQUEST_ID));

                        mNavDrawer.selectItem(NavigationDrawMenu.MPAYFRIENDS,dataBundle);
                    }
                    else if(_type == NotificationActivity.TYPE_LIKE || _type == NotificationActivity.TYPE_COMMENT){
                        int _post_id = Integer.valueOf(data.getExtras().getString(DefineValue.POST_ID,"0"));
                        if(mContent instanceof FragMainPage){
                            FragMainPage mFrag = (FragMainPage)mContent;
                            if(mFrag.getFragment(0) instanceof MyHistory){
                                MyHistory _history =(MyHistory) mFrag.getFragment(0);
                                _history.ScrolltoItem(_post_id);
                            }
                        }
                        Intent i = new Intent(this, HistoryDetailActivity.class);
                        i.putExtras(data);
                        switchActivity(i,ACTIVITY_RESULT);

                    }
                }
            }
            if(resultCode == RESULT_REFRESH_NAVDRAW) {
//                Timber.d("masuukk result refesh navdraw");
                mNavDrawer.refreshUINavDrawer();
                mNavDrawer.refreshDataNavDrawer();

            }
            if(resultCode == RESULT_FIRST_TIME){
                    checkField();
                mNavDrawer.refreshUINavDrawer();
                mNavDrawer.refreshDataNavDrawer();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private Boolean isLogin(){
        flagLogin = sp.getString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
        if(flagLogin == null)
            flagLogin = DefineValue.STRING_NO;
        Timber.d("isLoginMainPage");
        return flagLogin.equals(DefineValue.STRING_YES);
    }

    private void showLogoutDialog(){
        AlertDialog.Builder alertbox=new AlertDialog.Builder(this);
        alertbox.setTitle("Warning");
        alertbox.setMessage("Exit Application?");
        alertbox.setPositiveButton("OK", new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        switchLogout();
                    }
                });
        alertbox.setNegativeButton(getString(R.string.cancel), new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
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

        MenuItemCompat.setActionView(itemData, R.layout.ab_notification);
        NotificationActionView actionView = (NotificationActionView) itemData.getActionView();
        actionView.setItemData(menu, itemData);
        actionView.setCount(AmountNotif); // initial value
        if(AmountNotif == 0) actionView.hideView();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        invalidateOptionsMenu();
        if(item.getItemId() == R.id.notifications){
            if(getSupportFragmentManager().getBackStackEntryCount() == 0 ){
                //NotificationActionView.setCountDelta(this, 3);
                //FragNotification fragNotification = new FragNotification();
                //switchContent(fragNotification, "Notification",true);
                Intent i = new Intent(this,NotificationActivity.class);
                switchActivity(i,ACTIVITY_RESULT);
            }
            else getSupportFragmentManager().popBackStack();
            return true;
        }
        else if(item.getItemId() == R.id.right_drawer){
            refreshPromo();
            if (mDrawerLayout.isDrawerOpen(mRightDrawerRelativeLayout)){
                mDrawerLayout.closeDrawer(mRightDrawerRelativeLayout);
            }
            mDrawerLayout.openDrawer(mRightDrawerRelativeLayout);
            return true;
        }
        else if(item.getItemId() == R.id.menu_item_home) {
            invalidateOptionsMenu();
            Fragment newFragment = new FragMainPage();
            switchContent(newFragment, getString(R.string.toolbar_title_home));
            mNavDrawer.setPositionNull();
            invalidateOptionsMenu();
        }
        invalidateOptionsMenu();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if(mDrawerToggle != null)
            mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        if(mDrawerToggle != null)
            mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        Timber.w("get Back Stack Entry Count:" + String.valueOf(getSupportFragmentManager().getBackStackEntryCount()));
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            showLogoutDialog();
        }
        else super.onBackPressed();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(isForeground) {
            doBindToService();
            doBindToAppInfoService();
            doBindToUserProfileService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isForeground) {
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

    }

    @Override
    protected void onPause() {
        if(isForeground){
            if(serviceReferenceBalance != null)
                serviceReferenceBalance.StopCallBalance();
            if(serviceAppInfoReference != null)
                serviceAppInfoReference.StopCallAppInfo();
        }
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isForeground) {
            doUnbindService();
            doUnbindAppInfoService();
            doUnbindUserProfileService();
        }
    }

    @Override
    protected void onDestroy() {
        Timber.w("destroy main page");
        /*serviceReferenceBalance.StopCallBalance();
        doUnbindService();*/
        if(progdialog != null && progdialog.isShowing()) {
            progdialog.dismiss();
        }
        MyApiClient.CancelRequestWS(this,true);
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
                switch (idx){
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

    private void callAgentShopService() {
        AgentShopService.getAgentShop(MainPage.this);
    }
}