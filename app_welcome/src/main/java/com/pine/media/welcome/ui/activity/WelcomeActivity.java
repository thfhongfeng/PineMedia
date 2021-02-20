package com.pine.media.welcome.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.pine.media.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.media.base.component.ads.AdsSdkManager;
import com.pine.media.base.component.ads.ISplashAdsInteractionListener;
import com.pine.media.base.component.ads.ISplashAdsListener;
import com.pine.media.base.router.command.RouterMainCommand;
import com.pine.media.config.ConfigKey;
import com.pine.media.config.switcher.ConfigSwitcherServer;
import com.pine.media.welcome.R;
import com.pine.media.welcome.databinding.WelcomeActivityBinding;
import com.pine.media.welcome.remote.WelcomeRouterClient;
import com.pine.media.welcome.vm.WelcomeVm;
import com.pine.tool.router.IRouterCallback;
import com.pine.tool.util.LogUtils;

public class WelcomeActivity extends BaseMvvmNoActionBarActivity<WelcomeActivityBinding, WelcomeVm> {
    private final static int WELCOME_STAY_MIN_TIME = 1000;
    private long mStartTimeMillis;

    @Override
    public void observeInitLiveData(Bundle savedInstanceState) {

    }

    @Override
    protected int getActivityLayoutResId() {
        return R.layout.wel_activity_welcome;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mStartTimeMillis = System.currentTimeMillis();
        if (ConfigSwitcherServer.getInstance().isEnable(ConfigKey.CONFIG_ADS_ALLOW_KEY)) {
            loadAds();
        } else {
            goMainHomeActivity();
        }
    }

    private void goMainHomeActivity() {
        long delay = WELCOME_STAY_MIN_TIME - (System.currentTimeMillis() - mStartTimeMillis);
        delay = delay > 0 ? delay : 0;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                WelcomeRouterClient.goMainHomeActivity(WelcomeActivity.this, null, new IRouterCallback() {
                    @Override
                    public void onSuccess(Bundle responseBundle) {
                        LogUtils.d(TAG, "onSuccess " + RouterMainCommand.goMainHomeActivity);
                        finish();
                        return;
                    }

                    @Override
                    public boolean onFail(int failCode, String errorInfo) {
                        return false;
                    }
                });
            }
        }, delay);
    }

    private void loadAds() {
        LogUtils.d(TAG, "loadSplashAds");
        AdsSdkManager.loadSplashAd(this, mBinding.welAdsContainer, false, new ISplashAdsListener() {
            @Override
            public void onError(int code, String message) {
                goMainHomeActivity();
            }

            @Override
            public void onTimeout() {
                goMainHomeActivity();
            }

            @Override
            public void onAdLoad(View adsView) {
                if (adsView != null && mBinding.welAdsContainer != null && !WelcomeActivity.this.isFinishing()) {
                    mBinding.welAdsContainer.removeAllViews();
                    //把SplashView 添加到ViewGroup中,注意开屏广告view：width >=70%屏幕宽；height >=50%屏幕高
                    mBinding.welAdsContainer.addView(adsView);
                } else {
                    goMainHomeActivity();
                }
            }
        }, new ISplashAdsInteractionListener() {

            @Override
            public void onAdClicked(View view, int type) {

            }

            @Override
            public void onAdShow(View view, int type) {

            }

            @Override
            public void onAdSkip() {
                goMainHomeActivity();
            }

            @Override
            public void onAdTimeOver() {
                goMainHomeActivity();
            }
        }, null);
    }

    @Override
    public void observeSyncLiveData(int liveDataObjTag) {

    }
}
