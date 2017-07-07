package com.example.shinelon.ocrcamera;

import android.os.Bundle;
import android.os.Handler;
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
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Shinelon on 2017/7/5.
 */

public class ForgetPassActicity extends AppCompatActivity implements View.OnClickListener{

    private EditText mPhone;
    private EditText mName;
    private EditText mCode;
    private EditText mNewpass;
    private Button mCodeBt;
    private Button mDoneBt;
    private final static String USERNAME = "username";
    private final static String PASSWORD = "password";
    private final static String PHONE = "phone";
    private final static String CODE = "captcha";

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_forget);

        mPhone = (EditText) findViewById(R.id.phone_forget);
        mCode = (EditText) findViewById(R.id.code_forget);
        mNewpass = (EditText) findViewById(R.id.pass_new);
        mCodeBt = (Button) findViewById(R.id.reget_button);
        mDoneBt = (Button) findViewById(R.id.done_button);
        mName = (EditText) findViewById(R.id.name_forget);

        mCode.setOnClickListener(this);
        mDoneBt.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        String phone = mPhone.getText().toString();
        String code = mCode.getText().toString();
        String newpass = mNewpass.getText().toString();
        String name = mName.getText().toString();
        OkHttpClient client = new OkHttpClient();
        switch (view.getId()){
            case R.id.reget_button:
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
                            mCodeBt.post(new ButtonPoster(str,mCodeBt,false));
                        }
                        mCodeBt.post(new ButtonPoster("获取验证码",mCodeBt,true));
                    }
                }).start();
                 if(!(phone.equals(""))){
                     RequestBody body = new FormBody.Builder()
                             .add(PHONE,phone)
                             .build();
                     Request request = new Request.Builder()
                             .url("http://10.110.101.226:80/api/user/password/captcha")
                             .post(body)
                             .build();
                     try{
                         client.newCall(request).enqueue(new Callback() {
                             @Override
                             public void onFailure(Call call, IOException e) {
                                 e.printStackTrace();
                             }

                             @Override
                             public void onResponse(Call call, Response response) throws IOException {
                                     if (response.isSuccessful()) {
                                         Log.d("okhttp", response.body().string());
                                         Log.d("okhttp", "" + response.code());
                                         new Handler(getMainLooper()).post(new Runnable() {
                                             @Override
                                             public void run() {
                                                 Toast.makeText(ForgetPassActicity.this, "验证码已发送！", Toast.LENGTH_SHORT).show();
                                             }
                                         });
                                     } else {
                                         Log.d("okhttp", "fail");
                                         new Handler(getMainLooper()).post(new Runnable() {
                                             @Override
                                             public void run() {
                                                 Toast.makeText(ForgetPassActicity.this, "验证码发送失败，请稍后再试!", Toast.LENGTH_SHORT).show();
                                             }
                                         });
                                     }
                                 }
                         });
                     }catch (Exception e){
                         e.printStackTrace();
                     }

                 }else {
                     messageDialog dialog = new messageDialog();
                     dialog.show(getSupportFragmentManager(),null);
                 }
                break;
            case R.id.done_button:
                if(!(phone.equals("") && code.equals("") && newpass.equals("")&&name.equals(""))){
                    RequestBody body = new FormBody.Builder()
                            .add(USERNAME,name)
                            .add(PHONE,phone)
                            .add(CODE,code)
                            .add(PASSWORD,newpass)
                            .build();
                    Request request = new Request.Builder()
                            .put(body)
                            .url("http://10.110.101.226:80/api/user/password")
                            .build();
                    try{
                       client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                {
                                    if (response.isSuccessful()) {
                                        Log.d("okhttp", response.body().string());
                                        Log.d("okhttp", "" + response.code());
                                        new Handler(getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ForgetPassActicity.this, "修改密码成功！", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        Log.d("okhttp", "fail");
                                        new Handler(getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ForgetPassActicity.this, "操作失败，请稍后再试!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                         }
                       });
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else {
                    messageDialog dialog = new messageDialog();
                    dialog.show(getSupportFragmentManager(),null);
                }
                break;
        }
    }
}

