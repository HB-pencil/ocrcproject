package com.example.shinelon.ocrcamera;

import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.shinelon.ocrcamera.helper.CameraView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Policy;

/**
 * Created by Shinelon on 2017/12/15.
 */

public class CameraActivity extends AppCompatActivity implements View.OnClickListener,Camera.PictureCallback{
    private Camera camera;
    ImageView takePhoto;
    ImageView turnLight;
    boolean flashState = false;
    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        camera = Camera.open(0);
        CameraView cameraView = new CameraView(this,camera);

        FrameLayout layout = (FrameLayout) findViewById(R.id.camera_container);
        takePhoto = (ImageView) findViewById(R.id.take);
        turnLight = (ImageView) findViewById(R.id.light);
        takePhoto.setOnClickListener(this);
        turnLight.setOnClickListener(this);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)layout.getLayoutParams();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int w = displayMetrics.widthPixels;
        int h = displayMetrics.heightPixels;
        layoutParams.width = (int) (w * 0.85);
        layoutParams.height =(int) (h * 0.7);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        layout.setLayoutParams(layoutParams);

        layout.addView(cameraView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.take:
                camera.takePicture(null,null,this);
                break;
            case R.id.light:
                Camera.Parameters parameters = camera.getParameters();
                if(!flashState){
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    camera.setParameters(parameters);
                }else {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(parameters);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera){
        File file =  getIntent().getParcelableExtra("filePath");
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(data);
            outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        setResult(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(camera==null){
            camera = Camera.open(0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera!=null){
            camera.release();
        }
    }
}
