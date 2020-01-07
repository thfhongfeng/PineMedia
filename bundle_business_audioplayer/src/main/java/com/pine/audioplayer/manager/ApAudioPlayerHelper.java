package com.pine.audioplayer.manager;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;

import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.audioplayer.ui.activity.ApMainActivity;
import com.pine.audioplayer.widget.AudioPlayerView;
import com.pine.audioplayer.widget.adapter.ApAudioControllerAdapter;
import com.pine.audioplayer.widget.plugin.ApOutRootLrcPlugin;
import com.pine.player.component.PineMediaWidget;
import com.pine.tool.RootApplication;
import com.pine.tool.util.AppUtils;
import com.pine.tool.util.LogUtils;

import java.util.List;

public class ApAudioPlayerHelper {
    private final String TAG = LogUtils.makeLogTag(this.getClass());
    private static ApAudioPlayerHelper mInstance;
    private Context mAppContext;
    private ApMusicModel mModel;
    private ApMusicSheet mRecentSheet, mPlayListSheet, mFavouriteSheet;

    private ApAudioControllerAdapter mControllerAdapter;
    private AudioPlayerView.IPlayerViewListener mPlayerViewListener =
            new AudioPlayerView.IPlayerViewListener() {
                @Override
                public void onPlayMusic(PineMediaWidget.IPineMediaPlayer player,
                                        ApSheetMusic oldPlayMusic, ApSheetMusic newPlayMusic) {
                    mModel.addSheetMusic(mAppContext, newPlayMusic, mRecentSheet.getId());
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
                public void onMusicListClear(List<ApSheetMusic> musicList) {
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

    public void destroy() {
        if (mControllerAdapter != null) {
            mControllerAdapter.destroy();
            mControllerAdapter = null;
        }
        mInstance = null;
    }

    public void attachPlayerViewFromGlobalController(@NonNull Context context, @NonNull AudioPlayerView playerView) {
        attachPlayerViewFromGlobalController(context, playerView, null, null);
    }

    public void attachPlayerViewFromGlobalController(@NonNull Context context, @NonNull AudioPlayerView playerView,
                                                     AudioPlayerView.IPlayerListener playerListener) {
        attachPlayerViewFromGlobalController(context, playerView, playerListener, null);
    }

    public void attachPlayerViewFromGlobalController(@NonNull Context context, @NonNull AudioPlayerView playerView,
                                                     ApOutRootLrcPlugin.ILyricUpdateListener lyricUpdateListener) {
        attachPlayerViewFromGlobalController(context, playerView, null, lyricUpdateListener);
    }

    public void attachPlayerViewFromGlobalController(@NonNull Context context, @NonNull AudioPlayerView playerView,
                                                     AudioPlayerView.IPlayerListener playerListener,
                                                     ApOutRootLrcPlugin.ILyricUpdateListener lyricUpdateListener) {
        playerView.init(context, TAG, mControllerAdapter, mPlayerViewListener, playerListener, lyricUpdateListener);
    }

    public void detachPlayerViewFromGlobalController(@NonNull AudioPlayerView playerView) {
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
