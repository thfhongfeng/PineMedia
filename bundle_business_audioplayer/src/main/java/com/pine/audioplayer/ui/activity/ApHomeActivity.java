package com.pine.audioplayer.ui.activity;

import android.os.Bundle;

import androidx.lifecycle.Observer;

import com.pine.audioplayer.R;
import com.pine.audioplayer.databinding.ApHomeActivityBinding;
import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.vm.ApHomeVm;
import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;

import java.util.List;

public class ApHomeActivity extends BaseMvvmNoActionBarActivity<ApHomeActivityBinding, ApHomeVm> {

    @Override
    public void observeInitLiveData(Bundle savedInstanceState) {
        mViewModel.mMusicListData.observe(this, new Observer<List<ApSheetMusic>>() {
            @Override
            public void onChanged(List<ApSheetMusic> list) {
                mBinding.setAllMusicCount(list == null ? 0 : list.size());
            }
        });
        mViewModel.mFavouriteSheetData.observe(this, new Observer<ApMusicSheet>() {
            @Override
            public void onChanged(ApMusicSheet apMusicSheet) {
                mBinding.setFavouriteCount(apMusicSheet == null ? 0 : apMusicSheet.getCount());
            }
        });
        mViewModel.mRecentSheetData.observe(this, new Observer<ApMusicSheet>() {
            @Override
            public void onChanged(ApMusicSheet apMusicSheet) {
                mBinding.setRecentCount(apMusicSheet == null ? 0 : apMusicSheet.getCount());
            }
        });
        mViewModel.mCustomSheetListData.observe(this, new Observer<List<ApMusicSheet>>() {
            @Override
            public void onChanged(List<ApMusicSheet> list) {
                mBinding.setCustomSheetCount(list == null ? 0 : list.size());
            }
        });
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
