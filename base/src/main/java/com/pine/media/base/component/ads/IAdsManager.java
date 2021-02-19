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
     * @param isExpress            是否请求模板广告
     * @param requestStateListener 广告请求监听器
     * @param interactionListener  广告交互监听器
     * @param downloadListener     广告下载监听器
     */
    void loadSplashAd(Activity activity, FrameLayout adsViewContainer, boolean isExpress,
                      @NonNull IAdsRequestStateListener requestStateListener,
                      @NonNull IAdsInteractionListener interactionListener,
                      IAdsDownloadListener downloadListener);
}
