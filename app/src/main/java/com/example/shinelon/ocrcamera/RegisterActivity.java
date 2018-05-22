package com.example.shinelon.ocrcamera;

import android.content.DialogInterface;
import android.content.Intent;
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

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private EditText mEditTextEmail;

    private Button mButtonCode;
    private Button mButtonDone;

    private final static String USERNAME = "username";
    private final static String PASSWORD = "password";
    private final static String PHONE = "phone";
    private final static String CODE = "captcha";
    private final static String EMAIL = "userEmail";

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.register_layout);

        mEditTextAccount = (EditText) findViewById(R.id.phone_account);
        mEditTextPass1 = (EditText) findViewById(R.id.password_1);
        mEditTextPass2 = (EditText) findViewById(R.id.password_2);
        mEditTextName = (EditText) findViewById(R.id.name);
        mEditTextCode = (EditText) findViewById(R.id.check_code);
        mEditTextEmail = (EditText) findViewById(R.id.email);

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
                    String json ="{\"phone\":"+ mEditTextAccount.getText().toString() +"}";
                    RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),json);
                    Request request = new Request.Builder()
                            .url("http://119.29.193.41/api/user/register/captcha")
                            .post(body)
                            .build();
                    try {
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                                new Handler(RegisterActivity.this.getMainLooper()).post(()->   Toast.makeText(RegisterActivity.this, "连接服务器失败，请检查网络！", Toast.LENGTH_SHORT).show());
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if(response.isSuccessful()){
                                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                                    executorService.submit(()->{
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
                                    });
                                    executorService.shutdown();
                                    String str = response.body().string();
                                    String result="";
                                    try{
                                        JSONObject jsonObject = new JSONObject(str);
                                        result = jsonObject.getString("code");
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    Log.d("okhttp",str);
                                    Log.d("okhttp", "" + response.code());
                                    if (result.equals("200")) {
                                        new Handler(getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(RegisterActivity.this, "验证码已发送！", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        Log.d("okhttp", "fail");
                                        new Handler(getMainLooper()).post(()-> Toast.makeText(RegisterActivity.this, "验证码发送失败，请稍后再试!", Toast.LENGTH_SHORT).show());
                                    }
                                }else{new Handler(RegisterActivity.this.getMainLooper()).post(()->   Toast.makeText(RegisterActivity.this, "访问失败！", Toast.LENGTH_SHORT).show());
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
                            .setPositiveButton(android.R.string.ok, (dialog1, which) -> {})
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
                String email = mEditTextEmail.getText().toString();

                Log.d("注册测试",account+name+pass1+code);

                if (!account.equals("") &&!name.equals("") && !pass1.equals("") && !pass2.equals("") && !code.equals("")&& !email.equals("")) {
                    Log.d("成功判别","Success!");
                    if (!pass1.equals(pass2)) {
                        Toast.makeText(this, "前后两次输入密码不一致！", Toast.LENGTH_SHORT);
                    }
                    String json ="{\"" + USERNAME + "\":\"" + name +"\",\""+PHONE +"\":\"" + account +"\",\"" + CODE
                            + "\":\"" + code + "\",\"" + PASSWORD + "\":\"" + pass1 + "\",\""+EMAIL+ "\":\"" + email +"\" }" ;
                    RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),json);
                    final Request request = new Request.Builder()
                            .url(" http://119.29.193.41/api/user/register")
                            .post(body)
                            .build();
                    try {
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                                new Handler(getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(RegisterActivity.this, "请检查网络！", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if(response.isSuccessful()){
                                    String str = response.body().string();
                                    Log.d("注册",str+"");
                                    String result="";
                                    try{
                                        JSONObject jsonObject = new JSONObject(str);
                                        result = jsonObject.getString("code");
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    if (result.equals("200")) {
                                        Log.d("okhttp", str);
                                        Log.d("okhttp", "" + response.code());
                                        new Handler(getMainLooper()).post(()->  Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_SHORT).show());
                                        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                                        try {
                                            Thread.sleep(500);
                                        }catch (Exception e){
                                            e.getMessage();
                                        }

                                        startActivity(intent);
                                        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                                        finish();
                                    }else if(code.equals("400")){
                                        new Handler(getMainLooper()).post(()->   Toast.makeText(RegisterActivity.this, "手机号已被使用！若被占用，请联系管理员!", Toast.LENGTH_SHORT).show());
                                    } else {
                                        Log.d("okhttp", "fail");
                                        new Handler(getMainLooper()).post(()->Toast.makeText(RegisterActivity.this, "注册失败，请稍后再试", Toast.LENGTH_SHORT).show());
                                    }
                                }else{
                                    new Handler(getMainLooper()).post(()-> Toast.makeText(RegisterActivity.this, "访问失败！", Toast.LENGTH_SHORT).show());
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
                default:break;
        }
    }
}