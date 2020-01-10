package com.pine.audioplayer.vm;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.manager.ApAudioPlayerHelper;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.tool.architecture.mvvm.vm.ViewModel;
import com.pine.tool.binding.data.ParametricLiveData;
import com.pine.tool.service.TimerWorkHelper;

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

    public void startTimingWork(int minutes) {
        if (minutes > 0) {
            // 双保险：Timer在不同版本和不同手机上，手机熄屏时会使得Timer计时器的行为不一样，所以这里同时使用了两种方式来进行处理。
            // 有一种方式生效即可。
            // Timer方式
//            ApTimingCloseWorker worker = new ApTimingCloseWorker();
//            TimerWorkHelper.getInstance().schemeTimerWork(TAG, minutes * 60 * 100, worker);
            // MediaPlayer监听器方式
            ApAudioPlayerHelper.getInstance().schemeRelease(minutes * 60 * 100);
        } else if (minutes == 0) {
            ApAudioPlayerHelper.getInstance().schemeRelease(0);
        } else {
            ApAudioPlayerHelper.getInstance().cancelDelayRelease();
            TimerWorkHelper.getInstance().cancel(TAG);
        }
    }
}
