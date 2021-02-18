package com.pine.media.videoplayer.ui.activity;

import android.os.Bundle;

import com.pine.media.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.media.videoplayer.R;
import com.pine.media.videoplayer.databinding.PvHomeActivityBinding;
import com.pine.media.videoplayer.vm.PvHomeVm;

public class PvHomeActivity extends BaseMvvmNoActionBarActivity<PvHomeActivityBinding, PvHomeVm> {
    @Override
    public void observeInitLiveData(Bundle savedInstanceState) {

    }

    @Override
    public void observeSyncLiveData(int liveDataObjTag) {

    }

    @Override
    protected int getActivityLayoutResId() {
        return R.layout.pv_activity_home;
    }

    @Override
    protected void init(Bundle onCreateSavedInstanceState) {

    }
}
