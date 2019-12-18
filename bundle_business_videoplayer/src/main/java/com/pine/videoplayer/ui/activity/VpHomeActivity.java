package com.pine.videoplayer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.base.util.SysIntentUtils;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.adapter.DefaultVideoControllerAdapter;
import com.pine.videoplayer.R;
import com.pine.videoplayer.adapter.VpPlayFilesAdapter;
import com.pine.videoplayer.bean.VpFileBean;
import com.pine.videoplayer.databinding.VpHomeActivityBinding;
import com.pine.videoplayer.vm.VpHomeVm;

import java.util.ArrayList;
import java.util.List;

public class VpHomeActivity extends BaseMvvmNoActionBarActivity<VpHomeActivityBinding, VpHomeVm> {
    private final int REQUEST_CODE_CHOOSE_FILE = 1;
    private VpPlayFilesAdapter mRecentPlayedFilesAdapter;

    private PineMediaWidget.IPineMediaPlayer mPlayer;
    private PineMediaController mController;
    private DefaultVideoControllerAdapter mMediaControllerAdapter;

    @Override
    public void observeInitLiveData(Bundle savedInstanceState) {
        mViewModel.mFileListData.observe(this, new Observer<ArrayList<VpFileBean>>() {
            @Override
            public void onChanged(ArrayList<VpFileBean> list) {
                mRecentPlayedFilesAdapter.setData(list);
                mRecentPlayedFilesAdapter.setCurMediaPosition(mViewModel.mFileListData.getCustomData());
                mRecentPlayedFilesAdapter.notifyDataSetChangedSafely();
            }
        });
        mViewModel.mMediaListData.observe(this, new Observer<List<PineMediaPlayerBean>>() {
            @Override
            public void onChanged(List<PineMediaPlayerBean> list) {
                mMediaControllerAdapter = new DefaultVideoControllerAdapter(VpHomeActivity.this, mPlayer, list);
                mMediaControllerAdapter.setMediaItemChangeListener(new DefaultVideoControllerAdapter.IOnMediaItemChangeListener() {
                    @Override
                    public void onMediaChange(int oldMediaBeanPosition, int newMediaBeanPosition) {
                        mRecentPlayedFilesAdapter.setCurMediaPosition(newMediaBeanPosition);
                        mRecentPlayedFilesAdapter.notifyDataSetChangedSafely();
                    }
                });
                mController.setMediaControllerAdapter(mMediaControllerAdapter);
                int playIndex = mViewModel.mMediaListData.getCustomData();
                mMediaControllerAdapter.mediaSelect(mViewModel.mMediaListData.getCustomData(), playIndex != -1);
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
        mRecentPlayedFilesAdapter = new VpPlayFilesAdapter();
        mRecentPlayedFilesAdapter.enableEmptyComplete(false, false);
        mRecentPlayedFilesAdapter.setMediaItemClick(new VpPlayFilesAdapter.IOnMediaItemClick() {
            @Override
            public void onMediaItemClick(View view, int position) {
                mMediaControllerAdapter.mediaSelect(position, true);
            }
        });
        mBinding.mediaListRv.setAdapter(mRecentPlayedFilesAdapter);

        mController = new PineMediaController(this);
        mBinding.videoView.init(TAG, mController);
        mPlayer = mBinding.videoView.getMediaPlayer();
        mPlayer.setAutocephalyPlayMode(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE_FILE) {
            if (resultCode == RESULT_OK) {
                mViewModel.onFileChosen(this, data);
            }
        }
    }

    public class Presenter {
        public void onChooseFileClick(View view) {
            // 调用系统文件管理器打开指定路径目录
            Intent intent = SysIntentUtils.getFileSelectorIntent(new String[]{"video/*", "audio/*"});
            startActivityForResult(intent, REQUEST_CODE_CHOOSE_FILE);
        }
    }
}
