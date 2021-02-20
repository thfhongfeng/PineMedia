package com.pine.media.base.component.ads;

public interface IFullScreenAdsInteractionListener {
    //广告的展示回调
    void onAdShow();

    //广告的下载bar点击回调
    void onAdVideoBarClick();

    //广告关闭的回调
    void onAdClose();

    //视频播放完毕的回调
    void onVideoComplete();

    //跳过视频播放
    void onSkippedVideo();
}
