package com.pine.audioplayer.vm;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.tool.architecture.mvvm.vm.ViewModel;
import com.pine.tool.binding.data.ParametricLiveData;
import com.pine.tool.util.ColorUtils;

public class ApMainVm extends ViewModel {
    private ApMusicModel mModel = new ApMusicModel();

    public ParametricLiveData<ApSheetMusic, Boolean> mPlayStateData = new ParametricLiveData<>();
    public ParametricLiveData<Boolean, GradientDrawable> mIsLightThemeData = new ParametricLiveData();
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

    public void setMainThemeColor(int mainThemeColor) {
        int[] alphaColor = {0xff000000, 0x00000000};
        alphaColor[0] = alphaColor[0] | (mainThemeColor & 0x00ffffff);
        alphaColor[1] = alphaColor[1] | (mainThemeColor & 0x00ffffff);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, alphaColor);
        mIsLightThemeData.setValue(ColorUtils.isLightColor(mainThemeColor), bg);
    }
}
