package com.pine.media.base.component.ads;

import android.view.View;

public interface IAdsInteractionListener {
    //点击回调
    void onAdClicked(View view, int type);

    //展示回调
    void onAdShow(View view, int type);

    //跳过回调
    void onAdSkip();

    //超时倒计时结束
    void onAdTimeOver();
}
