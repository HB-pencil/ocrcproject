package com.example.shinelon.ocrcamera;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * Created by Shinelon on 2017/7/7.
 */

public class ReviseActivity extends FragmentActivity {

    private Fragment fragment;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revise);
        FragmentManager fm = getSupportFragmentManager();
        if(getIntent().getStringExtra("mark").equals("修改手机")){
            fragment = new revisePhoneFragment();
        }else{
            fragment = new reviseKeyFragment();
        }
        fm.beginTransaction().add(R.id.fragment_container,fragment)
                .commit();
    }
}
