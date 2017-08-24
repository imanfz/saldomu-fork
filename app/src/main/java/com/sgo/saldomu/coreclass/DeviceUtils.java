package com.sgo.saldomu.coreclass;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by yuddistirakiki on 3/3/16.
 */
public class DeviceUtils {

    public String getWifiMcAddress(){
        WifiManager wifiManager = (WifiManager) CoreApp.getAppContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        return wInfo.getMacAddress();
    }

    public String getDeviceModelID(){
        return Build.MODEL;
    }

    public static String getAndroidID(){
        return Settings.Secure.getString(CoreApp.getAppContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        manufacturer = manufacturer.substring(0, 1).toUpperCase() + manufacturer.substring(1);

        String model = Build.MODEL;
        model = model.substring(0, 1).toUpperCase() + model.substring(1);

        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    public static String getDeviceAPILevel() {
        return Build.VERSION.SDK_INT + "";
    }

    public static String getDeviceOS() {
        Field[] fields = Build.VERSION_CODES.class.getFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            int fieldValue = -1;

            try {
                fieldValue = field.getInt(new Object());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            if (fieldValue == Build.VERSION.SDK_INT) {
                return fieldName;
            }
        }
        return "UNSPECIFIED";
    }

    public static double getLastLng() {
        SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
        return Double.parseDouble(prefs.getString(DefineValue.LAST_CURRENT_LONGITUDE, "0"));
    }

    public static double getLastlat() {
        SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
        return Double.parseDouble(prefs.getString(DefineValue.LAST_CURRENT_LATITUDE, "0"));
    }

    public static String getDeviceMemory() {
        ActivityManager actManager = (ActivityManager) CoreApp.getAppContext().getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        double totalMemory = (double)memInfo.totalMem;
        String lastValue;

        double mb = totalMemory / 1024.0;
        double gb = totalMemory / 1048576.0;
        double tb = totalMemory / 1073741824.0;

        DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
        if (tb > 1) {
            lastValue = twoDecimalForm.format(tb).concat(" TB");
        } else if (gb > 1) {
            lastValue = twoDecimalForm.format(gb).concat(" GB");
        } else if (mb > 1) {
            lastValue = twoDecimalForm.format(mb).concat(" MB");
        } else {
            lastValue = twoDecimalForm.format(totalMemory).concat(" KB");
        }

        return lastValue;
    }

    public static String getPinCode() {
        Context mContext = CoreApp.getAppContext();
        Geocoder geoCoder = new Geocoder(mContext, Locale.getDefault());
        List<Address> address;
        try {
            address = geoCoder.getFromLocation(getLastlat(), getLastLng(), 1);
            if (address.size() > 0) {
                String postCode = address.get(0).getPostalCode();

                if (postCode != null) {
                    return postCode;
                }
            }
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return "";
    }

    public static String getDeviceTimeZone() {
        return TimeZone.getDefault().getID();
    }
}
