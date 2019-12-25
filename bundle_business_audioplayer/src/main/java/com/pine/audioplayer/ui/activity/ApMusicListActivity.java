package com.pine.audioplayer.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
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
import com.pine.audioplayer.uitls.ApLocalMusicUtils;
import com.pine.audioplayer.vm.ApMusicListVm;
import com.pine.audioplayer.widget.view.ApSimpleAudioPlayerView;
import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.base.recycle_view.adapter.BaseListAdapter;
import com.pine.base.util.DialogUtils;
import com.pine.base.widget.dialog.SelectItemDialog;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.tool.util.ResourceUtils;
import com.pine.tool.widget.dialog.PopupMenu;

import java.util.ArrayList;
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
            public void onItemClick(View view, int position, String tag, ApSheetMusic sheetMusic) {
                switch (tag) {
                    case "menu":
                        showMusicItemMenu(sheetMusic);
                        break;
                    default:
                        playMusic(mMusicListAdapter.getMediaList().get(position), sheetMusic, true);
                        break;
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        mBinding.recycleView.setLayoutManager(layoutManager);
        mBinding.recycleView.setAdapter(mMusicListAdapter);
        mBinding.audioView.init(this, TAG, new ApSimpleAudioPlayerView.IOnMediaListChangeListener() {
            @Override
            public void onMediaRemove(PineMediaPlayerBean mediaBean) {
                mViewModel.removeMusicFromRecent(mediaBean.getMediaCode());
            }

            @Override
            public void onMediaListClear(List<PineMediaPlayerBean> mediaBeanList) {
                mViewModel.clearRecentSheetMusic();
            }
        });
        List<PineMediaPlayerBean> recentMediaList = new ArrayList<>();
        List<ApSheetMusic> recentMusicList = new ArrayList<>();
        mViewModel.getRecentMediaList(recentMusicList, recentMediaList);
        if (recentMediaList != null && recentMediaList.size() > 0) {
            ApSheetMusic music = recentMusicList.get(0);
            Bitmap bitmap = null;
            if (music != null) {
                bitmap = ApLocalMusicUtils.getAlbumArtBitmap(this,
                        music.getSongId(), music.getAlbumId(), true);
            }
            mBinding.audioView.playMediaList(recentMediaList, recentMediaList.get(0), bitmap, false);
        }
    }

    @Override
    protected void onRealResume() {
        super.onRealResume();
        mViewModel.refreshData();
    }

    private void playAllMediaList(@NonNull List<PineMediaPlayerBean> mediaBeanList,
                                  @NonNull PineMediaPlayerBean playBean, ApSheetMusic music,
                                  boolean startPlay) {
        Bitmap bitmap = null;
        if (music != null) {
            bitmap = ApLocalMusicUtils.getAlbumArtBitmap(this,
                    music.getSongId(), music.getAlbumId(), true);
        }
        mBinding.audioView.playMediaList(mediaBeanList, playBean, bitmap, startPlay);
        mViewModel.addAllMusicsToRecent();
    }

    private void playMusic(@NonNull PineMediaPlayerBean playBean, ApSheetMusic music, boolean startPlay) {
        Bitmap bitmap = null;
        if (music != null) {
            bitmap = ApLocalMusicUtils.getAlbumArtBitmap(this,
                    music.getSongId(), music.getAlbumId(), true);
        }
        mBinding.audioView.playMedia(playBean, bitmap, startPlay);
        mViewModel.addMusicToRecent(music);
    }

    private void showMusicItemMenu(final ApSheetMusic sheetMusic) {
        DialogUtils.createItemSelectDialog(ApMusicListActivity.this, sheetMusic.getName(),
                ResourceUtils.getResIdArray(ApMusicListActivity.this, R.array.ap_music_item_menu_img),
                getResources().getStringArray(R.array.ap_music_item_menu_name),
                new SelectItemDialog.IDialogSelectListener() {
                    @Override
                    public void onSelect(String selectText, int position) {
                        switch (position) {
                            case 0:
                                mViewModel.addMusicToFavourite(sheetMusic);
                                break;
                            case 1:
                                Intent intent = new Intent(ApMusicListActivity.this, ApAddMusicToSheetActivity.class);
                                intent.putExtra("musicSheet", mViewModel.mSheetData.getValue());
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
                List<PineMediaPlayerBean> playerBeanList = mMusicListAdapter.getMediaList();
                List<ApSheetMusic> sheetMusicList = mMusicListAdapter.getOriginData();
                if (playerBeanList != null && playerBeanList.size() > 0) {
                    playAllMediaList(playerBeanList, playerBeanList.get(0), sheetMusicList.get(0), true);
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
