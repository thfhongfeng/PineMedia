package com.pine.base.widget.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.pine.base.R;
import com.pine.base.widget.adapter.BaseSimpleAudioControllerAdapter;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.PineMediaPlayerView;
import com.pine.tool.ui.Activity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BaseSimpleAudioPlayerView extends RelativeLayout {
    private View mRoot;
    private PineMediaPlayerView mMediaPlayerView;
    private View controller_view, play_music_list_view;
    private PineMediaController mMediaController;
    private PineMediaWidget.IPineMediaPlayer mMediaPlayer;
    private BaseSimpleAudioControllerAdapter mControllerAdapter;

    public BaseSimpleAudioPlayerView(Context context) {
        super(context);
        initView();
    }

    public BaseSimpleAudioPlayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BaseSimpleAudioPlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mRoot = LayoutInflater.from(getContext()).inflate(R.layout.base_simple_audio_layout, this, true);
        mMediaPlayerView = mRoot.findViewById(R.id.player_view);
        controller_view = mRoot.findViewById(R.id.player_view);
        play_music_list_view = mRoot.findViewById(R.id.player_view);
    }

    public void init(String tag, Activity context) {
        mMediaController = new PineMediaController(getContext());
        mMediaPlayerView.init(tag, mMediaController);
        mMediaPlayer = mMediaPlayerView.getMediaPlayer();
        mMediaPlayer.setAutocephalyPlayMode(true);
        mControllerAdapter = new BaseSimpleAudioControllerAdapter(getContext(), mMediaPlayer,
                (ViewGroup) controller_view);
        mMediaController.setMediaControllerAdapter(mControllerAdapter);
    }

    public void playMediaList(@NonNull List<PineMediaPlayerBean> mediaBeanList) {
        if (mediaBeanList == null && mediaBeanList.size() < 1) {
            return;
        }
        mControllerAdapter.addMediaList(mediaBeanList);
        mControllerAdapter.onMediaSelect(mediaBeanList.get(0).getMediaCode(), true);
    }

    public void playMedia(@NonNull PineMediaPlayerBean mediaBean) {
        if (mediaBean == null) {
            return;
        }
        mControllerAdapter.addMedia(mediaBean);
        mControllerAdapter.onMediaSelect(mediaBean.getMediaCode(), true);
    }
}
