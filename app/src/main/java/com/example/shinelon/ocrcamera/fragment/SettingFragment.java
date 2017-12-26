package com.example.shinelon.ocrcamera.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.example.shinelon.ocrcamera.AboutActivity;
import com.example.shinelon.ocrcamera.LoginActivity;
import com.example.shinelon.ocrcamera.R;
import com.example.shinelon.ocrcamera.dataModel.UpdateInfo;
import com.example.shinelon.ocrcamera.helper.CheckHelper;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Shinelon on 2017/12/26.
 */

public class SettingFragment extends PreferenceFragment{
    private OkHttpClient mOkHttpClient;
    private UpdateInfo info;
    private boolean isNewVersion = true;
    public static String downloadUrl = "";
    private SwitchPreference switchPreference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting_layout);
        findPreference("check").setOnPreferenceClickListener(preference -> {
            checkUpdate();
            if(isNewVersion){
                Toast.makeText(getActivity(),"当前版本已经是最新！",Toast.LENGTH_SHORT).show();
            }
            return false;
        });
        findPreference("logout").setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity(),LoginActivity.class);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("token","无token");
            editor.apply();
            startActivity(intent);
            getActivity().overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
            getActivity().finish();
            return false;
        });
        findPreference("about").setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity(),AboutActivity.class);
            startActivity(intent);
            getActivity().overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
            return false;
        });
        findPreference("deleteAll").setOnPreferenceClickListener(preference -> {
            Toast.makeText(getActivity(),"暂未实现！",Toast.LENGTH_SHORT).show();
            return false;
        });
        switchPreference = (SwitchPreference)findPreference("connect");
    }

    /**
     * 检查更新
     */
    public void checkUpdate(){
        mOkHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://119.29.193.41/api/app/update")
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                new Handler(getActivity().getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "连接服务器失败，请检查网络！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    String str = response.body().string();
                    Log.e("str:",str + "");
                    int code = 0;
                    try{
                        JSONObject jsonObject = new JSONObject(str);
                        code = jsonObject.getInt("code");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    parseJson(str);
                    if(code == 200){
                        final CheckHelper helper = new CheckHelper(getActivity(),info);
                        if(helper.hasNewVersion()){
                            new Handler(getActivity().getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    helper.showDialog(getActivity());
                                }
                            });
                            isNewVersion = false;
                        }else {
                            isNewVersion = true;
                        }
                    }else{
                        new Handler(getActivity().getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(),info.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
    }

    public void parseJson(String json){
        info = JSON.parseObject(json, UpdateInfo.class);
        downloadUrl = info.getData().getUpdateUrl();
    }


}
