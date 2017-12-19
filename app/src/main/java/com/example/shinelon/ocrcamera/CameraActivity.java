package com.example.shinelon.ocrcamera;

import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.shinelon.ocrcamera.helper.CameraView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.security.Policy;

/**
 * Created by Shinelon on 2017/12/15.
 */

public class CameraActivity extends AppCompatActivity implements View.OnClickListener,Camera.PictureCallback{
    private Camera camera;
    ImageView takePhoto;
    ImageView turnLight;
    boolean flashState = false;
    CameraView cameraView;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        camera = Camera.open(0);
        cameraView = new CameraView(this,camera);
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
                if(!flashState){
                    flashState = true;
                    cameraView.setFlashOn();
                    turnLight.setImageResource(R.drawable.lighton);
                }else {
                    flashState = false;
                    cameraView.setFlashOff();
                    turnLight.setImageResource(R.drawable.lightoff);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera){
        File file =  (File)getIntent().getSerializableExtra("filePath");
        Log.w("File and Data",(file==null) + "    " + data.length);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(data);
            outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        setResult(0);
        MessageHandler messageHandler = new MessageHandler();
        messageHandler.postDelayed(()-> {
            Toast.makeText(this,"已拍照",Toast.LENGTH_SHORT).show();
            finish();
        },1000);
    }

    private static class MessageHandler extends Handler{}


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
