package com.pine.audioplayer.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pine.audioplayer.R;
import com.pine.audioplayer.adapter.ApMultiMusicSelectAdapter;
import com.pine.audioplayer.databinding.ApMultiMusicSelectActivityBinding;
import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.vm.ApMusicListVm;
import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmActionBarImageMenuActivity;
import com.pine.base.recycle_view.adapter.BaseListAdapter;

import java.util.List;

import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ApMultiMusicSelectActivity extends BaseMvvmActionBarImageMenuActivity<ApMultiMusicSelectActivityBinding, ApMusicListVm> {
    private TextView mTitleTv;

    private ApMultiMusicSelectAdapter mMultiMusicSelectAdapter;

    @Override
    protected void setupActionBar(ImageView goBackIv, TextView titleTv, ImageView menuBtnIv) {
        mTitleTv = titleTv;
        menuBtnIv.setImageResource(R.mipmap.res_ic_check_complete);
        menuBtnIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ApSheetMusic> selectList = mMultiMusicSelectAdapter.getSelectMusicList(mViewModel.mSheetData.getValue().getId());
                if (selectList.size() > 0) {

                }
            }
        });
    }

    @Override
    public void observeInitLiveData(Bundle savedInstanceState) {
        mViewModel.mSheetData.observe(this, new Observer<ApMusicSheet>() {
            @Override
            public void onChanged(ApMusicSheet apMusicSheet) {
                mTitleTv.setText(apMusicSheet.getName());
                mBinding.setMusicSheet(apMusicSheet);
            }
        });
        mViewModel.mSheetMusicListData.observe(this, new Observer<List<ApSheetMusic>>() {
            @Override
            public void onChanged(List<ApSheetMusic> list) {
                mMultiMusicSelectAdapter.setData(list);
            }
        });
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
        mBinding.setPresenter(new Presenter());

        mMultiMusicSelectAdapter = new ApMultiMusicSelectAdapter();
        mMultiMusicSelectAdapter.setOnItemClickListener(new BaseListAdapter.IOnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, String tag, Object customData) {
                onMusicSelectChange();
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        mBinding.recycleView.setLayoutManager(layoutManager);
        mBinding.recycleView.setAdapter(mMultiMusicSelectAdapter);
    }

    private void onMusicSelectChange() {
        int selectCount = mMultiMusicSelectAdapter.getSelectMusicCount();
        boolean isAllSelect = selectCount == mMultiMusicSelectAdapter.getOriginData().size();
        mBinding.allSelectBtn.setSelected(isAllSelect);
        mBinding.allSelectBtn.setText(isAllSelect ? R.string.ap_mms_all_cancel_select : R.string.ap_mms_all_select);
        mTitleTv.setText(selectCount > 0 ? getString(R.string.ap_mms_select_count, selectCount) : mViewModel.mSheetData.getValue().getName());
    }

    public class Presenter {
        public void onAllSelectClick(View view) {
            mMultiMusicSelectAdapter.setAllSelectMusic(!view.isSelected());
            onMusicSelectChange();
        }
    }
}
