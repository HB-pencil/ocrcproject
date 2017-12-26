package com.example.shinelon.ocrcamera.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shinelon.ocrcamera.R;
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
 * Created by Shinelon on 2017/7/7.
 */

public class reviseKeyFragment extends Fragment {

    private Button mButton;

    private EditText mEditText1;
    private EditText mEditText2;
    private OkHttpClient client;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_revisekey,viewGroup,false);

        mButton = (Button) v.findViewById(R.id.revise_key_done);
        mEditText1 = (EditText) v.findViewById(R.id.new_pass);
        mEditText2 = (EditText) v.findViewById(R.id.new_pass_confirm);
        client = new OkHttpClient();

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mEditText1.getText().toString().equals("")&&!mEditText2.getText().toString().equals("")){
                    if(mEditText1.getText().toString().equals(mEditText2.getText().toString())){
                        String json ="{\"password\":\"" + mEditText1.getText().toString() + "\"}";
                        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),json);
                        String userid = UserInfoLab.getUserInfo().getUserId();
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String token = sp.getString("token","");
                        Log.d("userid + token",userid + token);
                        Request request = new Request.Builder()
                                .url("http://119.29.193.41/api/user/"+ userid +"/password")
                                .addHeader("Authorization",token)
                                .put(body)
                                .build();
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                                new Handler(getActivity().getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "请检查网络！", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if(response.isSuccessful()){
                                    String str = response.body().string();
                                    Log.d("修改密码：",str);
                                    String result="";
                                    try{
                                        JSONObject jsonObject = new JSONObject(str);
                                        result = jsonObject.getString("code");
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    if(result.equals("200")){
                                        new Handler(getActivity().getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getActivity(), "修改密码成功！", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }else{
                                        new Handler(getActivity().getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getActivity(), "修改失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }else{
                                    new Handler(getActivity().getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getActivity(), "访问服务器失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                    }else{
                        Toast.makeText(getActivity(),"前后密码不一致！",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    messageDialog dialog = new messageDialog();
                    dialog.show(getActivity().getSupportFragmentManager(),null);
                }
            }
        });
        return v;
    }
}
