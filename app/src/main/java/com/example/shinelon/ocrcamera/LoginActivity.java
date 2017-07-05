package com.example.shinelon.ocrcamera;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.example.shinelon.ocrcamera.helper.JavaBean;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Shinelon on 2017/6/30.
 */

public class LoginActivity extends AppCompatActivity {

    private EditText mLoginEdit;
    private EditText mPassEdit;
    private Button mLoginButton;
    private TextView mRegisterButton;
    private TextView mForgetButton;
    private final static String USERNAME = "username";
    private final static String PASSWORD = "password";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.login_layout);

        mLoginEdit = (EditText) findViewById(R.id.login_et);
        mPassEdit = (EditText) findViewById(R.id.pass_et);
        mLoginButton = (Button) findViewById(R.id.login_bt);
        mRegisterButton = (TextView) findViewById(R.id.register_t);
        mForgetButton = (TextView) findViewById(R.id.forget_t);

        mPassEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE ) {
                    loginAccount();
                }
                return true;
            }
        });
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                intent.putExtra(USERNAME,mLoginEdit.getText().toString());
                startActivity(intent);
                finish();
              try {
                    if (checkLogin()) {
                        loginAccount();
                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        mForgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    /**
     * 验证登录信息
     */
    public boolean checkLogin() throws Exception {
        String account = mLoginEdit.getText().toString();
        String password = mPassEdit.getText().toString();
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add(USERNAME, account)
                .add(PASSWORD, password)
                .build();

        Request request = new Request.Builder().url("http://10.110.101.226:80/api/user/token").post(body).build();
        Response response = client.newCall(request).execute();
        if(response.isSuccessful()){
            parseUserInfo(response);
            return true;
        }else {
            return false;
        }

    }

    /**
     * 登录行为封装
     */
    public void loginAccount(){
        try {
            if (checkLogin()) {
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                intent.putExtra(USERNAME,mLoginEdit.getText().toString());
                startActivity(intent);
                finish();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                AlertDialog dialog = builder.setMessage("账号或密码错误或无网络，请重新输入或稍后再试！")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).create();
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseUserInfo(Response resultResponse){
        Response response = resultResponse;
        String resultJson = response.body().toString();
        JavaBean javaBean = JSON.parseObject(resultJson,JavaBean.class);
    }
}