package com.pine.audioplayer.vm;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.pine.audioplayer.db.entity.ApMusic;
import com.pine.audioplayer.manager.ApAudioPlayerHelper;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.audioplayer.util.ApLocalMusicUtils;
import com.pine.tool.architecture.mvp.model.IModelAsyncResponse;
import com.pine.tool.architecture.mvvm.vm.ViewModel;
import com.pine.tool.binding.data.ParametricLiveData;
import com.pine.tool.service.TimerWorkHelper;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

public class ApMainVm extends ViewModel {
    private ApMusicModel mModel = new ApMusicModel();

    public ParametricLiveData<ApMusic, Boolean> mPlayMusicStateData = new ParametricLiveData<>();
    public ParametricLiveData<ApMusic, Boolean> mInitMusicStateData = new ParametricLiveData<>();
    public MutableLiveData<Long> mPlayListSheetIdData = new MutableLiveData<>();
    public boolean mInitPlayerMode = false;

    private ApMusic mPlayMusic = null;

    @Override
    public boolean parseIntentData(Context activity, @NonNull Bundle bundle) {
        long sheetId = bundle.getLong("sheetId", -1);
        mPlayMusic = (ApMusic) bundle.getSerializable("music");
        boolean playing = bundle.getBoolean("playing", false);
        if (sheetId < 0 || mPlayMusic == null) {
            mInitPlayerMode = true;
            Uri musicPathUri = bundle.getParcelable("data");
            if (musicPathUri == null) {
                finishUi();
                return true;
            }
            mPlayMusic = ApLocalMusicUtils.getMusicFromPath(activity, musicPathUri);
        } else {
            setPlayedMusic(mPlayMusic, playing);
        }
        mPlayListSheetIdData.setValue(sheetId);
        return false;
    }

    @Override
    public void afterViewInit(Context activity) {
        super.afterViewInit(activity);
        if (mInitPlayerMode) {
            mInitMusicStateData.setValue(mPlayMusic, true);
        }
    }

    public void setPlayedMusic(ApMusic music, boolean playing) {
        mPlayMusicStateData.setValue(music, playing);
    }

    public void refreshPlayMusic(Context context) {
        if (mInitPlayerMode) {
            setPlayedMusic(mInitMusicStateData.getValue(), mInitMusicStateData.getCustomData());
            return;
        }
        mModel.getSheetMusic(context, mPlayListSheetIdData.getValue(), mPlayMusicStateData.getValue().getSongId(),
                new IModelAsyncResponse<ApMusic>() {
                    @Override
                    public void onResponse(ApMusic music) {
                        if (music != null) {
                            setPlayedMusic(music, mPlayMusicStateData.getCustomData());
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

    public void startTimingWork(int minutes) {
        if (minutes > 0) {
            ApAudioPlayerHelper.getInstance().schemeRelease(minutes * 60 * 1000);
        } else if (minutes == 0) {
            ApAudioPlayerHelper.getInstance().schemeRelease(0);
        } else {
            ApAudioPlayerHelper.getInstance().cancelDelayRelease();
            TimerWorkHelper.getInstance().cancel(TAG);
        }
    }
}
