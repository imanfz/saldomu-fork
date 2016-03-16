package com.sgo.orimakardaya.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.*;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import com.sgo.orimakardaya.fragments.BillerInput;
import com.sgo.orimakardaya.fragments.ListBillerMerchant;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/*
  Created by Administrator on 3/4/2015.
 */
public class BillerActivity extends BaseActivity {

    SecurePreferences sp;
    public final static int PAYMENT_TYPE = 221;
    public final static int PURCHASE_TYPE = 222;

    public final static String FRAG_BIL_LIST_MERCHANT = "listMerchant";
    public final static String FRAG_BIL_INPUT = "bilInput";
    public final static String FRAG_BIL_DESCRIPTION = "bilDesc";
    public final static String FRAG_BIL_CONFIRM = "bilConfirm";
    JSONArray mDataArray;
    FragmentManager fragmentManager;
    String _biller_data, _biller_name, _biller_type, _biller_id,_biller_item_id,_biller_comm_id , _biller_comm_code,
            _biller_merchant_name, _biller_api_key, _biller_call_back,userID,accessKey;
    ProgressDialog out;
    Boolean isOneBiller;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        Intent intent    = getIntent();
        _biller_data = intent.getStringExtra(DefineValue.BILLER_DATA);
        _biller_name = intent.getStringExtra(DefineValue.BILLER_NAME);
        _biller_type = intent.getStringExtra(DefineValue.BILLER_TYPE);

        InitializeToolbar();

