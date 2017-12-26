package com.example.shinelon.ocrcamera;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.shinelon.ocrcamera.R;
import com.example.shinelon.ocrcamera.fragment.SettingFragment;


/**
 * Created by Shinelon on 2017/12/26.
 */

public class SettingActivity extends AppCompatActivity {
    private Fragment fragment;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revise);
        FragmentManager fm = getFragmentManager();
        fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = new SettingFragment();
        }
        fm.beginTransaction()
                .add(R.id.fragment_container,fragment)
                .commit();
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
