package com.example.shinelon.ocrcamera.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.example.shinelon.ocrcamera.R;
import com.example.shinelon.ocrcamera.dataModel.JavaBean;
import com.example.shinelon.ocrcamera.dataModel.UserInfoLab;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Shinelon on 2017/11/19.
 */

public class reviseInfoFragment extends Fragment implements View.OnClickListener{

    private EditText userEmail;
    private EditText userPhone;
    private Button button;
    private OkHttpClient client;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_revisephone,container,false);
        userPhone = (EditText) view.findViewById(R.id.phone_new);
        userEmail = (EditText) view.findViewById(R.id.phone_old);
        button = (Button) view.findViewById(R.id.revise_phone_done);
        client = new OkHttpClient();
        button.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        String phone = userPhone.getText().toString();
        String useremail = userEmail.getText().toString();
        String json = "{\"userPhone\":\""+ phone+"\",\"userEmail\":\""+useremail+"\" }";
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),json);
        Request request = new Request.Builder()
                .url("http://119.29.193.41/api/user/"+ UserInfoLab.getUserInfo().getUserId()+"/info")
                .put(body)
                .addHeader("token",getToken())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("request", "onFailure: ",e );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    String string = response.body().string();
                    JavaBean javaBean = JSON.parseObject(string, JavaBean.class);
                    if (javaBean.getCode()==200){
                        String phone = javaBean.getData().getUserPhone();
                        String email = javaBean.getData().getUserEmail();
                        UserInfoLab.getUserInfo().setPhone(phone);
                        UserInfoLab.getUserInfo().setEmail(email);
                        new Handler(Looper.getMainLooper()).post(()-> Toast.makeText(getContext(),"修改成功！",Toast.LENGTH_SHORT).show());
                    }else {
                        new Handler(Looper.getMainLooper()).post(()-> Toast.makeText(getContext(),"修改失败！",Toast.LENGTH_SHORT).show());
                    }

                }
            }
        });
    }

    public  String getToken(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        String token = sp.getString("token","");
        Log.d("SecondActivity",token);
        return token;
    }

}
