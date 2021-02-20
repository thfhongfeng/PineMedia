package com.pine.media.base.component.ads;

import androidx.annotation.MainThread;

public interface IRewardAdsListener {
    //请求广告失败
    @MainThread
    void onError(int code, String message);

    //视频广告的素材加载完毕，比如视频url等，在此回调后，可以播放在线视频，网络不好可能出现加载缓冲，影响体验。
    @MainThread
    void onRewardVideoAdLoad();

    //视频广告加载后，视频资源缓存到本地的回调，在此回调后，播放本地视频，流畅不阻塞。
    void onRewardVideoCached();
}
