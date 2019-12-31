package com.pine.audioplayer.manager;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.audioplayer.ui.activity.ApMainActivity;
import com.pine.audioplayer.widget.IAudioPlayerView;
import com.pine.audioplayer.widget.adapter.ApAudioControllerAdapter;
import com.pine.player.component.PineMediaWidget;
import com.pine.tool.RootApplication;
import com.pine.tool.util.AppUtils;
import com.pine.tool.util.LogUtils;

import java.util.List;

import androidx.annotation.NonNull;

public class ApAudioPlayerHelper {
    private final String TAG = LogUtils.makeLogTag(this.getClass());
    private static ApAudioPlayerHelper mInstance;
    private Context mAppContext;
    private ApMusicModel mModel;
    private ApMusicSheet mRecentSheet, mPlayListSheet;

    private ApAudioControllerAdapter mControllerAdapter;
    private IAudioPlayerView.IPlayerViewListener mPlayerViewListener =
            new IAudioPlayerView.IPlayerViewListener() {
                @Override
                public void onPlayMusic(PineMediaWidget.IPineMediaPlayer player,
                                        ApSheetMusic oldPlayMusic, ApSheetMusic newPlayMusic) {
                    mModel.addSheetMusic(mAppContext, newPlayMusic, mRecentSheet.getId());
                }

                @Override
                public void onLyricDownloaded(ApSheetMusic music, String filePath) {
//                    mModel.updateMusicLyric(mAppContext, music, filePath);
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
                public void onViewClick(View view, String tag) {
                    boolean hasMedia = mControllerAdapter != null && mControllerAdapter.getMusicList().size() > 0;
                    if (hasMedia) {
                        Intent intent = new Intent(RootApplication.mCurResumedActivity, ApMainActivity.class);
                        intent.putExtra("music", mControllerAdapter.getCurMusic());
                        intent.putExtra("playing", mControllerAdapter.mPlayer != null && mControllerAdapter.mPlayer.isPlaying());
                        RootApplication.mCurResumedActivity.startActivity(intent);
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
        mAppContext = null;
        mRecentSheet = null;
        mPlayListSheet = null;
        mInstance = null;
    }

    public void attachGlobalController(@NonNull Context context, @NonNull IAudioPlayerView playerView) {
        playerView.init(context, TAG, mControllerAdapter, mPlayerViewListener, null);
    }

    public void attachGlobalController(@NonNull Context context, @NonNull IAudioPlayerView playerView,
                                       IAudioPlayerView.ILyricUpdateListener lyricUpdateListener) {
        playerView.init(context, TAG, mControllerAdapter, mPlayerViewListener, lyricUpdateListener);
    }

    public void playMusic(@NonNull IAudioPlayerView playerView,
                          @NonNull ApSheetMusic music, boolean startPlay) {
        if (music == null) {
            return;
        }
        playerView.playMusic(music, startPlay);
        mModel.addSheetMusic(mAppContext, music, mPlayListSheet.getId());
    }

    public void playMusicList(@NonNull IAudioPlayerView playerView,
                              @NonNull List<ApSheetMusic> musicList, boolean startPlay) {
        if (musicList == null && musicList.size() < 1) {
            return;
        }
        playerView.playMusicList(musicList, startPlay);
        mModel.addSheetMusicList(mAppContext, musicList, mPlayListSheet.getId());
    }
}
