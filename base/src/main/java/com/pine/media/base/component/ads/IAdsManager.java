package com.pine.media.base.component.ads;

import android.content.Context;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.pine.tool.ui.Activity;

public interface IAdsManager {
    void init(Context context);

    //部分机型需要主动申请权限，如 READ_PHONE_STATE权限
    void requestPermissionIfNecessary(Context context);

    //退出时尝试显示"提示安装app"对话框，返回值：true显示对话框、false不显示对话框
    boolean tryShowInstallDialogWhenExit(Activity context, final IExitInstallListener listener);

    //获取穿山甲sdk版本号
    String getSDKVersion();

    /**
     * 加载开屏广告
     *
     * @param activity
     * @param adsViewContainer
     * @param isExpress              是否请求模板广告
     * @param adsListener            广告请求监听器
     * @param adsInteractionListener 广告交互监听器
     * @param downloadListener       广告下载监听器
     */
    void loadSplashAd(Activity activity, FrameLayout adsViewContainer, boolean isExpress,
                      final @NonNull ISplashAdsListener adsListener,
                      final @NonNull ISplashAdsInteractionListener adsInteractionListener,
                      final IAdsDownloadListener downloadListener);

    /**
     * 加载激励广告
     *
     * @param activity
     * @param isExpress
     * @param adsListener            广告请求监听器
     * @param adsInteractionListener 广告交互监听器
     * @param downloadListener       广告下载监听器
     */
    void loadRewardVideoAd(Activity activity, boolean isExpress,
                           final @NonNull IRewardAdsListener adsListener,
                           final @NonNull IRewardAdsInteractionListener adsInteractionListener,
                           final IAdsDownloadListener downloadListener);

    /**
     * 加载全屏广告
     *
     * @param activity
     * @param isExpress
     * @param adsListener            广告请求监听器
     * @param adsInteractionListener 广告交互监听器
     * @param downloadListener       广告下载监听器
     */
    void loadFullScreenVideoAd(final Activity activity, boolean isExpress,
                               final IFullScreenAdsListener adsListener,
                               final IFullScreenAdsInteractionListener adsInteractionListener,
                               final IAdsDownloadListener downloadListener);
}
