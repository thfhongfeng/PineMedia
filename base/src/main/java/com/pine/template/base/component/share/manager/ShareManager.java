package com.pine.template.base.component.share.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.pine.template.base.component.share.bean.ShareBean;
import com.pine.template.base.component.share.bean.UriListShareBean;
import com.pine.template.base.component.share.bean.UrlTextShareBean;
import com.pine.template.base.util.DialogUtils;
import com.pine.tool.util.LogUtils;

import java.util.ArrayList;

/**
 * Created by tanghongfeng on 2018/10/11
 */

public class ShareManager {
    private final static String TAG = LogUtils.makeLogTag(ShareManager.class);
    private static volatile ShareManager mInstance;
    private ShareBean mCurShareBean;

    private ShareManager() {
    }

    public static ShareManager getInstance() {
        if (mInstance == null) {
            synchronized (ShareManager.class) {
                if (mInstance == null) {
                    mInstance = new ShareManager();
                }
            }
        }
        return mInstance;
    }

    public static void init(Context context, String appNameId, String baseUrl,
                            TencentShareManager.TencentConfig tencentConfig,
                            SinaShareManager.SinaConfig sinaConfig) {
        getInstance().initTencent(tencentConfig, appNameId, baseUrl);
        getInstance().initSina(context, sinaConfig);
    }

    public void initTencent(TencentShareManager.TencentConfig tencentConfig, String appName, String baseUrl) {
        TencentShareManager.getInstance().init(tencentConfig, appName, baseUrl);
    }

    public void initSina(Context context, SinaShareManager.SinaConfig sinaConfig) {
        SinaShareManager.getInstance().init(context, sinaConfig);
    }

    public AlertDialog createShareDialog(final Activity context, @NonNull final ArrayList<ShareBean> shareBeanList) {
        return DialogUtils.createShareDialog(context, shareBeanList);
    }

    public <T extends ShareBean> void share(Activity activity, T t) {
        switch (t.getShareTarget()) {
            case ShareBean.SHARE_TARGET_QQ:
                ShareManager.getInstance().shareToQQ(activity, t);
                break;
            case ShareBean.SHARE_TARGET_WX:
                ShareManager.getInstance().shareToWX(activity, true, t);
                break;
            case ShareBean.SHARE_TARGET_WX_FRIEND_CIRCLE:
                ShareManager.getInstance().shareToWX(activity, false, t);
                break;
            case ShareBean.SHARE_TARGET_WEI_BO:
                ShareManager.getInstance().shareToWeiBo(activity, t);
                break;
        }
    }

    /**
     * 分享到QQ
     *
     * @param context
     * @param t
     */
    private <T extends ShareBean> void shareToQQ(Context context, T t) {
        mCurShareBean = t;
        if (t instanceof UrlTextShareBean) {
            UrlTextShareBean shareBean = (UrlTextShareBean) t;
            TencentShareManager.getInstance().shareWebPageToQQ(context, t.getShareTitle(),
                    shareBean.getShareDescription(), shareBean.getShareUrl());
        }
    }

    /**
     * 分享到微信朋友 or 朋友圈
     *
     * @param context
     * @param isTimeline true为朋友  false为朋友圈
     * @param t
     */
    private <T extends ShareBean> void shareToWX(Context context, boolean isTimeline, T t) {
        mCurShareBean = t;
        if (t instanceof UrlTextShareBean) {
            UrlTextShareBean shareBean = (UrlTextShareBean) t;
            TencentShareManager.getInstance().shareWebPageToWX(context, isTimeline,
                    shareBean.getShareUrl(), shareBean.getShareTitle(), shareBean.getShareDescription());
        }
    }

    /**
     * 分享到新浪微博
     *
     * @param activity
     * @param t
     */
    private <T extends ShareBean> void shareToWeiBo(Activity activity, T t) {
        mCurShareBean = t;
        if (t instanceof UrlTextShareBean && t.getShareContentType() == ShareBean.SHARE_CONTENT_TYPE_TEXT_URL) {
            UrlTextShareBean shareBean = (UrlTextShareBean) t;
            if (shareBean.getShareThumbId() != 0) {
                SinaShareManager.getInstance().shareWebPageToWeiBo(activity,
                        shareBean.getShareTitle(), shareBean.getShareText(), shareBean.getShareDescription(),
                        shareBean.getShareThumbId(), shareBean.getShareUrl());
            } else {
                SinaShareManager.getInstance().shareTextToWeiBo(activity,
                        shareBean.getShareTitle(), shareBean.getShareText(), shareBean.getShareUrl());
            }
        } else if (t instanceof UriListShareBean) {
            UriListShareBean shareBean = (UriListShareBean) t;
            switch (shareBean.getShareContentType()) {
                case ShareBean.SHARE_CONTENT_TYPE_IMAGE:
                    if (shareBean.getShareUriList() != null && shareBean.getShareUriList().size() > 0) {
                        SinaShareManager.getInstance().shareImageToWeiBo(activity,
                                shareBean.getShareTitle(), shareBean.getShareDescription(),
                                shareBean.getShareUriList().get(0));
                    }
                    break;
                case ShareBean.SHARE_CONTENT_TYPE_MULTI_IMAGE:
                    if (shareBean.getShareUriList() != null) {
                        SinaShareManager.getInstance().shareMultiImageToWeiBo(activity,
                                shareBean.getShareUriList());
                    }
                    break;
                case ShareBean.SHARE_CONTENT_TYPE_VIDEO:
                    if (shareBean.getShareUriList() != null && shareBean.getShareUriList().size() > 0) {
                        SinaShareManager.getInstance().shareVideoToWeiBo(activity,
                                shareBean.getShareUriList().get(0));
                    }
                    break;
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data,
                                 @NonNull final ShareCallback callback) {
        if (mCurShareBean == null) {
            return;
        }
        switch (mCurShareBean.getShareTarget()) {
            case ShareBean.SHARE_TARGET_QQ:
            case ShareBean.SHARE_TARGET_WX:
            case ShareBean.SHARE_TARGET_WX_FRIEND_CIRCLE:
                // TODO: 2022/9/27  
                break;
            case ShareBean.SHARE_TARGET_WEI_BO:
                // TODO: 2022/9/27  
                break;
        }
    }

    public interface ShareCallback<T extends ShareBean> {
        void onShareSuccess(T t);

        void onShareCancel(T t);

        void onShareFail(T t);
    }
}
