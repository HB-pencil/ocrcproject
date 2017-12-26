package com.example.shinelon.ocrcamera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.example.shinelon.ocrcamera.dataModel.JavaBean;
import com.example.shinelon.ocrcamera.dataModel.UserInfoLab;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Shinelon on 2017/6/30.
 */

public class SplashActivity extends AppCompatActivity {

    boolean flag = false;
    OkHttpClient client;
    Intent intent = null;
    JavaBean javaBean = null;

    @Override
    public void onCreate(Bundle savedInstanceBundle){
        super.onCreate(savedInstanceBundle);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_layout);
        client = new OkHttpClient();
        checkToken();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(flag){
                    intent = new Intent(SplashActivity.this,MainActivity.class);

                }else {
                    intent = new Intent(SplashActivity.this,LoginActivity.class);
                }
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
            }
        },2000);
    }



    public void checkToken(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String userId = sp.getString("userId",null);
        String result = sp.getString("token","无token");
        if("无token".equals(result)){
            flag = false;
        }else{
            vertifyToken(result,userId);
        }
    }

    public void vertifyToken(String token,String userId){
        Request request = new Request.Builder()
                .url("http://119.29.193.41/api/user/"+userId+"/info")
                .addHeader("token",token)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("验证Token","失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String rsJson = response.body().string();
                if(response.isSuccessful()){
                    try {
                        JSONObject jsonObject = new JSONObject(rsJson);
                        int resultCode = jsonObject.getInt("code");
                        if(resultCode==200){
                            flag = true;
                            javaBean = com.alibaba.fastjson.JSON.parseObject(rsJson,JavaBean.class);
                            UserInfoLab.getUserInfo().setName(javaBean.getData().getUsername());
                            UserInfoLab.getUserInfo().setPhone(javaBean.getData().getUserPhone());
                            UserInfoLab.getUserInfo().setUserId(javaBean.getData().getUserId());
                            UserInfoLab.getUserInfo().setEmail(javaBean.getData().getUserEmail());
                        }else {
                            flag = false;
                        }
                        Log.e("验证Token", "onResponse: "+rsJson );
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
