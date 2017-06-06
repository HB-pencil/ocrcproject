package com.example.shinelon.ocrcamera;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by Shinelon on 2017/4/2.识别结果Activity
 */

public class SecondActivity extends AppCompatActivity {

    private static final String RECOGNIZEDTEXT = "recognized_text";
    private TextView mTextView;

    @Override
    public void onCreate(Bundle savedInStanceState){
        super.onCreate(savedInStanceState);
        setContentView(R.layout.activity_second);
    }

    //启动Activity封装方法
    public static Intent newInstance(Context context){
        Intent intent = new Intent(context,SecondActivity.class);
        return intent;
    }
}
