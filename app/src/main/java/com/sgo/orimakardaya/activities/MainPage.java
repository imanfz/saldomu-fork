package com.sgo.orimakardaya.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import com.activeandroid.ActiveAndroid;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.Beans.commentModel;
import com.sgo.orimakardaya.Beans.likeModel;
import com.sgo.orimakardaya.Beans.listHistoryModel;
import com.sgo.orimakardaya.Beans.listTimeLineModel;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.*;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import com.sgo.orimakardaya.fragments.NavigationDrawMenu;
import com.sgo.orimakardaya.fragments.RightSideDrawMenu;
import com.sgo.orimakardaya.services.AppInfoService;
import com.sgo.orimakardaya.services.BalanceService;
import com.squareup.picasso.Picasso;
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
    public static final int RESULT_ERROR = -1 ;//untuk Result Code dari child activity ke parent activity kalau error (aplikasi exit)
    public static final int RESULT_LOGOUT = 1;//untuk Result Code dari child activity ke parent activity kalau sukses (aplikasi auto logout)
    public static final int RESULT_NORMAL = 2 ;//untuk Result Code dari child activity ke parent activity kalau normal (aplikasi close ke parent activity)
    public static final int RESULT_BALANCE = 3;
    public static final int RESULT_PROFILE_PIC = 4;
    public static final int RESULT_NOTIF = 5;
    public static final int RESULT_MYPROFILE = 6;
    public static final int RESULT_BILLER = 7 ;
    public static final int RESULT_DAP = 8 ;
    public static final int ACTIVITY_RESULT = 1;

    public static String action_id = "";
    protected static boolean activityVisible;
    private static int AmountNotif;

    String flagLogin,userID,accessKey;
    SecurePreferences sp;
    Fragment mContent;
    NavigationDrawMenu mNavDrawer;
    FragmentManager mFragmentManager;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    ProgressDialog progdialog;
    private RelativeLayout mOuterRelativeContent;
    private FrameLayout mLeftDrawerRelativeLayout;
    private FrameLayout mRightDrawerRelativeLayout;
    private float lastTranslate = 0.0f;

    public ImageView headerCustImage;
    public TextView headerCustName,headerCustID,headerCurrency,balanceValue, currencyLimit, limitValue,periodeLimit;
    public LinearLayout llHeaderProfile;

    private BalanceService serviceReferenceBalance;
    private AppInfoService serviceAppInfoReference;
    private boolean isBound, isBoundAppInfo;

    private Animation frameAnimation;
    private ImageView btn_refresh_balance;
    BalanceHandler mBH;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        if (savedInstanceState != null)
            mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");


        if(!checkLogin()){
            openFirstScreen();
        }
        else{
            getAppVersion();
            ActiveAndroid.initialize(this);
            progdialog = DefinedDialog.CreateProgressDialog(this, "Initialize");
            progdialog.show();
            InitializeNavDrawer();
            InitializeService();
            CheckNotification();
        }

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

    public void InitializeService(){
        Intent intent = new Intent(this, BalanceService.class);
        startService(intent);
    }

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

    public void InitializeNavDrawer(){
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
            }
        };
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

