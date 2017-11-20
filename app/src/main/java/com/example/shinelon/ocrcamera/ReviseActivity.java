package com.example.shinelon.ocrcamera;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Shinelon on 2017/7/7.
 */

public class ReviseActivity extends AppCompatActivity {

    private Fragment fragment;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revise);
        FragmentManager fm = getSupportFragmentManager();
        fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            if(getIntent().getStringExtra("mark").equals("修改密码")){
                fragment = new reviseKeyFragment();
            }else {
                fragment = new reviseInfoFragment();
            }
        }
        fm.beginTransaction()
                .add(R.id.fragment_container,fragment)
                .commit();
    }

}
