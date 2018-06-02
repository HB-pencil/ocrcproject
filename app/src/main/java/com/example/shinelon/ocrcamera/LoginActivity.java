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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.example.shinelon.ocrcamera.helper.ButtonPoster;
import com.example.shinelon.ocrcamera.dataModel.JavaBean;
import com.example.shinelon.ocrcamera.dataModel.UserInfoLab;
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
    private final static String PHONE = "userPhone";
    private JavaBean javaBean;
    private CheckBox mSavedA;
    private CheckBox mSavedP;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        mLoginEdit = (EditText) findViewById(R.id.login_et);
        mPassEdit = (EditText) findViewById(R.id.pass_et);
        mLoginButton = (Button) findViewById(R.id.login_bt);
        mRegisterButton = (TextView) findViewById(R.id.register_t);
        mForgetButton = (TextView) findViewById(R.id.forget_t);
        mSavedA = (CheckBox) findViewById(R.id.saveA);
        mSavedP = (CheckBox) findViewById(R.id.saveP);

        mLoginEdit.setText(getAccount());
        mPassEdit.setText(getPass());
        mSavedA.setChecked(getCheckedA());
        mSavedP.setChecked(getCheckedP());

        Log.d("我是onCreate()","调用");
        mPassEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE ) {
                    try{
                       doHandle();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });
        mLoginButton.setOnClickListener((v)->{
            try{
                doHandle();
            }catch (Exception e){
                e.printStackTrace();
            }
        });

        mRegisterButton.setOnClickListener((view)-> {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        });

        mForgetButton.setOnClickListener((v)->{
            Intent intent = new Intent(LoginActivity.this,ForgetPassActicity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        });

        mSavedA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    String str = mLoginEdit.getText().toString();
                    if(str.equals("")){
                        Toast.makeText(LoginActivity.this,"请先输入账号",Toast.LENGTH_SHORT).show();
                        mSavedA.setChecked(false);
                    }else{
                        preferences.edit().putString("saved_account",str).apply();
                    }
                }else{
                    preferences.edit().putString("saved_account","").apply();
                }
            }
        });
        mSavedP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    String str = mPassEdit.getText().toString();
                    if(str.equals("")){
                        Toast.makeText(LoginActivity.this,"请先输入密码",Toast.LENGTH_SHORT).show();
                        mSavedP.setChecked(false);
                    }else{
                        preferences.edit().putString("saved_pass",str).apply();
                    }
                    preferences.edit().putBoolean("isReP",true).apply();
                }else{
                    preferences.edit().putString("saved_pass","").apply();
                    preferences.edit().putBoolean("isReP",false).apply();
                }
            }
        });

    }

    /**
     * 获取记住的信息
     */
    public String getAccount(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getString("saved_account","");
    }

    public Boolean getCheckedA(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getBoolean("isReA",false);
    }

    public Boolean getCheckedP(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getBoolean("isReP",false);
    }

    public String getPass(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getString("saved_pass","");
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
                    new Handler(LoginActivity.this.getMainLooper()).post(()->
                        Toast.makeText(LoginActivity.this, "连接服务器失败，请检查网络！", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()){
                        final String str = response.body().string();
                        Log.d("okhttp:",str);
                        Log.d("okhttp:",response.code()+"");
                        int code = 0;
                        try{
                            JSONObject jsonObject = new JSONObject(str);
                            code = jsonObject.getInt("code");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        if(code == 200){
                            Log.d("okhttp:",str);
                            parseUserInfo(str);
                            mLoginButton.post(new ButtonPoster("登录",mLoginButton,false));
                            loginAccount();
                            new Handler(getMainLooper()).post(()->Toast.makeText(LoginActivity.this, "登录成功！", Toast.LENGTH_SHORT).show());
                        }else{
                            new Handler(getMainLooper()).post(()->{
                                JSONObject jsonObject = null;
                                try{
                                    jsonObject = new JSONObject(str);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                    AlertDialog dialog = builder.setMessage(jsonObject.getString("message")+"!")
                                            .setPositiveButton(android.R.string.ok, (dialog1, which) -> {}).create();
                                    dialog.show();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                            });
                        }
                    }else{
                        new Handler(getMainLooper()).post(()->  Toast.makeText(LoginActivity.this,javaBean.getMessage(),Toast.LENGTH_SHORT).show());
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
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                finish();
            }




    /**
     * 获取JAVABEAN
     * @param resultJson
     */
    public void parseUserInfo(String resultJson){
        javaBean = JSON.parseObject(resultJson,JavaBean.class);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("token",javaBean.getToken());
        Log.w("javaBean",""+javaBean.getData());
        editor.putString("userId",javaBean.getData().getUserId());
        editor.apply();

        UserInfoLab.getUserInfo().setName(javaBean.getData().getUsername());
        UserInfoLab.getUserInfo().setPhone(javaBean.getData().getUserPhone());
        UserInfoLab.getUserInfo().setUserId(javaBean.getData().getUserId());
        UserInfoLab.getUserInfo().setEmail(javaBean.getData().getUserEmail());

        Log.d("JavaBean",javaBean.getData().getUsername());
        Log.d("Javabean",javaBean.getToken());
        Log.d("UserInfo-Email",UserInfoLab.getUserInfo().getEmail());
    }



    /**
     * 记住密码处理逻辑
     */
    public void doHandle(){

        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        try{
            checkLogin();
            if(!mSavedA.isChecked()){
                mPreferences.edit().putString("saved_account","").apply();
                mPreferences.edit().putBoolean("isReA",false).apply();
            }else {
                mPreferences.edit().putBoolean("isReA",true).apply();
            }
            if(!mSavedP.isChecked()){
                mPreferences.edit().putString("saved_pass","").apply();
                mPreferences.edit().putBoolean("isReP",false).apply();
            }else {
                mPreferences.edit().putBoolean("isReP",true).apply();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
