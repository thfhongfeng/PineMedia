package com.pine.media.login.model;

public interface ILoginResponse {
    boolean onLoginResponse(boolean isSuccess, String msg);

    void onCancel();
}
