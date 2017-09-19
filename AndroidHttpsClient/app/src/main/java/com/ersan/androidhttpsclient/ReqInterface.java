package com.ersan.androidhttpsclient;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by admin on 2017/6/20.
 */

public interface ReqInterface {
    @GET("/")
    Observable<String> test();
}
