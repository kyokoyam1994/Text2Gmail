package com.example.kosko.text2gmail.retrofit;

public class GoogleResponse {

    private String access_token;
    private String id_token;
    private String refresh_token;
    private int expires_in;
    private String token_type;

    public String getAccessToken() {
        return access_token;
    }

    public void setAccessToken(String access_token) {
        this.access_token = access_token;
    }

    public String getIdToken() {
        return id_token;
    }

    public void setIdToken(String id_token) {
        this.id_token = id_token;
    }

    public String getRefreshToken() {
        return refresh_token;
    }

    public void setRefreshToken(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public int getExpiresIn() {
        return expires_in;
    }

    public void setExpiresIn(int expires_in) {
        this.expires_in = expires_in;
    }

    public String getTokenType() {
        return token_type;
    }

    public void setTokenType(String token_type) {
        this.token_type = token_type;
    }

}
