package com.sgo.saldomu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "Preview";

    SurfaceHolder mHolder;
    public Camera camera;
    public Preview(Context context) {
        super(context);
        mHolder=getHolder();
        mHolder.addCallback(this);
        //noinspection deprecation
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        while (camera==null){
            camera=Camera.open();
        }
        try {
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    FileOutputStream outputStream=null;
                    try {
                        outputStream=new FileOutputStream(String.format("/sdcard/%d.jpg",System.currentTimeMillis()));
                        outputStream.write(data);
                        outputStream.close();
                        Log.d(TAG, "onPreviewFrame - wrote bytes: " + data.length);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Preview.this.invalidate();
                }
            });
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Parameters parameters=camera.getParameters();
        camera.setParameters(parameters);
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera=null;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint p= new Paint(Color.RED);
        Log.d(TAG,"draw");
        canvas.drawText("PREVIEW", canvas.getWidth()/2, canvas.getHeight()/2, p );
    }
}
