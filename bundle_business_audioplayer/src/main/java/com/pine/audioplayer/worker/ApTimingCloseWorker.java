package com.pine.audioplayer.worker;

import com.pine.audioplayer.manager.ApAudioPlayerHelper;

public class ApTimingCloseWorker implements Runnable {
    @Override
    public void run() {
        if (ApAudioPlayerHelper.isPlayerAlive()) {
            ApAudioPlayerHelper.getInstance().releasePlayer();
        }
    }
}
