package com.sgo.saldomu;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "Preview";
    private SurfaceHolder mHolder;
    public Camera mCamera;
    public Preview(Context context, Camera camera) {
        super(context);
        mCamera=camera;
        mHolder=getHolder();
        mHolder.addCallback(this);
        //noinspection deprecation
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface()==null) {
            return;
        }
        try {
            mCamera.stopPreview();
        }catch (Exception e){

        }
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
