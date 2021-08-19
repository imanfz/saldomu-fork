package com.sgo.saldomu.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.fragments.BillerDesciption;
import com.sgo.saldomu.fragments.BillerInput;
import com.sgo.saldomu.fragments.BillerInputData;
import com.sgo.saldomu.fragments.BillerInputPLN;
import com.sgo.saldomu.fragments.BillerInputPulsa;
import com.sgo.saldomu.fragments.FragGridEmoney;
import com.sgo.saldomu.fragments.FragGridGame;
import com.sgo.saldomu.fragments.ListBillerMerchant;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.BillerDenomResponse;
import com.sgo.saldomu.models.BillerItem;
import com.sgo.saldomu.models.DenomDataItem;
import com.sgo.saldomu.widgets.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import timber.log.Timber;

/*
  Created by Administrator on 3/4/2015.
 */
public class BillerActivity extends BaseActivity {

    private SecurePreferences sp;
    public final static int PAYMENT_TYPE = 221;
    public final static int PURCHASE_TYPE = 222;

    public final static String FRAG_BIL_LIST_MERCHANT = "listMerchant";
    public final static String FRAG_BIL_INPUT = "bilInput";
    public final static String FRAG_BIL_DESCRIPTION = "bilDesc";
    private FragmentManager fragmentManager;
    private String _biller_merchant_name;
    private String userID;
    private String accessKey;
    public String _biller_type_code;
    private Boolean isOneBiller;
    private Boolean isEmptyBiller;
    private List<BillerItem> billerData;
    private List<DenomDataItem> denomDataItems;
    private Realm realm;
    private RealmChangeListener realmListener;
    //    BillerActivityRF mWorkFragment;
    ProgressDialog progdialog;
    String IdNumber = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");

        if (savedInstanceState != null) {
            return;
        }

        Intent intent = getIntent();
//        realm = Realm.getInstance(RealmManager.BillerConfiguration);
        realm = Realm.getInstance(RealmManager.realmConfiguration);
//        if (intent.getStringExtra(DefineValue.FAVORITE_CUSTOMER_ID) != null) {
//
//            setActionBarIcon(R.drawable.ic_arrow_left);
//            setToolbarTitle(intent.getStringExtra(DefineValue.BILLER_TYPE) + " - " + intent.getStringExtra(DefineValue.COMMUNITY_NAME));
//
//            Bundle mArgs = new Bundle();
//            Fragment mFrag;
//            String tag = "";
//            _biller_type_code = intent.getStringExtra(DefineValue.BILLER_TYPE);
//            if (_biller_type_code .equalsIgnoreCase("AIR")) {
//                mArgs.putString(DefineValue.COMMUNITY_ID, intent.getStringExtra(DefineValue.COMMUNITY_ID));
//                mArgs.putString(DefineValue.COMMUNITY_NAME, intent.getStringExtra(DefineValue.COMMUNITY_NAME));
//                mArgs.putString(DefineValue.CUST_ID, intent.getStringExtra(DefineValue.FAVORITE_CUSTOMER_ID));
//                mArgs.putString(DefineValue.ITEM_ID, intent.getStringExtra(DefineValue.ITEM_ID));
//                if (intent.getStringExtra(DefineValue.BILLER_TYPE).equals("GAME") || intent.getStringExtra(DefineValue.BILLER_TYPE).equals("VCHR")) {
//                    mArgs.putInt(DefineValue.BUY_TYPE, PURCHASE_TYPE);
//                } else {
//                    mArgs.putInt(DefineValue.BUY_TYPE, PAYMENT_TYPE);
//                }
//
//                mArgs.putString(DefineValue.SHARE_TYPE, "ss");
//                mArgs.putString(DefineValue.BILLER_TYPE, intent.getStringExtra(DefineValue.BILLER_TYPE));
//
//                mFrag = new BillerDesciption();
//                tag = BillerInput.TAG;
//
//                mFrag.setArguments(mArgs);
//                fragmentManager = getSupportFragmentManager();
//                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.biller_content, mFrag, tag);
//                fragmentTransaction.commitAllowingStateLoss();
//                setResult(MainPage.RESULT_NORMAL);
//                return;
//            }


//            mFrag.setArguments(mArgs);
//            fragmentManager = getSupportFragmentManager();
//            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.biller_content, mFrag, tag);
//            fragmentTransaction.commitAllowingStateLoss();
//            setResult(MainPage.RESULT_NORMAL);
//        }

