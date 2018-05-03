package com.sgo.saldomu.activities;

import android.Manifest;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sgo.saldomu.R;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import com.sgo.saldomu.databinding.ActivityRtclandingBinding;

import static com.sgo.saldomu.app_rtc.util.Constants.EXTRA_ROOMID;

public class RtclandingActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AppRTCMainActivity";
    private static final int CONNECTION_REQUEST = 1;
    private static final int RC_CALL = 111;
    private ActivityRtclandingBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rtclanding);
        binding.connectButton.setOnClickListener(v -> connect());
        binding.roomEdittext.requestFocus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_CALL)
    private void connect() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            connectToRoom(binding.roomEdittext.getText().toString());
        } else {
            EasyPermissions.requestPermissions(this, "Need some permissions", RC_CALL, perms);
        }
    }

    private void connectToRoom(String roomId) {
        Intent intent = new Intent(this, RtccallingActivity.class);
        intent.putExtra(EXTRA_ROOMID, roomId);
        startActivityForResult(intent, CONNECTION_REQUEST);
    }
}
