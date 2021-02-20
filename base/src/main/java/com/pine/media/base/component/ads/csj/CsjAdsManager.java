package com.pine.media.base.component.ads.csj;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.bytedance.sdk.openadsdk.TTSplashAd;
import com.bytedance.sdk.openadsdk.downloadnew.core.ExitInstallListener;
import com.pine.media.base.component.ads.IAdsDownloadListener;
import com.pine.media.base.component.ads.IAdsManager;
import com.pine.media.base.component.ads.IExitInstallListener;
import com.pine.media.base.component.ads.IFullScreenAdsInteractionListener;
import com.pine.media.base.component.ads.IFullScreenAdsListener;
import com.pine.media.base.component.ads.IRewardAdsInteractionListener;
import com.pine.media.base.component.ads.IRewardAdsListener;
import com.pine.media.base.component.ads.ISplashAdsInteractionListener;
import com.pine.media.base.component.ads.ISplashAdsListener;
import com.pine.media.config.BuildConfig;
import com.pine.tool.ui.Activity;
import com.pine.tool.util.LogUtils;

public class CsjAdsManager implements IAdsManager {
    private final static String TAG = LogUtils.makeLogTag(CsjAdsManager.class);

    //开屏广告加载超时时间,建议大于3000,这里为了冷启动第一次加载到广告并且展示,示例设置了3000ms
    private static final int AD_TIME_OUT = 3000;

    private static CsjAdsManager mInstance;
    private static String mCodeId = BuildConfig.ADS_CSJ_FOR_CODE_ID;

    public static IAdsManager getInstance() {
        if (mInstance == null) {
            synchronized (CsjAdsManager.class) {
                if (mInstance == null) {
                    LogUtils.releaseLog(TAG, "use third ads: chuanshanjia mCodeId:" + mCodeId);
                    mInstance = new CsjAdsManager();
                }
            }
        }
        return mInstance;
    }

