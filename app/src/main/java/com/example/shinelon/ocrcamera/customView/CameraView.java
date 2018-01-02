package com.example.shinelon.ocrcamera.customView;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.example.shinelon.ocrcamera.CameraActivity;
import java.util.List;


/**
 * Created by Shinelon on 2017/12/15.自定相机
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback{
    private Camera camera;
    private SurfaceHolder holder;
    Camera.Parameters parameters;

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
        initCamera(false);
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        }catch (Exception e){
            e.getMessage();
        }
        Log.w("created","success");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.w("changed","success");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.w("destroy","success");
    }

    public void initCamera(Boolean state){
        parameters = camera.getParameters();
        List<Camera.Size> preList = parameters.getSupportedPreviewSizes();
        List<Camera.Size> picList = parameters.getSupportedPictureSizes();
        if(CameraActivity.isVertical){
            parameters.setRotation(90);
        }else if(!CameraActivity.isVertical) {
            parameters.setRotation(0);
        }
        parameters.setPreviewSize(preList.get(preList.size()-1).width,preList.get(preList.size()-1).height);
        parameters.setPictureSize( picList.get((int)( picList.size()*0.8)).width,picList.get((int)(picList.size()*0.8)).height);
        parameters.setPreviewFormat(ImageFormat.JPEG);
        parameters.setPictureFormat(ImageFormat.JPEG);
        if(state){
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        }else {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        List<String> focusList = parameters.getSupportedFocusModes();
        if(focusList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        camera.cancelAutoFocus();
        camera.setParameters(parameters);
    }

}
