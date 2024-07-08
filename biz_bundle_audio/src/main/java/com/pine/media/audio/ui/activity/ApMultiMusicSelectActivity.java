package com.pine.media.audio.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pine.media.audio.R;
import com.pine.media.audio.adapter.ApMultiMusicSelectAdapter;
import com.pine.media.audio.databinding.ApMultiMusicSelectActivityBinding;
import com.pine.media.audio.db.entity.ApMusic;
import com.pine.media.audio.db.entity.ApSheet;
import com.pine.media.audio.vm.ApMusicListVm;
import com.pine.template.base.architecture.mvvm.ui.activity.BaseMvvmActionBarImageMenuActivity;
import com.pine.template.base.recycle_view.adapter.BaseListAdapter;

import java.util.ArrayList;
import java.util.List;

public class ApMultiMusicSelectActivity extends BaseMvvmActionBarImageMenuActivity<ApMultiMusicSelectActivityBinding, ApMusicListVm> {
    private TextView mTitleTv;

    private ApMultiMusicSelectAdapter mMultiMusicSelectAdapter;

    @Override
    protected void setupActionBar(View actionbar, ImageView goBackIv, TextView titleTv, ImageView menuBtnIv) {
        mTitleTv = titleTv;
        menuBtnIv.setImageResource(R.mipmap.ap_ic_check_complete);
        menuBtnIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ApMusic> selectList = mMultiMusicSelectAdapter.getSelectMusicList();
                if (selectList.size() > 0) {
                    Intent data = new Intent();
                    data.putExtra("selectList", selectList);
                    setResult(RESULT_OK, data);
                }
                finish();
            }
        });
    }

    @Override
    public void observeInitLiveData(Bundle savedInstanceState) {
        mViewModel.mSheetData.observe(this, new Observer<ApSheet>() {
            @Override
            public void onChanged(ApSheet apSheet) {
                mTitleTv.setText(apSheet.getName());
                mBinding.setMusicSheet(apSheet);
            }
        });
        mViewModel.mSheetMusicListData.observe(this, new Observer<List<ApMusic>>() {
            @Override
            public void onChanged(List<ApMusic> list) {
                mMultiMusicSelectAdapter.setData(list);
            }
        });
        mViewModel.mActionData.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean hasAction) {
                mBinding.setHasAction(hasAction);
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
        mViewModel.refreshData(this);
    }

    private void onMusicSelectChange() {
        int selectCount = mMultiMusicSelectAdapter.getSelectMusicCount();
        boolean isAllSelect = selectCount == mMultiMusicSelectAdapter.getOriginData().size();
        mBinding.allSelectBtn.setSelected(isAllSelect);
        mBinding.allSelectBtn.setText(isAllSelect ? R.string.ap_mms_all_cancel_select : R.string.ap_mms_all_select);
        mTitleTv.setText(selectCount > 0 ? getString(R.string.ap_mms_select_count, selectCount) : mViewModel.mSheetData.getValue().getName());
        mBinding.addToSheetBtn.setEnabled(selectCount > 0);
        mBinding.deleteSelectBtn.setEnabled(selectCount > 0);
    }

    public class Presenter {
        public void onAllSelectClick(View view) {
            mMultiMusicSelectAdapter.setAllSelectMusic(!view.isSelected());
            onMusicSelectChange();
        }

        public void onAddToSheetClick(View view) {
            if (mMultiMusicSelectAdapter.getSelectMusicList().size() < 1) {
                return;
            }
            Intent intent = new Intent(ApMultiMusicSelectActivity.this, ApAddMusicToSheetActivity.class);
            intent.putExtra("excludeSheetId", mViewModel.mSheetData.getValue().getId());
            intent.putExtra("selectList", mMultiMusicSelectAdapter.getSelectMusicList());
            startActivity(intent);
        }

        public void onDeleteClick(View view) {
            mViewModel.deleteSheetMusics(ApMultiMusicSelectActivity.this, mMultiMusicSelectAdapter.getSelectMusicList());
            finish();
        }
    }
}
