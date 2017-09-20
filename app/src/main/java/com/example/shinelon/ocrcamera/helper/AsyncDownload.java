package com.example.shinelon.ocrcamera.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Shinelon on 2017/9/16.
 */

public class AsyncDownload extends AsyncTask<String,Long,Boolean> {
    String apkName;
    Context mContext;
    Boolean result = true;
    float total;
    ProgressDialog mProgressDialog;
    File file;

  public AsyncDownload(Context context){
      this.mContext = context;
      this.mProgressDialog = new ProgressDialog(context);
  }

    @Override
    public void onPreExecute() {
        mProgressDialog.setTitle("准备下载");
        mProgressDialog.setMax(100);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setProgress(0);

        mProgressDialog.show();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String url = params[0];
        Response response = null;
        OkHttpClient client = new OkHttpClient();
        apkName = url.substring(url.lastIndexOf("/")+1);
        Request request = new Request.Builder()
                .url(url)
                .build();
        try{
            response = client.newCall(request).execute();
        } catch (Exception e){e.printStackTrace();}

            if (response.isSuccessful()){
                InputStream in = null;
                FileOutputStream out = null;
                if (response.code()==200){
                    try{
                        long length = response.body().contentLength();
                        Log.e("total",""+total);
                        in = response.body().byteStream();
                        Log.d("In","in不为空！");
                        if(in!=null){
                            Log.d("In","in不为空！");
                            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),apkName);
                            if(!file.exists()){
                                file.createNewFile();
                            }
                            Log.e("File",file.getAbsolutePath()+"");
                            out = new FileOutputStream(file);
                            byte [] buf = new byte[1024];
                            int ch = -1;
                            long process = 0;
                            while((ch=in.read(buf))!=-1){
                                out.write(buf,0,ch);
                                process += ch;
                                publishProgress(length,process);
                            }
                            out.flush();
                            out.close();
                        }
                        result =true;
                    }catch (Exception e){
                        e.printStackTrace();
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext,"下载失败！",Toast.LENGTH_SHORT).show();
                            }
                        });
                        result = false;
                    }finally {
                        try{
                            if(in != null){
                                in.close();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }else {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext,"下载失败！",Toast.LENGTH_SHORT).show();
                    }
                });
                result = false;
            }

            return result;
    }

    @Override
    protected void onProgressUpdate(Long... values) {

        float size = values[0]/1024.0F/1024.0F;
        float downloaded = values[1]/1024.0F/1024.0F;
        DecimalFormat format = new DecimalFormat("#.00");
        Log.d("大小",size+"            "+downloaded);
        mProgressDialog.setTitle("正在下载");
        mProgressDialog.setMessage("已下载"+format.format(downloaded)+"M，"+"总共"+format.format(size)+"M");

    }

    @Override
    public void onPostExecute(Boolean result) {
        if(mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
        if (result){
            AlertDialog dialog = new AlertDialog.Builder(mContext)
                    .setTitle("下载完成")
                    .setMessage("已经成功下载安装包，要立刻安装吗？")
                    .setCancelable(true)
                    .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N){
                                Uri mUri = FileProvider.getUriForFile(mContext,"com.example.shinelon.ocrcamera.ocrProvider",file);
                                intent.setDataAndType(mUri,"application/vnd.android.package-archive");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            }else{
                                intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            }
                            mContext.startActivity(intent);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        }
                    })
                    .create();
            dialog.show();
        }
    }
}
