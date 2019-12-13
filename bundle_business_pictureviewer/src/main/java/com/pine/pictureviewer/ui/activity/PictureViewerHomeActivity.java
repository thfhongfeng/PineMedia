package com.pine.pictureviewer.ui.activity;

import android.os.Bundle;

import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.pictureviewer.R;
import com.pine.pictureviewer.databinding.PictureViewerHomeActivityBinding;
import com.pine.pictureviewer.vm.PictureViewerHomeVm;

public class PictureViewerHomeActivity extends BaseMvvmNoActionBarActivity<PictureViewerHomeActivityBinding, PictureViewerHomeVm> {
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
}
