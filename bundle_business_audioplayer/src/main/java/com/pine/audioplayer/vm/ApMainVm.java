package com.pine.audioplayer.vm;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.pine.audioplayer.db.entity.ApMusic;
import com.pine.audioplayer.db.entity.ApSheet;
import com.pine.audioplayer.manager.ApAudioPlayerHelper;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.tool.architecture.mvvm.vm.ViewModel;
import com.pine.tool.binding.data.ParametricLiveData;
import com.pine.tool.service.TimerWorkHelper;

public class ApMainVm extends ViewModel {
    private ApMusicModel mModel = new ApMusicModel();

    public ParametricLiveData<ApMusic, Boolean> mPlayStateData = new ParametricLiveData<>();
    private ApSheet mPlayListSheet;

    @Override
    public boolean parseIntentData(@NonNull Bundle bundle) {
        ApMusic music = (ApMusic) bundle.getSerializable("music");
        boolean playing = bundle.getBoolean("playing", false);
        mPlayListSheet = mModel.getPlayListSheet(getContext());
        if (mPlayListSheet == null || music == null) {
            finishUi();
            return true;
        }
        setPlayedMusic(music, playing);
        return false;
    }

    public void setPlayedMusic(ApMusic music, boolean playing) {
        mPlayStateData.setValue(music, playing);
    }

    public void refreshPlayMusic() {
        ApMusic music = mModel.getSheetMusic(getContext(), mPlayListSheet.getId(), mPlayStateData.getValue().getSongId());
        setPlayedMusic(music, mPlayStateData.getCustomData());
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
