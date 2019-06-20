package com.sgo.saldomu;

import android.hardware.Camera;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class CameraViewActivity extends AppCompatActivity {
    private static final String TAG = "CameraViewActivity";
    Camera camera;
    Preview preview;
    FloatingActionButton buttonSnap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);
        preview=new Preview(this);
        ((FrameLayout)findViewById(R.id.frameLayout)).addView(preview);
        buttonSnap=findViewById(R.id.take_picture);
        buttonSnap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
            }
        });
    }
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            Log.d(TAG, "onShutter'd");
        }
    };

    /** Handles data for raw picture */
    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken - raw");
        }
    };

    /** Handles data for jpeg picture */
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            FileOutputStream outputStream=null;
            long time=0;
            try {
                time=System.currentTimeMillis();
                outputStream=new FileOutputStream(String.format("/sdcard/%d.jpg",time));
                outputStream.write(data);
                outputStream.close();
                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "onPictureTaken - jpeg");
        }
    };
}
