package com.pine.pictureviewer.ui.activity;

import android.os.Bundle;

import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.pictureviewer.R;
import com.pine.pictureviewer.databinding.PvHomeActivityBinding;
import com.pine.pictureviewer.vm.PvHomeVm;

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