        try {
            mDataArray = new JSONArray(_biller_data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(mDataArray.length() != 0) {
            if (findViewById(R.id.biller_content) != null) {
                if (savedInstanceState != null) {
                    return;
                }

                if (mDataArray.length() > 1) {
                    isOneBiller = false;
                    initializeListBiller(null);
                } else {
                    try {
                        isOneBiller = true;
                        _biller_item_id = mDataArray.getJSONObject(0).getString(WebParams.DENOM_ITEM_ID);
                        _biller_comm_id = mDataArray.getJSONObject(0).getString(WebParams.COMM_ID);
                        _biller_comm_code = mDataArray.getJSONObject(0).getString(WebParams.COMM_CODE);
                        _biller_merchant_name = mDataArray.getJSONObject(0).getString(WebParams.COMM_NAME);
                        _biller_api_key = mDataArray.getJSONObject(0).getString(WebParams.API_KEY);
                        _biller_call_back = mDataArray.getJSONObject(0).getString(WebParams.CALLBACK_URL);
                        if (_biller_item_id.isEmpty())
                            getDenomRetail(_biller_comm_id);
                        else
                            initializeListBiller(null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else {
            Toast.makeText(this,getString(R.string.biller_empty_data),Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }


    public void initializeListBiller(String _array_denom){
        Bundle mArgs = new Bundle();
        mArgs.putString(DefineValue.BILLER_DATA,_biller_data);
        mArgs.putString(DefineValue.BILLER_NAME,_biller_name);
        mArgs.putString(DefineValue.BILLER_TYPE, _biller_type);

        Fragment mLBM ;

        if(isOneBiller){
            mLBM = new BillerInput();
            mArgs.putString(DefineValue.BILLER_ITEM_ID,_biller_item_id);
            mArgs.putString(DefineValue.BILLER_COMM_ID,_biller_comm_id);
            mArgs.putString(DefineValue.BILLER_COMM_CODE,_biller_comm_code);
            mArgs.putString(DefineValue.BILLER_API_KEY,_biller_api_key);
            mArgs.putString(DefineValue.COMMUNITY_NAME,_biller_merchant_name);
            mArgs.putString(DefineValue.CALLBACK_URL,_biller_call_back);
            if(_biller_item_id.isEmpty())
                mArgs.putString(DefineValue.DENOM_DATA,_array_denom);
        }
        else mLBM = new ListBillerMerchant();

        mLBM.setArguments(mArgs);
        fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.biller_content, mLBM,getString(R.string.biller_ab_title));
        fragmentTransaction.commit();
        setResult(MainPage.RESULT_NORMAL);

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_biller;
    }

    public void setToolbarTitle(String _title) {
        setActionBarTitle(_title);
    }

    /*public void getCommEspay(final String comm_id){
        try{
            out = DefinedDialog.CreateProgressDialog(this, null);
            out.show();

            RequestParams params = new RequestParams();
            params.put(WebParams.COMM_ID, comm_id);

            Timber.d("isi params sent Comm Espay :"+ params.toString());

            MyApiClient.sentCommEspay(params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Timber.d("Isi response comm Espay:"+ response.toString());
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            if(_biller_item_id.isEmpty())
                                getDenomRetail(comm_id, response);
                            else {
                                initializeListBiller(null, response);
                                out.dismiss();
                            }
                        } else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(BillerActivity.this, code, Toast.LENGTH_LONG).show();
                            BillerActivity.this.finish();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.w("Error Koneksi get Biller Type", throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient", e.getMessage());
        }
    }*/

    public void getDenomRetail(String _comm_id){
        try{

            out = DefinedDialog.CreateProgressDialog(this, "");


            RequestParams params = MyApiClient.getSignatureWithParams(_comm_id,MyApiClient.LINK_DENOM_RETAIL,
                    userID,accessKey);
            //params.put(WebParams.BILLER_ID, _biller_id);
            params.put(WebParams.COMM_ID, _comm_id);
            params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE,""));

            Timber.d("isi params sent Denom Retail:" + params.toString());

            MyApiClient.sentDenomRetail(params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        if(!isFinishing())
                            out.dismiss();
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("Isi response Denom Retail:"+response.toString());
                            String arrayDenom = response.getString(WebParams.DENOM_DATA);
                            initializeListBiller(arrayDenom);
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(BillerActivity.this,message);
                        }
                        else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(BillerActivity.this, code, Toast.LENGTH_LONG).show();
                            BillerActivity.this.finish();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if(out.isShowing())
                            out.dismiss();
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
                    if(out.isShowing())
                        out.dismiss();
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(BillerActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(BillerActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                    BillerActivity.this.finish();
                    Timber.w("Error Koneksi get denom retail:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    public void switchContent(Fragment mFragment,String fragName,String next_frag_title,Boolean isBackstack) {

        if(isBackstack){
            Timber.d("backstack:"+ "masuk");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.biller_content, mFragment, fragName)
                    .addToBackStack(fragName)
                    .commit();
        }
        else {
            Timber.d("bukan backstack:"+"masuk");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.biller_content, mFragment, fragName)
                    .commit();

        }
        if(next_frag_title!=null)setActionBarTitle(next_frag_title);
        ToggleKeyboard.hide_keyboard(this);
    }

    public void switchActivity(Intent mIntent, int j) {
        switch (j){
            case MainPage.ACTIVITY_RESULT:
                startActivityForResult(mIntent,MainPage.REQUEST_FINISH);
                this.setResult(MainPage.RESULT_BALANCE);
                break;
            case 2:
                break;
        }
        ToggleKeyboard.hide_keyboard(this);
    }

    public void setResultActivity(int result){
        setResult(MainPage.RESULT_BALANCE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.d("onActivity result", "Biller Activity"+" / "+requestCode+" / "+resultCode);
        if (requestCode == MainPage.REQUEST_FINISH) {
//            Log.d("onActivity result", "Biller Activity masuk request exit");
            if(resultCode == MainPage.RESULT_BILLER){
//                Log.d("onActivity result", "Biller Activity masuk result normal" + " / " + getSupportFragmentManager().getBackStackEntryCount());
                if(getSupportFragmentManager().getBackStackEntryCount()>1){
                    FragmentManager fm = getSupportFragmentManager();
                    fm.popBackStack(BillerActivity.FRAG_BIL_INPUT, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//                    Log.d("onActivity result", "Biller Activity masuk backstack entry > 1");
                }
            }
            else if (resultCode == MainPage.RESULT_LOGOUT) {
                setResult(MainPage.RESULT_LOGOUT);
                finish();
            }

        }

    }

    public void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.biller_ab_title));
    }

    public void togglerBroadcastReceiver(Boolean _on, BroadcastReceiver _myreceiver){
        Timber.wtf("masuk turnOnBR:"+"oke");
        if(_on){
            IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            registerReceiver(_myreceiver,filter);
            filter.setPriority(999);
            filter.addCategory("android.intent.category.DEFAULT");
        }
        else unregisterReceiver(_myreceiver);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount()>0)getFragmentManager().popBackStack();
        else super.onBackPressed();
    }
}