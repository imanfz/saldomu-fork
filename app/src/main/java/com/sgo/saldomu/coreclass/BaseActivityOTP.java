package com.sgo.saldomu.coreclass;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.view.Menu;

import androidx.fragment.app.Fragment;

import com.sgo.saldomu.R;
import com.sgo.saldomu.widgets.BaseActivity;

import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 8/25/2017.
 */

public abstract class BaseActivityOTP extends BaseActivity {

    private Fragment currentFragment;

    public interface GetSMSOTP{
        void onGetSMSOTP(String kodeOTP, String member_code);
    }

    private GetSMSOTP getSMSOTP;

    protected void setCurrentFragment(Fragment currentFragment){
        this.currentFragment = currentFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        setCurrentFragment(fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void togglerBroadcastReceiver(Boolean _on){
        if(currentFragment instanceof GetSMSOTP) {
            getSMSOTP = (GetSMSOTP) currentFragment;
            Timber.wtf("masuk turnOnBR cashout");
            if(_on){
                IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
                registerReceiver(myReceiver,filter);
                filter.setPriority(999);
                filter.addCategory("android.intent.category.DEFAULT");
            }
            else unregisterReceiver(myReceiver);
        }
        else {
            Timber.d("%s must implement OnFragmentInteractionListener", currentFragment.toString());
            try {
                unregisterReceiver(myReceiver);
            } catch (IllegalArgumentException e) {
                Timber.i("Receiver is already unregistered");
            }

        }


    }

    public BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle mBundle = intent.getExtras();
            SmsMessage[] mSMS;
            String strMessage = "";
            String _kode_otp = "";
            String _member_code = "";
            String[] kode = context.getResources().getStringArray(R.array.broadcast_kode_compare);

            if(mBundle != null){
                Object[] pdus = (Object[]) mBundle.get("pdus");
                mSMS = new SmsMessage[pdus.length];

                for (int i = 0; i < mSMS.length ; i++){
                    mSMS[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                    strMessage += mSMS[i].getMessageBody();
                    strMessage += "\n";
                }

                String[] words = strMessage.split(" ");
                for (int i = 0 ; i <words.length;i++)
                {
                    if(_kode_otp.equalsIgnoreCase("")){
                        if(words[i].equalsIgnoreCase(kode[0])){
                            if(words[i+1].equalsIgnoreCase(kode[1]))
                                _kode_otp = words[i+2];
                            _kode_otp =  _kode_otp.replace(".","").replace(" ","");
                        }
                    }

                    if(_member_code.equals("")){
                        if(words[i].equalsIgnoreCase(kode[2]))
                            _member_code = words[i+1];
                    }
                }

                getSMSOTP.onGetSMSOTP(_kode_otp,_member_code);
                //Toast.makeText(context,strMessage,Toast.LENGTH_SHORT).show();
            }
        }
    };



}