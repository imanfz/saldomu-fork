package com.sgo.orimakardaya.coreclass;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 5/18/16.
 */
public class SMSclass {

    private Context mContext;
    TelephonyManager telephonyManager;
    BroadcastReceiver receiverSent;
    BroadcastReceiver receiverDelivered;

    private static final String SMS_VERIFY = "REG IMEI "+MyApiClient.COMM_ID;

    public interface SMS_SIM_STATE{
        void sim_state(Boolean isExist, String msg);

    }

    public interface SMS_VERIFY_LISTENER{
        void success();
        void failed();

    }

    public SMSclass(){

    }

    public SMSclass(Context _context){
        this.setmContext(_context);
        telephonyManager = (TelephonyManager) getmContext().getSystemService(Context.TELEPHONY_SERVICE);
    }

    public void sendSMSVerify(String phoneNo,String imei, String iccid,String TimeStamp, SMS_VERIFY_LISTENER listener){

        String msg = SMS_VERIFY + " "+imei+"_"+iccid+"_"+TimeStamp+"_"+MyApiClient.APP_ID;
        sendSMS(phoneNo,msg,listener);

    }

    private void sendSMS(final String phoneNumber, final String message, final SMS_VERIFY_LISTENER listener)
    {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(getmContext(), 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(getmContext(), 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---

        receiverSent = new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:

                        break;
                    default:
                        listener.failed();
                        Toast.makeText(getmContext(), getmContext().getString(R.string.toast_msg_fail_smsclass),
                                Toast.LENGTH_SHORT).show();
                        deleteSMS(message,phoneNumber);
                        break;

                }
            }
        };

        receiverDelivered = new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                deleteSMS(message,phoneNumber);
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        listener.success();
                        Toast.makeText(getmContext(), getmContext().getString(R.string.toast_msg_success_smsclass),
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        listener.failed();
                        Toast.makeText(getmContext(), getmContext().getString(R.string.toast_msg_fail_smsclass),
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        getmContext().registerReceiver(receiverSent, new IntentFilter(SENT));
        //---when the SMS has been delivered---
        getmContext().registerReceiver(receiverDelivered, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
        Timber.d("Send message sms : "+ message);
    }

    public void Close(){
        if(receiverSent != null)
            getmContext().unregisterReceiver(receiverSent);
        if(receiverDelivered != null)
            getmContext().unregisterReceiver(receiverDelivered);
    }



    public void isSimExists(SMS_SIM_STATE listener)
    {

        int SIM_STATE = telephonyManager.getSimState();

        if(SIM_STATE == TelephonyManager.SIM_STATE_READY)
            listener.sim_state(true,"Ada isinya");
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
            listener.sim_state(false,SimState);
        }
    }


    public Boolean isSimSameSP()
    {
        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        String imei = sp.getString(DefineValue.DEIMEI,"");
        String iccid = sp.getString(DefineValue.DEICCID,"");

        if(!imei.isEmpty()){
            if(!iccid.isEmpty()){
                String diccid = getDeviceICCID();
                if(diccid != null) {
                    if (diccid.equals(iccid) && getDeviceIMEI().equals(imei))
                        return true;
                }
            }
        }

        return false;
    }

    public String getDeviceIMEI(){
        return telephonyManager.getDeviceId();
    }

    public String getDeviceICCID(){
        return telephonyManager.getSimSerialNumber();
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }


    public List<SimInfo> getSIMInfo() {
        List<SimInfo> simInfoList = new ArrayList<>();
        Uri URI_TELEPHONY = Uri.parse("content://telephony/siminfo/");
        Cursor c = getmContext().getContentResolver().query(URI_TELEPHONY, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndex("_id"));
                int slot = 0;
                if(c.getColumnIndex("slot") != -1)
                    slot = c.getInt(c.getColumnIndex("slot"));
                String display_name = c.getString(c.getColumnIndex("display_name"));
                String icc_id = c.getString(c.getColumnIndex("icc_id"));
                SimInfo simInfo = new SimInfo(id, display_name, icc_id, slot);
                Timber.d("apipas_sim_info" + simInfo.toString());
                simInfoList.add(simInfo);
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }

        return simInfoList;
    }

