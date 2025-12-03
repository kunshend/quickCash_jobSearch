package com.example.quickcash.utilities;

public interface AccessTokenListener {
    void onAccessTokenReceived(String token);
    void onAccessTokenError(Exception exception);
}
