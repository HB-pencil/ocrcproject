package com.example.shinelon.ocrcamera.helper;

import com.example.shinelon.ocrcamera.dataModel.TentcentRs;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;


/**
 * Created by Shinelon on 2017/10/25.
 */

public interface RetorfitRequest {
    /**
     *
     * @param appid
     * @param bucket
     * @param file
     * @return
     */
    @Multipart
    @POST("ocr/general")
    Observable<TentcentRs> getResult(@Header("Authorization") String authorization, @Part("appid")RequestBody appid, @Part("bucket") RequestBody bucket,
                                     @Part MultipartBody.Part file);
}
