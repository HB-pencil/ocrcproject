package com.example.shinelon.ocrcamera.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.shinelon.ocrcamera.MainActivity;

/**
 * Created by Shinelon on 2017/9/16.检查更新的辅助类，依据返回结果判断版本号
 */

public class CheckHelper {

    private Context mActivity;
    private UpdateInfo mInfo;
    private String mUpdateMessage;

    public void setUpdateMessage(String updateMessage) {
        if(updateMessage==null){
            mUpdateMessage = "本次暂无更新内容详细信息！";
        }else{
            mUpdateMessage = updateMessage;
        }

    }

    public String getUpdateMessage() {
        return mUpdateMessage;
    }

    public CheckHelper(){}

    public CheckHelper(Context activity,UpdateInfo info){
        this.mActivity = activity;
        this.mInfo = info;
    }

    /**
     * 检查版本
     * @return
     */
    public boolean hasNewVersion(){
        int newVersion =  mInfo.getData().getLatestVersionCode();
        String updateInfo = mInfo.getData().getUpgradeInfo();
        Log.e("更新信息：",updateInfo + "");
        int oldVersion = 1000;
        try{
            PackageManager manager =  mActivity.getPackageManager();
            PackageInfo packageInfo = manager.getPackageInfo(mActivity.getPackageName(),0);
            oldVersion = packageInfo.versionCode;
        }catch (Exception e){
            e.getMessage();
        }
        if(oldVersion<newVersion){
            setUpdateMessage(updateInfo);
            return true;
        }else {
            return false;
        }
    }

    /**
     *如果 有新版本弹出对话框
     */
    public void showDialog(final FragmentActivity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle("检测到新版本，要更新吗？")
                .setMessage("本次更新内容："+getUpdateMessage())
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     dialog.dismiss();
                   }
                 })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final AsyncDownload downloadTask = new AsyncDownload(activity);
                        final String url = MainActivity.downloadUrl;

                        new Handler(mActivity.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                downloadTask.execute(url);
                            }
                        });
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }



}
