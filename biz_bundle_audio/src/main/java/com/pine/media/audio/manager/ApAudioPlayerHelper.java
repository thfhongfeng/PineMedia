package com.pine.media.audio.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pine.media.audio.bean.ApSheetMusicDetail;
import com.pine.media.audio.db.entity.ApMusic;
import com.pine.media.audio.db.entity.ApSheet;
import com.pine.media.audio.model.ApMusicModel;
import com.pine.media.audio.ui.activity.ApMainActivity;
import com.pine.media.audio.widget.AudioPlayerView;
import com.pine.media.audio.widget.adapter.ApAudioControllerAdapter;
import com.pine.media.audio.widget.plugin.ApOutRootLrcPlugin;
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

public class ApAudioPlayerHelper {
    private final String TAG = LogUtils.makeLogTag(this.getClass());
    private static ApAudioPlayerHelper mInstance;
    private Context mAppContext;
    private ApMusicModel mModel;
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
                    if (mPlayerViewListenerMap.size() > 0) {
                        Iterator<Map.Entry<Integer, AudioPlayerView.IPlayerViewListener>> iterator = mPlayerViewListenerMap.entrySet().iterator();
                        while (iterator.hasNext()) {
                            iterator.next().getValue().onPlayStateChange(music, fromState, toState);
                        }
                    }
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

    private ApAudioPlayerHelper() {

    }

    public static boolean isPlayerAlive() {
        return mInstance != null;
    }

    public synchronized static ApAudioPlayerHelper getInstance() {
        if (mInstance == null) {
            mInstance = new ApAudioPlayerHelper();
            mInstance.init();
        }
        return mInstance;
    }

    private void init() {
        mAppContext = AppUtils.getApplicationContext();

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

    public void cancelDelayRelease() {
        if (mControllerAdapter != null) {
            mControllerAdapter.clearDelayRelease();
        }
    }

    /*
     * @param immediately  true:立即停止播放, false:播放完当前内容后停止
     */

    /**
     * 按计划停止播放器
     *
     * @param delay 小于0：立即停止播放；0：播放完当前内容后停止播放；大于0：delay时间后停止播放
     */
    public void schemeRelease(long delay) {
        if (mControllerAdapter != null) {
            mControllerAdapter.schemeRelease(delay);
        }
    }

    public void destroy() {
        if (mControllerAdapter != null) {
            mControllerAdapter.destroy();
            mControllerAdapter = null;
        }
        mInstance = null;
    }

    public void initPlayerView(@NonNull AudioPlayerView playerView) {
        playerView.init(TAG, mControllerAdapter);
    }

    public void attachPlayerView(@NonNull AudioPlayerView playerView) {
        attachPlayerView(playerView, null, null);
    }

    public void attachPlayerView(@NonNull AudioPlayerView playerView, AudioPlayerView.IPlayerViewListener playerListener) {
        attachPlayerView(playerView, playerListener, null);
    }

    public void attachPlayerView(@NonNull AudioPlayerView playerView, ApOutRootLrcPlugin.ILyricUpdateListener lyricUpdateListener) {
        attachPlayerView(playerView, null, lyricUpdateListener);
    }

    public void attachPlayerView(@NonNull AudioPlayerView playerView,
                                 AudioPlayerView.IPlayerViewListener playerListener,
                                 ApOutRootLrcPlugin.ILyricUpdateListener lyricUpdateListener) {
        if (playerListener != null) {
            mPlayerViewListenerMap.put(playerView.hashCode(), playerListener);
        }
        playerView.attachView(mPlayerViewListener, lyricUpdateListener);
    }

    public void detachPlayerView(@NonNull AudioPlayerView playerView) {
        if (mPlayerViewListenerMap.containsKey(playerView.hashCode())) {
            mPlayerViewListenerMap.remove(playerView.hashCode());
        }
        playerView.detachView();
    }

    public void playMusic(final @NonNull AudioPlayerView playerView,
                          @NonNull ApMusic music, final boolean startPlay) {
        if (music == null) {
            return;
        }
        mModel.addSheetMusic(mAppContext, music, mPlayListSheetId, new IModelAsyncResponse<ApMusic>() {
            @Override
            public void onResponse(ApMusic music) {
                if (music != null) {
                    playerView.playMusic(music, startPlay);
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

    public void playMusicList(final @NonNull AudioPlayerView playerView,
                              @NonNull List<ApMusic> musicList, final boolean startPlay) {
        if (musicList == null && musicList.size() < 1) {
            return;
        }
        mModel.addSheetMusicList(mAppContext, musicList, mPlayListSheetId,
                new IModelAsyncResponse<List<ApMusic>>() {
                    @Override
                    public void onResponse(List<ApMusic> list) {
                        if (list != null && list.size() > 0) {
                            playerView.playMusicList(list, startPlay);
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
}
