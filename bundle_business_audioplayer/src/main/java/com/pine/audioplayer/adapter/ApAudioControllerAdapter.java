package com.pine.audioplayer.adapter;

import android.app.Activity;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pine.audioplayer.R;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.viewholder.PineBackgroundViewHolder;
import com.pine.player.widget.viewholder.PineControllerViewHolder;
import com.pine.player.widget.viewholder.PineRightViewHolder;
import com.pine.player.widget.viewholder.PineWaitingProgressViewHolder;

import java.io.File;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class ApAudioControllerAdapter extends PineMediaController.AbstractMediaControllerAdapter {
    private Activity mContext;
    private ViewGroup mRoot;
    private PineBackgroundViewHolder mBackgroundViewHolder;
    private PineControllerViewHolder mControllerViewHolder;
    private RelativeLayout mBackgroundView;
    private ViewGroup mControllerView;
    private PineMediaWidget.IPineMediaPlayer mPlayer;
    private List<PineMediaPlayerBean> mMediaList;
    private int mCurrentVideoPosition = -1;

    public ApAudioControllerAdapter(Activity context, PineMediaWidget.IPineMediaPlayer player, ViewGroup root) {
        mContext = context;
        mPlayer = player;
        mRoot = root;
    }

    public void setMediaList(List<PineMediaPlayerBean> mediaList) {
        mMediaList = mediaList;
    }

    @Override
    protected final PineBackgroundViewHolder onCreateBackgroundViewHolder(
            PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode) {
        if (mBackgroundViewHolder == null) {
            mBackgroundViewHolder = new PineBackgroundViewHolder();
            if (mBackgroundView == null) {
                PineMediaPlayerBean playerBean = player.getMediaPlayerBean();
                Uri imgUri = playerBean == null ? null : playerBean.getMediaImgUri();
                ImageView mediaBackgroundView = new ImageView(mContext);
                mediaBackgroundView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (imgUri == null) {
                    mediaBackgroundView.setBackgroundResource(android.R.color.darker_gray);
                } else {
                    mediaBackgroundView.setImageURI(Uri.fromFile(new File(imgUri.getPath())));
                }
                mBackgroundView = new RelativeLayout(mContext);
                mBackgroundView.addView(mediaBackgroundView,
                        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
                mBackgroundViewHolder.setContainer(mBackgroundView);
            }
        }
        mBackgroundViewHolder.setContainer(mBackgroundView);
        return mBackgroundViewHolder;
    }

    @Override
    protected final PineControllerViewHolder onCreateInRootControllerViewHolder(
            PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode) {
        return null;
    }

    private final void initControllerViewHolder(
            PineControllerViewHolder viewHolder, View root) {
        viewHolder.setPausePlayButton(root.findViewById(R.id.audio_play_pause_btn));
        SeekBar seekBar = root.findViewById(R.id.audio_progress_bar);
        viewHolder.setPlayProgressBar(seekBar);
        viewHolder.setEndTimeText(root.findViewById(R.id.audio_end_time_tv));
        viewHolder.setCurrentTimeText(root.findViewById(R.id.audio_cur_time_tv));
    }

    @Override
    public PineControllerViewHolder onCreateOutRootControllerViewHolder(
            PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode) {
        if (mControllerViewHolder == null) {
            mControllerViewHolder = new PineControllerViewHolder();
            if (mControllerView == null) {
                mControllerView = mRoot;
            }
            initControllerViewHolder(mControllerViewHolder, mControllerView);
        }
        mControllerViewHolder.setContainer(mControllerView);
        return mControllerViewHolder;
    }

    @Override
    protected final PineWaitingProgressViewHolder onCreateWaitingProgressViewHolder(
            PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode) {
        return null;
    }

    @Override
    protected List<PineRightViewHolder> onCreateRightViewHolderList(
            PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode) {
        return null;
    }

    protected PineMediaController.ControllerMonitor onCreateControllerMonitor() {
        return new PineMediaController.ControllerMonitor() {
            public boolean onCurrentTimeUpdate(PineMediaWidget.IPineMediaPlayer player,
                                               View currentTimeText, int currentTime) {
                if (currentTimeText instanceof TextView) {
                    ((TextView) currentTimeText).setText(stringForTime(currentTime));
                }
                return true;
            }

            public boolean onEndTimeUpdate(PineMediaWidget.IPineMediaPlayer player,
                                           View endTimeText, int endTime) {
                if (endTimeText instanceof TextView) {
                    ((TextView) endTimeText).setText(stringForTime(player.getDuration()));
                }
                return true;
            }
        };
    }

    @Override
    public boolean mediaSelect(int position, boolean startPlay) {
        PineMediaPlayerBean pineMediaPlayerBean = null;
        if (mMediaList != null && mMediaList.size() > 0) {
            if (position >= 0 && position < mMediaList.size()) {
                pineMediaPlayerBean = mMediaList.get(position);
            } else {
                return false;
            }
        } else {
            pineMediaPlayerBean = mPlayer.getMediaPlayerBean();
        }
        if (mCurrentVideoPosition != position) {
            mPlayer.setPlayingMedia(pineMediaPlayerBean);
        }
        if (startPlay) {
            mPlayer.start();
        }
        mCurrentVideoPosition = position;
        return true;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
        formatBuilder.setLength(0);
        if (hours > 0) {
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @Override
    protected PineMediaController.ControllersActionListener onCreateControllersActionListener() {
        return new PineMediaController.ControllersActionListener() {

        };
    }
}

