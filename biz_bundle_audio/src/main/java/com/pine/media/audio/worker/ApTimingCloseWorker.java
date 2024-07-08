package com.pine.media.audio.worker;

import com.pine.media.audio.manager.ApAudioPlayerHelper;
import com.pine.tool.util.LogUtils;

public class ApTimingCloseWorker implements Runnable {
    private final String TAG = LogUtils.makeLogTag(this.getClass());

    @Override
    public void run() {
        if (ApAudioPlayerHelper.isPlayerAlive()) {
            LogUtils.d(TAG, "run ApTimingCloseWorker");
            ApAudioPlayerHelper.getInstance().schemeRelease(-1);
        }
    }
}
