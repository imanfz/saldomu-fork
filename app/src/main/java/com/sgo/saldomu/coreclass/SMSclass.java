package com.sgo.saldomu.coreclass;

import android.Manifest;
import android.annotation.TargetApi;
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
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/**
 * Created by yuddistirakiki on 5/18/16.
 */
public class SMSclass {

    private Context mContext;
    private TelephonyManager telephonyManager;
    private BroadcastReceiver receiverSent;
    private BroadcastReceiver receiverDelivered;

    private static final String SMS_VERIFY = "REG EMO " + MyApiClient.COMM_ID;
    String[] perms = {Manifest.permission.READ_PHONE_STATE};

    public interface SMS_SIM_STATE {
        void sim_state(Boolean isExist, String msg);

    }

    public interface SMS_VERIFY_LISTENER {
        void success();

        void failed();

    }

    public BroadcastReceiver simStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase("android.intent.action.SIM_STATE_CHANGED")) {
                if (intent.getStringExtra("ss").equalsIgnoreCase("ABSENT")) {
                    if (getmContext() instanceof Activity) {
                        Timber.d("sim is Absent");
                        if (!isSimExists()) {
                            ((Activity) getmContext()).finish();
                            Toast.makeText(getmContext(), R.string.sim_not_found, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

            }
        }
    };

    public static IntentFilter simStateIntentFilter = new IntentFilter("android.intent.action.SIM_STATE_CHANGED");

    public SMSclass(Context _context) {
        this(_context, null);
    }

    public SMSclass(Context _context, BroadcastReceiver simListener) {
        this.setmContext(_context);
        telephonyManager = (TelephonyManager) getmContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (simListener != null)
            simStateReceiver = simListener;
    }

    public void sendSMSVerify(String phoneNo, String imei, String iccid, String TimeStamp, String DateTime, SMS_VERIFY_LISTENER listener) {

        String msg = SMS_VERIFY + " " + imei + "_" + iccid + "_" + TimeStamp + "_" + MyApiClient.APP_ID + "_" + DateTime;
        sendSMS(phoneNo, msg, listener);

    }

    private void sendSMS(final String phoneNumber, final String message, final SMS_VERIFY_LISTENER listener) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(getmContext(), 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(getmContext(), 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---

        receiverSent = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:

                        break;
                    default:
                        listener.failed();
                        Toast.makeText(getmContext(), getmContext().getString(R.string.toast_msg_fail_smsclass),
                                Toast.LENGTH_SHORT).show();
                        deleteSMS(message, phoneNumber);
                        break;

                }
            }
        };

        receiverDelivered = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                deleteSMS(message, phoneNumber);
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        listener.success();
                        Timber.d("Sukses send Message SMS");
                        Toast.makeText(getmContext(), getmContext().getString(R.string.toast_msg_success_smsclass),
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        listener.failed();
                        Timber.d("Gagal send Message SMS");
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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager subscriptionManager = SubscriptionManager.from(mContext);
            if (ContextCompat.checkSelfPermission(getmContext(), android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
                List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
                Timber.d("subscriptionId slot 1 = " + subscriptionInfoList.get(0).getSubscriptionId());
                if (SmsManager.getSmsManagerForSubscriptionId(subscriptionInfoList.get(0).getSubscriptionId()) != null)
                    SmsManager.getSmsManagerForSubscriptionId(subscriptionInfoList.get(0).getSubscriptionId()).sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
                else
                    sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
            }
        } else {
            if (sendSMS(mContext, getIndexActiveSIMSlot(), phoneNumber, null, message, sentPI, deliveredPI))
                Timber.d("Sukses jalanin sendSMS dibawah Lollipop");
        }
        Timber.d("Send message sms : " + message);

    }

    public void Close() {
        if (receiverSent != null)
            getmContext().unregisterReceiver(receiverSent);
        if (receiverDelivered != null)
            getmContext().unregisterReceiver(receiverDelivered);
    }

    private static boolean getSIMStateBySlot(Context context, String predictedMethodName, int slotID) throws GeminiMethodNotFoundException {

        boolean isReady = false;

        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        try {

            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimStateGemini = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimStateGemini.invoke(telephony, obParameter);

            if (ob_phone != null) {
                int simState = Integer.parseInt(ob_phone.toString());
                if (simState == TelephonyManager.SIM_STATE_READY) {
                    isReady = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return isReady;
    }

    private static class GeminiMethodNotFoundException extends Exception {

        private static final long serialVersionUID = -996812356902545308L;

        public GeminiMethodNotFoundException(String info) {
            super(info);
        }
    }


    public boolean isSimExists() {
        return isSimExists(null);
    }

    public boolean isSimExists(SMS_SIM_STATE listener) {
        if (!EasyPermissions.hasPermissions(getmContext(), perms)) {
            return false;
        } else {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 && EasyPermissions.hasPermissions(getmContext(), perms)) {
                SubscriptionManager subscriptionManager = SubscriptionManager.from(mContext);
                if (ContextCompat.checkSelfPermission(getmContext(), android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
                    if (subscriptionInfoList != null && subscriptionInfoList.size() > 0) {
                        if (SmsManager.getSmsManagerForSubscriptionId(subscriptionInfoList.get(0).getSubscriptionId()) != null) {
                            if (listener != null)
                                listener.sim_state(true, "Ada isinya");
                            return true;
                        } else {
                            if (listener != null)
                                listener.sim_state(false, getmContext().getString(R.string.sim_not_found));
                            return false;
                        }
                    } else {
                        if (listener != null)
                            listener.sim_state(false, getmContext().getString(R.string.sim_not_found));
                        return false;
                    }
                }
            } else {

                int SIM_STATE = telephonyManager.getSimState();

                if (SIM_STATE == TelephonyManager.SIM_STATE_READY) {
                    if (listener != null)
                        listener.sim_state(true, "Ada isinya");
                    return true;
                } else {
                    String SimState = getmContext().getString(R.string.sim_failed);
                    switch (SIM_STATE) {
                        case TelephonyManager.SIM_STATE_ABSENT:
                            SimState = getmContext().getString(R.string.sim_not_found);
                            break;
                        case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                            SimState = getmContext().getString(R.string.sim_network_locked);
                            break;
                        case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                            SimState = getmContext().getString(R.string.sim_pin_required);
                            break;
                        case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                            SimState = getmContext().getString(R.string.sim_puk_required);
                            break;
                        case TelephonyManager.SIM_STATE_UNKNOWN:
                            SimState = getmContext().getString(R.string.sim_failed);
                            break;
                    }
                    if (listener != null)
                        listener.sim_state(false, SimState);
                    return false;
                }
            }
        }
        return true;
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
        String imei = "";

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager sm = SubscriptionManager.from(mContext);
            if (ContextCompat.checkSelfPermission(getmContext(), android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED ) {
                List<SubscriptionInfo> sis = sm.getActiveSubscriptionInfoList();
                imei = telephonyManager.getDeviceId(sis.get(0).getSimSlotIndex());
            }
        } else {
            if ( isPermissionGranted() ) {
                if (telephonyManager.getDeviceId() == null)
                    imei = "00000";
                else
                    imei = telephonyManager.getDeviceId();
            }
        }

        return imei;
    }

    public String getSimNumber(){
        if (ContextCompat.checkSelfPermission(getmContext(), android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED ) {
            return telephonyManager.getLine1Number();
        }
        return "";
    }

    public Boolean isPermissionGranted() {
        if (ContextCompat.checkSelfPermission(getmContext(), android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED ) {
            return true;
        }
        return false;

    }


    /**
     * Return iccid untuk lollipop 5.1 keatas akan diambil iccid di simcard 1,
     * sedangkan lollipop 5.1 kebawah default ambil iccid di simcard 1 juga.
     * Terkecuali kondisi dimana simcard dimasukkan hanya di slot 2, maka otomatis ambil
     * iccid di slot ke 2
     * @return iccid di simcard slot 1
     */
    public String getDeviceICCID(){
        String iccId="";
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager sm = SubscriptionManager.from(mContext);
            if (ContextCompat.checkSelfPermission(getmContext(), android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
                List<SubscriptionInfo> sis = sm.getActiveSubscriptionInfoList();
                SubscriptionInfo si = sis.get(0);
                iccId = si.getIccId();
            }
        }
        else {
            if (ContextCompat.checkSelfPermission(getmContext(), android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED  && telephonyManager.getSimSerialNumber() == null)
                iccId = "00000";
            else
                iccId = telephonyManager.getSimSerialNumber();
        }

        return iccId;
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

    private int getIndexActiveSIMSlot(){
        boolean isSIM1Ready = false;
        boolean isSIM2Ready = false;

        try {
            isSIM1Ready = getSIMStateBySlot(mContext, "getSimStateGemini", 0);
        } catch (GeminiMethodNotFoundException e) {
            e.printStackTrace();
            try {
                isSIM1Ready = getSIMStateBySlot(mContext, "getSimState", 0);
            } catch (GeminiMethodNotFoundException e1) {
                //Call here for next manufacturer's predicted method name if you wish
                e1.printStackTrace();
            }
        }

        try {
            isSIM2Ready = getSIMStateBySlot(mContext, "getSimStateGemini", 1);
        } catch (GeminiMethodNotFoundException e) {
            e.printStackTrace();
            try {
                isSIM2Ready = getSIMStateBySlot(mContext, "getSimState", 1);
            } catch (GeminiMethodNotFoundException e1) {
                //Call here for next manufacturer's predicted method name if you wish
                e1.printStackTrace();
            }
        }

        if(isSIM1Ready)
            return 0;

        if(isSIM2Ready)
            return 1;

        return 0;
    }

    private void deleteSMS( String message, String number) {
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            // only for gingerbread and newer versions
            if(!isWriteEnabled(mContext.getApplicationContext())) {
                setWriteEnabled(mContext.getApplicationContext(), true);
            }
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
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
            args[0] = code;
            args[1] = uid;
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
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
            args[0] = code;
            args[1] = uid;
            args[2] = context.getPackageName();
            args[3] = mode;
            setModeMethod.invoke(appOpsManager, args);

            return true;
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
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


    public boolean sendSMS(Context ctx, int simID, String toNum, String centerNum, String smsText, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        String name;

        try {
            if (simID == 0) {
                Timber.d("send SMS using sim 1");
                name = "isms";
                // for model : "Philips T939" name = "isms0"
            } else if (simID == 1) {
                Timber.d("send SMS using sim 2");
                name = "isms2";
            } else {
                throw new Exception("can not get service which for sim '" + simID + "', only 0,1 accepted as values");
            }
            Method method = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            method.setAccessible(true);
            Object param = method.invoke(null, name);

            method = Class.forName("com.android.internal.telephony.ISms$Stub").getDeclaredMethod("asInterface", IBinder.class);
            method.setAccessible(true);
            Object stubObj = method.invoke(null, param);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                method = stubObj.getClass().getMethod("sendText", String.class, String.class, String.class, PendingIntent.class, PendingIntent.class);
                method.invoke(stubObj, toNum, centerNum, smsText, sentIntent, deliveryIntent);
            } else {
                method = stubObj.getClass().getMethod("sendText", String.class, String.class, String.class, String.class, PendingIntent.class, PendingIntent.class);
                method.invoke(stubObj, ctx.getPackageName(), toNum, centerNum, smsText, sentIntent, deliveryIntent);
            }

            return true;
        } catch (ClassNotFoundException e) {
            Timber.e( "ClassNotFoundException:" + e.getMessage());
        } catch (NoSuchMethodException e) {
            Timber.e("NoSuchMethodException:" + e.getMessage());
        } catch (InvocationTargetException e) {
            Timber.e("InvocationTargetException:" + e.getMessage());
        } catch (IllegalAccessException e) {
            Timber.e("IllegalAccessException:" + e.getMessage());
        } catch (Exception e) {
            Timber.e("Exception:" + e.getMessage());
        }
        return false;
    }


}
