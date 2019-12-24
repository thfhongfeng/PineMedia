package com.pine.audioplayer.adapter;

import android.content.Context;
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

import androidx.annotation.NonNull;

public class ApAudioControllerAdapter extends PineMediaController.AbstractMediaControllerAdapter {
    private Context mContext;
    private ViewGroup mRoot;
    private PineBackgroundViewHolder mBackgroundViewHolder;
    private PineControllerViewHolder mControllerViewHolder;
    private RelativeLayout mBackgroundView;
    private ViewGroup mControllerView;
    private PineMediaWidget.IPineMediaPlayer mPlayer;
    private List<PineMediaPlayerBean> mMediaList;
    private String mCurrentMediaCode = "";
    private int mCurrentMediaPos = -1;

    public ApAudioControllerAdapter(Context context, PineMediaWidget.IPineMediaPlayer player, ViewGroup root) {
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

    private int findMediaPosition(String mediaCode) {
        if (mMediaList != null && mMediaList.size() > 0) {
            for (int i = 0; i < mMediaList.size(); i++) {
                if (mediaCode.equals(mMediaList.get(i).getMediaCode())) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public boolean onPreMediaSelect(@NonNull String curMediaCode, boolean startPlay) {
        int position = findMediaPosition(curMediaCode);
        if (position == -1) {
            return false;
        }
        position--;
        return playMedia(position, startPlay);
    }

    @Override
    public boolean onNextMediaSelect(@NonNull String curMediaCode, boolean startPlay) {
        int position = findMediaPosition(curMediaCode);
        if (position == -1) {
            return false;
        }
        position++;
        return playMedia(position, startPlay);
    }

    @Override
    public boolean onMediaSelect(String mediaCode, boolean startPlay) {
        int position = findMediaPosition(mediaCode);
        if (position == -1) {
            return false;
        }
        return playMedia(position, startPlay);
    }

    private boolean playMedia(int position, boolean startPlay) {
        if (mControllerViewHolder != null) {
            if (mControllerViewHolder.getPrevButton() != null) {
                mControllerViewHolder.getPrevButton().setEnabled(position > 0);
            }
            if (mControllerViewHolder.getNextButton() != null) {
                mControllerViewHolder.getNextButton().setEnabled(position < mMediaList.size() - 1);
            }
        }
        if (position >= 0 && position < mMediaList.size()) {
            PineMediaPlayerBean mediaBean = mMediaList.get(position);
            if (mCurrentMediaCode != mediaBean.getMediaCode()) {
                mPlayer.setPlayingMedia(mediaBean);
            }
            if (startPlay) {
                mPlayer.start();
            }
            mCurrentMediaPos = position;
            mCurrentMediaCode = mediaBean.getMediaCode();
            return true;
        }
        return false;
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

