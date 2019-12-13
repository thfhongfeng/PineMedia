package com.pine.videoplayer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.videoplayer.R;
import com.pine.videoplayer.adapter.VpPlayFilesAdapter;
import com.pine.videoplayer.bean.VpChooseFileBean;
import com.pine.videoplayer.databinding.VpHomeActivityBinding;
import com.pine.videoplayer.vm.VpHomeVm;

import java.util.ArrayList;

public class VpHomeActivity extends BaseMvvmNoActionBarActivity<VpHomeActivityBinding, VpHomeVm> {
    private final int REQUEST_CODE_CHOOSE_FILE = 1;
    private VpPlayFilesAdapter mRecentAdapter;

    @Override
    public void observeInitLiveData(Bundle savedInstanceState) {
        mViewModel.mChooseFileList.observe(this, new Observer<ArrayList<VpChooseFileBean>>() {
            @Override
            public void onChanged(ArrayList<VpChooseFileBean> list) {
                mRecentAdapter.setData(list);
                mRecentAdapter.notifyDataSetChangedSafely();
            }
        });
    }

    @Override
    public void observeSyncLiveData(int liveDataObjTag) {

    }

    @Override
    protected int getActivityLayoutResId() {
        return R.layout.vp_activity_home;
    }

    @Override
    protected void init(Bundle onCreateSavedInstanceState) {
        mBinding.setPresenter(new Presenter());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mBinding.mediaListRv.setLayoutManager(linearLayoutManager);
        mRecentAdapter = new VpPlayFilesAdapter();
        mBinding.mediaListRv.setAdapter(mRecentAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE_FILE) {
            if (resultCode == RESULT_OK) {
                mViewModel.onFileChosen(data);
            }
        }
    }

    public class Presenter {
        public void onChooseFileClick(View view) {
            // 调用系统文件管理器打开指定路径目录
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_CODE_CHOOSE_FILE);
        }
    }
}
