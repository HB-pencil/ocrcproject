package com.example.shinelon.ocrcamera;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.example.shinelon.ocrcamera.helper.ButtonPoster;
import com.example.shinelon.ocrcamera.helper.JavaBean;
import com.example.shinelon.ocrcamera.helper.UserInfoLab;
import com.example.shinelon.ocrcamera.helper.messageDialog;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
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
    private final static String PHONE = "phone";
    private Boolean result;

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
                    try {
                        checkLogin();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 try{
                     checkLogin();
                 }catch (Exception e){
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
                Intent intent = new Intent(LoginActivity.this,ForgetPassActicity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 验证登录信息
     */
    public void checkLogin() throws Exception {

        String account = mLoginEdit.getText().toString();
        String password = mPassEdit.getText().toString();
        OkHttpClient client = new OkHttpClient();

        if(!account.equals("")&&!password.equals("")){
            String json ="{\""+ PHONE + "\":\"" + account + "\",\"" + PASSWORD + "\":\"" + password + "\"}";
            RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),json);
            Request request = new Request.Builder().url("http://119.29.193.41/api/user/login").post(body).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    new Handler(LoginActivity.this.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "连接服务器失败，请检查网络！", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()){
                        String str = response.body().string();
                        Log.d("okhttp:",str);
                        Log.d("okhttp:",response.code()+"");
                        String code="";
                        try{
                            JSONObject jsonObject = new JSONObject(str);
                            code = jsonObject.getString("code");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        if(code.equals("200")){
                            parseUserInfo(str);
                            Log.d("okhttp:",str);
                            mLoginButton.post(new ButtonPoster("登陆",mLoginButton,false));
                            loginAccount();
                            new Handler(getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "登陆成功！", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else if(!mLoginEdit.getText().toString().equals("")&&!mPassEdit.getText().toString().equals("")){
                            new Handler(getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                    AlertDialog dialog = builder.setMessage("账号或密码错误，请重新输入或稍后再试！")
                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            }).create();
                                    dialog.show();
                                }
                            });
                        }
                    }else{
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this,"访问出错！",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }else {
            messageDialog dialog = new messageDialog();
            dialog.show(getSupportFragmentManager(),null);
        }
    }

    /**
     * 登录行为封装
     */
    public void loginAccount(){
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                intent.putExtra(USERNAME,UserInfoLab.getUserInfo().getName());
                startActivity(intent);
                finish();
            }




    /**
     * 获取JAVABEAN
     * @param resultJson
     */
    public void parseUserInfo(String resultJson){
        JavaBean javaBean = JSON.parseObject(resultJson,JavaBean.class);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("token",javaBean.getToken());
        editor.apply();

        UserInfoLab.getUserInfo().setName(javaBean.getUser().getUsername());
        UserInfoLab.getUserInfo().setPhone(javaBean.getUser().getPhone());
        UserInfoLab.getUserInfo().setUserId(javaBean.getUser().getUserId());

        Log.d("JavaBean",javaBean.getUser().getUsername());
        Log.d("Javabean",javaBean.getToken());
        Log.d("UserInfo",UserInfoLab.getUserInfo().getName());
    }
}
