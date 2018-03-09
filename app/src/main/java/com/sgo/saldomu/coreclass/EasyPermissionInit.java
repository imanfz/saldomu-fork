package com.sgo.saldomu.coreclass;

import android.app.Activity;
import android.support.v4.app.Fragment;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by User on 11/10/2017.
 */

public class EasyPermissionInit {
    private static EasyPermissionInit EASYPERMITINIT = new EasyPermissionInit();

    public EasyPermissionInit() {}

    public static EasyPermissionInit sharedInstance() {
        if (EASYPERMITINIT == null) {
            EASYPERMITINIT = new EasyPermissionInit();
        }
        return EASYPERMITINIT;
    }

    public boolean initEasyPermission(Fragment frag, String [] perms, int requestCode, String errorCode){

        if (EasyPermissions.hasPermissions(frag.getActivity(), perms)) {
            // Already have permission, do the thing
            return true;
        } else {
            // Do not have permissions, request them now
                EasyPermissions.requestPermissions(frag, errorCode,
                        requestCode, perms);
            return false;
        }
    }

    public boolean initEasyPermission(Activity act, String [] perms, int requestCode, String errorCode){

        if (EasyPermissions.hasPermissions(act, perms)) {
            // Already have permission, do the thing
            return true;
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(act, errorCode,
                    requestCode, perms);
            return false;
        }
    }
}
