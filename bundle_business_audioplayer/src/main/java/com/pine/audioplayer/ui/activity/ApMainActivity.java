package com.pine.audioplayer.ui.activity;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.lifecycle.Observer;

import com.pine.audioplayer.R;
import com.pine.audioplayer.adapter.ApAudioControllerAdapter;
import com.pine.audioplayer.databinding.ApMainActivityBinding;
import com.pine.audioplayer.vm.ApMainVm;
import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.widget.PineMediaController;

import java.util.List;

public class ApMainActivity extends BaseMvvmNoActionBarActivity<ApMainActivityBinding, ApMainVm> {
    private PineMediaController mMediaController;
    private PineMediaWidget.IPineMediaPlayer mMediaPlayer;
    private ApAudioControllerAdapter mControllerAdapter;

    @Override
    public void observeInitLiveData(Bundle savedInstanceState) {
        mViewModel.mMediaListData.observe(this, new Observer<List<PineMediaPlayerBean>>() {
            @Override
            public void onChanged(List<PineMediaPlayerBean> list) {
                mControllerAdapter.setMediaList(list);

            }
        });
        mViewModel.mPlayStateData.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer position) {
                mControllerAdapter.mediaSelect(position, mViewModel.mPlayStateData.getCustomData());
            }
        });
    }

    @Override
    public void observeSyncLiveData(int liveDataObjTag) {

    }

    @Override
    protected int getActivityLayoutResId() {
        return R.layout.ap_activity_main;
    }

    @Override
    protected void init(Bundle onCreateSavedInstanceState) {
        mMediaController = new PineMediaController(this);
        mBinding.playerView.init(TAG, mMediaController);
        mMediaPlayer = mBinding.playerView.getMediaPlayer();
        mMediaPlayer.setAutocephalyPlayMode(true);
        mControllerAdapter = new ApAudioControllerAdapter(this, mMediaPlayer,
                (ViewGroup) mBinding.controllerInclude);
        mMediaController.setMediaControllerAdapter(mControllerAdapter);
    }
}