    public class SimInfo {
        private int id_;
        private String display_name;
        private String icc_id;
        private int slot;

        public SimInfo(int id_, String display_name, String icc_id, int slot) {
            this.id_ = id_;
            this.display_name = display_name;
            this.icc_id = icc_id;
            this.slot = slot;
        }

        public int getId_() {
            return id_;
        }

        public String getDisplay_name() {
            return display_name;
        }

        public String getIcc_id() {
            return icc_id;
        }

        public int getSlot() {
            return slot;
        }

        @Override
        public String toString() {
            return "SimInfo{" +
                    "id_=" + id_ +
                    ", display_name='" + display_name + '\'' +
                    ", icc_id='" + icc_id + '\'' +
                    ", slot=" + slot +
                    '}';
        }
    }

    private void deleteSMS( String message, String number) {
        if(!isWriteEnabled(mContext.getApplicationContext())) {
            setWriteEnabled(mContext.getApplicationContext(), true);
        }

        try {
            Uri uriSms = Uri.parse("content://sms");
            Cursor c = mContext.getContentResolver().query(uriSms,
                    new String[] { "_id", "thread_id", "address",
                            "person", "date", "body" }, null, null, null);

            if (c != null && c.moveToFirst()) {
                do {
                    long id = c.getLong(0);
                    String address = c.getString(2);
                    String body = c.getString(5);

                    if (message.equals(body) && address.equals(number)) {
                        // mLogger.logInfo("Deleting SMS with id: " + threadId);
                        mContext.getContentResolver().delete(Uri.parse("content://sms/" + id), "date=?",new String[] { c.getString(4) });
                        break;
                    }
                } while (c.moveToNext());
            }

            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            Timber.e("log>>>" + e.toString());
            Timber.e("log>>>" + e.getMessage());
        }
    }

    private static final int OP_WRITE_SMS = 15;

    public static boolean isWriteEnabled(Context context) {
        int uid = getUid(context);
        Object opRes = checkOp(context, OP_WRITE_SMS, uid);

        if (opRes instanceof Integer) {
            return (Integer) opRes == AppOpsManager.MODE_ALLOWED;
        }
        return false;
    }

    public static boolean setWriteEnabled(Context context, boolean enabled) {
        int uid = getUid(context);
        int mode = enabled ?
                AppOpsManager.MODE_ALLOWED : AppOpsManager.MODE_IGNORED;

        return setMode(context, OP_WRITE_SMS, uid, mode);
    }

    private static Object checkOp(Context context, int code, int uid) {
        AppOpsManager appOpsManager =
                (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        Class appOpsManagerClass = appOpsManager.getClass();

        try {
            Class[] types = new Class[3];
            types[0] = Integer.TYPE;
            types[1] = Integer.TYPE;
            types[2] = String.class;
            Method checkOpMethod =
                    appOpsManagerClass.getMethod("checkOp", types);

            Object[] args = new Object[3];
            args[0] = Integer.valueOf(code);
            args[1] = Integer.valueOf(uid);
            args[2] = context.getPackageName();
            Object result = checkOpMethod.invoke(appOpsManager, args);

            return result;
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean setMode(Context context, int code,
                                   int uid, int mode) {
        AppOpsManager appOpsManager =
                (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        Class appOpsManagerClass = appOpsManager.getClass();

        try {
            Class[] types = new Class[4];
            types[0] = Integer.TYPE;
            types[1] = Integer.TYPE;
            types[2] = String.class;
            types[3] = Integer.TYPE;
            Method setModeMethod =
                    appOpsManagerClass.getMethod("setMode", types);

            Object[] args = new Object[4];
            args[0] = Integer.valueOf(code);
            args[1] = Integer.valueOf(uid);
            args[2] = context.getPackageName();
            args[3] = Integer.valueOf(mode);
            setModeMethod.invoke(appOpsManager, args);

            return true;
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static int getUid(Context context) {
        try {

            return context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA).uid;
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

}
