package com.pine.media.pic.ui.activity;

import android.os.Bundle;

import com.pine.media.pic.R;
import com.pine.media.pic.databinding.PicHomeActivityBinding;
import com.pine.media.pic.vm.PicHomeVm;
import com.pine.template.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;

public class PicHomeActivity extends BaseMvvmNoActionBarActivity<PicHomeActivityBinding, PicHomeVm> {
    @Override
    public void observeInitLiveData(Bundle savedInstanceState) {

    }

    @Override
    public void observeSyncLiveData(int liveDataObjTag) {

    }

    @Override
    protected int getActivityLayoutResId() {
        return R.layout.pic_activity_home;
    }

    @Override
    protected void init(Bundle onCreateSavedInstanceState) {

    }
}
