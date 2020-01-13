package com.pine.audioplayer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pine.audioplayer.R;
import com.pine.audioplayer.adapter.ApMusicSheetAdapter;
import com.pine.audioplayer.databinding.ApAddMusicActivityBinding;
import com.pine.audioplayer.db.entity.ApMusic;
import com.pine.audioplayer.db.entity.ApSheet;
import com.pine.audioplayer.vm.ApAddMusicVm;
import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmActionBarActivity;
import com.pine.base.recycle_view.adapter.BaseListAdapter;

import java.util.List;

public class ApAddMusicActivity extends BaseMvvmActionBarActivity<ApAddMusicActivityBinding, ApAddMusicVm> {
    private final int REQUEST_CODE_GO_MULTI_MUSIC_SELECT = 1;

    private ApMusicSheetAdapter mMusicSheetAdapter;

    @Override
    protected void setupActionBar(View actionbar, ImageView goBackIv, TextView titleTv) {
        titleTv.setText(R.string.ap_ml_add_music);
    }

    @Override
    public void observeInitLiveData(Bundle savedInstanceState) {
        mViewModel.mAllMusicSheetData.observe(this, new Observer<ApSheet>() {
            @Override
            public void onChanged(ApSheet apSheet) {
                mBinding.setAllMusicSheet(apSheet);
            }
        });
        mViewModel.mFavouriteSheetData.observe(this, new Observer<ApSheet>() {
            @Override
            public void onChanged(ApSheet apSheet) {
                mBinding.setFavouriteSheet(apSheet);
            }
        });
        mViewModel.mRecentSheetData.observe(this, new Observer<ApSheet>() {
            @Override
            public void onChanged(ApSheet apSheet) {
                mBinding.setRecentSheet(apSheet);
            }
        });
        mViewModel.mCustomSheetListData.observe(this, new Observer<List<ApSheet>>() {
            @Override
            public void onChanged(List<ApSheet> list) {
                list.remove(mViewModel.mSheetBeAddTo);
                mBinding.setCustomSheetCount(list == null ? 0 : list.size());
                mMusicSheetAdapter.setData(list);
            }
        });
    }

    @Override
    public void observeSyncLiveData(int liveDataObjTag) {

    }

    @Override
    protected int getActivityLayoutResId() {
        return R.layout.ap_activity_add_music;
    }

    @Override
    protected void init(Bundle onCreateSavedInstanceState) {
        mBinding.setPresenter(new Presenter());

        mMusicSheetAdapter = new ApMusicSheetAdapter();
        mMusicSheetAdapter.setOnItemClickListener(new BaseListAdapter.IOnItemClickListener<ApSheet>() {
            @Override
            public void onItemClick(View view, int position, String tag, ApSheet sheet) {
                goMultiMusicSelectActivity(sheet);
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mBinding.sheetRv.setLayoutManager(linearLayoutManager);
        mBinding.sheetRv.setAdapter(mMusicSheetAdapter);
    }

    @Override
    protected void onRealResume() {
        super.onRealResume();
        mViewModel.refreshData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GO_MULTI_MUSIC_SELECT) {
            if (resultCode == RESULT_OK) {
                List<ApMusic> selectList = (List<ApMusic>) data.getSerializableExtra("selectList");
                if (selectList != null && selectList.size() > 0) {
                    mViewModel.addMusicList(selectList);
                }
            }
        }
    }

    private void goMultiMusicSelectActivity(ApSheet sheet) {
        Intent intent = new Intent(this, ApMultiMusicSelectActivity.class);
        intent.putExtra("musicSheet", sheet);
        startActivityForResult(intent, REQUEST_CODE_GO_MULTI_MUSIC_SELECT);
    }

    public class Presenter {
        public void onGoAllMusicClick(View view, ApSheet sheet) {
            goMultiMusicSelectActivity(sheet);
        }

        public void onGoRecentMusicClick(View view, ApSheet sheet) {
            goMultiMusicSelectActivity(sheet);
        }

        public void onGoFavouriteMusicClick(View view, ApSheet sheet) {
            goMultiMusicSelectActivity(sheet);
        }
    }
}