//        ImageButton mRightDrawer = (ImageButton) getToolbar().findViewById(R.id.main_toolbar_right_drawer);
//
//        mRightDrawer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mDrawerLayout.isDrawerOpen(mRightDrawerRelativeLayout)){
//                    mDrawerLayout.closeDrawer(mRightDrawerRelativeLayout);
//                }
//                mDrawerLayout.openDrawer(mRightDrawerRelativeLayout);
//            }
//        });

        llHeaderProfile = (LinearLayout) findViewById(R.id.llHeaderProfile);
        headerCustImage = (ImageView) findViewById(R.id.header_cust_image);
        headerCurrency = (TextView) findViewById(R.id.currency_value);
        headerCustName = (TextView) findViewById(R.id.header_cust_name);
        headerCustID = (TextView) findViewById(R.id.header_cust_id);
        balanceValue = (TextView) findViewById(R.id.balance_value);
        currencyLimit = (TextView) findViewById(R.id.currency_limit_value);
        limitValue = (TextView) findViewById(R.id.limit_value);
        periodeLimit = (TextView) findViewById(R.id.periode_limit_value);

        setImageProfPic();
        headerCustName.setText(sp.getString(DefineValue.CUST_NAME, getString(R.string.text_strip)));
        headerCustID.setText(sp.getString(DefineValue.CUST_ID, getString(R.string.text_strip)));

        llHeaderProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainPage.this, MyProfileActivity.class);
                switchActivity(i, ACTIVITY_RESULT);
            }
        });

        getDataListMember();
        mNavDrawer = (NavigationDrawMenu) getSupportFragmentManager().findFragmentById(R.id.main_list_menu_fragment);
        btn_refresh_balance = (ImageView) mNavDrawer.layoutContainer.findViewById(R.id.btn_refresh_balance);
        frameAnimation = AnimationUtils.loadAnimation(this, R.anim.spinner_animation);
        frameAnimation.setRepeatCount(Animation.INFINITE);

        btn_refresh_balance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_refresh_balance.setEnabled(false);
                btn_refresh_balance.startAnimation(frameAnimation);
                getBalance(sp.getString(DefineValue.MEMBER_ID, ""), false);
            }
        });
    }

    private void refreshPromo(){
        RightSideDrawMenu mRightDrawer = (RightSideDrawMenu) getSupportFragmentManager().findFragmentById(R.id.main_list_menu_fragment_right);
        if(mRightDrawer != null)
            mRightDrawer.autoRefreshList();
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
            mPic = MyPicasso.getImageLoader(this);
        else
            mPic= Picasso.with(this);

        if(_url_profpic !=null && _url_profpic.isEmpty()){
            mPic.load(R.drawable.user_unknown_menu)
                .error(roundedImage)
                .fit().centerInside()
                .placeholder(R.anim.progress_animation)
                .transform(new RoundImageTransformation(this)).into(headerCustImage);
        }
        else {
            mPic.load(_url_profpic)
                .error(roundedImage)
                .fit().centerInside()
                .placeholder(R.anim.progress_animation)
                .transform(new RoundImageTransformation(this)).into(headerCustImage);
        }

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main_page;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void getDataListMember(){
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

            MyApiClient.sentDataListMember(params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            String arraynya = response.getString(WebParams.MEMBER_DATA);
                            Timber.d("Isi response listmember:" + response.toString());
                            if (!arraynya.isEmpty()) {
                                setMemberToProfile(arraynya);
                                refreshPromo();

                                JSONArray mArrayMember = new JSONArray(arraynya);
                                String atm_topup_data = mArrayMember.getJSONObject(0).getString(WebParams.ATM_TOPUP_DATA);

                                getBalance(mArrayMember.getJSONObject(0).getString(WebParams.MEMBER_ID), true);

                                Timber.d("atm topup:" + atm_topup_data);
                                String bank_code = "";
                                String no_va = "";
                                String bank_name = "";
                                JSONArray mArrayATM = new JSONArray(atm_topup_data);
                                for (int i = 0; i < mArrayATM.length(); i++) {
                                    if (i == mArrayATM.length() - 1) {
                                        bank_code += mArrayATM.getJSONObject(i).getString(WebParams.BANK_CODE);
                                        no_va += mArrayATM.getJSONObject(i).getString(WebParams.NO_VA);
                                        bank_name += mArrayATM.getJSONObject(i).getString(WebParams.BANK_NAME);
                                    } else {
                                        bank_code += mArrayATM.getJSONObject(i).getString(WebParams.BANK_CODE) + ",";
                                        no_va += mArrayATM.getJSONObject(i).getString(WebParams.NO_VA) + ",";
                                        bank_name += mArrayATM.getJSONObject(i).getString(WebParams.BANK_NAME) + ",";
                                    }
                                }
                                Timber.d("atm topup:" + bank_name);

                                SecurePreferences.Editor mEditor = sp.edit();
                                mEditor.putString(DefineValue.BANK_ATM_CODE, bank_code);
                                mEditor.putString(DefineValue.NO_VA, no_va);
                                mEditor.putString(DefineValue.BANK_ATM_NAME, bank_name);
                                mEditor.apply();



                            }
                            else Toast.makeText(MainPage.this, "List Member is Empty", Toast.LENGTH_LONG).show();

                            String member_dap = response.getString(WebParams.MEMBER_DAP);
                            SecurePreferences.Editor mEditor = sp.edit();
                            mEditor.putString(DefineValue.MEMBER_DAP, member_dap);
                            mEditor.apply();


                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:" + response.toString());
                            progdialog.dismiss();
                            String message = response.getString(WebParams.ERROR_MESSAGE);

                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginMain(MainPage.this, message);
                        }
                        else {
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
                    if (progdialog.isShowing())
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
    public void getBalance(String member_id, final Boolean checkFirstTime){
        try{

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_SALDO,
                    userID,accessKey);
            params.put(WebParams.MEMBER_ID, member_id);
			params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params get Balance:" + params.toString());

            MyApiClient.getSaldo(params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {

                                    Timber.d("Isi response getBalance:" + response.toString());
                                    headerCurrency.setText(response.getString(WebParams.CCY_ID));
                                    balanceValue.setText(CurrencyFormat.format(response.getDouble(WebParams.AMOUNT)));
                                    currencyLimit.setText(response.getString(WebParams.CCY_ID));
                                    limitValue.setText(CurrencyFormat.format(response.getDouble(WebParams.REMAIN_LIMIT)));


                                    if (response.getString(WebParams.PERIOD_LIMIT).equals("Monthly"))
                                        periodeLimit.setText(R.string.header_monthly_limit);
                                    else
                                        periodeLimit.setText(R.string.header_daily_limit);

                                    if (checkFirstTime) {
                                        showCreatePin();
                                        showChangePassword();
                                    }

                                    btn_refresh_balance.setEnabled(true);
                                    btn_refresh_balance.clearAnimation();
                                    mBH = new BalanceHandler(MainPage.this, sp);

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


                                    btn_refresh_balance.setEnabled(true);
                                    btn_refresh_balance.clearAnimation();
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

                        private void failure(Throwable throwable) {
                            if (MyApiClient.PROD_FAILURE_FLAG)
                                Toast.makeText(MainPage.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(MainPage.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                            if (progdialog.isShowing())
                                progdialog.dismiss();
                            btn_refresh_balance.setEnabled(true);
                            btn_refresh_balance.clearAnimation();
                            Timber.w("Error Koneksi get saldo main page:" + throwable.toString());
                        }
                    }

            );
            }catch (Exception e){
            Timber.d("httpclient:" + e.getMessage());
        }
    }


    public void setMemberToProfile(String response){
        SecurePreferences.Editor mEditor = sp.edit();
        try {
            JSONArray arrayJson = new JSONArray(response);
            mEditor.putString(DefineValue.MEMBER_CODE, arrayJson.getJSONObject(0).getString(WebParams.MEMBER_CODE));
            mEditor.putString(DefineValue.MEMBER_ID, arrayJson.getJSONObject(0).getString(WebParams.MEMBER_ID));
            mEditor.putString(DefineValue.MEMBER_NAME, arrayJson.getJSONObject(0).getString(WebParams.MEMBER_NAME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mEditor.apply();
    }

    public void showChangePassword(){
        if(sp.getString(DefineValue.IS_FIRST_TIME,"").equals(DefineValue.YES)){
            Intent i = new Intent(this, ChangePassword.class);
            i.putExtra(DefineValue.IS_FIRST_TIME, DefineValue.YES);
            switchActivity(i,ACTIVITY_RESULT);
        }
    }




//----------------------------------------------------------------------------------------------------------------
    public void openFirstScreen(){
        Intent i = new Intent(this,Registration.class);
        startActivity(i);
        finish();
    }

    public void setBalance(final String _balance) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Timber.d("masuk ui thread main page:" + _balance);
                balanceValue.setText(_balance);
            }
        });
    }

    public void setMonthlyLimit(final String _limit, final String _period) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Timber.d("masuk ui thread main page:" + _limit);
                limitValue.setText(_limit);

                if (_period.equals("Monthly"))
                    periodeLimit.setText(R.string.header_monthly_limit);
                else
                    periodeLimit.setText(R.string.header_daily_limit);
            }
        });
    }


    public void setNotifAmount(String _amount) {
        AmountNotif = Integer.parseInt(_amount);
        invalidateOptionsMenu();
    }

    public void switchContent(Fragment mFragment,String fragName) {
        mContent = mFragment;
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
                action_id = "";
                startActivityForResult(mIntent, REQUEST_FINISH);
                break;
            case 2:
                break;
        }
    }

    public void switchMenu(Bundle data, int IdxItemMenu) {
        mNavDrawer.selectItem(IdxItemMenu, data);
    }

    public void switchLogout() {
        sentLogout();
    }
    public void Logout() {
        deleteData();
        SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
        SecurePreferences.Editor mEditor = prefs.edit();

        mEditor.putString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
        mEditor.putString(DefineValue.BANK_ATM_CODE, "");
        mEditor.putString(DefineValue.NO_VA, "");
        mEditor.putString(DefineValue.BANK_ATM_NAME, "");
        mEditor.commit();
        openFirstScreen();
    }
	
	public void sentLogout(){
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

            MyApiClient.sentLogout(params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String message = response.getString(WebParams.ERROR_MESSAGE);
                        progdialog.dismiss();
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("logout:"+response.toString());
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
//                    Logout();
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
        Timber.d("isi request code:"+String.valueOf(requestCode));

        Timber.d("isi result Code:"+String.valueOf(resultCode));
        if (requestCode == REQUEST_FINISH) {
            if (resultCode == RESULT_LOGOUT) {
                switchLogout();
            }
            if (resultCode == RESULT_ERROR) {
                this.finish();
            }
            if(resultCode == RESULT_BALANCE){
                Timber.w("Masuk result Balance");
                mBH.sentData();
            }
            if(resultCode == RESULT_PROFILE_PIC){
                Timber.w("Masuk result Prof Pic");
                setImageProfPic();
                headerCustName.setText(sp.getString(DefineValue.CUST_NAME, getString(R.string.text_strip)));
            }
            if(resultCode == RESULT_NOTIF){
                Timber.w("Masuk result notif");
                CheckNotification();
                invalidateOptionsMenu();
                if(data != null){
                    if(data.hasExtra(DefineValue.AMOUNT)){
                        Bundle dataBundle = new Bundle();
                        dataBundle.putString(DefineValue.AMOUNT,data.getStringExtra(DefineValue.AMOUNT));
                        dataBundle.putString(DefineValue.CUST_NAME,data.getStringExtra(DefineValue.CUST_NAME));
                        dataBundle.putString(DefineValue.MESSAGE,data.getStringExtra(DefineValue.MESSAGE));
                        dataBundle.putString(DefineValue.USERID_PHONE,data.getStringExtra(DefineValue.USERID_PHONE));
                        dataBundle.putString(DefineValue.TRX,data.getStringExtra(DefineValue.TRX));
                        dataBundle.putString(DefineValue.REQUEST_ID,data.getStringExtra(DefineValue.REQUEST_ID));

                        mNavDrawer.selectItem(4,dataBundle);
                    }
                }
            }
            if(resultCode == RESULT_MYPROFILE) {
                headerCustName.setText(sp.getString(DefineValue.CUST_NAME, getString(R.string.text_strip)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public Boolean checkLogin(){
        flagLogin = sp.getString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);

        return !flagLogin.equals("N");
    }

    public void showLogoutDialog(){
        AlertDialog.Builder alertbox=new AlertDialog.Builder(this);
        alertbox.setTitle("Warning");
        alertbox.setMessage("Exit Application?");
        alertbox.setPositiveButton("OK", new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        switchLogout();
                    }
                });
        alertbox.setNegativeButton("Cancel", new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {}
                });
        alertbox.show();
    }


    public void getAppVersion(){
        try{
            MyApiClient.getAppVersion(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("Isi response get App Version:"+response.toString());
                            String arrayApp = response.optString(WebParams.APP_DATA,"");
                            if(!arrayApp.isEmpty() && arrayApp.equalsIgnoreCase(null)) {
                                final JSONObject mObject = new JSONObject(arrayApp);
                                String package_version = mObject.getString(WebParams.PACKAGE_VERSION);
                                final String package_name = mObject.getString(WebParams.PACKAGE_NAME);
                                final String type = mObject.getString(WebParams.TYPE);
                                Timber.d("Isi Version Name / version code:"+DefineValue.VERSION_NAME + " / " + DefineValue.VERSION_CODE);
                                if (!package_version.equals(DefineValue.VERSION_NAME)) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainPage.this)
                                            .setTitle("Update")
                                            .setMessage("Application is out of date,  Please update immediately")
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setCancelable(false)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (type.equalsIgnoreCase("1")) {
                                                        try {
                                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + package_name)));
                                                        } catch (android.content.ActivityNotFoundException anfe) {
                                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + package_name)));
                                                        }
                                                    } else if (type.equalsIgnoreCase("2")) {
                                                        String download_url = "";
                                                        try {
                                                            download_url = mObject.getString(WebParams.DOWNLOAD_URL);
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                        if (!Patterns.WEB_URL.matcher(download_url).matches())
                                                            download_url = "http://www.google.com";
                                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(download_url)));
                                                    }
                                                    MainPage.this.finish();
                                                    android.os.Process.killProcess(android.os.Process.myPid());
                                                    System.exit(0);
                                                    getParent().finish();
                                                }
                                            });
                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                }
                            }
                        } else if (code.equals("0381")) {
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainPage.this)
                                    .setTitle("Maintenance")
                                    .setMessage(message)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            MainPage.this.finish();
                                            android.os.Process.killProcess(android.os.Process.myPid());
                                            System.exit(0);
                                            getParent().finish();
                                        }
                                    });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        } else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();
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
                    Timber.w("Error Koneksi app info main page:" + throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
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
            if (mDrawerLayout.isDrawerOpen(mRightDrawerRelativeLayout)){
                mDrawerLayout.closeDrawer(mRightDrawerRelativeLayout);
            }
            mDrawerLayout.openDrawer(mRightDrawerRelativeLayout);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        Timber.w("get Back Stack Entry Count:" + String.valueOf(getSupportFragmentManager().getBackStackEntryCount()));
        if(getSupportFragmentManager().getBackStackEntryCount() == 0 ){
            showLogoutDialog();
        }
        else super.onBackPressed();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        Timber.w("on attach main page");
        if(!action_id.equals("")){
            Timber.w("cek dan panggil logout handler");
            //mLh.sentData();
        }
        super.onAttachFragment(fragment);
    }

    @Override
    protected void onStart() {
        super.onStart();
        doBindToService();
        doBindToAppInfoService();
    }

    @Override
    protected void onResume() {
        super.onResume();

        action_id = "V";
        activityVisible = true;
        if(!action_id.equals("")){
            Timber.d("cek dan panggil logout handler onResume");
        }
        if(serviceReferenceBalance !=null) {
            serviceReferenceBalance.StartCallBalance();
        }
        if(serviceAppInfoReference!=null){
            serviceAppInfoReference.StartCallAppInfo();
        }

        if(mBH !=null)
            mBH.sentData();
    }

    @Override
    public void onUserInteraction() {
        if(activityVisible){
            action_id = "V";
        }
        super.onUserInteraction();
    }

    @Override
    protected void onPause() {
        activityVisible = false;
        if(serviceReferenceBalance != null)
            serviceReferenceBalance.StopCallBalance();
        if(serviceAppInfoReference != null)
            serviceAppInfoReference.StopCallAppInfo();
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        doUnbindService();
        doUnbindAppInfoService();

    }

    @Override
    protected void onDestroy() {
        Timber.w("destroy main page");
        /*serviceReferenceBalance.StopCallBalance();
        doUnbindService();*/
        if(progdialog != null && progdialog.isShowing()) {
            progdialog.dismiss();
        }
        super.onDestroy();
        if (isFinishing()) {
            Timber.i("Main page destroy service");
//            stop service as activity being destroyed and we won't use it any more
            Intent intentStopService = new Intent(this, BalanceService.class);
            stopService(intentStopService);
        }
    }


    private void deleteData() {
        listTimeLineModel.deleteAll();
        listHistoryModel.deleteAll();
        commentModel.deleteAll();
        likeModel.deleteAll();
    }

    public void showCreatePin() {

        if(sp.getString(DefineValue.AUTHENTICATION_TYPE,"").equalsIgnoreCase("PIN") && sp.getString(DefineValue.IS_HAVE_PIN,"").equalsIgnoreCase("N")) {
            Intent i = new Intent(this, CreatePIN.class);
            switchActivity(i,MainPage.ACTIVITY_RESULT);
        }
    }

}