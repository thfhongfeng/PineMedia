package com.pine.base.widget.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.pine.base.R;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.viewholder.PineBackgroundViewHolder;
import com.pine.player.widget.viewholder.PineControllerViewHolder;
import com.pine.player.widget.viewholder.PineRightViewHolder;
import com.pine.player.widget.viewholder.PineWaitingProgressViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BaseSimpleAudioControllerAdapter extends PineMediaController.AbstractMediaControllerAdapter {
    private Context mContext;
    private ViewGroup mRoot;
    private PineBackgroundViewHolder mBackgroundViewHolder;
    private PineControllerViewHolder mControllerViewHolder;
    private RelativeLayout mBackgroundView;
    private ViewGroup mControllerView;
    private PineMediaWidget.IPineMediaPlayer mPlayer;
    private List<PineMediaPlayerBean> mMediaList = new ArrayList<>();
    private int mCurrentVideoPosition = -1;

    public BaseSimpleAudioControllerAdapter(Context context, PineMediaWidget.IPineMediaPlayer player, ViewGroup root) {
        mContext = context;
        mPlayer = player;
        mRoot = root;
    }

    public void setMediaList(List<PineMediaPlayerBean> list) {
        mMediaList = list;
        if (mMediaList == null) {
            mMediaList = new ArrayList<>();
        }
    }

    public void addMedia(PineMediaPlayerBean bean) {
        if (bean == null) {
            return;
        }
        mMediaList.add(0, bean);
    }

    public void addMediaList(List<PineMediaPlayerBean> list) {
        if (list == null && list.size() < 1) {
            return;
        }
        mMediaList.addAll(0, list);
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
        viewHolder.setPausePlayButton(root.findViewById(R.id.play_pause_btn));
//        SeekBar seekBar = root.findViewById(R.id.progress_bar);
//        viewHolder.setPlayProgressBar(seekBar);
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

    @Override
    protected PineMediaController.ControllersActionListener onCreateControllersActionListener() {
        return new PineMediaController.ControllersActionListener() {

        };
    }
}