        _biller_type_code = intent.getStringExtra(DefineValue.BILLER_TYPE);
        Timber.d("isi biller type code " + _biller_type_code);
        _biller_merchant_name = intent.getStringExtra(DefineValue.BILLER_NAME);
        Timber.d("isi biller merchant name " + _biller_merchant_name);
        isEmptyBiller = false;

        if (intent.hasExtra(DefineValue.BILLER_ID_NUMBER)) {
            IdNumber = intent.getStringExtra(DefineValue.BILLER_ID_NUMBER);
        }
        Timber.d("isi biller activity " + intent.getExtras().toString());
        initializeToolbar();
        getBillerData();

        Log.wtf("onCreate BillerActivity", "onCreate BillerActivity");

//        //auto updater realm biller
//        realmListener = new RealmChangeListener() {
//            @Override
//            public void onChange() {
//                Timber.d("Masuk realm listener bilactive asdfasdfa");
//                if(!BillerActivity.this.isFinishing()){
//                    if(progdialog != null && progdialog.isShowing())
//                        progdialog.dismiss();
//                    if(isEmptyBiller){
//                        initializeData();
//                    }
//                    else {
//                        mBillerTypeData = realm.where(Biller_Type_Data_Model.class)
//                                .equalTo(WebParams.BILLER_TYPE_CODE, _biller_type_code)
//                                .findFirst();
//                        if(mBillerTypeData.getBiller_data_models().size() == 0) {
//                            BillerActivity.this.finish();
//                        }
//                    }
//
//                }
//            }};
//        realm.addChangeListener(realmListener);
//
//        FragmentManager fm = getSupportFragmentManager();
//        // Check to see if we have retained the worker fragment.
//        mWorkFragment = (BillerActivityRF) fm.findFragmentByTag(BillerActivityRF.BILLERACTIV_TAG);
//        // If not retained (or first time running), we need to create it.
//        if (mWorkFragment == null) {
//            mWorkFragment = new BillerActivityRF();
//            // Tell it who it is working with.
//            fm.beginTransaction().add(mWorkFragment, BillerActivityRF.BILLERACTIV_TAG).commit();
//        }
//
//        mWorkFragment.getBillerList(_biller_type_code, isOneBiller);
//
//        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
//            @Override
//            public void onBackStackChanged() {
//                if(isFragmentValid())
//                    mWorkFragment.runQueing();
//            }
//        });
    }

    private void getBillerData() {
        showProgressDialog();
        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_BILLER_DENOM, _biller_type_code);
        params.put(WebParams.USER_ID, userPhoneID);
        params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
        params.put(WebParams.BILLER_TYPE, _biller_type_code);

        Timber.d("param getBillerDenom : %s", params);

        RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_BILLER_DENOM, params, new ResponseListener() {
            @Override
            public void onResponses(JsonObject object) {
                Gson gson = new Gson();
                BillerDenomResponse billerDenomResponse = gson.fromJson(object, BillerDenomResponse.class);

                if (billerDenomResponse.getErrorCode().equals(WebParams.SUCCESS_CODE)) {
                    realm.beginTransaction();
                    realm.delete(BillerItem.class);
                    realm.copyToRealmOrUpdate(billerDenomResponse.getBiller());
                    billerData = billerDenomResponse.getBiller();
                    realm.commitTransaction();
                } else
                    Toast.makeText(getApplicationContext(), billerDenomResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {
                dismissProgressDialog();
                initializeData();
            }
        });
    }

    private void initializeData() {
//        billerData = realm.where(BillerItem.class).findAll();

        if (billerData != null) {
            Timber.d("isi billeractivity isinya " + billerData.size());

            if (billerData.size() != 0) {
                if (findViewById(R.id.biller_content) != null) {
                    isEmptyBiller = false;
                    isOneBiller = billerData.size() <= 1;
                    initializeListBiller();
                }
            } else {
                Toast.makeText(this, getString(R.string.biller_empty_data), Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    @SuppressLint("NewApi")
    private void initializeListBiller() {
        Bundle mArgs = new Bundle();
        mArgs.putString(DefineValue.BILLER_TYPE, _biller_type_code);
        mArgs.putString(DefineValue.BILLER_NAME, _biller_merchant_name);
        Fragment mLBM;
        String tag;
        Intent intent = getIntent();

        if (isOneBiller && !_biller_type_code.equalsIgnoreCase("DATA") && !_biller_type_code.equalsIgnoreCase("PLS")
                && !_biller_type_code.equalsIgnoreCase("TKN") && !_biller_type_code.equalsIgnoreCase("EMON")
                && !_biller_type_code.equalsIgnoreCase("GAME")) {
            mLBM = new BillerInput();
            if (intent.hasExtra(DefineValue.FAVORITE_CUSTOMER_ID)) {
                mArgs.putString(DefineValue.CUST_ID, intent.getStringExtra(DefineValue.FAVORITE_CUSTOMER_ID));
            }
            mArgs.putString(DefineValue.COMMUNITY_ID, billerData.get(0).getCommId());
            mArgs.putString(DefineValue.COMMUNITY_NAME, billerData.get(0).getCommName());
            mArgs.putString(DefineValue.BILLER_ITEM_ID, billerData.get(0).getItemId());
            mArgs.putString(DefineValue.BILLER_COMM_CODE, billerData.get(0).getCommCode());

            tag = BillerInput.TAG;
        } else {
            if (_biller_type_code.equalsIgnoreCase("PLS")) {
                mLBM = new BillerInputPulsa();
                if (intent.hasExtra(DefineValue.FAVORITE_CUSTOMER_ID)) {
                    mArgs.putString(DefineValue.CUST_ID, intent.getStringExtra(DefineValue.FAVORITE_CUSTOMER_ID));
                }
                tag = BillerInput.TAG;
            } else if (_biller_type_code.equalsIgnoreCase("DATA")) {
                mLBM = new BillerInputData();
                if (intent.hasExtra(DefineValue.FAVORITE_CUSTOMER_ID)) {
                    mArgs.putString(DefineValue.CUST_ID, intent.getStringExtra(DefineValue.FAVORITE_CUSTOMER_ID));
                }
                tag = BillerInput.TAG;
            } else if (_biller_type_code.equalsIgnoreCase("TKN")) {
                mLBM = new BillerInputPLN();
                if (intent.hasExtra(DefineValue.FAVORITE_CUSTOMER_ID)) {
                    mArgs.putString(DefineValue.CUST_ID, intent.getStringExtra(DefineValue.FAVORITE_CUSTOMER_ID));
                }
                tag = BillerInput.TAG;
            } else if (_biller_type_code.equalsIgnoreCase("EMON")) {
                mLBM = new FragGridEmoney();
                if (intent.hasExtra(DefineValue.FAVORITE_CUSTOMER_ID)) {
                    mArgs.putString(DefineValue.CUST_ID, intent.getStringExtra(DefineValue.FAVORITE_CUSTOMER_ID));
                }
                tag = BillerInput.TAG;
            } else if (_biller_type_code.equalsIgnoreCase("GAME")) {
                mLBM = new FragGridGame();
                ArrayList<BillerItem> billerItemArrayList = new ArrayList<>(billerData.size());
                billerItemArrayList.addAll(billerData);
                mArgs.putSerializable(DefineValue.BILLER_DATA, billerItemArrayList);
                tag = BillerInput.TAG;
            } else {
                if (intent.hasExtra(DefineValue.FAVORITE_CUSTOMER_ID)) {
                        mLBM = new BillerInput();
                    mArgs.putString(DefineValue.COMMUNITY_ID, intent.getStringExtra(DefineValue.COMMUNITY_ID));
                    mArgs.putString(DefineValue.COMMUNITY_NAME, intent.getStringExtra(DefineValue.COMMUNITY_NAME));
                    mArgs.putString(DefineValue.BILLER_ITEM_ID, intent.getStringExtra(DefineValue.ITEM_ID));
                    mArgs.putString(DefineValue.CUST_ID, intent.getStringExtra(DefineValue.FAVORITE_CUSTOMER_ID));

                    mArgs.putString(DefineValue.BILLER_TYPE, intent.getStringExtra(DefineValue.BILLER_TYPE));
                    mArgs.putString(DefineValue.BILLER_NAME, intent.getStringExtra(DefineValue.BILLER_NAME));
                    mArgs.putString(DefineValue.BILLER_COMM_CODE, intent.getStringExtra(DefineValue.BILLER_COMM_CODE));
                    tag = BillerInput.TAG;
                } else {
                    mLBM = new ListBillerMerchant();
                    tag = BillerInput.TAG;
                    Log.wtf("ListBillerMerchant ", "ListBillerMerchant");
                }
            }
        }

        if (IdNumber != null) {
            mArgs.putString(DefineValue.BILLER_ID_NUMBER, IdNumber);
            Log.wtf("IdNumber ", "IdNumber");
        }

        setToolbarTitle(getString(R.string.biller_ab_title) + " - " + _biller_merchant_name);

        mLBM.setArguments(mArgs);
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.biller_content, mLBM, tag);
        fragmentTransaction.commitAllowingStateLoss();
        setResult(MainPage.RESULT_NORMAL);
        Log.wtf("initializeBiller ", "initializeBiller");
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_biller;
    }

    public void setToolbarTitle(String _title) {
        setActionBarTitle(_title);
    }

//    public void updateDenom(String comm_id, String comm_name) {
//        if (mWorkFragment != null)
//            mWorkFragment.getDenomRetail(comm_id, comm_name);
//    }

    public void switchContent(Fragment mFragment, String fragName, String next_frag_title, Boolean isBackstack, String tag) {

        if (isBackstack) {
            Timber.d("backstack:" + "masuk");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.biller_content, mFragment, tag)
                    .addToBackStack(fragName)
                    .commitAllowingStateLoss();
        } else {
            Timber.d("bukan backstack:" + "masuk");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.biller_content, mFragment, tag)
                    .commitAllowingStateLoss();

        }
        if (next_frag_title != null) setActionBarTitle(next_frag_title);
        ToggleKeyboard.hide_keyboard(this);
    }

    public void switchContent1(Fragment mFragment, String fragName, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.biller_content, mFragment)
                .addToBackStack(tag)
                .commitAllowingStateLoss();
        setToolbarTitle(fragName);
    }

    public void switchActivity(Intent mIntent, int j) {
        switch (j) {
            case MainPage.ACTIVITY_RESULT:
                startActivityForResult(mIntent, MainPage.REQUEST_FINISH);
                this.setResult(MainPage.RESULT_BALANCE);
                break;
            case 2:
                break;
        }
        ToggleKeyboard.hide_keyboard(this);
    }

    public void setResultActivity(int result) {
        setResult(MainPage.RESULT_BALANCE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.d("onActivity result", "Biller Activity"+" / "+requestCode+" / "+resultCode);
        if (requestCode == MainPage.REQUEST_FINISH) {
//            Log.d("onActivity result", "Biller Activity masuk request exit");
            if (resultCode == MainPage.RESULT_BILLER) {
//                Log.d("onActivity result", "Biller Activity masuk result normal" + " / " + getSupportFragmentManager().getBackStackEntryCount());
                if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                    FragmentManager fm = getSupportFragmentManager();
                    fm.popBackStackImmediate(BillerActivity.FRAG_BIL_INPUT, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//                    Log.d("onActivity result", "Biller Activity masuk backstack entry > 1");
                }
            } else if (resultCode == MainPage.RESULT_LOGOUT) {
                setResult(MainPage.RESULT_LOGOUT);
                finish();
            }

        }

    }

    private void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.biller_ab_title));
    }

    public void togglerBroadcastReceiver(Boolean _on, BroadcastReceiver _myreceiver) {
        Timber.wtf("masuk turnOnBR:" + "oke");
        if (_on) {
            IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            registerReceiver(_myreceiver, filter);
            filter.setPriority(999);
            filter.addCategory("android.intent.category.DEFAULT");
        } else unregisterReceiver(_myreceiver);

    }

    public Boolean isFragmentValid() {
        BillerDesciption myFragment = (BillerDesciption) getSupportFragmentManager().findFragmentByTag(BillerDesciption.TAG);
        return !(myFragment != null && myFragment.isVisible());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        if (realm != null && !realm.isInTransaction() && !realm.isClosed()) {
//            realm.removeChangeListener(realmListener);
            realm.close();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0)
            getFragmentManager().popBackStack();
        else super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}