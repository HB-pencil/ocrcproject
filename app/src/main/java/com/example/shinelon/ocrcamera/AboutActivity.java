package com.example.shinelon.ocrcamera;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by Shinelon on 2017/6/30.
 */

public class AboutActivity extends AppCompatActivity {

    private TextView mTextView;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);
        mTextView = (TextView) findViewById(R.id.version);
        try{
        PackageInfo info =  getPackageManager().getPackageInfo(getPackageName(),0);
        mTextView.setText(getString(R.string.version_info,info.versionName));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
