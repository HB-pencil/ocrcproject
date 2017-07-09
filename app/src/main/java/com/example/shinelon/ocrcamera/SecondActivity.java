package com.example.shinelon.ocrcamera;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.shinelon.ocrcamera.helper.AsycProcessTask;
import com.example.shinelon.ocrcamera.helper.UserInfoLab;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Shinelon on 2017/4/2.识别结果Activity
 */

public class SecondActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText  mEditText;
    private Button mButton;
    private static final String IMAGE_PATH = "IMAGE_PATH";
    private String imageUrl;
    private String file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        mButton = (Button) findViewById(R.id.confirm_bt);
        mEditText = (EditText) findViewById(R.id.edit_text);
        mButton.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();

        if( extras != null) {
            imageUrl = extras.getString(IMAGE_PATH);
            doRecognize();
        }
    }

    public void doRecognize(){
        // Starting recognition process
        new AsycProcessTask(this).execute(imageUrl);
    }

    public static Intent newInstance(Context context,String...values){
        Intent intent = new Intent(context,SecondActivity.class);
        intent.putExtra(IMAGE_PATH,values[0]);
        intent.putExtra(USER_NAME,values[1]);
        return  intent;
    }

    private final static String USER_NAME = "username";

    @Override
    public void onClick(View view){
        if(view.getId() == R.id.confirm_bt){
            String content = mEditText.getText().toString();
            String account = getIntent().getStringExtra(USER_NAME);
            Calendar time = Calendar.getInstance();
            String fileName = account + time.get(Calendar.YEAR) +"年"+(time.get(Calendar.MONTH)+1)+"月"+time.get(Calendar.DAY_OF_MONTH)+"号"
                    + time.get(Calendar.HOUR_OF_DAY)+time.get(Calendar.MINUTE)+"分"+time.get(Calendar.SECOND) + "秒";
            Log.d("文本文件名",fileName);
            file = fileName;
            try{
                FileOutputStream out = this.openFileOutput(fileName,MODE_PRIVATE);
                out.write(content.getBytes());
                Log.d("写入content:",content);
                out.close();
            }catch(Exception e){
                e.printStackTrace();
            }
            File txtfile = new File(getFilesDir(),fileName);
            Log.d("SecondActivity","文本路径"+txtfile.getPath());
            File imgfile = new File(imageUrl);
            Log.d("SecondActivity","图片路径"+imgfile.getPath());
            if(txtfile.exists()){
                try {
                    sendtxtFile(txtfile);
                    sendimgFile(imgfile);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * 发送文件
     * @param
     */
   public void sendimgFile (File imgFile) throws Exception{
       OkHttpClient client = new OkHttpClient();
       RequestBody body = new MultipartBody.Builder()
               .addPart(Headers.of("Content-Disposition","form-data;name=\""+USER_NAME+"\""),
                       RequestBody.create(null,UserInfoLab.getUserInfo().getName()))
               .addPart(Headers.of("Content-Disposition","form-data;name=\"picture\";filename=\""+ imgFile.getName() +"\""),
                       RequestBody.create(MediaType.parse("image/jpeg"),imgFile))
               .build();
       String userId = UserInfoLab.getUserInfo().getUserId();
       Request request = new Request.Builder().url("http://10.110.101.226:80/api/user/"+ userId + "/picture").post(body).build();
       client.newCall(request).enqueue(new Callback() {
           @Override
           public void onFailure(Call call, IOException e) {
               e.printStackTrace();
               new Handler(SecondActivity.this.getMainLooper()).post(new Runnable() {
                   @Override
                   public void run() {
                       Toast.makeText(SecondActivity.this, "请检查网络！", Toast.LENGTH_SHORT).show();
                   }
               });
           }

           @Override
           public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String str = response.body().string();
                        Log.d("发送图片：", str);
                        String result = "";
                        try {
                            JSONObject jsonObject = new JSONObject(str);
                            result = jsonObject.getString("code");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (result.equals("200")) {
                            new Handler(getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SecondActivity.this, "发送图片成功！", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            new Handler(getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SecondActivity.this, "发送失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        new Handler(SecondActivity.this.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SecondActivity.this, "访问服务器失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
       });
    }

    /**
     *
     */
    public void sendtxtFile (File txtFile) throws Exception {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new MultipartBody.Builder()
                .addPart(Headers.of("Content-Disposition", "form-data;name=\"" + USER_NAME + "\""),
                        RequestBody.create(null, UserInfoLab.getUserInfo().getName()))
                .addPart(Headers.of("Content-Disposition", "form-data;name=\"txt\";filename=\"" + txtFile.getName() + "\""),
                        RequestBody.create(MediaType.parse("text/plain"), txtFile))
                .build();
        String userId = UserInfoLab.getUserInfo().getUserId();
        Request request = new Request.Builder().url("http://10.110.101.226:80/api/user/" + userId + "/txt").post(body).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                new Handler(SecondActivity.this.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SecondActivity.this, "请检查网络！", Toast.LENGTH_SHORT).show();
                    }
                });
                deleteFile(file);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String str = response.body().string();
                    Log.d("发送文本：", str);
                    String result = "";
                    try {
                        JSONObject jsonObject = new JSONObject(str);
                        result = jsonObject.getString("code");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (result.equals("200")) {
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SecondActivity.this, "发送文本成功！", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SecondActivity.this, "发送失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    deleteFile(file);
                } else {
                    new Handler(SecondActivity.this.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SecondActivity.this, "访问服务器失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                        }
                    });
                    deleteFile(file);
                }
            }
        });
    }

            /**
             * @param message
             */
            public void updateResult(String message) {

                displayMessage(message);

            }

            public void displayMessage(String text) {
                mEditText.post(new MessagePoster(text));
            }

            class MessagePoster implements Runnable {
                public MessagePoster(String message) {
                    _message = message;
                }

                public void run() {
                    mEditText.setText(_message);
                }

                private final String _message;
            }

  }
