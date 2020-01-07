package com.pine.audioplayer.vm;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.tool.architecture.mvvm.vm.ViewModel;
import com.pine.tool.binding.data.ParametricLiveData;

public class ApMainVm extends ViewModel {
    private ApMusicModel mModel = new ApMusicModel();

    public ParametricLiveData<ApSheetMusic, Boolean> mPlayStateData = new ParametricLiveData<>();
    private ApMusicSheet mPlayListSheet;

    @Override
    public boolean parseIntentData(@NonNull Bundle bundle) {
        ApSheetMusic music = bundle.getParcelable("music");
        boolean playing = bundle.getBoolean("playing", false);
        mPlayListSheet = mModel.getPlayListSheet(getContext());
        if (mPlayListSheet == null || music == null) {
            return true;
        }
        setPlayedMusic(music, playing);
        return false;
    }

    public void setPlayedMusic(ApSheetMusic music, boolean playing) {
        mPlayStateData.setValue(music, playing);
    }

    public void refreshPlayMusic() {
        ApSheetMusic music = mModel.getSheetMusic(getContext(), mPlayListSheet.getId(), mPlayStateData.getValue().getSongId());
        setPlayedMusic(music, mPlayStateData.getCustomData());
    }
}
