package com.example.shinelon.ocrcamera;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by Shinelon on 2017/4/2.识别结果Activity
 */

public class SecondActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText  mEditText;
    private String outputPath;
    private Button mButton;
    private static final String IMAGE_PATH = "IMAGE_PATH";
    private static final String OUTPUT_PATH = "OUTPUT_PATH";
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
            outputPath = extras.getString(OUTPUT_PATH);
            System.out.println("extras is " + imageUrl + "and" + outputPath);
            doRecognize();
        }
    }

    public void doRecognize(){
        // Starting recognition process
  
    }

    public static Intent newInstance(Context context,String...values){
        Intent intent = new Intent(context,SecondActivity.class);
        intent.putExtra(IMAGE_PATH,values[0]);
        intent.putExtra(OUTPUT_PATH,values[1]);
        return  intent;
    }

    @Override
    public void onClick(View view){
        if(view.getId() == R.id.confirm_bt){
            deleteFile(outputPath);
        }
    }

    public void updateResult(Boolean success) {
        if (!success)
            return;
        try {
            StringBuilder contents = new StringBuilder();

            FileInputStream fis = openFileInput(outputPath);
            try {
                InputStreamReader reader = new InputStreamReader(fis, "UTF-8");
                BufferedReader bufReader = new BufferedReader(reader);
                String text = null;
                while ((text = bufReader.readLine()) != null) {
                    contents.append(text).append(System.getProperty("line.separator"));
                }
            } finally {
                fis.close();
            }

            displayMessage(contents.toString());
        } catch (Exception e) {
            displayMessage("Error: " + e.getMessage());
        }
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
