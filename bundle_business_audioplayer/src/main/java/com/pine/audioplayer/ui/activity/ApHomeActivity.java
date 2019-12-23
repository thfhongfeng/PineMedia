package com.pine.audioplayer.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pine.audioplayer.R;
import com.pine.audioplayer.adapter.ApMusicSheetAdapter;
import com.pine.audioplayer.databinding.ApHomeActivityBinding;
import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.vm.ApSheetListVm;
import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.base.recycle_view.adapter.BaseListAdapter;
import com.pine.base.util.DialogUtils;
import com.pine.base.widget.dialog.InputTextDialog;

import java.util.List;

public class ApHomeActivity extends BaseMvvmNoActionBarActivity<ApHomeActivityBinding, ApSheetListVm> {
    private ApMusicSheetAdapter mMusicSheetAdapter;

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
        return R.layout.ap_activity_home;
    }

    @Override
    protected void init(Bundle onCreateSavedInstanceState) {
        mBinding.setPresenter(new Presenter());

        mMusicSheetAdapter = new ApMusicSheetAdapter();
        mMusicSheetAdapter.setOnItemClickListener(new BaseListAdapter.IOnItemClickListener<ApMusicSheet>() {
            @Override
            public void onItemClick(View view, int position, String tag, ApMusicSheet sheet) {
                goMusicListActivity(sheet);
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

    private void goMusicListActivity(ApMusicSheet sheet) {
        Intent intent = new Intent(this, ApMusicListActivity.class);
        intent.putExtra("musicSheet", sheet);
        startActivity(intent);
    }

    public class Presenter {
        public void onCreateSheetClick(View view) {
            DialogUtils.createTextInputDialog(ApHomeActivity.this, getString(R.string.ap_home_create_sheet_title),
                    "", 50, new InputTextDialog.IActionClickListener() {
                        @Override
                        public boolean onSubmitClick(Dialog dialog, List<String> textList) {
                            if (TextUtils.isEmpty(textList.get(0))) {
                                showShortToast(R.string.ap_home_sheet_name_non_empty);
                                return true;
                            } else {
                                mViewModel.createSheet(textList.get(0));
                                return false;
                            }
                        }

                        @Override
                        public boolean onCancelClick(Dialog dialog) {
                            return false;
                        }
                    }).show();
        }

        public void onShowAllClick(View view, ApMusicSheet sheet) {
            goMusicListActivity(sheet);
        }

        public void onShowFavouriteClick(View view, ApMusicSheet sheet) {
            goMusicListActivity(sheet);
        }

        public void onShowRecentClick(View view, ApMusicSheet sheet) {
            goMusicListActivity(sheet);
        }
    }
}
