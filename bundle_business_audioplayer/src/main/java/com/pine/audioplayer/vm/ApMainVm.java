package com.pine.audioplayer.vm;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.pine.audioplayer.db.entity.ApMusic;
import com.pine.audioplayer.manager.ApAudioPlayerHelper;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.tool.architecture.mvp.model.IModelAsyncResponse;
import com.pine.tool.architecture.mvvm.vm.ViewModel;
import com.pine.tool.binding.data.ParametricLiveData;
import com.pine.tool.service.TimerWorkHelper;

public class ApMainVm extends ViewModel {
    private ApMusicModel mModel = new ApMusicModel();

    public ParametricLiveData<ApMusic, Boolean> mPlayStateData = new ParametricLiveData<>();
    private long mPlayListSheetId;

    @Override
    public boolean parseIntentData(Context activity, @NonNull Bundle bundle) {
        mPlayListSheetId = bundle.getLong("sheetId", -1);
        ApMusic music = (ApMusic) bundle.getSerializable("music");
        boolean playing = bundle.getBoolean("playing", false);
        if (mPlayListSheetId < 0 || music == null) {
            finishUi();
            return true;
        }
        setPlayedMusic(music, playing);
        return false;
    }

    public void setPlayedMusic(ApMusic music, boolean playing) {
        mPlayStateData.setValue(music, playing);
    }

    public void refreshPlayMusic(Context context) {
        mModel.getSheetMusic(context, mPlayListSheetId, mPlayStateData.getValue().getSongId(),
                new IModelAsyncResponse<ApMusic>() {
                    @Override
                    public void onResponse(ApMusic music) {
                        if (music != null) {
                            setPlayedMusic(music, mPlayStateData.getCustomData());
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
