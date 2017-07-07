package com.example.shinelon.ocrcamera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.shinelon.ocrcamera.helper.UserInfoLab;

/**
 * Created by Shinelon on 2017/7/7.
 */

public class UserInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText phoneInfo;
    private EditText nameInfo;
    private Button revisePhoneBt;
    private Button reviseKeyBt;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info);
        phoneInfo = (EditText) findViewById(R.id.phone_info);
        nameInfo = (EditText) findViewById(R.id.user_info);
        revisePhoneBt = (Button) findViewById(R.id.revise_phone);
        reviseKeyBt = (Button) findViewById(R.id.revise_key);
        revisePhoneBt.setOnClickListener(this);
        reviseKeyBt.setOnClickListener(this);

        phoneInfo.setText(UserInfoLab.getUserInfo().getPhone());
        nameInfo.setText(UserInfoLab.getUserInfo().getName());
    }
    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.revise_phone:
                Intent i = new Intent(this,ReviseActivity.class);
                i.putExtra("mark",revisePhoneBt.getText().toString());
                startActivity(i);
                break;
            case R.id.revise_key:
                Intent o = new Intent(this,ReviseActivity.class);
                o.putExtra("mark",reviseKeyBt.getText().toString());
                startActivity(o);
                break;
        }
    }
}
