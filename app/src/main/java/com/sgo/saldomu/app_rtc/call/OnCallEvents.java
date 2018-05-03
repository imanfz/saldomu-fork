package com.sgo.saldomu.app_rtc.call;

/**
 * Created by Lenovo on 02/05/2018.
 */

/**
 * Call control interface for container activity.
 */
public interface OnCallEvents {
    void onCallHangUp();

    void onCameraSwitch();

    void onCaptureFormatChange(int width, int height, int framerate);

    boolean onToggleMic();
}
