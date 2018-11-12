package com.kyokoyama.android.text2gmail.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GoogleApiClient {

    private static GoogleApi instance;

    public static GoogleApi getInstance(){
        if(instance == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(GoogleApi.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            instance = retrofit.create(GoogleApi.class);
        }
        return instance;
    }

}
