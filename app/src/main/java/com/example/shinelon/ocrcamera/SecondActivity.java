package com.example.shinelon.ocrcamera;

import android.app.ProgressDialog;
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
import android.text.style.UpdateAppearance;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shinelon.ocrcamera.helper.CheckApplication;
import com.example.shinelon.ocrcamera.task.AsycProcessTask;
import com.example.shinelon.ocrcamera.helper.LogInterceptor;
import com.example.shinelon.ocrcamera.dataModel.UserInfoLab;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private EditText  mEditText1;
    private EditText  mEditText2;
    private Button mButton1;
    private Button mButton2;
    private static final String IMAGE_PATH = "IMAGE_PATH";
    private String imageUrl;
    private String customFileName;
    private String file;
    private AsycProcessTask task;
    private ProgressDialog mProgressDialog;
    private ProgressDialog tipsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        mButton1 = (Button) findViewById(R.id.confirm_bt1);
        mButton2 = (Button) findViewById(R.id.confirm_bt2);
        mEditText1 = (EditText) findViewById(R.id.edit_text_one);
        mEditText2 = (EditText) findViewById(R.id.edit_text_two);
        mEditText1.setEnabled(false);
        mEditText2.setEnabled(false);
        mButton1.setOnClickListener(this);
        mButton2.setOnClickListener(this);
        Bundle extras = getIntent().getExtras();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        tipsDialog = new ProgressDialog(this);
        tipsDialog.setCancelable(false);
        tipsDialog.setCanceledOnTouchOutside(false);
        tipsDialog.setMessage("处理结果发送中");

        if( extras != null) {
            imageUrl = extras.getString(IMAGE_PATH);
            doRecognize(mProgressDialog);
        }
    }

    public void doRecognize(ProgressDialog dialog) {
        // Starting recognition process
        task = new AsycProcessTask(dialog,imageUrl);
        task.registerlistener((message1,message2)->displayMessage(message1,message2));
        task.executeOnExecutor(AsycProcessTask.SERIAL_EXECUTOR,imageUrl);
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

            String content = "";
            if (view.getId()==R.id.confirm_bt1){
                content = mEditText1.getText().toString();
            }else {
                content = mEditText2.getText().toString();
            }
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
            EditText editText = dialogview.findViewById(R.id.edit_dialog);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogview)
                    .setCancelable(true)
                    .setTitle("提示：")
                    .setPositiveButton("确认", (p1,p2)->{
                        customFileName = editText.getText().toString();
                        if(txtfile.exists()&&imgfile.exists()){
                            try {
                                sendFile(txtfile,imgfile);
                                Log.d("SEND", "onClick: 开始发送文件");
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                    })
                    .setNegativeButton("取消",(p1,p2)->Log.d("Cancel","用户取消"))
                    .create();
            dialog.show();
            dialog.setCanceledOnTouchOutside(true);
    }

    /**
     * 发送文件
     * @param
     */
   public void sendFile (File txtFile,File imgFile) throws Exception{
       tipsDialog.show();
       String txtName = txtFile.getName();
       String imgName = imgFile.getName();
       if(customFileName!=null){
           txtName = customFileName + ".txt";
           file = txtName;
           imgName = customFileName + ".jpg";
       }
       Log.e( "sendFile ",txtName + "     "+imgName );
       LogInterceptor interceptor = new LogInterceptor(CheckApplication.getCotex());
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
               new Handler(SecondActivity.this.getMainLooper()).post(()->{
                   tipsDialog.dismiss();
                   Toast.makeText(SecondActivity.this, "请检查网络！", Toast.LENGTH_SHORT).show();
               });
           }

           @Override
           public void onResponse(Call call, Response response) throws IOException {
               String str = response.body().string();
                    Log.e("响应",str);
                    if (response.isSuccessful()) {
                        Log.d("发送文件：", str);
                        String result = "";
                        try {
                            JSONObject jsonObject = new JSONObject(str);
                            result = jsonObject.getString("code");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if ("200".equals(result)) {
                            new Handler(getMainLooper()).post(()->{
                                tipsDialog.dismiss();
                                Toast.makeText(SecondActivity.this, "发送成功！", Toast.LENGTH_SHORT).show();
                                File fileTxt = new File(getFilesDir(),file);
                                if (fileTxt.exists()){
                                    fileTxt.delete();
                                }
                                Log.w("删除文本", " 删除" );
                            });
                        } else {
                            new Handler(getMainLooper()).post(()->{
                                tipsDialog.dismiss();
                                Toast.makeText(SecondActivity.this, "发送失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        new Handler(SecondActivity.this.getMainLooper()).post(()->{
                            tipsDialog.dismiss();
                            Toast.makeText(SecondActivity.this, "访问服务器失败，请稍后再试！", Toast.LENGTH_SHORT).show();
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
                mEditText1.setEnabled(true);
                mEditText2.setEnabled(true);
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

            public void displayMessage(String text1,String text2) {
                mEditText1.post(new MessagePoster(text1,text2));
                try{
                    Thread.sleep(200);
                }catch (Exception e){
                    e.printStackTrace();
                }
                System.out.println("结果"+text1);
                Log.d("结果：",text1);
            }

            class MessagePoster implements Runnable {
                public MessagePoster(String message1,String message2) {
                    mMessage1 = message1;
                    mMessage2 = message2;
                }

                @Override
                public void run() {
                    mEditText1.setText(Html.fromHtml(mMessage1));
                    mEditText2.setText(Html.fromHtml(mMessage2));
                }

                private final String mMessage1;
                private final String mMessage2;
            }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        task.unregisterListener();
    }
}
