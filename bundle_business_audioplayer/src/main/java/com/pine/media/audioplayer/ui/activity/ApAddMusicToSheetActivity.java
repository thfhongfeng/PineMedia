package com.pine.media.audioplayer.ui.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pine.media.audioplayer.R;
import com.pine.media.audioplayer.adapter.ApAddMusicToSheetAdapter;
import com.pine.media.audioplayer.databinding.ApAddMusicToSheetActivityBinding;
import com.pine.media.audioplayer.db.entity.ApMusic;
import com.pine.media.audioplayer.db.entity.ApSheet;
import com.pine.media.audioplayer.vm.ApAddMusicToSheetVm;
import com.pine.media.base.architecture.mvvm.ui.activity.BaseMvvmActionBarActivity;
import com.pine.media.base.recycle_view.adapter.BaseListAdapter;
import com.pine.media.base.util.DialogUtils;
import com.pine.media.base.widget.dialog.InputTextDialog;

import java.util.List;

public class ApAddMusicToSheetActivity extends BaseMvvmActionBarActivity<ApAddMusicToSheetActivityBinding, ApAddMusicToSheetVm> {
    private ApAddMusicToSheetAdapter mAdapter;

    private TextView mTitleTv;

    @Override
    protected void setupActionBar(View actionbar, ImageView goBackIv, TextView titleTv) {
        mTitleTv = titleTv;
    }

    @Override
    public void observeInitLiveData(Bundle savedInstanceState) {
        mViewModel.mSheetListData.observe(this, new Observer<List<ApSheet>>() {
            @Override
            public void onChanged(List<ApSheet> list) {
                ApSheet addSheet = new ApSheet();
                addSheet.setName(getString(R.string.ap_amts_create_sheet));
                addSheet.setSheetType(-1);
                list.add(0, addSheet);
                mAdapter.setData(list);
            }
        });
        mViewModel.mSelectMusicListData.observe(this, new Observer<List<ApMusic>>() {
            @Override
            public void onChanged(List<ApMusic> list) {
                mTitleTv.setText(getString(R.string.ap_amts_title, list.size()));
            }
        });
    }

    @Override
    public void observeSyncLiveData(int liveDataObjTag) {

    }

    @Override
    protected int getActivityLayoutResId() {
        return R.layout.ap_activity_add_music_to_sheet;
    }

    @Override
    protected void init(Bundle onCreateSavedInstanceState) {
        mAdapter = new ApAddMusicToSheetAdapter();
        mAdapter.setOnItemClickListener(new BaseListAdapter.IOnItemClickListener<ApSheet>() {
            @Override
            public void onItemClick(View view, int position, String tag, ApSheet sheet) {
                if (position == 0) {
                    DialogUtils.createTextInputDialog(ApAddMusicToSheetActivity.this, getString(R.string.ap_home_create_sheet_title),
                            "", 50, new InputTextDialog.IActionClickListener() {
                                @Override
                                public boolean onSubmitClick(Dialog dialog, List<String> textList) {
                                    if (TextUtils.isEmpty(textList.get(0))) {
                                        showShortToast(R.string.ap_home_sheet_name_non_empty);
                                        return true;
                                    } else {
                                        mViewModel.createAndAddMusicToSheet(ApAddMusicToSheetActivity.this, textList.get(0));
                                        finish();
                                        return false;
                                    }
                                }

                                @Override
                                public boolean onCancelClick(Dialog dialog) {
                                    return false;
                                }
                            }).show();
                } else {
                    mViewModel.addMusicToSheet(ApAddMusicToSheetActivity.this, sheet);
                    finish();
                }
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mBinding.sheetRv.setLayoutManager(linearLayoutManager);
        mBinding.sheetRv.setAdapter(mAdapter);
    }

    @Override
    protected void onRealResume() {
        super.onRealResume();
        mViewModel.refreshData(this);
    }
}