    @Override
    public void init(Context context) {
        TTAdSdk.init(context,
                new TTAdConfig.Builder()
                        .appId(BuildConfig.ADS_CSJ_FOR_APP_ID)
                        .useTextureView(true) //默认使用SurfaceView播放视频广告,当有SurfaceView冲突的场景，可以使用TextureView
                        .appName(BuildConfig.APP_NAME)
                        .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)//落地页主题
                        .allowShowNotify(true) //是否允许sdk展示通知栏提示
                        .debug(BuildConfig.DEBUG) //测试阶段打开，可以通过日志排查问题，上线时去除该调用
                        .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI) //允许直接下载的网络状态集合,没有设置的网络下点击下载apk会有二次确认弹窗，弹窗中会披露应用信息
                        .supportMultiProcess(false) //是否支持多进程，true支持
                        .asyncInit(true) //是否异步初始化sdk,设置为true可以减少SDK初始化耗时
                        //.httpStack(new MyOkStack3())//自定义网络库，demo中给出了okhttp3版本的样例，其余请自行开发或者咨询工作人员。
                        .build());
    }

    @Override
    public void requestPermissionIfNecessary(Context context) {
        TTAdSdk.getAdManager().requestPermissionIfNecessary(context);
    }

    @Override
    public boolean tryShowInstallDialogWhenExit(Activity context, final IExitInstallListener listener) {
        return TTAdSdk.getAdManager().tryShowInstallDialogWhenExit(context, new ExitInstallListener() {
            @Override
            public void onExitInstall() {
                listener.onExitInstall();
            }
        });
    }

    @Override
    public String getSDKVersion() {
        return TTAdSdk.getAdManager().getSDKVersion();
    }

    @Override
    public void loadSplashAd(Activity activity, FrameLayout adsViewContainer, boolean isExpress,
                             @NonNull final ISplashAdsListener adsListener,
                             @NonNull final ISplashAdsInteractionListener adsInteractionListener,
                             final IAdsDownloadListener downloadListener) {
        TTAdNative tTAdNative = TTAdSdk.getAdManager().createAdNative(activity);
        //创建开屏广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = null;
        if (isExpress) {
            //个性化模板广告需要传入期望广告view的宽、高，单位dp，请传入实际需要的大小，
            //比如：广告下方拼接logo、适配刘海屏等，需要考虑实际广告大小
            //float expressViewWidth = UIUtils.getScreenWidthDp(this);
            //float expressViewHeight = UIUtils.getHeight(this);
            adSlot = new AdSlot.Builder()
                    .setCodeId(mCodeId)
                    //模板广告需要设置期望个性化模板广告的大小,单位dp,代码位是否属于个性化模板广告，请在穿山甲平台查看
                    //view宽高等于图片的宽高
                    .setExpressViewAcceptedSize(1080, 1920)
                    .build();
        } else {
            adSlot = new AdSlot.Builder()
                    .setCodeId(mCodeId)
                    .setImageAcceptedSize(1080, 1920)
                    .build();
        }

        //step4:请求广告，调用开屏广告异步请求接口，对请求回调的广告作渲染处理
        tTAdNative.loadSplashAd(adSlot, new TTAdNative.SplashAdListener() {
            @Override
            @MainThread
            public void onError(int code, String message) {
                LogUtils.d(TAG, "splashAd load onError：" + String.valueOf(message));
                if (adsListener != null) {
                    adsListener.onError(code, message);
                }
            }

            @Override
            @MainThread
            public void onTimeout() {
                LogUtils.d(TAG, "splashAd load onTimeout");
                if (adsListener != null) {
                    adsListener.onTimeout();
                }
            }

            @Override
            @MainThread
            public void onSplashAdLoad(TTSplashAd ad) {
                LogUtils.d(TAG, "splashAd load success");
                if (ad == null) {
                    return;
                }
                if (adsListener != null) {
                    adsListener.onAdLoad(ad.getSplashView());
                }

                //设置SplashView的交互监听器
                ad.setSplashInteractionListener(new TTSplashAd.AdInteractionListener() {
                    @Override
                    public void onAdClicked(View view, int type) {
                        LogUtils.d(TAG, "splashAd interaction onAdClicked");
                        if (adsInteractionListener != null) {
                            adsInteractionListener.onAdClicked(view, type);
                        }
                    }

                    @Override
                    public void onAdShow(View view, int type) {
                        LogUtils.d(TAG, "splashAd interaction onAdShow");
                        if (adsInteractionListener != null) {
                            adsInteractionListener.onAdShow(view, type);
                        }
                    }

                    @Override
                    public void onAdSkip() {
                        LogUtils.d(TAG, "splashAd interaction onAdSkip");
                        if (adsInteractionListener != null) {
                            adsInteractionListener.onAdSkip();
                        }
                    }

                    @Override
                    public void onAdTimeOver() {
                        LogUtils.d(TAG, "splashAd interaction onAdTimeOver");
                        if (adsInteractionListener != null) {
                            adsInteractionListener.onAdTimeOver();
                        }
                    }
                });
                if (ad.getInteractionType() == TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
                    ad.setDownloadListener(new TTAppDownloadListener() {
                        boolean hasShowDownloadActive = false;

                        @Override
                        public void onIdle() {
                            LogUtils.d(TAG, "splashAd download onIdle");
                            if (downloadListener != null) {
                                downloadListener.onIdle();
                            }
                        }

                        @Override
                        public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                            if (!hasShowDownloadActive) {
                                LogUtils.d(TAG, "splashAd download onDownloadActive");
                                hasShowDownloadActive = true;
                            }
                            if (downloadListener != null) {
                                downloadListener.onDownloadActive(totalBytes, currBytes, fileName, appName);
                            }
                        }

                        @Override
                        public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                            LogUtils.d(TAG, "splashAd download onDownloadPaused");
                            if (downloadListener != null) {
                                downloadListener.onDownloadPaused(totalBytes, currBytes, fileName, appName);
                            }
                        }

                        @Override
                        public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                            LogUtils.d(TAG, "splashAd download onDownloadFailed");
                            if (downloadListener != null) {
                                downloadListener.onDownloadFailed(totalBytes, currBytes, fileName, appName);
                            }
                        }

                        @Override
                        public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                            LogUtils.d(TAG, "splashAd download onDownloadFinished");
                            if (downloadListener != null) {
                                downloadListener.onDownloadFinished(totalBytes, fileName, appName);
                            }
                        }

                        @Override
                        public void onInstalled(String fileName, String appName) {
                            LogUtils.d(TAG, "splashAd download onInstalled");
                            if (downloadListener != null) {
                                downloadListener.onInstalled(fileName, appName);
                            }
                        }
                    });
                }
            }
        }, AD_TIME_OUT);
    }

    @Override
    public void loadRewardVideoAd(final Activity activity, boolean isExpress,
                                  final IRewardAdsListener adsListener,
                                  final IRewardAdsInteractionListener adsInteractionListener,
                                  final IAdsDownloadListener downloadListener) {
        TTAdSdk.getAdManager().requestPermissionIfNecessary(activity);
        TTAdNative tTAdNative = TTAdSdk.getAdManager().createAdNative(activity);
        AdSlot adSlot;
        if (isExpress) {
            //个性化模板广告需要传入期望广告view的宽、高，单位dp，
            adSlot = new AdSlot.Builder()
                    .setCodeId(mCodeId)
                    //模板广告需要设置期望个性化模板广告的大小,单位dp,激励视频场景，只要设置的值大于0即可
                    .setExpressViewAcceptedSize(500, 500)
                    .build();
        } else {
            //模板广告需要设置期望个性化模板广告的大小,单位dp,代码位是否属于个性化模板广告，请在穿山甲平台查看
            adSlot = new AdSlot.Builder()
                    .setCodeId(mCodeId)
                    .build();
        }
        //step5:请求广告
        tTAdNative.loadRewardVideoAd(adSlot, new TTAdNative.RewardVideoAdListener() {
            private TTRewardVideoAd mTTRewardVideoAd = null;

            private String getAdType(int type) {
                switch (type) {
                    case TTAdConstant.AD_TYPE_COMMON_VIDEO:
                        return "普通激励视频，type=" + type;
                    case TTAdConstant.AD_TYPE_PLAYABLE_VIDEO:
                        return "Playable激励视频，type=" + type;
                    case TTAdConstant.AD_TYPE_PLAYABLE:
                        return "纯Playable，type=" + type;
                }
                return "未知类型+type=" + type;
            }

            @Override
            public void onError(int code, String message) {
                LogUtils.d(TAG, "rewardAd load onError：" + String.valueOf(message));
                if (adsListener != null) {
                    adsListener.onError(code, message);
                }
            }

            //视频广告的素材加载完毕，比如视频url等，在此回调后，可以播放在线视频，网络不好可能出现加载缓冲，影响体验。
            @Override
            public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
                LogUtils.d(TAG, "rewardAd load onRewardVideoAdLoad 广告类型：" + getAdType(ad.getRewardVideoAdType()));
                mTTRewardVideoAd = ad;
                ad.setRewardAdInteractionListener(new TTRewardVideoAd.RewardAdInteractionListener() {

                    @Override
                    public void onAdShow() {
                        LogUtils.d(TAG, "rewardAd interaction onAdShow");
                        if (adsInteractionListener != null) {
                            adsInteractionListener.onAdShow();
                        }
                    }

                    @Override
                    public void onAdVideoBarClick() {
                        LogUtils.d(TAG, "rewardAd interaction onAdVideoBarClick");
                        if (adsInteractionListener != null) {
                            adsInteractionListener.onAdVideoBarClick();
                        }
                    }

                    @Override
                    public void onAdClose() {
                        LogUtils.d(TAG, "rewardAd interaction onAdClose");
                        if (adsInteractionListener != null) {
                            adsInteractionListener.onAdClose();
                        }
                    }

                    //视频播放完成回调
                    @Override
                    public void onVideoComplete() {
                        LogUtils.d(TAG, "rewardAd interaction onVideoComplete");
                        if (adsInteractionListener != null) {
                            adsInteractionListener.onVideoComplete();
                        }
                    }

                    @Override
                    public void onVideoError() {
                        LogUtils.d(TAG, "rewardAd interaction onVideoError");
                        if (adsInteractionListener != null) {
                            adsInteractionListener.onVideoError();
                        }
                    }

                    //视频播放完成后，奖励验证回调，rewardVerify：是否有效，rewardAmount：奖励梳理，rewardName：奖励名称
                    @Override
                    public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int errorCode, String errorMsg) {
                        String logString = "verify:" + rewardVerify + " amount:" + rewardAmount +
                                " name:" + rewardName + " errorCode:" + errorCode + " errorMsg:" + errorMsg;
                        LogUtils.d(TAG, "rewardAd interaction onRewardVerify :" + logString);
                        if (adsInteractionListener != null) {
                            adsInteractionListener.onRewardVerify(rewardVerify, rewardAmount, rewardName,
                                    errorCode, errorMsg);
                        }
                    }

                    @Override
                    public void onSkippedVideo() {
                        LogUtils.d(TAG, "rewardAd interaction onSkippedVideo");
                        if (adsInteractionListener != null) {
                            adsInteractionListener.onSkippedVideo();
                        }
                    }
                });
                if (ad.getInteractionType() == TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
                    ad.setDownloadListener(new TTAppDownloadListener() {
                        boolean hasShowDownloadActive = false;

                        @Override
                        public void onIdle() {
                            LogUtils.d(TAG, "splashAd download onIdle");
                            if (downloadListener != null) {
                                downloadListener.onIdle();
                            }
                        }

                        @Override
                        public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                            if (!hasShowDownloadActive) {
                                LogUtils.d(TAG, "splashAd download onDownloadActive");
                                hasShowDownloadActive = true;
                            }
                            if (downloadListener != null) {
                                downloadListener.onDownloadActive(totalBytes, currBytes, fileName, appName);
                            }
                        }

                        @Override
                        public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                            LogUtils.d(TAG, "splashAd download onDownloadPaused");
                            if (downloadListener != null) {
                                downloadListener.onDownloadPaused(totalBytes, currBytes, fileName, appName);
                            }
                        }

                        @Override
                        public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                            LogUtils.d(TAG, "splashAd download onDownloadFailed");
                            if (downloadListener != null) {
                                downloadListener.onDownloadFailed(totalBytes, currBytes, fileName, appName);
                            }
                        }

                        @Override
                        public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                            LogUtils.d(TAG, "splashAd download onDownloadFinished");
                            if (downloadListener != null) {
                                downloadListener.onDownloadFinished(totalBytes, fileName, appName);
                            }
                        }

                        @Override
                        public void onInstalled(String fileName, String appName) {
                            LogUtils.d(TAG, "splashAd download onInstalled");
                            if (downloadListener != null) {
                                downloadListener.onInstalled(fileName, appName);
                            }
                        }
                    });
                }

                if (adsListener != null) {
                    adsListener.onRewardVideoCached();
                }
            }

            //视频广告加载后，视频资源缓存到本地的回调，在此回调后，播放本地视频，流畅不阻塞。
            @Override
            public void onRewardVideoCached() {
                LogUtils.d(TAG, "rewardAd load onRewardVideoCached");
                if (mTTRewardVideoAd != null) {
                    mTTRewardVideoAd.showRewardVideoAd(activity, TTAdConstant.RitScenes.CUSTOMIZE_SCENES, "scenes_test");
                }
                if (adsListener != null) {
                    adsListener.onRewardVideoCached();
                }
                mTTRewardVideoAd = null;
            }
        });
    }

    @Override
    public void loadFullScreenVideoAd(final Activity activity, boolean isExpress,
                                      final IFullScreenAdsListener adsListener,
                                      final IFullScreenAdsInteractionListener adsInteractionListener,
                                      final IAdsDownloadListener downloadListener) {
        TTAdSdk.getAdManager().requestPermissionIfNecessary(activity);
        TTAdNative tTAdNative = TTAdSdk.getAdManager().createAdNative(activity);
        AdSlot adSlot;
        if (isExpress) {
            adSlot = new AdSlot.Builder()
                    .setCodeId(mCodeId)
                    //模板广告需要设置期望个性化模板广告的大小,单位dp,全屏视频场景，只要设置的值大于0即可
                    .setExpressViewAcceptedSize(500, 500)
                    .build();

        } else {
            adSlot = new AdSlot.Builder()
                    .setCodeId(mCodeId)
                    .build();
        }
        //step5:请求广告
        tTAdNative.loadFullScreenVideoAd(adSlot, new TTAdNative.FullScreenVideoAdListener() {
            private TTFullScreenVideoAd mttFullVideoAd = null;

            private String getAdType(int type) {
                switch (type) {
                    case TTAdConstant.AD_TYPE_COMMON_VIDEO:
                        return "普通全屏视频，type=" + type;
                    case TTAdConstant.AD_TYPE_PLAYABLE_VIDEO:
                        return "Playable全屏视频，type=" + type;
                    case TTAdConstant.AD_TYPE_PLAYABLE:
                        return "纯Playable，type=" + type;
                }

                return "未知类型+type=" + type;
            }

            @Override
            public void onError(int code, String message) {
                LogUtils.d(TAG, "fullScreenAd load onError：" + String.valueOf(message));
                if (adsListener != null) {
                    adsListener.onError(code, message);
                }
            }

            @Override
            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
                LogUtils.d(TAG, "fullScreenAd load onFullScreenVideoAdLoad 广告类型：" + getAdType(ad.getFullVideoAdType()));
                mttFullVideoAd = ad;
                mttFullVideoAd.setFullScreenVideoAdInteractionListener(new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {

                    @Override
                    public void onAdShow() {
                        LogUtils.d(TAG, "fullScreenAd interaction onAdShow");
                        if (adsInteractionListener != null) {
                            adsInteractionListener.onAdShow();
                        }
                    }

                    @Override
                    public void onAdVideoBarClick() {
                        LogUtils.d(TAG, "fullScreenAd interaction onAdVideoBarClick");
                        if (adsInteractionListener != null) {
                            adsInteractionListener.onAdVideoBarClick();
                        }
                    }

                    @Override
                    public void onAdClose() {
                        LogUtils.d(TAG, "fullScreenAd interaction onAdClose");
                        if (adsInteractionListener != null) {
                            adsInteractionListener.onAdClose();
                        }
                    }

                    @Override
                    public void onVideoComplete() {
                        LogUtils.d(TAG, "fullScreenAd interaction onVideoComplete");
                        if (adsInteractionListener != null) {
                            adsInteractionListener.onVideoComplete();
                        }
                    }

                    @Override
                    public void onSkippedVideo() {
                        LogUtils.d(TAG, "fullScreenAd interaction onSkippedVideo");
                        if (adsInteractionListener != null) {
                            adsInteractionListener.onSkippedVideo();
                        }
                    }

                });

                if (ad.getInteractionType() == TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
                    ad.setDownloadListener(new TTAppDownloadListener() {
                        boolean hasShowDownloadActive = false;

                        @Override
                        public void onIdle() {
                            LogUtils.d(TAG, "fullScreenAd download onIdle");
                            if (downloadListener != null) {
                                downloadListener.onIdle();
                            }
                        }

                        @Override
                        public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                            if (!hasShowDownloadActive) {
                                LogUtils.d(TAG, "fullScreenAd download onDownloadActive");
                                hasShowDownloadActive = true;
                            }
                            if (downloadListener != null) {
                                downloadListener.onDownloadActive(totalBytes, currBytes, fileName, appName);
                            }
                        }

                        @Override
                        public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                            LogUtils.d(TAG, "fullScreenAd download onDownloadPaused");
                            if (downloadListener != null) {
                                downloadListener.onDownloadPaused(totalBytes, currBytes, fileName, appName);
                            }
                        }

                        @Override
                        public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                            LogUtils.d(TAG, "fullScreenAd download onDownloadFailed");
                            if (downloadListener != null) {
                                downloadListener.onDownloadFailed(totalBytes, currBytes, fileName, appName);
                            }
                        }

                        @Override
                        public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                            LogUtils.d(TAG, "fullScreenAd download onDownloadFinished");
                            if (downloadListener != null) {
                                downloadListener.onDownloadFinished(totalBytes, fileName, appName);
                            }
                        }

                        @Override
                        public void onInstalled(String fileName, String appName) {
                            LogUtils.d(TAG, "fullScreenAd download onInstalled");
                            if (downloadListener != null) {
                                downloadListener.onInstalled(fileName, appName);
                            }
                        }
                    });
                }
                if (adsListener != null) {
                    adsListener.onFullScreenVideoAdLoad();
                }
            }

            @Override
            public void onFullScreenVideoCached() {
                LogUtils.d(TAG, "fullScreenAd load onFullScreenVideoCached");
                if (mttFullVideoAd != null) {
                    mttFullVideoAd.showFullScreenVideoAd(activity, TTAdConstant.RitScenes.CUSTOMIZE_SCENES, "scenes_test");
                }
                if (adsListener != null) {
                    adsListener.onFullScreenVideoCached();
                }
                mttFullVideoAd = null;
            }
        });
    }
}
