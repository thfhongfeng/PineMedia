package com.pine.videoplayer.ui.activity;

import android.os.Bundle;

import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.videoplayer.R;
import com.pine.videoplayer.databinding.VideoPlayerHomeActivityBinding;
import com.pine.videoplayer.vm.VideoPlayerHomeVm;

public class VideoPlayerHomeActivity extends BaseMvvmNoActionBarActivity<VideoPlayerHomeActivityBinding, VideoPlayerHomeVm> {
    @Override
    public void observeInitLiveData(Bundle savedInstanceState) {

    }

    @Override
    public void observeSyncLiveData(int liveDataObjTag) {

    }

    @Override
    protected int getActivityLayoutResId() {
        return R.layout.vp_activity_home;
    }
}
