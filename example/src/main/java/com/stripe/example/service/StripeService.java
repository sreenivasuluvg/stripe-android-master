
package com.stripe.example.service;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;
import rx.Observable;

/**
 * A Retrofit service used to communicate with a server.
 */
public interface StripeService
{

    @FormUrlEncoded
    @POST("ephemeral_keys")
    Observable<ResponseBody> createEphemeralKey(@FieldMap Map<String, String> apiVersionMap);

    @FormUrlEncoded
    @POST("payment/createtoken")
    Observable<ResponseBody> createPaymentIntent(@Header("Authorization") String authorization,@FieldMap Map<String, Object> params);
}
