package com.pine.audioplayer.ui.activity;

import android.os.Bundle;

import com.pine.audioplayer.R;
import com.pine.audioplayer.databinding.AudioPlayerHomeActivityBinding;
import com.pine.audioplayer.vm.AudioPlayerHomeVm;
import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;

public class AudioPlayerHomeActivity extends BaseMvvmNoActionBarActivity<AudioPlayerHomeActivityBinding, AudioPlayerHomeVm> {
    @Override
    public void observeInitLiveData(Bundle savedInstanceState) {

    }

    @Override
    public void observeSyncLiveData(int liveDataObjTag) {

    }

    @Override
    protected int getActivityLayoutResId() {
        return R.layout.ap_activity_home;
    }
}
