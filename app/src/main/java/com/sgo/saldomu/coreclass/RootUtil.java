package com.sgo.saldomu.coreclass;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Created by User on 9/7/2017.
 */

public class RootUtil {
    public static boolean isDeviceRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
    }

    private static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkRootMethod2() {
        String[] paths = { "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private static boolean checkRootMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[] { "/system/xbin/which", "su" });
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) return true;
            return false;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }
}

//if (RootUtil.isDeviceRooted()){
//        switchErrorActivity(ErrorActivity.DEVICE_ROOTED);
//        }else if (GooglePlayUtils.isGooglePlayServicesAvailable(this)) {
//        if (checkNotification()) {
//        int type = Integer.valueOf(getIntent().getExtras().getString("type"));
//
//        FCMManager fcmManager = new FCMManager(this);
//        Intent intent = fcmManager.checkingAction(type);
//        startActivity(intent);
//        this.finish();
//        } else {
//        if (!isLogin()) {
//        openFirstScreen(FIRST_SCREEN_INTRO);
//        } else {
//        isForeground = true;
//        agent = sp.getBoolean(DefineValue.IS_AGENT, false);
//        utilsLoader = new UtilsLoader(this, sp);
//        utilsLoader.getAppVersion();
//        ActiveAndroid.initialize(this);
//        progdialog = DefinedDialog.CreateProgressDialog(this, getString(R.string.initialize));
//        progdialog.show();
//        InitializeNavDrawer();
//        setupFab();
//        AlertDialogLogout.getInstance();    //inisialisasi alertdialoglogout
//        startService(new Intent(this, UpdateLocationService.class));
//        }
//        }
//        }else {
//        switchErrorActivity(ErrorActivity.GOOGLE_SERVICE_TYPE);
//        }
