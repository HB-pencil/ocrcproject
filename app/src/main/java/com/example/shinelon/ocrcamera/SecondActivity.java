package com.example.shinelon.ocrcamera;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shinelon.ocrcamera.helper.AsycProcessTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
            String fileName = account + System.currentTimeMillis();
            try{
                FileOutputStream out = this.openFileOutput(fileName,MODE_PRIVATE);
                out.write(content.getBytes());
                out.close();
            }catch(Exception e){
                e.printStackTrace();
            }
            File txtfile = new File(getFilesDir(),fileName);
            Log.d("SecondActivity",txtfile.getPath());
            File imgfile = new File(imageUrl);
            if(txtfile.exists()){
                try {
                    senFile(txtfile,imgfile);
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    deleteFile(fileName);
                }

            }
        }
    }

    /**
     * 发送文件
     * @param
     */
   public void senFile (File txtFile, File imgFile) throws Exception{
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new MultipartBody.Builder().addPart(
                Headers.of("Content-Disposition","form-data;name=\"username\""),
                RequestBody.create(null,getIntent().getStringExtra(USER_NAME)))
                                                      .addPart(
                Headers.of("Content-Disposition","form-data;name=\"txt\";filename=\""+ txtFile.getName() + "\""),
                RequestBody.create(MediaType.parse("text/plain"),txtFile))
                                                      .addPart(
                Headers.of("Content-Disposition","form-data;name=\"img\"filename=\""+ imgFile.getName() + "\""),
                RequestBody.create(MediaType.parse("img/jpeg"),imgFile))
                                                      .build();

       Request request = new Request.Builder().url("").post(body).build();
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
                if(response.isSuccessful()){

                }else{
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

    public void updateResult(String message) {

        displayMessage(message);

    }

    public void displayMessage( String text )
    {
        mEditText.post( new MessagePoster( text ) );
    }

    class MessagePoster implements Runnable {
        public MessagePoster( String message )
        {
            _message = message;
        }

        public void run() {
            mEditText.append( _message + "\n" );
        }

        private final String _message;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
        }
        return super.onKeyDown(keyCode,keyEvent);
    }

}
