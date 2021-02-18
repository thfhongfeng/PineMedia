package com.pine.media.audioplayer.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pine.media.audioplayer.ApConstants;
import com.pine.media.audioplayer.R;
import com.pine.media.audioplayer.adapter.ApMusicListAdapter;
import com.pine.media.audioplayer.databinding.ApMusicListActivityBinding;
import com.pine.media.audioplayer.databinding.ApMusicListTopMenuBinding;
import com.pine.media.audioplayer.db.entity.ApMusic;
import com.pine.media.audioplayer.db.entity.ApSheet;
import com.pine.media.audioplayer.manager.ApAudioPlayerHelper;
import com.pine.media.audioplayer.vm.ApMusicListVm;
import com.pine.media.audioplayer.widget.AudioPlayerView;
import com.pine.media.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.media.base.recycle_view.adapter.BaseListAdapter;
import com.pine.media.base.util.DialogUtils;
import com.pine.media.base.widget.dialog.SelectItemDialog;
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
        public void onPlayMusic(PineMediaWidget.IPineMediaPlayer mPlayer, @Nullable ApMusic newMusic) {
            LogUtils.d(TAG, "onPlayMusic newMusic:" + newMusic);
            mMusicListAdapter.setPlayMusic(newMusic, mPlayer != null && mPlayer.isPlaying());
        }

        @Override
        public void onPlayStateChange(ApMusic music, PinePlayState fromState, PinePlayState toState) {
            LogUtils.d(TAG, "onPlayStateChange fromState:" + fromState + ",toState:" + toState + ", music:" + music);
            mMusicListAdapter.setPlayMusic(music, toState == PinePlayState.STATE_PLAYING);
        }

        @Override
        public void onAlbumArtChange(String mediaCode, ApMusic music, Bitmap smallBitmap,
                                     Bitmap bigBitmap, int mainColor) {
        }
    };

    @Override
    public void observeInitLiveData(Bundle savedInstanceState) {
        mViewModel.mSheetData.observe(this, new Observer<ApSheet>() {
            @Override
            public void onChanged(ApSheet apSheet) {
                mBinding.setMusicSheet(apSheet);
            }
        });
        mViewModel.mSheetMusicListData.observe(this, new Observer<List<ApMusic>>() {
            @Override
            public void onChanged(List<ApMusic> list) {
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
        mMusicListAdapter.setOnItemClickListener(new BaseListAdapter.IOnItemClickListener<ApMusic>() {
            @Override
            public void onItemClick(View view, int position, String tag, ApMusic sheetMusic) {
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
        mViewModel.refreshData(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ApAudioPlayerHelper.getInstance().detachPlayerView(mBinding.playerView);
    }

    private void showMusicItemMenu(final ApMusic sheetMusic) {
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
                                mViewModel.updateMusicFavourite(ApMusicListActivity.this, sheetMusic, true);
                                break;
                            case 1:
                                Intent intent = new Intent(ApMusicListActivity.this, ApAddMusicToSheetActivity.class);
                                intent.putExtra("excludeSheetId", mViewModel.mSheetData.getValue().getId());
                                ArrayList<ApMusic> list = new ArrayList<>();
                                list.add(sheetMusic);
                                intent.putExtra("selectList", list);
                                startActivity(intent);
                                break;
                            case 2:
                                mViewModel.deleteSheetMusic(ApMusicListActivity.this, sheetMusic);
                                mViewModel.refreshData(ApMusicListActivity.this);
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
                List<ApMusic> musicList = mMusicListAdapter.getOriginData();
                if (musicList != null && musicList.size() > 0) {
                    ApAudioPlayerHelper.getInstance().playMusicList(mBinding.playerView, musicList, true);
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
                        mViewModel.deleteMusicSheet(ApMusicListActivity.this);
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
