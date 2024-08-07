package com.pine.media.audio.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;

import com.pine.media.audio.R;
import com.pine.media.audio.databinding.ApActionMainTimingDialogBinding;
import com.pine.media.audio.databinding.ApItemMainTimingDialogBinding;
import com.pine.media.audio.databinding.ApMainActivityBinding;
import com.pine.media.audio.db.entity.ApMusic;
import com.pine.media.audio.manager.ApAudioPlayerHelper;
import com.pine.media.audio.vm.ApMainVm;
import com.pine.media.audio.widget.AudioPlayerView;
import com.pine.media.audio.widget.plugin.ApOutRootLrcPlugin;
import com.pine.player.applet.subtitle.bean.PineSubtitleBean;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.component.PinePlayState;
import com.pine.template.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.template.base.util.DialogUtils;
import com.pine.template.base.widget.dialog.CustomListDialog;
import com.pine.template.base.widget.dialog.SelectItemDialog;
import com.pine.tool.permission.PermissionsAnnotation;
import com.pine.tool.util.ColorUtils;
import com.pine.tool.util.ResourceUtils;

import java.util.ArrayList;
import java.util.List;

@PermissionsAnnotation(Permissions = {Manifest.permission.MANAGE_EXTERNAL_STORAGE})
public class ApMainActivity extends BaseMvvmNoActionBarActivity<ApMainActivityBinding, ApMainVm> {
    private final int REQUEST_CODE_ADD_TO_SHEET = 1;
    private SelectItemDialog mTopMenuDialog;
    private CustomListDialog mTimingDialog;

    private int mCurSelectPosition = 0;

    private AudioPlayerView.IPlayerViewListener mPlayListener = new AudioPlayerView.IPlayerViewListener() {
        @Override
        public void onPlayMusic(PineMediaWidget.IPineMediaPlayer mPlayer, @Nullable ApMusic newMusic) {
            mViewModel.setPlayedMusic(newMusic, mPlayer != null && mPlayer.isPlaying());
        }

        @Override
        public void onPlayStateChange(ApMusic music, PinePlayState fromState, PinePlayState toState) {

        }

        @Override
        public void onAlbumArtChange(String mediaCode, ApMusic music, Bitmap smallBitmap,
                                     Bitmap bigBitmap, int mainColor) {
            int[] alphaColor = {0xff000000, 0x00000000};
            alphaColor[0] = alphaColor[0] | (mainColor & 0x00ffffff);
            alphaColor[1] = alphaColor[1] | (mainColor & 0x00ffffff);
            GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, alphaColor);
            mBinding.alphaView.setBackground(bg);
            mBinding.setIsLightTheme(ColorUtils.isLightColor(mainColor));
            mBinding.subtitleContainerLl.setBackgroundColor(mainColor);
            mBinding.albumArtBgIv.setImageBitmap(bigBitmap);
            mBinding.executePendingBindings();
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
        mViewModel.mPlayMusicStateData.observe(this, new Observer<ApMusic>() {
            @Override
            public void onChanged(ApMusic music) {
                if (music == null) {
                    finish();
                    return;
                }
                mBinding.setMusic(music);
                mBinding.playerView.updateMusicData(music);
            }
        });
        mViewModel.mInitMusicStateData.observe(this, new Observer<ApMusic>() {
            @Override
            public void onChanged(ApMusic music) {
                if (music == null) {
                    finish();
                    return;
                }
                mBinding.setMusic(music);
                ApAudioPlayerHelper.getInstance().playMusic(mBinding.playerView, music, mViewModel.mInitMusicStateData.getCustomData());
            }
        });
        mViewModel.mPlayListSheetIdData.observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long sheetId) {
                mBinding.setSheetId(sheetId);
                mBinding.playerView.enableMediaList(sheetId > 0);
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
        ApAudioPlayerHelper.getInstance().initPlayerView(mBinding.playerView);
    }

    @Override
    protected void onRealResume() {
        super.onRealResume();
        ApAudioPlayerHelper.getInstance().attachPlayerView(mBinding.playerView, mPlayListener, mLyricUpdateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ApAudioPlayerHelper.getInstance().detachPlayerView(mBinding.playerView);
    }

    @Override
    protected void onDestroy() {
        if (mViewModel.mInitPlayerMode) {
            ApAudioPlayerHelper.getInstance().destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_TO_SHEET) {
            mViewModel.refreshPlayMusic(this);
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
                        mViewModel.mPlayMusicStateData.getValue().getName(), menuImages, menuNames,
                        new SelectItemDialog.IDialogSelectListener() {
                            @Override
                            public void onSelect(String selectText, int position) {
                                switch (position) {
                                    case 0:
                                        Intent intent = new Intent(ApMainActivity.this, ApAddMusicToSheetActivity.class);
                                        ArrayList<ApMusic> list = new ArrayList<>();
                                        list.add(mViewModel.mPlayMusicStateData.getValue());
                                        intent.putExtra("selectList", list);
                                        startActivityForResult(intent, REQUEST_CODE_ADD_TO_SHEET);
                                        break;
                                    case 1:
                                        showTimingDialog();
                                        break;
                                }
                            }

                            @Override
                            public void onCancel() {

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
