package com.pine.audioplayer.manager;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.audioplayer.widget.adapter.ApSimpleAudioControllerAdapter;
import com.pine.audioplayer.widget.view.ApSimpleAudioPlayerView;
import com.pine.tool.util.AppUtils;
import com.pine.tool.util.LogUtils;

import java.util.List;

public class ApSimpleAudioPlayerHelper {
    private final String TAG = LogUtils.makeLogTag(this.getClass());
    private static ApSimpleAudioPlayerHelper mInstance;
    private Context mAppContext;
    private ApMusicModel mModel;
    private ApMusicSheet mRecentSheet, mPlayListSheet;

    private ApSimpleAudioControllerAdapter mControllerAdapter;
    private ApSimpleAudioPlayerView.IOnMediaListChangeListener mMediaListChangeListener =
            new ApSimpleAudioPlayerView.IOnMediaListChangeListener() {
                @Override
                public void onMediaRemove(ApSheetMusic music) {
                    mModel.removeSheetMusic(mAppContext, mPlayListSheet.getId(), music.getSongId());
                }

                @Override
                public void onMediaListClear(List<ApSheetMusic> musicList) {
                    mModel.clearSheetMusic(mAppContext, mPlayListSheet.getId());
                }
            };

    private ApSimpleAudioPlayerHelper() {

    }

    public synchronized static ApSimpleAudioPlayerHelper getInstance() {
        if (mInstance == null) {
            mInstance = new ApSimpleAudioPlayerHelper();
            mInstance.init();
        }
        return mInstance;
    }

    private void init() {
        mAppContext = AppUtils.getApplicationContext();
        mModel = new ApMusicModel();
        mRecentSheet = mModel.getRecentSheet(mAppContext);
        mPlayListSheet = mModel.getPlayListSheet(mAppContext);

        mControllerAdapter = new ApSimpleAudioControllerAdapter(mAppContext);

        List<ApSheetMusic> oncePlayedMusicList = mModel.getSheetMusicList(mAppContext, mPlayListSheet.getId());
        if (oncePlayedMusicList != null && oncePlayedMusicList.size() > 0) {
            mControllerAdapter.addMusicList(oncePlayedMusicList, false);
        }
    }

    public void release() {
        if (mControllerAdapter != null) {
            mControllerAdapter.setMusicList(null, false);
            mControllerAdapter = null;
        }
        mAppContext = null;
        mRecentSheet = null;
        mPlayListSheet = null;
        mInstance = null;
    }

    public void attachGlobalController(@NonNull Context context, @NonNull ApSimpleAudioPlayerView playerView) {
        mControllerAdapter.setControllerView(playerView.getControllerView());
        playerView.init(context, TAG, mControllerAdapter, mMediaListChangeListener);
    }

    public void playMusic(@NonNull ApSimpleAudioPlayerView playerView,
                          @NonNull ApSheetMusic music, boolean startPlay) {
        if (music == null) {
            return;
        }
        playerView.playMusic(music, startPlay);
        mModel.addSheetMusic(mAppContext, music, mPlayListSheet.getId());
        mModel.addSheetMusic(mAppContext, music, mRecentSheet.getId());
    }

    public void playMusicList(@NonNull ApSimpleAudioPlayerView playerView,
                              @NonNull List<ApSheetMusic> musicList, boolean startPlay) {
        if (musicList == null && musicList.size() < 1) {
            return;
        }
        playerView.playMusicList(musicList, startPlay);
        mModel.addSheetMusicList(mAppContext, musicList, mPlayListSheet.getId());
        mModel.addSheetMusicList(mAppContext, musicList, mRecentSheet.getId());
    }
}
