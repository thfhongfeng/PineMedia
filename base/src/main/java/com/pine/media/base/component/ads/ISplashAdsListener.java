package com.pine.media.base.component.ads;

import android.view.View;

import androidx.annotation.MainThread;

public interface ISplashAdsListener {
    //请求广告失败
    @MainThread
    void onError(int code, String message);

    //请求广告超时
    void onTimeout();

    //请求广告成功
    @MainThread
    void onAdLoad(View adsView);
}
