package com.pine.media.audio.manager;

import static android.content.Context.WINDOW_SERVICE;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.pine.media.audio.bean.ApSheetMusicDetail;
import com.pine.media.audio.db.entity.ApMusic;
import com.pine.media.audio.db.entity.ApSheet;
import com.pine.media.audio.model.ApMusicModel;
import com.pine.media.audio.ui.activity.ApMainActivity;
import com.pine.media.audio.widget.AudioPlayerView;
import com.pine.media.audio.widget.adapter.ApAudioControllerAdapter;
import com.pine.media.audio.widget.view.ApSimpleAudioPlayerView;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.component.PinePlayState;
import com.pine.tool.RootApplication;
import com.pine.tool.architecture.mvp.model.IModelAsyncResponse;
import com.pine.tool.util.AppUtils;
import com.pine.tool.util.LogUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ApFloatViewManager {
    private final String TAG = LogUtils.makeLogTag(this.getClass());
    private static ApFloatViewManager mInstance;
    private Context mAppContext;
    private WindowManager mWindowManager;

    private ApSimpleAudioPlayerView mFloatingSimpleAudioPlayerView;
    private ApMusicModel mModel = new ApMusicModel();
    private long mRecentSheetId, mPlayListSheetId;

    private ApAudioControllerAdapter mControllerAdapter;
    private HashMap<Integer, AudioPlayerView.IPlayerViewListener> mPlayerViewListenerMap = new HashMap<>();
    private AudioPlayerView.PlayerViewListener mPlayerViewListener =
            new AudioPlayerView.PlayerViewListener() {
                @Override
                public void onPlayMusic(PineMediaWidget.IPineMediaPlayer mPlayer, @Nullable ApMusic newMusic) {
                    if (newMusic != null) {
                        mModel.addSheetMusic(mAppContext, newMusic, mRecentSheetId, new IModelAsyncResponse<ApMusic>() {
                            @Override
                            public void onResponse(ApMusic music) {

                            }

                            @Override
                            public boolean onFail(Exception e) {
                                return false;
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
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
                    mModel.updateMusicLyric(mAppContext, music, filePath, charset, new IModelAsyncResponse<Boolean>() {
                        @Override
                        public void onResponse(Boolean success) {

                        }

                        @Override
                        public boolean onFail(Exception e) {
                            return false;
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                }

                @Override
                public void onMusicRemove(ApMusic music) {
                    mModel.removeSheetMusic(mAppContext, mPlayListSheetId, music.getSongId(), new IModelAsyncResponse<Boolean>() {
                        @Override
                        public void onResponse(Boolean success) {

                        }

                        @Override
                        public boolean onFail(Exception e) {
                            return false;
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                }

                @Override
                public void onMusicListClear() {
                    mModel.clearSheetMusic(mAppContext, mPlayListSheetId, new IModelAsyncResponse<Boolean>() {
                        @Override
                        public void onResponse(Boolean success) {

                        }

                        @Override
                        public boolean onFail(Exception e) {
                            return false;
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                }

                @Override
                public void onViewClick(final View view, final ApMusic music, String tag) {
                    switch (tag) {
                        case "content":
                            boolean hasMedia = mControllerAdapter != null && mControllerAdapter.getMusicList().size() > 0;
                            if (hasMedia) {
                                Intent intent = new Intent(RootApplication.mCurResumedActivity, ApMainActivity.class);
                                intent.putExtra("sheetId", mPlayListSheetId);
                                intent.putExtra("music", mControllerAdapter.getCurMusic());
                                intent.putExtra("playing", mControllerAdapter.mPlayer != null && mControllerAdapter.mPlayer.isPlaying());
                                RootApplication.mCurResumedActivity.startActivity(intent);
                            }
                            break;
                        case "favourite":
                            final boolean isSelected = view.isSelected();
                            mModel.updateMusicFavourite(mAppContext, music, view.isSelected(), new IModelAsyncResponse<Boolean>() {
                                @Override
                                public void onResponse(Boolean success) {
                                    music.setFavourite(isSelected);
                                    view.setSelected(isSelected);
                                }

                                @Override
                                public boolean onFail(Exception e) {
                                    view.setSelected(!isSelected);
                                    return false;
                                }

                                @Override
                                public void onCancel() {
                                    view.setSelected(!isSelected);
                                }
                            });
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

        mControllerAdapter = new ApAudioControllerAdapter(mAppContext);
        mModel = new ApMusicModel();

        mModel.getRecentSheet(mAppContext, new IModelAsyncResponse<ApSheet>() {
            @Override
            public void onResponse(ApSheet sheet) {
                mRecentSheetId = sheet.getId();
            }

            @Override
            public boolean onFail(Exception e) {
                return false;
            }

            @Override
            public void onCancel() {

            }
        });
        mModel.getPlayListDetail(mAppContext, new IModelAsyncResponse<ApSheetMusicDetail>() {
            @Override
            public void onResponse(ApSheetMusicDetail sheetDetail) {
                if (sheetDetail != null) {
                    if (sheetDetail.getSheet() != null) {
                        mPlayListSheetId = sheetDetail.getSheet().getId();
                    }
                    List<ApMusic> oncePlayedMusicList = sheetDetail.getMusicList();
                    if (oncePlayedMusicList != null && oncePlayedMusicList.size() > 0) {
                        mControllerAdapter.addMusicList(oncePlayedMusicList, false);
                    }
                }
            }

            @Override
            public boolean onFail(Exception e) {
                return false;
            }

            @Override
            public void onCancel() {

            }
        });
    }

    public void clear() {
        RootApplication.removeAppForegroundChangeListener(mAppLifecycleListener);
        clearFloatSimpleAudioPlayer();
        mInstance = null;
    }

    RootApplication.IOnAppForegroundChangeListener mAppLifecycleListener = new RootApplication.IOnAppForegroundChangeListener() {
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
