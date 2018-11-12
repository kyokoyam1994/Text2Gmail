package com.kyokoyama.android.text2gmail.retrofit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface GoogleApi {

    String BASE_URL = "https://www.googleapis.com";

    @POST("/oauth2/v4/token")
    @FormUrlEncoded
    Call<GoogleResponse> getAccessToken(@Field("code") String code,
                                        @Field("client_id") String clientId,
                                        @Field("client_secret") String clientSecret,
                                        @Field("grant_type") String grantType,
                                        @Field("refresh_token") String refreshToken);

}
