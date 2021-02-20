package com.pine.media.base.component.ads;

import android.content.Context;
import android.widget.FrameLayout;

import com.pine.tool.ui.Activity;
import com.pine.tool.util.LogUtils;

public class AdsSdkManager {
    private final static String TAG = LogUtils.makeLogTag(AdsSdkManager.class);
    private static volatile IAdsManager mAdsManagerImpl;

    private AdsSdkManager() {

    }

    public static void init(Context context, IAdsManagerFactory factory) {
        mAdsManagerImpl = factory.makeAdsManager(context);
        mAdsManagerImpl.init(context);
    }

    public static void loadSplashAd(Activity activity, FrameLayout adsViewContainer, boolean isExpress,
                                    ISplashAdsListener adsListener,
                                    ISplashAdsInteractionListener adsInteractionListener,
                                    IAdsDownloadListener downloadListener) {
        mAdsManagerImpl.loadSplashAd(activity, adsViewContainer, isExpress, adsListener,
                adsInteractionListener, downloadListener);
    }

    public static void loadRewardVideoAd(Activity activity, boolean isExpress,
                                         IRewardAdsListener adsListener,
                                         IRewardAdsInteractionListener adsInteractionListener,
                                         IAdsDownloadListener downloadListener) {
        mAdsManagerImpl.loadRewardVideoAd(activity, isExpress, adsListener,
                adsInteractionListener, downloadListener);
    }

    public static void loadFullScreenVideoAd(Activity activity, boolean isExpress,
                                         IFullScreenAdsListener adsListener,
                                         IFullScreenAdsInteractionListener adsInteractionListener,
                                         IAdsDownloadListener downloadListener) {
        mAdsManagerImpl.loadFullScreenVideoAd(activity, isExpress, adsListener,
                adsInteractionListener, downloadListener);
    }
}

