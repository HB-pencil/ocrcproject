package com.example.shinelon.ocrcamera;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.shinelon.ocrcamera.helper.AsycProcessTask;

import java.io.File;
import java.io.FileOutputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

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
        return  intent;
    }

    private final static String USER_NAME = "user_name";

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
            File file = new File(getFilesDir(),fileName);
            if(file.exists()){

            }
        }
    }

    /**
     * 发送文件
     * @param file
     */
   public void senFile (File file) throws Exception{
        MediaType mediaType = MediaType.parse("text/plain;charset=utf-s");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("").post(ResponseBody.create(mediaType,)).build();
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


}
