package com.example.shinelon.ocrcamera.helper;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Created by Shinelon on 2017/7/30.权限辅助类
 */

public class PermissionChecker {
    public static final int REQUEST_STORAGY = 1;
    /**
     * 检测权限
     * @param permissions
     */
    public boolean checkPermissions(Activity activity, String [] permissions){
        if(lackPermission(activity,permissions)){
             requestPermission(activity,permissions);
            Log.d("请求权限","我被调用了！");
            return true;
        }else {
            return false;
        }
    }

    /**
     * 逐个检查
     * @param context
     * @param permissions
     * @return
     */
    public  boolean lackPermission(Activity context,String [] permissions){
        for(String permission : permissions){
            if(ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_DENIED){
                return true;
            }
        }
        return false;
    }

    /**
     * 动态申请权限
     * @param context
     * @param permissions
     */
    public void requestPermission(Activity context,String [] permissions){
        ActivityCompat.requestPermissions(context,permissions,REQUEST_STORAGY);
    }

    /**
     * 启动手动配置界面
     * @param activity
     */
    public void startAppSetting(Activity activity){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",activity.getPackageName(),null);
        intent.setData(uri);
        activity.startActivity(intent);
    }


}
