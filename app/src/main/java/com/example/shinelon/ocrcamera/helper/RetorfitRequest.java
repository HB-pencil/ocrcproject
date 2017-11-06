package com.example.shinelon.ocrcamera.helper;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;


/**
 * Created by Shinelon on 2017/10/25.
 */

public interface RetorfitRequest {
    /**
     *
     * @param token token
     * @param userid 用户id
     * @param type 文本/图片
     * @param number 每页数量
     * @return 返回
     */
    @GET("{userid}/{type}/8/{number}")
    Observable<TxtInfo> getResult(@Header("token") String token, @Path("userid") String userid,
                                  @Path("type") String type, @Path("number") int number);
}
