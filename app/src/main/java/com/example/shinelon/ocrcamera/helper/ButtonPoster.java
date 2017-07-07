package com.example.shinelon.ocrcamera.helper;

import android.widget.Button;

/**
 * Created by Shinelon on 2017/7/7.线程中post更新Ui
 */

public class ButtonPoster implements Runnable {
    Button mButton;
    String messsage;
    Boolean mTouch;
    public ButtonPoster(String str,Button button,Boolean touch){
        messsage = str;
        mButton = button;
        mTouch = touch;
    }
    @Override
    public void run(){
        mButton.setEnabled(mTouch);
        mButton.setText(messsage);
    }
}

