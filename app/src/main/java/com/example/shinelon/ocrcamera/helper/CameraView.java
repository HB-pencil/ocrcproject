package com.example.shinelon.ocrcamera.helper;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.security.Policy;
import java.util.List;


/**
 * Created by Shinelon on 2017/12/15.
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback{
    private Camera camera;
    private SurfaceHolder holder;

    public CameraView(Context context,Camera c){
        super(context);
        camera = c;
        camera.setDisplayOrientation(90);
        holder = getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSPARENT);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> preList = parameters.getSupportedPreviewSizes();
        List<Camera.Size> picList = parameters.getSupportedPictureSizes();
        parameters.setPreviewSize(preList.get(preList.size()-1).width,preList.get(preList.size()-1).height);
        parameters.setPictureSize(picList.get(picList.size()-2).width,picList.get(picList.size()-2).height);
        parameters.setPreviewFormat(ImageFormat.JPEG);
        parameters.setPictureFormat(ImageFormat.JPEG);
        List<String> focusList = parameters.getSupportedFocusModes();
        if(focusList.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        }catch (Exception e){
            e.getMessage();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
