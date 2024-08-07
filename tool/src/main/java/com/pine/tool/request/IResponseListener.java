package com.pine.tool.request;

import java.util.List;
import java.util.Map;

/**
 * Created by tanghongfeng on 2018/9/16
 */

public interface IResponseListener {
    interface ResponseListener {
    }

    interface OnResponseListener extends ResponseListener {
        void onStart(int what);

        void onSucceed(int what, Response response);

        void onFailed(int what, Response response);

        void onFinish(int what);
    }

    interface OnDownloadListener extends ResponseListener {
        void onDownloadError(int what, Exception exception);

        void onStart(int what, boolean isResume, long rangeSize, Map<String, List<String>> responseHeaders, long allCount);

        /**
         * @param what
         * @param progress  0-100
         * @param fileCount
         * @param speed
         */
        void onProgress(int what, int progress, long fileCount, long speed);

        void onFinish(int what, String filePath);

        void onCancel(int what);
    }

    interface OnUploadListener extends ResponseListener {
        void onStart(int what, UploadRequestBean.FileBean fileBean);

        void onCancel(int what, UploadRequestBean.FileBean fileBean);

        /**
         * @param what
         * @param fileBean
         * @param progress 0-100
         */
        void onProgress(int what, UploadRequestBean.FileBean fileBean, int progress);

        void onFinish(int what, UploadRequestBean.FileBean fileBean);

        void onError(int what, UploadRequestBean.FileBean fileBean, Exception exception);
    }
}
