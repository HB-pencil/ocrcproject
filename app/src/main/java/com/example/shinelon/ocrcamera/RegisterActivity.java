package com.example.shinelon.ocrcamera;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

    private final static String USERNAME = "username";
    private final static String PASSWORD = "password";
    private final static String PHONE = "phone";
    private final static String CODE = "captcha";

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
        OkHttpClient client = new OkHttpClient();
        switch (view.getId()){
            case R.id.get_code:
                if (!mEditTextAccount.getText().toString().equals("")){
                    RequestBody body = new FormBody.Builder()
                            .add(PHONE,mEditTextAccount.getText().toString())
                            .build();
                    Request request = new Request.Builder()
                            .url("http://10.110.101.226:80/api/user/register/captcha")
                            .post(body)
                            .build();
                    try{
                        Response response = client.newCall(request).execute();
                        if(response.isSuccessful()){
                            Toast.makeText(this,"验证码已发送！",Toast.LENGTH_SHORT);
                        }else{
                            Toast.makeText(this,"验证码获取失败，请稍后再试！",Toast.LENGTH_SHORT);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    mButtonCode.setEnabled(false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for(int i = 60;i>0;i--){
                                try{
                                    Thread.sleep(1000);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                String str = i + "秒";
                                mButtonCode.setText(str);
                            }
                            mButtonCode.setText("获取验证码");
                            mButtonCode.setEnabled(true);
                        }
                    }).start();
                }else {
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setMessage("请先输入手机号码！")
                            .setCancelable(true)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .create();
                    dialog.show();
                }
                break;
            case R.id.register_done:
                String account = mEditTextAccount.getText().toString();
                String name = mEditTextName.getText().toString();
                String pass1 = mEditTextPass1.getText().toString();
                String pass2 = mEditTextPass2.getText().toString();
                String code = mEditTextCode.getText().toString();
                if(!account.equals("")&&name.equals("")&&pass1.equals("")&&pass2.equals("")&&code.equals("")){
                    if(!pass1.equals(pass2)){
                        Toast.makeText(this,"前后两次输入密码不一致！",Toast.LENGTH_SHORT);
                    }
                    RequestBody body = new FormBody.Builder()
                            .add(USERNAME,name)
                            .add(PHONE,account)
                            .add(CODE,code)
                            .add(PASSWORD,pass1)
                            .build();
                    Request request = new Request.Builder()
                            .url(" http://10.110.101.226:80/api/user/register")
                            .post(body)
                            .build();
                    try{
                        Response response = client.newCall(request).execute();
                        if(response.isSuccessful()){
                            Toast.makeText(this,"注册成功！",Toast.LENGTH_SHORT);
                        }else {
                            Toast.makeText(this,"注册失败，请稍后再试！",Toast.LENGTH_SHORT);
                        }
                    }catch (Exception e){e.printStackTrace();}

                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setMessage("请输入所有信息！")
                            .setCancelable(true)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .create();
                    dialog.show();
                }
                break;
        }
    }
}
