package com.pine.audioplayer.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.audioplayer.ui.activity.ApMainActivity;
import com.pine.audioplayer.widget.AudioPlayerView;
import com.pine.audioplayer.widget.adapter.ApAudioControllerAdapter;
import com.pine.audioplayer.widget.plugin.ApOutRootLrcPlugin;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.component.PinePlayState;
import com.pine.tool.RootApplication;
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
    private ApMusicSheet mRecentSheet, mPlayListSheet, mFavouriteSheet;

    private ApAudioControllerAdapter mControllerAdapter;
    private HashMap<Integer, AudioPlayerView.IPlayerViewListener> mPlayerViewListenerMap = new HashMap<>();
    private AudioPlayerView.PlayerViewListener mPlayerViewListener =
            new AudioPlayerView.PlayerViewListener() {
                @Override
                public void onPlayMusic(PineMediaWidget.IPineMediaPlayer mPlayer, @Nullable ApSheetMusic newMusic) {
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
                public void onPlayStateChange(ApSheetMusic music, PinePlayState fromState, PinePlayState toState) {
                    if (mPlayerViewListenerMap.size() > 0) {
                        Iterator<Map.Entry<Integer, AudioPlayerView.IPlayerViewListener>> iterator = mPlayerViewListenerMap.entrySet().iterator();
                        while (iterator.hasNext()) {
                            iterator.next().getValue().onPlayStateChange(music, fromState, toState);
                        }
                    }
                }

                @Override
                public void onAlbumArtChange(String mediaCode, ApSheetMusic music, Bitmap smallBitmap,
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
                public void onLyricDownloaded(String mediaCode, ApSheetMusic music, String filePath, String charset) {
                    mModel.updateMusicLyric(mAppContext, music, filePath, charset);
                }

                @Override
                public void onMusicRemove(ApSheetMusic music) {
                    mModel.removeSheetMusic(mAppContext, mPlayListSheet.getId(), music.getSongId());
                }

                @Override
                public void onMusicListClear() {
                    mModel.clearSheetMusic(mAppContext, mPlayListSheet.getId());
                }

                @Override
                public void onViewClick(View view, ApSheetMusic music, String tag) {
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
                            if (view.isSelected()) {
                                mModel.addSheetMusic(mAppContext, music, mFavouriteSheet.getId());
                            } else {
                                mModel.removeSheetMusic(mAppContext, mFavouriteSheet.getId(), music.getSongId());
                            }
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
        mModel = new ApMusicModel();
        mRecentSheet = mModel.getRecentSheet(mAppContext);
        mPlayListSheet = mModel.getPlayListSheet(mAppContext);
        mFavouriteSheet = mModel.getFavouriteSheet(mAppContext);

        mControllerAdapter = new ApAudioControllerAdapter(mAppContext);

        List<ApSheetMusic> oncePlayedMusicList = mModel.getSheetMusicList(mAppContext, mPlayListSheet.getId());
        if (oncePlayedMusicList != null && oncePlayedMusicList.size() > 0) {
            mControllerAdapter.addMusicList(oncePlayedMusicList, false);
        }
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

    public void playMusic(@NonNull AudioPlayerView playerView,
                          @NonNull ApSheetMusic music, boolean startPlay) {
        if (music == null) {
            return;
        }
        ApSheetMusic playMusic = mModel.addSheetMusic(mAppContext, music, mPlayListSheet.getId());
        if (playMusic != null) {
            playerView.playMusic(playMusic, startPlay);
        }
    }

    public void playMusicList(@NonNull AudioPlayerView playerView,
                              @NonNull List<ApSheetMusic> musicList, boolean startPlay) {
        if (musicList == null && musicList.size() < 1) {
            return;
        }
        List<ApSheetMusic> playMusicList = mModel.addSheetMusicList(mAppContext, musicList, mPlayListSheet.getId());
        if (playMusicList != null && playMusicList.size() > 0) {
            playerView.playMusicList(playMusicList, startPlay);
        }
    }
}
