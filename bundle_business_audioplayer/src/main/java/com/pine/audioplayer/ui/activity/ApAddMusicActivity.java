package com.pine.audioplayer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pine.audioplayer.R;
import com.pine.audioplayer.adapter.ApMusicSheetAdapter;
import com.pine.audioplayer.databinding.ApAddMusicActivityBinding;
import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.vm.ApSheetListVm;
import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmActionBarActivity;
import com.pine.base.recycle_view.adapter.BaseListAdapter;

import java.util.List;

import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ApAddMusicActivity extends BaseMvvmActionBarActivity<ApAddMusicActivityBinding, ApSheetListVm> {
    private final int REQUEST_CODE_GO_MULTI_MUSIC_SELECT = 1;

    private ApMusicSheetAdapter mMusicSheetAdapter;

    @Override
    protected void setupActionBar(ImageView goBackIv, TextView titleTv) {
        titleTv.setText(R.string.ap_ml_add_music);
    }

    @Override
    public void observeInitLiveData(Bundle savedInstanceState) {
        mViewModel.mAllMusicSheetData.observe(this, new Observer<ApMusicSheet>() {
            @Override
            public void onChanged(ApMusicSheet apMusicSheet) {
                mBinding.setAllMusicSheet(apMusicSheet);
            }
        });
        mViewModel.mFavouriteSheetData.observe(this, new Observer<ApMusicSheet>() {
            @Override
            public void onChanged(ApMusicSheet apMusicSheet) {
                mBinding.setFavouriteSheet(apMusicSheet);
            }
        });
        mViewModel.mRecentSheetData.observe(this, new Observer<ApMusicSheet>() {
            @Override
            public void onChanged(ApMusicSheet apMusicSheet) {
                mBinding.setRecentSheet(apMusicSheet);
            }
        });
        mViewModel.mCustomSheetListData.observe(this, new Observer<List<ApMusicSheet>>() {
            @Override
            public void onChanged(List<ApMusicSheet> list) {
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
        mMusicSheetAdapter.setOnItemClickListener(new BaseListAdapter.IOnItemClickListener<ApMusicSheet>() {
            @Override
            public void onItemClick(View view, int position, String tag, ApMusicSheet sheet) {
                goMultiMusicSelectActivity(sheet);
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mBinding.sheetRv.setLayoutManager(linearLayoutManager);
        mBinding.sheetRv.setAdapter(mMusicSheetAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GO_MULTI_MUSIC_SELECT) {
            if (resultCode == RESULT_OK) {
                mViewModel.onRefresh();
            }
        }
    }

    private void goMultiMusicSelectActivity(ApMusicSheet sheet) {
        Intent intent = new Intent(this, ApMultiMusicSelectActivity.class);
        intent.putExtra("musicSheet", sheet);
        startActivityForResult(intent, REQUEST_CODE_GO_MULTI_MUSIC_SELECT);
    }

    public class Presenter {
        public void onGoAllMusicClick(View view, ApMusicSheet sheet) {
            goMultiMusicSelectActivity(sheet);
        }

        public void onGoRecentMusicClick(View view, ApMusicSheet sheet) {
            goMultiMusicSelectActivity(sheet);
        }

        public void onGoFavouriteMusicClick(View view, ApMusicSheet sheet) {
            goMultiMusicSelectActivity(sheet);
        }
    }
}
