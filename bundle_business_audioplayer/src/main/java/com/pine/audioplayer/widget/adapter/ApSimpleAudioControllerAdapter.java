package com.pine.audioplayer.widget.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.pine.audioplayer.R;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.viewholder.PineBackgroundViewHolder;
import com.pine.player.widget.viewholder.PineControllerViewHolder;
import com.pine.player.widget.viewholder.PineRightViewHolder;
import com.pine.player.widget.viewholder.PineWaitingProgressViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;

public class ApSimpleAudioControllerAdapter extends PineMediaController.AbstractMediaControllerAdapter {
    private Context mContext;
    private ViewGroup mRoot;
    private PineBackgroundViewHolder mBackgroundViewHolder;
    private PineControllerViewHolder mControllerViewHolder;
    private RelativeLayout mBackgroundView;
    private ViewGroup mControllerView;
    private List<PineMediaPlayerBean> mMediaList = new LinkedList<>();
    private HashMap<String, PineMediaPlayerBean> mMediaCodeListMap = new HashMap<>();
    private String mCurrentMediaCode = "";

    public ApSimpleAudioControllerAdapter(Context context, ViewGroup root) {
        mContext = context;
        mRoot = root;
    }

    public String getCurMediaCode() {
        return mCurrentMediaCode;
    }

    public void setMediaList(List<PineMediaPlayerBean> list) {
        mMediaList = new ArrayList<>();
        mMediaCodeListMap = new HashMap<>();
        if (list == null || list.size() < 1) {
            mPlayer.release();
        } else {
            addMediaList(list);
        }
    }

    public List<PineMediaPlayerBean> getMediaList() {
        return mMediaList;
    }

    public void addMedia(PineMediaPlayerBean bean) {
        if (mMediaCodeListMap.containsKey(bean.getMediaCode())) {
            mMediaList.remove(mMediaCodeListMap.get(bean.getMediaCode()));
        }
        mMediaList.add(0, bean);
        mMediaCodeListMap.put(bean.getMediaCode(), bean);
    }

    public void addMediaList(List<PineMediaPlayerBean> list) {
        if (list == null) {
            return;
        }
        removeDuplicateBean(list);
        for (int i = list.size() - 1; i >= 0; i--) {
            PineMediaPlayerBean bean = list.get(i);
            mMediaList.add(0, bean);
            mMediaCodeListMap.put(bean.getMediaCode(), bean);
        }
    }

    public void removeMedia(PineMediaPlayerBean bean) {
        int position = findMediaPosition(mCurrentMediaCode);
        if (mMediaCodeListMap.containsKey(bean.getMediaCode())) {
            mMediaList.remove(mMediaCodeListMap.get(bean.getMediaCode()));
            mMediaCodeListMap.remove(bean.getMediaCode());
        }
        if (mMediaList.size() < 1) {
            mPlayer.release();
        } else {
            playMedia(position, mPlayer.isPlaying());
        }
    }

    private void removeDuplicateBean(@NonNull List<PineMediaPlayerBean> list) {
        List<PineMediaPlayerBean> duplicateItemList = new ArrayList<>();
        for (PineMediaPlayerBean bean : list) {
            if (mMediaCodeListMap.containsKey(bean.getMediaCode())) {
                duplicateItemList.add(mMediaCodeListMap.get(bean.getMediaCode()));
            }
        }
        mMediaList.removeAll(duplicateItemList);
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
        viewHolder.setNextButton(root.findViewById(R.id.next_btn));
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
        position = position % mMediaList.size();
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
            mCurrentMediaCode = mediaBean.getMediaCode();
            return true;
        }
        return false;
    }

    @Override
    protected PineMediaController.ControllersActionListener onCreateControllersActionListener() {
        return new PineMediaController.ControllersActionListener() {
            @Override
            public boolean onNextBtnClick(View nextBtn, PineMediaWidget.IPineMediaPlayer player) {
                PineMediaPlayerBean mediaPlayerBean = player.getMediaPlayerBean();
                if (mediaPlayerBean != null) {
                    onNextMediaSelect(mediaPlayerBean.getMediaCode(), true);
                    return true;
                }
                return false;
            }
        };
    }
}

