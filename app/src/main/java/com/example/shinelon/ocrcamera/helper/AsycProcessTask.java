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
    private static String str ="";//不能为null，不然会报错，因为子线程返回值必须为String

    public AsycProcessTask(SecondActivity activity){
        this.mSecondActivity = activity;
    }

    @Override
    public void onPreExecute(){
        mProgressDialog = new ProgressDialog(mSecondActivity);
        mProgressDialog.setMessage("准备识别");
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

        publishProgress("开始识别");

        /**
         * 此方法又开启了一个异步线程！并且是耗时的！！！
         */
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
            String mResult = sb.toString();
            publishProgress("正在识别");
             if(mResult.length()>=1){
                 publishProgress("识别完成");
                 setResult(mResult);
             }else{
                 publishProgress("识别失败");
             }
            Log.d("RESULT",mResult);
            System.out.print(mResult);
        }
        @Override
        public void onError(OCRError error) {
            // 调用失败，返回OCRError对象
            setResult(error.getMessage());
        }
    });
        /**
         * 要有足够长的时间等待ocr线程的完成！
         */
        try {
            Thread.sleep(4000);
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.d("getResult",getResult());
        System.out.println("我是doinbackground的返回值  "+ getResult());
        return getResult();
    }
    @Override
    public void onProgressUpdate(String... values){
        mProgressDialog.setMessage(values[0]);
    }
    @Override
    public void onPostExecute(String result){
        Log.d("onPostExecute",result);
        System.out.println("识别result我被执行了！"+result);
        if(result.length()>=1){
            mSecondActivity.updateResult(result);
        }else{
            mSecondActivity.updateResult("识别出错，请重试！");
        }
        if(mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
    }

    public void setResult(String result){
        str = result;
        Log.d("setResult()","我是setReult "+str);
    }

    public String getResult(){
        Log.d("getResult()","我是getReult "+str);
        return str;
    }

    }
