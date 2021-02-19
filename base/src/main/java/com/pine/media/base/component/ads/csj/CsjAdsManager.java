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
import com.bytedance.sdk.openadsdk.TTSplashAd;
import com.bytedance.sdk.openadsdk.downloadnew.core.ExitInstallListener;
import com.pine.media.base.component.ads.IAdsDownloadListener;
import com.pine.media.base.component.ads.IAdsInteractionListener;
import com.pine.media.base.component.ads.IAdsManager;
import com.pine.media.base.component.ads.IAdsRequestStateListener;
import com.pine.media.base.component.ads.IExitInstallListener;
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
                             @NonNull final IAdsRequestStateListener requestStateListener,
                             @NonNull final IAdsInteractionListener interactionListener,
                             final IAdsDownloadListener downloadListener) {
        TTAdNative mTTAdNative = TTAdSdk.getAdManager().createAdNative(activity);
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
        mTTAdNative.loadSplashAd(adSlot, new TTAdNative.SplashAdListener() {
            @Override
            @MainThread
            public void onError(int code, String message) {
                LogUtils.d(TAG, "splashAd request onError：" + String.valueOf(message));
                if (requestStateListener != null) {
                    requestStateListener.onError(code, message);
                }
            }

            @Override
            @MainThread
            public void onTimeout() {
                LogUtils.d(TAG, "splashAd request onTimeout");
                if (requestStateListener != null) {
                    requestStateListener.onTimeout();
                }
            }

            @Override
            @MainThread
            public void onSplashAdLoad(TTSplashAd ad) {
                LogUtils.d(TAG, "splashAd request success");
                if (ad == null) {
                    return;
                }
                if (requestStateListener != null) {
                    requestStateListener.onAdsRequestSuccess(ad.getSplashView());
                }

                //设置SplashView的交互监听器
                ad.setSplashInteractionListener(new TTSplashAd.AdInteractionListener() {
                    @Override
                    public void onAdClicked(View view, int type) {
                        LogUtils.d(TAG, "splashAd interaction onAdClicked");
                        if (interactionListener != null) {
                            interactionListener.onAdClicked(view, type);
                        }
                    }

                    @Override
                    public void onAdShow(View view, int type) {
                        LogUtils.d(TAG, "splashAd interaction onAdShow");
                        if (interactionListener != null) {
                            interactionListener.onAdShow(view, type);
                        }
                    }

                    @Override
                    public void onAdSkip() {
                        LogUtils.d(TAG, "splashAd interaction onAdSkip");
                        if (interactionListener != null) {
                            interactionListener.onAdSkip();
                        }
                    }

                    @Override
                    public void onAdTimeOver() {
                        LogUtils.d(TAG, "splashAd interaction onAdTimeOver");
                        if (interactionListener != null) {
                            interactionListener.onAdTimeOver();
                        }
                    }
                });
                if (ad.getInteractionType() == TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
                    ad.setDownloadListener(new TTAppDownloadListener() {
                        boolean hasShow = false;

                        @Override
                        public void onIdle() {
                            LogUtils.d(TAG, "splashAd download onIdle");
                            if (downloadListener != null) {
                                downloadListener.onIdle();
                            }
                        }

                        @Override
                        public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                            if (!hasShow) {
                                LogUtils.d(TAG, "splashAd download onDownloadActive");
                                hasShow = true;
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
}
