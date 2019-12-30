package com.pine.audioplayer.ui.activity;

import android.os.Bundle;
import android.text.Html;

import com.pine.audioplayer.R;
import com.pine.audioplayer.databinding.ApMainActivityBinding;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.manager.ApAudioPlayerHelper;
import com.pine.audioplayer.vm.ApMainVm;
import com.pine.audioplayer.widget.IAudioPlayerView;
import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.player.applet.subtitle.bean.PineSubtitleBean;
import com.pine.player.bean.PineMediaPlayerBean;

import androidx.lifecycle.Observer;

public class ApMainActivity extends BaseMvvmNoActionBarActivity<ApMainActivityBinding, ApMainVm> {

    private IAudioPlayerView.ILyricUpdateListener mLyricUpdateListener = new IAudioPlayerView.ILyricUpdateListener() {
        @Override
        public void updateLyricText(PineMediaPlayerBean mediaBean, PineSubtitleBean subtitle) {
            String text = "";
            if (subtitle != null) {
                text = subtitle.getTextBody();
                if (subtitle.getTransBody() != null && !subtitle.getTransBody().isEmpty()) {
                    text += "<br />" + subtitle.getTransBody();
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
        ApAudioPlayerHelper.getInstance().attachGlobalController(this, mBinding.playerView, mLyricUpdateListener);
    }
}
