package com.example.shinelon.ocrcamera;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.shinelon.ocrcamera.helper.ButtonPoster;
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

public class revisePhoneFragment extends Fragment {

    private Button mButton1;
    private Button mButton2;
    private EditText mEditText1;
    private EditText mEditText2;
    private EditText mEditText3;
    private OkHttpClient client;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_revisephone,viewGroup,false);
        mButton1 = (Button) view.findViewById(R.id.revise_phone_done);
        mButton2 = (Button) view.findViewById(R.id.revise_code);
        mEditText1 = (EditText) view.findViewById(R.id.phone_new);
        mEditText2 = (EditText) view.findViewById(R.id.phone_old);
        mEditText3 = (EditText) view.findViewById(R.id.user_code);
        client = new OkHttpClient();

        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(!mEditText1.getText().toString().equals("")&&!mEditText2.getText().toString().equals("")&&!mEditText3.getText().toString().equals("")){
                   String json ="{\"oldphone\":\"" + mEditText2.getText().toString() +"\",\"phone\":\"" + mEditText1.getText().toString() +"\",\"password\":\"" + mEditText3.getText().toString() + "\"}";
                   RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),json);
                   final Request request = new Request.Builder()
                           .url("http://10.110.101.226:80/api/user/phone/captcha")
                           .post(body)
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
                               String result="";
                               try{
                                   JSONObject jsonObject = new JSONObject(response.body().string());
                                   result = jsonObject.getString("code");
                               }catch (Exception e){
                                   e.printStackTrace();
                               }
                               Log.d("okhttp",response.body().string());
                               if(result.equals("200")){
                                   new Handler(getActivity().getMainLooper()).post(new Runnable() {
                                       @Override
                                       public void run() {
                                           Toast.makeText(getActivity(), "修改手机成功！", Toast.LENGTH_SHORT).show();
                                       }
                                   });
                               }else{
                                   new Handler(getActivity().getMainLooper()).post(new Runnable() {
                                       @Override
                                       public void run() {
                                           Toast.makeText(getActivity(), "修改手机失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                                       }
                                   });
                               }
                           }else{
                               new Handler(getActivity().getMainLooper()).post(new Runnable() {
                                   @Override
                                   public void run() {
                                       Toast.makeText(getActivity(), "访问失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                                   }
                               });
                           }
                       }
                   });
               }else{
                   messageDialog dialog = new messageDialog();
                   dialog.show(getActivity().getSupportFragmentManager(),null);
               }
            }
        });
        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mEditText2.getText().toString().equals("")){
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
                                mButton1.post(new ButtonPoster(str,mButton1,false));
                            }
                            mButton1.post(new ButtonPoster("获取验证码",mButton1,true));
                        }
                    }).start();
                    String json ="{\"oldphone\":\"" + mEditText2.getText().toString() + "\"}";
                    RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),json);
                    Request request = new Request.Builder()
                            .url("http://10.110.101.226:80/api/user/phone/captcha")
                            .post(body)
                            .build();
                    try {
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
                                        new Handler(getActivity().getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getActivity(), "验证码已发送！", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        Log.d("okhttp", "fail");
                                        new Handler(getActivity().getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getActivity(), "验证码发送失败，请稍后再试!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }else{

                                }

                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(getActivity(),"请输入原来手机号！",Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

}
