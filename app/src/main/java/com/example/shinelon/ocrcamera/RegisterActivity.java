package com.example.shinelon.ocrcamera;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Shinelon on 2017/7/4.
 */

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText mEditTextAccount;
    private EditText mEditTextName;
    private EditText mEditTextPass1;
    private EditText mEditTextPass2;
    private EditText mEditTextCode;

    private Button mButtonCode;
    private Button mButtonDone;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        getSupportActionBar().hide();
        setContentView(R.layout.register_layout);

        mEditTextAccount = (EditText) findViewById(R.id.phone_account);
        mEditTextPass1 = (EditText) findViewById(R.id.password_1);
        mEditTextPass2 = (EditText) findViewById(R.id.password_2);
        mEditTextName = (EditText) findViewById(R.id.name);
        mEditTextCode = (EditText) findViewById(R.id.check_code);

        mButtonCode = (Button) findViewById(R.id.get_code);
        mButtonDone = (Button) findViewById(R.id.register_done);
        mButtonCode.setOnClickListener(this);
        mButtonDone.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){

    }
}
