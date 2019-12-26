package com.pine.audioplayer.manager;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.audioplayer.widget.view.ApSimpleAudioPlayerView;
import com.pine.tool.RootApplication;
import com.pine.tool.util.AppUtils;
import com.pine.tool.util.LogUtils;

import java.util.List;

import static android.content.Context.WINDOW_SERVICE;

public class ApFloatViewManager {
    private final String TAG = LogUtils.makeLogTag(this.getClass());
    private static ApFloatViewManager mInstance;
    private Context mAppContext;
    private WindowManager mWindowManager;

    private ApSimpleAudioPlayerView mFloatingSimpleAudioPlayerView;
    private ApMusicModel mModel = new ApMusicModel();
    private ApMusicSheet mRecentSheet;

    private ApFloatViewManager() {

    }

    public synchronized static ApFloatViewManager getInstance() {
        if (mInstance == null) {
            mInstance = new ApFloatViewManager();
            mInstance.init();
        }
        return mInstance;
    }

    private void init() {
        mAppContext = AppUtils.getApplicationContext();
        //获取WindowManager对象
        mWindowManager = (WindowManager) mAppContext.getSystemService(WINDOW_SERVICE);

        RootApplication.addAppLifecycleListener(mAppLifecycleListener);
    }

    public void clear() {
        RootApplication.removeAppLifecycleListener(mAppLifecycleListener);
    }

    RootApplication.IOnAppLifecycleListener mAppLifecycleListener = new RootApplication.IOnAppLifecycleListener() {
        @Override
        public void onAppForegroundChange(boolean isForeground) {
            if (!isForeground) {
                if (mFloatingSimpleAudioPlayerView != null) {
                    mFloatingSimpleAudioPlayerView.setVisibility(View.GONE);
                }
            }
        }
    };

    public void setupFloatSimpleAudioPlayerView() {
        mRecentSheet = mModel.getRecentSheet(mAppContext);

        mFloatingSimpleAudioPlayerView = new ApSimpleAudioPlayerView(mAppContext);
        //设置WindowManger布局参数以及相关属性
        final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        //初始化位置
        layoutParams.gravity = Gravity.BOTTOM;
        mWindowManager.addView(mFloatingSimpleAudioPlayerView, layoutParams);

        mFloatingSimpleAudioPlayerView.init(mAppContext, TAG, new ApSimpleAudioPlayerView.IOnMediaListChangeListener() {
            @Override
            public void onMediaRemove(ApSheetMusic music) {
                mModel.removeSheetMusic(mAppContext, mRecentSheet.getId(), music.getSongId());
            }

            @Override
            public void onMediaListClear(List<ApSheetMusic> musicList) {
                mModel.clearSheetMusic(mAppContext, mRecentSheet.getId());
            }
        });
        mFloatingSimpleAudioPlayerView.setOnListDialogListener(new ApSimpleAudioPlayerView.IOnListDialogListener() {
            @Override
            public void onListDialogStateChange(boolean isShown) {
                mFloatingSimpleAudioPlayerView.setVisibility(isShown ? View.GONE : View.VISIBLE);
            }
        });
        mFloatingSimpleAudioPlayerView.setVisibility(View.GONE);
    }

    public ApSimpleAudioPlayerView getFloatSimpleAudioPlayer() {
        return mFloatingSimpleAudioPlayerView;
    }

    public void clearFloatSimpleAudioPlayer() {
        if (mFloatingSimpleAudioPlayerView != null) {
            mWindowManager.removeView(mFloatingSimpleAudioPlayerView);
            mFloatingSimpleAudioPlayerView = null;
        }
    }

    public void showSimpleAudioPlayerView() {
        if (mFloatingSimpleAudioPlayerView != null) {
            mFloatingSimpleAudioPlayerView.setVisibility(View.VISIBLE);
        }
    }

    public void hideSimpleAudioPlayerView() {
        if (mFloatingSimpleAudioPlayerView != null) {
            mFloatingSimpleAudioPlayerView.setVisibility(View.GONE);
        }
    }
}
