package com.example.shinelon.ocrcamera.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import com.abbyy.mobile.ocr4.ImageLoadingOptions;
import com.abbyy.mobile.ocr4.RecognitionManager;
import com.abbyy.mobile.ocr4.layout.MocrLayout;
import com.abbyy.mobile.ocr4.layout.MocrPrebuiltLayoutInfo;
import com.example.shinelon.ocrcamera.SecondActivity;
import com.example.shinelon.ocrcamera.helper.CheckApplication;

import java.io.File;
import java.io.FileInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Shinelon on 2017/6/30.
 */

public class AsycProcessTask extends AsyncTask<String,String,String> {

    private ProgressDialog mProgressDialog;

    private SecondActivity mSecondActivity;

    /**
     * str
     * 不能为null，不然会报错，因为子线程返回值必须为String
     */
    private static String str ="";

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
        setResult("");

    }
    @Override
    public String doInBackground(String... args){
        StringBuffer sb = new StringBuffer();
        String inputFile = args[0];

        File file = new File(inputFile);
        publishProgress("开始识别");
        /**
         * 要有足够长的时间等待ocr线程的完成！
         */
        publishProgress("正在处理");
        try {
            FileInputStream inputStream = new FileInputStream(file);
            ImageLoadingOptions options = new ImageLoadingOptions();
            options.setShouldUseOriginalImageResolution(true);
            MocrLayout mocrLayout =   CheckApplication.getManager().recognizeText(inputStream, options, new RecognitionManager.RecognitionCallback() {
                @Override
                public boolean onRecognitionProgress(int i, int i1) {
                    Log.e("i和i1",i+"   "+i1);
                    publishProgress("识别进度"+i);
                    return false;
                }

                @Override
                public void onRotationTypeDetected(RecognitionManager.RotationType rotationType) {

                }

                @Override
                public void onPrebuiltWordsInfoReady(MocrPrebuiltLayoutInfo mocrPrebuiltLayoutInfo) {

                }
            });
            String result = mocrLayout.getText();
            String regex = "[`~!@#$%^&*()\\-+={}':;,\\[\\].<>/?￥%…（）_+|【】‘；：”“’。，、？\\s]";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(result);
            setResult(matcher.replaceAll("").trim());
        }catch (Exception e){
            e.printStackTrace();
        }
        if(!"".equals(getResult())){
            Log.e("getResult",getResult());
            publishProgress("识别完成");
            return getResult();
        }else {
            return null;

        }
    }


    @Override
    public void onProgressUpdate(String... values){
        mProgressDialog.setMessage(values[0]);
    }

    @Override
    public void onPostExecute(String result){
        Log.d("onPostExecute",result);
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
