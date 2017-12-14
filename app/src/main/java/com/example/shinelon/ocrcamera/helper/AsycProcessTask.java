package com.example.shinelon.ocrcamera.helper;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.Word;
import com.baidu.ocr.sdk.model.WordSimple;
import com.example.shinelon.ocrcamera.SecondActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Shinelon on 2017/6/30.
 */

public class AsycProcessTask extends AsyncTask<String,String,String> {

    private ProgressDialog mProgressDialog;

    private SecondActivity mSecondActivity;

    private final int BAIDU = 0;
    private final int TENGXU =1;

    /**
     * str
     * 不能为null，不然会报错，因为子线程返回值必须为String
     */
    private static String str ="";
    private Retrofit retrofit;
    private RetorfitRequest request;

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
        // 通用文字识别参数设置

        File file = new File(inputFile);

        publishProgress("开始识别");

        /**
         * 要有足够长的时间等待ocr线程的完成！
         */
        publishProgress("正在处理");
        try {
            Thread.sleep(200);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(!"".equals(getResult())){
            Log.e("getResult",getResult());
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
