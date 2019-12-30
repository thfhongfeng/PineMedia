package com.pine.audioplayer.vm;

import android.os.Bundle;

import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.tool.architecture.mvvm.vm.ViewModel;
import com.pine.tool.binding.data.ParametricLiveData;

import androidx.annotation.NonNull;

public class ApMainVm extends ViewModel {

    public ParametricLiveData<ApSheetMusic, Boolean> mPlayStateData = new ParametricLiveData<>();

    @Override
    public boolean parseIntentData(@NonNull Bundle bundle) {
        ApSheetMusic music = bundle.getParcelable("music");
        boolean playing = bundle.getBoolean("playing", false);
        if (music == null) {
            return true;
        }
        mPlayStateData.setValue(music, playing);
        return false;
    }
}
