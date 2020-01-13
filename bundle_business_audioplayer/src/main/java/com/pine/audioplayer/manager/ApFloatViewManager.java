package com.pine.audioplayer.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.pine.audioplayer.db.entity.ApMusic;
import com.pine.audioplayer.db.entity.ApSheet;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.audioplayer.ui.activity.ApMainActivity;
import com.pine.audioplayer.widget.AudioPlayerView;
import com.pine.audioplayer.widget.adapter.ApAudioControllerAdapter;
import com.pine.audioplayer.widget.view.ApSimpleAudioPlayerView;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.component.PinePlayState;
import com.pine.tool.RootApplication;
import com.pine.tool.util.AppUtils;
import com.pine.tool.util.LogUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.content.Context.WINDOW_SERVICE;

public class ApFloatViewManager {
    private final String TAG = LogUtils.makeLogTag(this.getClass());
    private static ApFloatViewManager mInstance;
    private Context mAppContext;
    private WindowManager mWindowManager;

    private ApSimpleAudioPlayerView mFloatingSimpleAudioPlayerView;
    private ApMusicModel mModel = new ApMusicModel();
    private ApSheet mRecentSheet, mPlayListSheet;

    private ApAudioControllerAdapter mControllerAdapter;
    private HashMap<Integer, AudioPlayerView.IPlayerViewListener> mPlayerViewListenerMap = new HashMap<>();
    private AudioPlayerView.PlayerViewListener mPlayerViewListener =
            new AudioPlayerView.PlayerViewListener() {
                @Override
                public void onPlayMusic(PineMediaWidget.IPineMediaPlayer mPlayer, @Nullable ApMusic newMusic) {
                    if (newMusic != null) {
                        mModel.addSheetMusic(mAppContext, newMusic, mRecentSheet.getId());
                    }
                    if (mPlayerViewListenerMap.size() > 0) {
                        Iterator<Map.Entry<Integer, AudioPlayerView.IPlayerViewListener>> iterator = mPlayerViewListenerMap.entrySet().iterator();
                        while (iterator.hasNext()) {
                            iterator.next().getValue().onPlayMusic(mPlayer, newMusic);
                        }
                    }
                }

                @Override
                public void onPlayStateChange(ApMusic music, PinePlayState fromState, PinePlayState toState) {

                }

                @Override
                public void onAlbumArtChange(String mediaCode, ApMusic music, Bitmap smallBitmap,
                                             Bitmap bigBitmap, int mainColor) {
                    if (mPlayerViewListenerMap.size() > 0) {
                        Iterator<Map.Entry<Integer, AudioPlayerView.IPlayerViewListener>> iterator = mPlayerViewListenerMap.entrySet().iterator();
                        while (iterator.hasNext()) {
                            iterator.next().getValue().onAlbumArtChange(mediaCode, music,
                                    smallBitmap, bigBitmap, mainColor);
                        }
                    }
                }

                @Override
                public void onLyricDownloaded(String mediaCode, ApMusic music, String filePath, String charset) {
                    mModel.updateMusicLyric(mAppContext, music, filePath, charset);
                }

                @Override
                public void onMusicRemove(ApMusic music) {
                    mModel.removeSheetMusic(mAppContext, mPlayListSheet.getId(), music.getSongId());
                }

                @Override
                public void onMusicListClear() {
                    mModel.clearSheetMusic(mAppContext, mPlayListSheet.getId());
                }

                @Override
                public void onViewClick(View view, ApMusic music, String tag) {
                    switch (tag) {
                        case "content":
                            boolean hasMedia = mControllerAdapter != null && mControllerAdapter.getMusicList().size() > 0;
                            if (hasMedia) {
                                Intent intent = new Intent(RootApplication.mCurResumedActivity, ApMainActivity.class);
                                intent.putExtra("music", mControllerAdapter.getCurMusic());
                                intent.putExtra("playing", mControllerAdapter.mPlayer != null && mControllerAdapter.mPlayer.isPlaying());
                                RootApplication.mCurResumedActivity.startActivity(intent);
                            }
                            break;
                        case "favourite":
                            music.setFavourite(view.isSelected());
                            mModel.updateMusicFavourite(mAppContext, music, view.isSelected());
                            break;
                    }
                }
            };

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

        mModel = new ApMusicModel();
        mRecentSheet = mModel.getRecentSheet(mAppContext);
        mPlayListSheet = mModel.getPlayListSheet(mAppContext);

        mControllerAdapter = new ApAudioControllerAdapter(mAppContext);

        List<ApMusic> oncePlayedMusicList = mModel.getSheetMusicList(mAppContext, mPlayListSheet.getId());
        if (oncePlayedMusicList != null && oncePlayedMusicList.size() > 0) {
            mControllerAdapter.addMusicList(oncePlayedMusicList, false);
        }

        RootApplication.addAppLifecycleListener(mAppLifecycleListener);
    }

    public void clear() {
        RootApplication.removeAppLifecycleListener(mAppLifecycleListener);
        clearFloatSimpleAudioPlayer();
        mInstance = null;
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
        clearFloatSimpleAudioPlayer();
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

        mFloatingSimpleAudioPlayerView.setOnListDialogListener(new AudioPlayerView.IOnListDialogListener() {
            @Override
            public void onListDialogStateChange(boolean isShown) {
                mFloatingSimpleAudioPlayerView.setVisibility(isShown ? View.GONE : View.VISIBLE);
            }
        });
        mFloatingSimpleAudioPlayerView.init(TAG, mControllerAdapter);
        mFloatingSimpleAudioPlayerView.attachView(mPlayerViewListener, null);
        mFloatingSimpleAudioPlayerView.setVisibility(View.GONE);
    }

    public ApSimpleAudioPlayerView getFloatSimpleAudioPlayer() {
        return mFloatingSimpleAudioPlayerView;
    }

    public void clearFloatSimpleAudioPlayer() {
        if (mFloatingSimpleAudioPlayerView != null) {
            mFloatingSimpleAudioPlayerView.detachView();
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
