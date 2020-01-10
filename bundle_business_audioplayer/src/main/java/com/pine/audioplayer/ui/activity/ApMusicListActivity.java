package com.pine.audioplayer.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pine.audioplayer.ApConstants;
import com.pine.audioplayer.R;
import com.pine.audioplayer.adapter.ApMusicListAdapter;
import com.pine.audioplayer.databinding.ApMusicListActivityBinding;
import com.pine.audioplayer.databinding.ApMusicListTopMenuBinding;
import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.manager.ApAudioPlayerHelper;
import com.pine.audioplayer.vm.ApMusicListVm;
import com.pine.audioplayer.widget.AudioPlayerView;
import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.base.recycle_view.adapter.BaseListAdapter;
import com.pine.base.util.DialogUtils;
import com.pine.base.widget.dialog.SelectItemDialog;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.component.PinePlayState;
import com.pine.tool.util.LogUtils;
import com.pine.tool.util.ResourceUtils;
import com.pine.tool.widget.dialog.PopupMenu;

import java.util.ArrayList;
import java.util.List;

public class ApMusicListActivity extends BaseMvvmNoActionBarActivity<ApMusicListActivityBinding, ApMusicListVm> {
    private ApMusicListAdapter mMusicListAdapter;
    private PopupMenu mTopPopupMenu;
    private AudioPlayerView.IPlayerViewListener mPlayerListener = new AudioPlayerView.IPlayerViewListener() {
        @Override
        public void onPlayMusic(PineMediaWidget.IPineMediaPlayer mPlayer, @Nullable ApSheetMusic newMusic) {
            LogUtils.d(TAG, "onPlayMusic newMusic:" + newMusic);
            mMusicListAdapter.setPlayMusic(newMusic, mPlayer != null && mPlayer.isPlaying());
        }

        @Override
        public void onPlayStateChange(ApSheetMusic music, PinePlayState fromState, PinePlayState toState) {
            LogUtils.d(TAG, "onPlayStateChange fromState:" + fromState + ",toState:" + toState + ", music:" + music);
            mMusicListAdapter.setPlayMusic(music, toState == PinePlayState.STATE_PLAYING);
        }

        @Override
        public void onAlbumArtChange(String mediaCode, ApSheetMusic music, Bitmap smallBitmap,
                                     Bitmap bigBitmap, int mainColor) {
        }
    };

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
            public void onItemClick(View view, int position, String tag, ApSheetMusic sheetMusic) {
                switch (tag) {
                    case "menu":
                        showMusicItemMenu(sheetMusic);
                        break;
                    default:
                        ApAudioPlayerHelper.getInstance().playMusic(mBinding.playerView, sheetMusic, true);
                        break;
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        mBinding.recycleView.setLayoutManager(layoutManager);
        mBinding.recycleView.setAdapter(mMusicListAdapter);
        ApAudioPlayerHelper.getInstance().initPlayerView(mBinding.playerView);
    }

    @Override
    protected void onRealResume() {
        super.onRealResume();
        ApAudioPlayerHelper.getInstance().attachPlayerView(mBinding.playerView, mPlayerListener);
        mViewModel.refreshData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ApAudioPlayerHelper.getInstance().detachPlayerView(mBinding.playerView);
    }

    private void showMusicItemMenu(final ApSheetMusic sheetMusic) {
        int[] menuImages = ResourceUtils.getResIdArray(ApMusicListActivity.this,
                mViewModel.mSheetData.getValue().getSheetType() == ApConstants.MUSIC_SHEET_TYPE_ALL ?
                        R.array.ap_all_music_item_menu_img : R.array.ap_music_item_menu_img);
        String[] menuNames = getResources().getStringArray(mViewModel.mSheetData.getValue().getSheetType() == ApConstants.MUSIC_SHEET_TYPE_ALL ?
                R.array.ap_all_music_item_menu_name : R.array.ap_music_item_menu_name);
        DialogUtils.createItemSelectDialog(ApMusicListActivity.this, sheetMusic.getName(),
                menuImages, menuNames,
                new SelectItemDialog.IDialogSelectListener() {
                    @Override
                    public void onSelect(String selectText, int position) {
                        switch (position) {
                            case 0:
                                mViewModel.addMusicToFavourite(sheetMusic);
                                break;
                            case 1:
                                Intent intent = new Intent(ApMusicListActivity.this, ApAddMusicToSheetActivity.class);
                                intent.putExtra("excludeSheetId", mViewModel.mSheetData.getValue().getId());
                                ArrayList<ApSheetMusic> list = new ArrayList<>();
                                list.add(sheetMusic);
                                intent.putParcelableArrayListExtra("selectList", list);
                                startActivity(intent);
                                break;
                            case 2:
                                mViewModel.deleteSheetMusic(sheetMusic);
                                mViewModel.refreshData();
                                break;
                        }
                    }
                }).show();
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
                List<ApSheetMusic> sheetMusicList = mMusicListAdapter.getOriginData();
                if (sheetMusicList != null && sheetMusicList.size() > 0) {
                    ApAudioPlayerHelper.getInstance().playMusicList(mBinding.playerView, sheetMusicList, true);
                }
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
