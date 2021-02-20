package com.pine.media.base.component.ads;

public interface IRewardAdsInteractionListener {
    //广告的下载bar点击回调
    void onAdVideoBarClick();

    //视频广告关闭回调
    void onAdClose();

    //视频播放完成回调
    void onVideoComplete();

    //视频广告播放错误回调
    void onVideoError();

    //视频播放完成后，奖励验证回调，rewardVerify：是否有效，rewardAmount：奖励梳理，rewardName：奖励名称，code：错误码，msg：错误信息
    void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int code, String msg);

    //视频广告跳过回调
    void onSkippedVideo();

    //视频广告展示回调
    void onAdShow();
}
