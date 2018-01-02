package com.example.shinelon.ocrcamera;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.example.shinelon.ocrcamera.customView.CameraView;
import java.io.File;
import java.io.FileOutputStream;


/**
 * Created by Shinelon on 2017/12/15.
 */

public class CameraActivity extends AppCompatActivity implements View.OnClickListener,Camera.PictureCallback{
    private Camera camera;
    ImageView takePhoto;
    ImageView turnLight;
    boolean flashState = false;
    CameraView cameraView;
    public static boolean isVertical = true;
    Button orientation;
    private FrameLayout layout;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        isVertical = true;
        layout = (FrameLayout) findViewById(R.id.camera_container);
        takePhoto = (ImageView) findViewById(R.id.take);
        turnLight = (ImageView) findViewById(R.id.light);
        orientation = (Button) findViewById(R.id.changeOri);

        initCamera();
        orientation.setOnClickListener(this);
        takePhoto.setOnClickListener(this);
        turnLight.setOnClickListener(this);

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
                    cameraView.initCamera(flashState);
                    turnLight.setImageResource(R.drawable.lighton);
                }else {
                    flashState = false;
                    cameraView.initCamera(flashState);
                    turnLight.setImageResource(R.drawable.lightoff);
                }
                break;
            case R.id.changeOri:
                stopCamera();
                if(isVertical){
                    isVertical = false;
                    takePhoto.setRotation(90);
                    turnLight.setRotation(90);
                }else{
                    isVertical = true;
                    takePhoto.setRotation(0);
                    turnLight.setRotation(0);
                }
                initCamera();
            default:
                break;
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera){
        File file =  (File)getIntent().getSerializableExtra("filePath");
        Log.w("File and Data",(file==null) + "    " + data.length);
        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(data);
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        setResult(RESULT_OK);
        MessageHandler messageHandler = new MessageHandler();
        messageHandler.postDelayed(()-> {
            Toast.makeText(this,"已拍照",Toast.LENGTH_SHORT).show();
            finish();
        },200);
    }

    private static class MessageHandler extends Handler{}

    @Override
    protected void onResume(){
        super.onResume();
        if (camera==null){
            initCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("destroy","true"+ "  "+ isVertical);
    }

    public Camera getCamera(){
        if (camera == null){
            camera = Camera.open(0);
        }
        return camera;
    }

    public void initCamera(){
        camera = getCamera();
        cameraView = new CameraView(this,camera);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)layout.getLayoutParams();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int w = displayMetrics.widthPixels;
        int h = displayMetrics.heightPixels;
        layoutParams.width = (int) (w * 0.88);
        layoutParams.height =(int) (h * 0.72);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        layout.setLayoutParams(layoutParams);
        layout.addView(cameraView);
    }

    public void stopCamera(){
        camera.setPreviewCallback(null);
        camera.stopPreview();
        layout.removeView(cameraView);
        cameraView = null;
        camera.release();
        camera = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        finish();
    }
}
