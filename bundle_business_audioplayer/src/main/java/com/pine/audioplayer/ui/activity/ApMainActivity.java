package com.pine.audioplayer.ui.activity;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;

import androidx.lifecycle.Observer;

import com.pine.audioplayer.R;
import com.pine.audioplayer.databinding.ApMainActivityBinding;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.manager.ApAudioPlayerHelper;
import com.pine.audioplayer.vm.ApMainVm;
import com.pine.audioplayer.widget.AudioPlayerView;
import com.pine.audioplayer.widget.plugin.ApOutRootLrcPlugin;
import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.player.applet.subtitle.bean.PineSubtitleBean;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.tool.util.ColorUtils;

import java.util.List;

public class ApMainActivity extends BaseMvvmNoActionBarActivity<ApMainActivityBinding, ApMainVm> {


    private AudioPlayerView.IPlayerListener mPlayListener = new AudioPlayerView.IPlayerListener() {
        @Override
        public void onPlayMusic(ApSheetMusic music, boolean isPlaying) {

        }

        @Override
        public void onAlbumArtThemeChange(ApSheetMusic music, int mainColor) {
            setupAlbumArtAndTheme(music);
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

    private void setupAlbumArtAndTheme(ApSheetMusic music) {
        if (music == null) {
            return;
        }
        int mainAlbumArtColor = mBinding.playerView.getMainAlbumArtColor();
        int[] alphaColor = {0x00000000, 0xff000000};
        alphaColor[0] = alphaColor[0] + mainAlbumArtColor & 0x00ffffff;
        alphaColor[1] = alphaColor[1] + mainAlbumArtColor & 0x00ffffff;

        mBinding.subtitleContainerLl.setBackgroundColor(mainAlbumArtColor);
        boolean isLightColor = ColorUtils.isLightColor(mainAlbumArtColor);
        mBinding.subtitleText.setTextColor(getResources().getColor(isLightColor ? R.color.dark_gray_black : R.color.white));
        mBinding.titleText.setTextColor(getResources().getColor(isLightColor ? R.color.dark_gray_black : R.color.white));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, alphaColor);
        mBinding.alphaView.setBackground(bg);
        mBinding.albumArtBgIv.setImageBitmap(mBinding.playerView.getBigAlbumArtBitmap());
    }
}
