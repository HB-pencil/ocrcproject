package com.example.shinelon.ocrcamera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.shinelon.ocrcamera.dataModel.UserInfoLab;

import okhttp3.OkHttpClient;

/**
 * Created by Shinelon on 2017/7/7.
 */

public class UserInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText phoneInfo;
    private EditText nameInfo;
    private EditText userEmail;
    private Button reviseInfoBt;
    private Button reviseKeyBt;
    private OkHttpClient client;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info);
        phoneInfo = (EditText) findViewById(R.id.phone_info);
        nameInfo = (EditText) findViewById(R.id.user_info);
        userEmail = (EditText) findViewById(R.id.user_email);
        reviseInfoBt = (Button) findViewById(R.id.revise_phone);
        reviseKeyBt = (Button) findViewById(R.id.revise_key);
        client = new OkHttpClient();
        reviseInfoBt.setOnClickListener(this);
        reviseKeyBt.setOnClickListener(this);

    }
    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.revise_phone:
                Intent i = new Intent(this,ReviseActivity.class);
                i.putExtra("mark",reviseInfoBt.getText().toString());
                startActivity(i);
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                break;
            case R.id.revise_key:
                Intent o = new Intent(this,ReviseActivity.class);
                o.putExtra("mark",reviseKeyBt.getText().toString());
                startActivity(o);
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                break;
                default:break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        phoneInfo.setText(UserInfoLab.getUserInfo().getPhone());
        nameInfo.setText(UserInfoLab.getUserInfo().getName());
        userEmail.setText(UserInfoLab.getUserInfo().getEmail());
    }
}
