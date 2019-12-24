package com.pine.audioplayer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pine.audioplayer.R;
import com.pine.audioplayer.adapter.ApMusicListAdapter;
import com.pine.audioplayer.databinding.ApMusicListActivityBinding;
import com.pine.audioplayer.databinding.ApMusicListTopMenuBinding;
import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.vm.ApMusicListVm;
import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.base.recycle_view.adapter.BaseListAdapter;
import com.pine.base.util.DialogUtils;
import com.pine.base.widget.dialog.SelectItemDialog;
import com.pine.tool.util.ResourceUtils;
import com.pine.tool.widget.dialog.PopupMenu;

import java.util.List;

public class ApMusicListActivity extends BaseMvvmNoActionBarActivity<ApMusicListActivityBinding, ApMusicListVm> {

    private ApMusicListAdapter mMusicListAdapter;
    private PopupMenu mTopPopupMenu;

    @Override
    public void observeInitLiveData(Bundle savedInstanceState) {
        mViewModel.mSheetData.observe(this, new Observer<ApMusicSheet>() {
            @Override
            public void onChanged(ApMusicSheet apMusicSheet) {
                mBinding.setMusicSheet(apMusicSheet);
            }
        });
        mViewModel.mSheetMusicListData.observe(this, new Observer<List<ApSheetMusic>>() {
            @Override
            public void onChanged(List<ApSheetMusic> list) {
                mMusicListAdapter.setData(list);
            }
        });
    }

    @Override
    public void observeSyncLiveData(int liveDataObjTag) {

    }

    @Override
    protected int getActivityLayoutResId() {
        return R.layout.ap_activity_music_list;
    }

    @Override
    protected void init(Bundle onCreateSavedInstanceState) {
        mBinding.setPresenter(new Presenter());

        mMusicListAdapter = new ApMusicListAdapter();
        mMusicListAdapter.setOnItemClickListener(new BaseListAdapter.IOnItemClickListener<ApSheetMusic>() {
            @Override
            public void onItemClick(View view, int position, String tag, ApSheetMusic customData) {
                DialogUtils.createItemSelectDialog(ApMusicListActivity.this, customData.getName(),
                        ResourceUtils.getResIdArray(ApMusicListActivity.this, R.array.ap_music_item_menu_img),
                        getResources().getStringArray(R.array.ap_music_item_menu_name),
                        new SelectItemDialog.IDialogSelectListener() {
                            @Override
                            public void onSelect(String selectText, int position) {
                                
                            }
                        }).show();
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        mBinding.recycleView.setLayoutManager(layoutManager);
        mBinding.recycleView.setAdapter(mMusicListAdapter);
    }

    @Override
    protected void onRealResume() {
        super.onRealResume();
        mViewModel.refreshData();
    }

    private void goAddMusicActivity() {
        Intent intent = new Intent(this, ApAddMusicActivity.class);
        intent.putExtra("musicSheet", mViewModel.mSheetData.getValue());
        startActivity(intent);
    }

    public class Presenter {
        public void onPlayOrAddBtnClick(View view, boolean add) {
            if (add) {
                goAddMusicActivity();
            } else {

            }
        }

        public void onGoBackClick(View view) {
            finish();
        }

        public void onTopMenuClick(View view) {
            if (mTopPopupMenu == null) {
                mTopPopupMenu = new PopupMenu.Builder(ApMusicListActivity.this)
                        .create(R.layout.ap_music_list_top_menu_layout, view);
                ApMusicListTopMenuBinding binding = DataBindingUtil.bind(mTopPopupMenu.getContentView());
                binding.addMusicLl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mTopPopupMenu.dismiss();
                        goAddMusicActivity();
                    }
                });
                binding.deleteSheetLl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mTopPopupMenu.dismiss();
                        mViewModel.deleteMusicSheet();
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }
            mTopPopupMenu.showAsDropDown(view);
        }

        public void onGoMultiSelectUiClick(View view) {
            Intent intent = new Intent(ApMusicListActivity.this, ApMultiMusicSelectActivity.class);
            intent.putExtra("musicSheet", mViewModel.mSheetData.getValue());
            intent.putExtra("action", true);
            startActivity(intent);
        }
    }
}
