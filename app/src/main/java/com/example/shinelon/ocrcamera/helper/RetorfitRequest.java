package com.example.shinelon.ocrcamera.helper;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;


/**
 * Created by Shinelon on 2017/10/25.
 */

public interface RetorfitRequest {
    @GET("{userid/{type}/8/{number}")
    Call<ResponseBody> getResult(@Header("token") String token, @Path("userid") String userid,
                                 @Path("type") String type, @Path("number") int number);
}
