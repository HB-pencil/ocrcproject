package com.example.shinelon.ocrcamera.helper;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Shinelon on 2017/11/19.
 */

public class LogInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
         Request request = chain.request();
        Log.w("拦截器", "intercept: "+ request.body().contentLength() );
         return  chain.proceed(request);
    }
}
