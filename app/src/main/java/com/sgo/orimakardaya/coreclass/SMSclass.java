package com.sgo.orimakardaya.coreclass;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

/**
 * Created by yuddistirakiki on 5/18/16.
 */
public class SMSclass {

    private Context mContext;

    public interface SMS_SIM_STATE{
        void sim_state(Boolean isExist, String msg);

    }

    public SMSclass(){

    }

    public SMSclass(Context _context){
        this.setmContext(_context);
    }

    public void sendSMS(String phoneNo,String msg){
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getmContext(), "Message Sent",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getmContext(), ex.getMessage(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    public void isSimExists(SMS_SIM_STATE listener)
    {
        SMS_SIM_STATE response = listener;

        TelephonyManager telephonyManager = (TelephonyManager) getmContext().getSystemService(Context.TELEPHONY_SERVICE);
        int SIM_STATE = telephonyManager.getSimState();

        if(SIM_STATE == TelephonyManager.SIM_STATE_READY)
            response.sim_state(true,"Ada isinya");
        else
        {
            String SimState = "Access Sim Failed!";
            switch(SIM_STATE)
            {
                case TelephonyManager.SIM_STATE_ABSENT:
                    SimState = "No Sim Found!";
                    break;
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                    SimState = "Network Locked!";
                    break;
                case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                    SimState = "PIN Required to access SIM!";
                    break;
                case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                    SimState = "PUK Required to access SIM!"; // Personal Unblocking Code
                    break;
                case TelephonyManager.SIM_STATE_UNKNOWN:
                    SimState = "Unknown SIM State!";
                    break;
            }
            response.sim_state(false,SimState);
        }
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }
}
