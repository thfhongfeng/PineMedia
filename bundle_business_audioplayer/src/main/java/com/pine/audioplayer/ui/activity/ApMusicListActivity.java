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
import com.pine.tool.widget.dialog.PopupMenu;

import java.util.List;

public class ApMusicListActivity extends BaseMvvmNoActionBarActivity<ApMusicListActivityBinding, ApMusicListVm> {
    private final int REQUEST_CODE_GO_ADD_MUSIC = 1;

    private ApMusicListAdapter mMusicListAdapter;
    private PopupMenu mPopupMenu;

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
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        mBinding.recycleView.setLayoutManager(layoutManager);
        mBinding.recycleView.setAdapter(mMusicListAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GO_ADD_MUSIC) {
            if (resultCode == RESULT_OK) {
                mViewModel.onRefresh();
            }
        }
    }

    private void goAddMusicActivity() {
        Intent intent = new Intent(this, ApAddMusicActivity.class);
        startActivityForResult(intent, REQUEST_CODE_GO_ADD_MUSIC);
    }

    public class Presenter {
        public void onPlayOrAddBtnClick(View view, boolean add) {
            if (add) {
                goAddMusicActivity();
            } else {

            }
        }

        public void onGoBackClick(View view) {
            setResult(RESULT_OK);
            finish();
        }

        public void onTopMenuClick(View view) {
            if (mPopupMenu == null) {
                mPopupMenu = new PopupMenu.Builder(ApMusicListActivity.this)
                        .create(R.layout.ap_music_list_top_menu_layout, view);
                ApMusicListTopMenuBinding binding = DataBindingUtil.bind(mPopupMenu.getContentView());
                binding.addMusicLl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goAddMusicActivity();
                    }
                });
                binding.deleteSheetLl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewModel.deleteMusicSheet();
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }
            mPopupMenu.showAsDropDown(view);
        }

        public void onGoMultiSelectUiClick(View view) {

        }
    }
}
