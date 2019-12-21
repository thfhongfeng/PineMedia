package com.pine.audioplayer.ui.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.pine.audioplayer.R;
import com.pine.audioplayer.databinding.ApMultiMusicSelectActivityBinding;
import com.pine.audioplayer.vm.ApMultiMusicSelectVm;
import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmActionBarActivity;

public class ApMultiMusicSelectActivity extends BaseMvvmActionBarActivity<ApMultiMusicSelectActivityBinding, ApMultiMusicSelectVm> {

    @Override
    protected void setupActionBar(ImageView goBackIv, TextView titleTv) {

    }

    @Override
    public void observeInitLiveData(Bundle savedInstanceState) {

    }

    @Override
    public void observeSyncLiveData(int liveDataObjTag) {

    }

    @Override
    protected int getActivityLayoutResId() {
        return R.layout.ap_activity_multi_music_select;
    }

    @Override
    protected void init(Bundle onCreateSavedInstanceState) {

    }
}
