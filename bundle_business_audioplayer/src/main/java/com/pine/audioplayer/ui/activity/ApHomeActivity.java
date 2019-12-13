package com.pine.audioplayer.ui.activity;

import android.os.Bundle;

import com.pine.audioplayer.R;
import com.pine.audioplayer.databinding.ApHomeActivityBinding;
import com.pine.audioplayer.vm.ApHomeVm;
import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;

public class ApHomeActivity extends BaseMvvmNoActionBarActivity<ApHomeActivityBinding, ApHomeVm> {

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

    @Override
    protected void init(Bundle onCreateSavedInstanceState) {

    }
}
