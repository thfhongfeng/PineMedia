package com.pine.media.base.component.ads;

public interface IFullScreenAdsListener {
    //请求广告失败
    void onError(int code, String message);

    //广告物料加载完成的回调
    void onFullScreenVideoAdLoad();

    //广告视频本地加载完成的回调，接入方可以在这个回调后直接播放本地视频
    void onFullScreenVideoCached();
}
