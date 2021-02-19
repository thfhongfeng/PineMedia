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
                                    IAdsRequestStateListener requestStateListener,
                                    IAdsInteractionListener interactionListener,
                                    IAdsDownloadListener downloadListener) {
        mAdsManagerImpl.loadSplashAd(activity, adsViewContainer, isExpress, requestStateListener,
                interactionListener, downloadListener);
    }
}

