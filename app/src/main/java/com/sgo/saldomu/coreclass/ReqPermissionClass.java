package com.sgo.saldomu.coreclass;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * Created by yuddistirakiki on 6/9/16.
 */
public class ReqPermissionClass {
    public static final int PERMISSIONS_REQ_CAMERA = 124;
    public static final int PERMISSIONS_REQ_WRITEEXTERNALSTORAGE = 123;
    public static final int PERMISSIONS_REQ_READPHONESTATE = 122;
    public static final int PERMISSIONS_SEND_SMS = 121;
    public static final int PERMISSIONS_READ_SMS = 125;
    public static final int PERMISSIONS_READ_CONTACTS = 126;

    private Activity mActive;
    private Fragment mFrag;

    public ReqPermissionClass(Activity _active){
        this.setmActive(_active);
    }

    public void setTargetFragment(Fragment _frag){
        this.setmFrag(_frag);
    }

    public boolean checkPermission(String permission, int reqCode){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                getmActive().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            if(getmFrag() != null)
                getmFrag().requestPermissions(new String[]{permission}, reqCode);
            else
                getmActive().requestPermissions(new String[]{permission}, reqCode);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            return false;
        }
        else
            return true;
    }

    public boolean checkOnPermissionResult(int requestCode, @NonNull int[] grantResults, int reqCode) {
        return requestCode == reqCode && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }


    private Activity getmActive() {
        return mActive;
    }

    private void setmActive(Activity mActive) {
        this.mActive = mActive;
    }

    private Fragment getmFrag() {
        return mFrag;
    }

    private void setmFrag(Fragment mFrag) {
        this.mFrag = mFrag;
    }
}
