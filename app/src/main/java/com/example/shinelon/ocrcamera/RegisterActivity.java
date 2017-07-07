package com.example.shinelon.ocrcamera;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shinelon.ocrcamera.helper.ButtonPoster;
import com.example.shinelon.ocrcamera.helper.messageDialog;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Shinelon on 2017/7/4.
 */

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

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
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
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
    public void onClick(View view) {
        OkHttpClient client = new OkHttpClient();
        switch (view.getId()) {
            case R.id.get_code:
                if (!mEditTextAccount.getText().toString().equals("")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 60; i > 0; i--) {
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                String str = i + "秒";
                                mButtonCode.post(new ButtonPoster(str,mButtonCode,false));
                            }
                            mButtonCode.post(new ButtonPoster("获取验证码",mButtonCode,true));
                        }
                    }).start();
                    String json ="{\"phone\":"+ mEditTextAccount.getText().toString() +"}";
                    RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),json);
                    Request request = new Request.Builder()
                            .url("http://10.110.101.226:80/api/user/register/captcha")
                            .post(body)
                            .build();
                    try {
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                Log.d("okhttp", response.body().string());
                                Log.d("okhttp", "" + response.code());
                                if (response.isSuccessful()) {
                                    new Handler(getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(RegisterActivity.this, "验证码已发送！", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Log.d("okhttp", "fail");
                                    new Handler(getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(RegisterActivity.this, "验证码发送失败，请稍后再试!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setMessage("手机号码不能为空！")
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

                Log.d("注册测试",account+name+pass1+code);

                if (!(account.equals("") && name.equals("") && pass1.equals("") && pass2.equals("") && code.equals(""))) {
                    Log.d("成功判别","Success!");
                    if (!pass1.equals(pass2)) {
                        Toast.makeText(this, "前后两次输入密码不一致！", Toast.LENGTH_SHORT);
                    }
                    String json ="{\"" + USERNAME + "\":\"" + name +"\",\""+PHONE +"\":\"" + account +"\",\"" + CODE
                            + "\":\"" + code + "\",\"" + PASSWORD + "\":\"" + pass1 + "\"}" ;
                    RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),json);
                    Request request = new Request.Builder()
                            .url(" http://10.110.101.226:80/api/user/register")
                            .post(body)
                            .build();
                    try {
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    
                                    Log.d("okhttp", response.body().string());
                                    Log.d("okhttp", "" + response.code());
                                    new Handler(getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Log.d("okhttp", "fail");
                                    new Handler(getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(RegisterActivity.this, "注册失败，请稍后再试", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("注册","判别失败!");
                    messageDialog dialog = new messageDialog();
                    dialog.show(getSupportFragmentManager(), null);
                }
                break;
        }
    }
}