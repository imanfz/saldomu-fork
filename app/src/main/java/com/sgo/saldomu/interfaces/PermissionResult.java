package com.sgo.saldomu.interfaces;

/**
 * Created by yuddistirakiki on 1/25/18.
 */

public interface PermissionResult {
    void onReadPhoneStateGranted();
    void onAccessFineLocationGranted();
    void onReadContactsGranted();
    void onDeny();
}
