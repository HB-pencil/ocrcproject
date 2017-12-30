package com.example.shinelon.ocrcamera.helper;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.webkit.WebSettings;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Shinelon on 2017/11/19.
 */

public class LogInterceptor implements Interceptor {
    private Context context;
    public LogInterceptor(Context c){
        context = c;
    }
    @Override
    public Response intercept(Chain chain) throws IOException {
         Request request = chain.request().newBuilder()
                 .removeHeader("User-Agent")
                 .addHeader("User-Agent", WebSettings.getDefaultUserAgent(context))
                 .build();
        Response response = chain.proceed(request);
        Log.w("拦截器", "intercept: "+ response.code() );
         return response;
    }
}
