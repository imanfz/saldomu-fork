package com.sgo.orimakardaya.coreclass;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

/**
 * Created by yuddistirakiki on 6/9/16.
 */
public class ReqPermissionClass {
    private static final int PERMISSIONS_REQ_READPHONESTATE = 0x123;
    private static final int PERMISSIONS_SEND_SMS = 0x124;

    private Activity mActive;

    public ReqPermissionClass(Activity _active){
        this.setmActive(_active);
    }

    public boolean checkPermissionREADPHONESTATE(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                getmActive().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            getmActive().requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_REQ_READPHONESTATE);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            return false;
        }
        else
            return true;
    }

    public boolean checkOnRequestREADPHONESTATE(int requestCode, @NonNull int[] grantResults) {
        return requestCode == PERMISSIONS_REQ_READPHONESTATE && grantResults[0] != PackageManager.PERMISSION_GRANTED;
    }


    public Activity getmActive() {
        return mActive;
    }

    public void setmActive(Activity mActive) {
        this.mActive = mActive;
    }

}
