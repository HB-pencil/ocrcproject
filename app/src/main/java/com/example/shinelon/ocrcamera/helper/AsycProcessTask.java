package com.example.shinelon.ocrcamera.helper;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.example.shinelon.ocrcamera.SecondActivity;

import java.io.File;

/**
 * Created by Shinelon on 2017/6/30.
 */

public class AsycProcessTask extends AsyncTask<String,String,String> {

    private ProgressDialog mProgressDialog;
    private final SecondActivity mSecondActivity;
    private StringBuilder sb = new StringBuilder();
    private static String str = "";

    public AsycProcessTask(SecondActivity activity){
        this.mSecondActivity = activity;
    }

    @Override
    public void onPreExecute(){
        mProgressDialog = new ProgressDialog(mSecondActivity);
        mProgressDialog.setMessage("开始识别");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }
    @Override
    public String doInBackground(String... args){
        String inputFile = args[0];
        // 通用文字识别参数设置
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        param.setImageFile(new File(inputFile));

        publishProgress("正在识别");

    // 调用通用文字识别服务
    OCR.getInstance().recognizeGeneralBasic(param, new OnResultListener<GeneralResult>() {
        @Override
        public void onResult(GeneralResult result) {
            // 调用成功，返回GeneralResult对象
            for (WordSimple wordSimple : result.getWordList()) {
                // wordSimple不包含位置信息
                WordSimple word = wordSimple;
                sb.append(word.getWords());
                sb.append("\n");
            }
             if(!sb.toString().equals("")){
                 publishProgress("识别完成");
             }
            Log.d("RESULT",sb.toString());
            setResult(sb.toString());
        }

        @Override
        public void onError(OCRError error) {
            // 调用失败，返回OCRError对象
            setResult(error.getMessage());
        }
    });

        try{
            Thread.sleep(3000);
        }catch (Exception e){
            e.printStackTrace();
        }
        return getResult();

    }
    @Override
    public void onProgressUpdate(String... values){
        mProgressDialog.setMessage(values[0]);
    }
    @Override
    public void onPostExecute(String result){

         if(mProgressDialog.isShowing()){
             mSecondActivity.updateResult(result);
             mProgressDialog.dismiss();
         }
    }

/**
 * 识别结果
 */
public void setResult(String result){
    str = result;
}

public String getResult(){
    return str;
}

}
