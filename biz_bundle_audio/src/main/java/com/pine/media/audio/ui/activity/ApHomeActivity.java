package com.pine.media.audio.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pine.media.audio.R;
import com.pine.media.audio.adapter.ApMusicSheetAdapter;
import com.pine.media.audio.databinding.ApHomeActivityBinding;
import com.pine.media.audio.db.entity.ApSheet;
import com.pine.media.audio.manager.ApAudioPlayerHelper;
import com.pine.media.audio.vm.ApHomeVm;
import com.pine.template.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.template.base.recycle_view.adapter.BaseListAdapter;
import com.pine.template.base.util.DialogUtils;
import com.pine.template.base.widget.dialog.InputTextDialog;

import java.util.List;

public class ApHomeActivity extends BaseMvvmNoActionBarActivity<ApHomeActivityBinding, ApHomeVm> {
    private ApMusicSheetAdapter mMusicSheetAdapter;

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
        mViewModel.mCustomSheetListData.observe(this, new Observer<List<ApSheet>>() {
            @Override
            public void onChanged(List<ApSheet> list) {
                mBinding.setCustomSheetCount(list == null ? 0 : list.size());
                mMusicSheetAdapter.setData(list);
            }
        });
    }

    @Override
    public void observeSyncLiveData(int liveDataObjTag) {
        if (liveDataObjTag == ApHomeVm.LIVE_DATA_TAG_RECENT_SHEET) {
            mViewModel.mRecentSheetData.observe(this, new Observer<ApSheet>() {
                @Override
                public void onChanged(ApSheet apSheet) {
                    mBinding.setRecentSheet(apSheet);
                }
            });
        }
    }

    @Override
    protected int getActivityLayoutResId() {
        return R.layout.ap_activity_home;
    }

    @Override
    protected void init(Bundle onCreateSavedInstanceState) {
        mBinding.setPresenter(new Presenter());

        mMusicSheetAdapter = new ApMusicSheetAdapter();
        mMusicSheetAdapter.setOnItemClickListener(new BaseListAdapter.IOnItemClickListener<ApSheet>() {
            @Override
            public void onItemClick(View view, int position, String tag, ApSheet sheet) {
                goMusicListActivity(sheet);
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mBinding.sheetRv.setLayoutManager(linearLayoutManager);
        mBinding.sheetRv.setAdapter(mMusicSheetAdapter);
        ApAudioPlayerHelper.getInstance().initPlayerView(mBinding.playerView);
    }

    @Override
    protected void onRealResume() {
        super.onRealResume();
        ApAudioPlayerHelper.getInstance().attachPlayerView(mBinding.playerView);
        mViewModel.refreshData(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ApAudioPlayerHelper.getInstance().detachPlayerView(mBinding.playerView);
    }

    @Override
    protected void onDestroy() {
        ApAudioPlayerHelper.getInstance().destroy();
        super.onDestroy();
    }

    private void goMusicListActivity(ApSheet sheet) {
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
                                mViewModel.createSheet(ApHomeActivity.this, textList.get(0));
                                return false;
                            }
                        }

                        @Override
                        public boolean onCancelClick(Dialog dialog) {
                            return false;
                        }
                    }).show();
        }

        public void onShowAllClick(View view, ApSheet sheet) {
            goMusicListActivity(sheet);
        }

        public void onShowFavouriteClick(View view, ApSheet sheet) {
            goMusicListActivity(sheet);
        }

        public void onShowRecentClick(View view, ApSheet sheet) {
            goMusicListActivity(sheet);
        }
    }
}
