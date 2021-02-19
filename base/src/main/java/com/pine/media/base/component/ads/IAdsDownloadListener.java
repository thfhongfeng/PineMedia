package com.pine.media.base.component.ads;

public interface IAdsDownloadListener {
    void onIdle();

    void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName);

    void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName);

    void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName);

    void onDownloadFinished(long totalBytes, String fileName, String appName);

    void onInstalled(String fileName, String appName);
}
