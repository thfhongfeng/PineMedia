package com.pine.audioplayer.ui.activity;

import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;

import androidx.lifecycle.Observer;

import com.pine.audioplayer.R;
import com.pine.audioplayer.databinding.ApMainActivityBinding;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.manager.ApAudioPlayerHelper;
import com.pine.audioplayer.vm.ApMainVm;
import com.pine.audioplayer.widget.plugin.ApOutRootLrcPlugin;
import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.player.applet.subtitle.bean.PineSubtitleBean;
import com.pine.player.bean.PineMediaPlayerBean;

import java.util.List;

public class ApMainActivity extends BaseMvvmNoActionBarActivity<ApMainActivityBinding, ApMainVm> {

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
        ApAudioPlayerHelper.getInstance().attachGlobalController(this, mBinding.playerView, mLyricUpdateListener);
    }
}
