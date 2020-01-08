package com.pine.audioplayer.ui.activity;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.Observer;

import com.pine.audioplayer.R;
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
import com.pine.tool.util.ColorUtils;
import com.pine.tool.util.ResourceUtils;

import java.util.ArrayList;
import java.util.List;

public class ApMainActivity extends BaseMvvmNoActionBarActivity<ApMainActivityBinding, ApMainVm> {
    private final int REQUEST_CODE_ADD_TO_SHEET = 1;
    private SelectItemDialog mTopMenuDialog;
    private CustomListDialog mTimingDialog;
    private boolean mIsLightTheme;

    private AudioPlayerView.IPlayerListener mPlayListener = new AudioPlayerView.IPlayerListener() {
        @Override
        public void onPlayMusic(String mediaCode, ApSheetMusic music, boolean isPlaying) {
            mViewModel.setPlayedMusic(music, isPlaying);
        }

        @Override
        public void onAlbumArtThemeChange(String mediaCode, ApSheetMusic music, int mainColor) {
            mIsLightTheme = ColorUtils.isLightColor(mainColor);
            setupAlbumArtAndTheme(music, mainColor);
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

    private void setupAlbumArtAndTheme(ApSheetMusic music, int mainAlbumArtColor) {
        if (music == null) {
            return;
        }
        int[] alphaColor = {0xff000000, 0x00000000};
        alphaColor[0] = alphaColor[0] | (mainAlbumArtColor & 0x00ffffff);
        alphaColor[1] = alphaColor[1] | (mainAlbumArtColor & 0x00ffffff);

        mBinding.subtitleContainerLl.setBackgroundColor(mainAlbumArtColor);
        mBinding.titleText.setTextColor(getResources().getColor(mIsLightTheme ? R.color.dark_gray_black : R.color.white));
        mBinding.authorText.setTextColor(getResources().getColor(mIsLightTheme ? R.color.dark_gray_black : R.color.white));
        mBinding.subtitleText.setTextColor(getResources().getColor(mIsLightTheme ? R.color.dark_gray_black : R.color.white));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, alphaColor);
        mBinding.alphaView.setBackground(bg);
        mBinding.albumArtBgIv.setImageBitmap(mBinding.playerView.getBigAlbumArtBitmap());
        mBinding.setIsLightTheme(mIsLightTheme);
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
        if (mTimingDialog == null) {
            String[] mTimingItemNames = getResources().getStringArray(R.array.ap_music_main_timing_item_name);
            List<Integer> mTimingItemImages = ResourceUtils.getResIdList(this, R.array.ap_music_main_timing_item_img);
            int[] mTimingItemValues = getResources().getIntArray(R.array.ap_music_main_timing_item_value);
            mTimingDialog = DialogUtils.createBottomCustomListDialog(this, getString(R.string.ap_mm_time_to_close),
                    R.layout.ap_item_main_timing_dialog, R.layout.ap_main_timing_dialog_action_layout,
                    mTimingItemImages, new CustomListDialog.IOnViewBindCallback<Integer>() {
                        @Override
                        public void onViewBind(View titleView, View actionView, CustomListDialog dialog) {

                        }

                        @Override
                        public void onItemViewUpdate(View itemView, int position, Integer data, CustomListDialog dialog) {

                        }

                        @Override
                        public void onListDataChange(View titleView, View actionView, CustomListDialog dialog) {

                        }
                    });
        }
        mTimingDialog.show();
    }
}
