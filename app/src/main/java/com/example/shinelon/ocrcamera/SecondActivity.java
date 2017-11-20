package com.example.shinelon.ocrcamera;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.shinelon.ocrcamera.helper.AsycProcessTask;
import com.example.shinelon.ocrcamera.helper.LogInterceptor;
import com.example.shinelon.ocrcamera.helper.UserInfoLab;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.security.auth.login.LoginException;

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
    private String customFileName;
    private String file;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        mButton = (Button) findViewById(R.id.confirm_bt);
        mEditText = (EditText) findViewById(R.id.edit_text);
        mEditText.setEnabled(false);
        mButton.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();

        if( extras != null) {
            imageUrl = extras.getString(IMAGE_PATH);
            doRecognize();
        }
    }

    public void doRecognize() {
        // Starting recognition process
        new AsycProcessTask(this).execute(imageUrl);
        System.out.println("自动识别路径为    " + imageUrl);
        Log.d("自动识别路径:", imageUrl);
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
            SimpleDateFormat sf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            Calendar time = Calendar.getInstance();
            String fileName = account + "-"+ sf.format(time.getTime())+".txt";
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

            View dialogview = getLayoutInflater().inflate(R.layout.custom_dialog,null);
            EditText editText = (EditText)dialogview.findViewById(R.id.edit_dialog);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogview)
                    .setCancelable(false)
                    .setTitle("提示：")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                          customFileName = editText.getText().toString();
                            if(txtfile.exists()&&imgfile.exists()){
                                try {
                                    sendFile(txtfile,imgfile);
                                    Log.d("SEND", "onClick: 开始发送文件");
                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                            }
                        }
                    })
                    .create();
            dialog.show();
        }
    }

    /**
     * 发送文件
     * @param
     */
   public void sendFile (File txtFile,File imgFile) throws Exception{
       String txtName = txtFile.getName();
       String imgName = imgFile.getName();
       if(customFileName!=null){
           txtName = customFileName + ".txt";
           file = txtName;
           imgName = customFileName + ".jpg";
       }
       Log.e( "sendFile ",txtName + "     "+imgName );
       LogInterceptor interceptor = new LogInterceptor();
       OkHttpClient client = new OkHttpClient.Builder()
               .addInterceptor(interceptor)
               .build();

       RequestBody body = new MultipartBody.Builder()
               .setType(MultipartBody.FORM)
               .addPart(Headers.of("Content-Disposition","form-data;name=\"file\";filename=\""+ imgName +"\""),
                       RequestBody.create(MediaType.parse("image/jpeg"),imgFile))
               .addPart(Headers.of("Content-Disposition","form-data;name=\"file\";filename=\""+ txtName + "\""),
                       RequestBody.create(MediaType.parse("text/plain"),txtFile))
               .build();
       String userId = UserInfoLab.getUserInfo().getUserId();
       Request request = new Request.Builder()
               .url("http://119.29.193.41/api/user/"+ userId + "/file/ocr")
               .addHeader("token",getToken())
               .post(body)
               .build();
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
                        Log.d("发送文件：", str);
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
                                    Toast.makeText(SecondActivity.this, "发送成功！", Toast.LENGTH_SHORT).show();
                                    File fileTxt = new File(getFilesDir(),file);
                                    if (fileTxt.exists()){
                                        fileTxt.delete();
                                    }
                                    Log.w("删除文本", " 删除" );
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_second,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.edit:
                mEditText.setEnabled(true);
                    break;
                default:
                    break;
        }
        return super.onOptionsItemSelected(item);
    }

    public  String getToken(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String token = sp.getString("token","");
        Log.d("SecondActivity",token);
        return token;
    }

            /**
             * @param message
             */
            public void updateResult(String message) {

                displayMessage(message);

            }

            public void displayMessage(String text) {
                mEditText.post(new MessagePoster(text));
                try{
                    Thread.sleep(200);
                }catch (Exception e){
                    e.printStackTrace();
                }
                System.out.println("结果"+text);
                Log.d("结果：",text);
            }

            class MessagePoster implements Runnable {
                public MessagePoster(String message) {
                    mMessage = message;
                }

                @Override
                public void run() {
                    mEditText.setText(Html.fromHtml(mMessage));
                }

                private final String mMessage;
            }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
