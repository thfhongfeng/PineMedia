package com.pine.audioplayer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;

import com.pine.audioplayer.R;
import com.pine.audioplayer.databinding.ApActionMainTimingDialogBinding;
import com.pine.audioplayer.databinding.ApItemMainTimingDialogBinding;
import com.pine.audioplayer.databinding.ApMainActivityBinding;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.manager.ApAudioPlayerHelper;
import com.pine.audioplayer.vm.ApMainVm;
import com.pine.audioplayer.widget.AudioPlayerView;
import com.pine.audioplayer.widget.plugin.ApOutRootLrcPlugin;
import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.base.util.DialogUtils;
import com.pine.base.widget.dialog.CustomListDialog;
import com.pine.base.widget.dialog.SelectItemDialog;
import com.pine.player.applet.subtitle.bean.PineSubtitleBean;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.tool.util.ResourceUtils;

import java.util.ArrayList;
import java.util.List;

public class ApMainActivity extends BaseMvvmNoActionBarActivity<ApMainActivityBinding, ApMainVm> {
    private final int REQUEST_CODE_ADD_TO_SHEET = 1;
    private SelectItemDialog mTopMenuDialog;
    private CustomListDialog mTimingDialog;

    private int mCurSelectPosition = 0;

    private AudioPlayerView.IPlayerListener mPlayListener = new AudioPlayerView.IPlayerListener() {
        @Override
        public void onPlayMusic(String mediaCode, ApSheetMusic music, boolean isPlaying) {
            mViewModel.setPlayedMusic(music, isPlaying);
        }

        @Override
        public void onAlbumArtThemeChange(String mediaCode, ApSheetMusic music, int mainColor) {
            mViewModel.setMainThemeColor(mainColor);
            mBinding.subtitleContainerLl.setBackgroundColor(mainColor);
        }
    };

    private ApOutRootLrcPlugin.ILyricUpdateListener mLyricUpdateListener = new ApOutRootLrcPlugin.ILyricUpdateListener() {

        @Override
        public void updateLyricText(PineMediaPlayerBean mediaBean, List<PineSubtitleBean> allList,
                                    PineSubtitleBean curSubtitle, int position) {
            String text = "";
            if (curSubtitle != null) {
                text = curSubtitle.getTextBody();
                text = TextUtils.isEmpty(text) ? "" : text;
                if (curSubtitle.getTransBody() != null && !curSubtitle.getTransBody().isEmpty()) {
                    text += "<br />" + curSubtitle.getTransBody();
                }
            }
            mBinding.subtitleText.setText(Html.fromHtml(text));
        }

        @Override
        public void clearLyricText() {
            mBinding.subtitleText.setText("");
        }
    };

    @Override
    public void observeInitLiveData(Bundle savedInstanceState) {
        mViewModel.mPlayStateData.observe(this, new Observer<ApSheetMusic>() {
            @Override
            public void onChanged(ApSheetMusic music) {
                mBinding.setMusic(music);
                mBinding.playerView.updateMusicData(music);
            }
        });
        mViewModel.mIsLightThemeData.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLightTheme) {
                mBinding.alphaView.setBackground(mViewModel.mIsLightThemeData.getCustomData());
                mBinding.albumArtBgIv.setImageBitmap(mBinding.playerView.getBigAlbumArtBitmap());
                mBinding.setIsLightTheme(isLightTheme);
            }
        });
    }

    @Override
    public void observeSyncLiveData(int liveDataObjTag) {

    }

    @Override
    protected int getActivityLayoutResId() {
        return R.layout.ap_activity_main;
    }

    @Override
    protected void init(Bundle onCreateSavedInstanceState) {
        mBinding.setPresenter(new Presenter());
    }

    @Override
    protected void onRealResume() {
        super.onRealResume();
        ApAudioPlayerHelper.getInstance().attachPlayerViewFromGlobalController(this,
                mBinding.playerView, mPlayListener, mLyricUpdateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ApAudioPlayerHelper.getInstance().detachPlayerViewFromGlobalController(mBinding.playerView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_TO_SHEET) {
            mViewModel.refreshPlayMusic();
        }
    }

    public class Presenter {

        public void onGoBackClick(View view) {
            finish();
        }

        public void onTopMenuClick(View view) {
            if (mTopMenuDialog == null) {
                int[] menuImages = ResourceUtils.getResIdArray(ApMainActivity.this, R.array.ap_music_main_item_menu_img);
                String[] menuNames = getResources().getStringArray(R.array.ap_music_main_item_menu_name);
                mTopMenuDialog = DialogUtils.createItemSelectDialog(ApMainActivity.this,
                        mViewModel.mPlayStateData.getValue().getName(), menuImages, menuNames, new SelectItemDialog.IDialogSelectListener() {
                            @Override
                            public void onSelect(String selectText, int position) {
                                switch (position) {
                                    case 0:
                                        Intent intent = new Intent(ApMainActivity.this, ApAddMusicToSheetActivity.class);
                                        ArrayList<ApSheetMusic> list = new ArrayList<>();
                                        list.add(mViewModel.mPlayStateData.getValue());
                                        intent.putParcelableArrayListExtra("selectList", list);
                                        startActivityForResult(intent, REQUEST_CODE_ADD_TO_SHEET);
                                        break;
                                    case 1:
                                        showTimingDialog();
                                        break;
                                }
                            }
                        });
            }
            mTopMenuDialog.show();
        }
    }

    private void showTimingDialog() {
        mCurSelectPosition = 0;
        if (mTimingDialog == null) {
            String[] timingItemNames = getResources().getStringArray(R.array.ap_music_main_timing_item_name);
            final int[] timingItemValues = getResources().getIntArray(R.array.ap_music_main_timing_item_value);
            mTimingDialog = DialogUtils.createBottomCustomListDialog(this, getString(R.string.ap_mm_time_to_close),
                    R.layout.ap_item_main_timing_dialog, R.layout.ap_main_timing_dialog_action_layout,
                    timingItemNames, new CustomListDialog.IOnViewBindCallback<String>() {
                        @Override
                        public void onViewBind(View titleView, View actionView, CustomListDialog dialog) {
                            ApActionMainTimingDialogBinding binding = DataBindingUtil.bind(actionView);
                            binding.cancelBtnTv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mTimingDialog.dismiss();
                                }
                            });
                            binding.confirmBtnTv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mViewModel.startTimingWork(timingItemValues[mCurSelectPosition]);
                                    mTimingDialog.dismiss();
                                }
                            });
                        }

                        @Override
                        public void onItemViewUpdate(View itemView, final int position, String data, CustomListDialog dialog) {
                            final ApItemMainTimingDialogBinding binding = DataBindingUtil.bind(itemView);
                            binding.setText(data);
                            binding.setSelected(mCurSelectPosition == position);
                            binding.dividerView.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
                            binding.itemViewLl.setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mCurSelectPosition = position;
                                            binding.setSelected(true);
                                            mTimingDialog.getListAdapter().notifyDataSetChangedSafely();
                                        }
                                    }
                            );
                            binding.executePendingBindings();
                        }

                        @Override
                        public void onListDataChange(View titleView, View actionView, CustomListDialog dialog) {

                        }
                    });
        }
        mTimingDialog.show();
    }
}
