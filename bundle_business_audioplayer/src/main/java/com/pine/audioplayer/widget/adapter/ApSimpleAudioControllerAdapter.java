package com.pine.audioplayer.widget.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.pine.audioplayer.R;
import com.pine.audioplayer.bean.ApPlayListType;
import com.pine.audioplayer.db.entity.ApSheetMusic;
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

public class ApSimpleAudioControllerAdapter extends PineMediaController.AbstractMediaControllerAdapter {
    private Context mContext;
    private ViewGroup mControllerView;
    private PineBackgroundViewHolder mBackgroundViewHolder;
    private PineControllerViewHolder mControllerViewHolder;
    private RelativeLayout mBackgroundView;

    private List<ApSheetMusic> mMusicList = new LinkedList<>();
    private HashMap<String, PineMediaPlayerBean> mCodeMediaListMap = new HashMap<>();
    private HashMap<String, ApSheetMusic> mCodeMusicListMap = new HashMap<>();
    private String mCurrentMediaCode = "";

    private List<ApPlayListType> mPlayTypeList;
    private int mCurPlayTypePos = 0;

    public ApSimpleAudioControllerAdapter(Context context) {
        mContext = context;
        mPlayTypeList = ApPlayListType.getDefaultList(mContext);
    }

    public ApSimpleAudioControllerAdapter(Context context, ViewGroup root) {
        mContext = context;
        mControllerView = root;
        mPlayTypeList = ApPlayListType.getDefaultList(mContext);
    }

    public void setControllerView(ViewGroup root) {
        mControllerView = root;
        mBackgroundViewHolder = null;
        mControllerViewHolder = null;
        mBackgroundView = null;
    }

    public List<ApSheetMusic> getMusicList() {
        return mMusicList;
    }

    public void setCurMediaCode(String curMediaCode) {
        mCurrentMediaCode = curMediaCode;
    }

    public String getCurMediaCode() {
        return mCurrentMediaCode;
    }

    public ApSheetMusic getCurMusic() {
        return mCodeMusicListMap.get(mCurrentMediaCode);
    }

    public ApSheetMusic getListedMusic(String mediaCode) {
        return mCodeMusicListMap.get(mediaCode);
    }

    public ApPlayListType getCurPlayType() {
        return mPlayTypeList.get(mCurPlayTypePos % mPlayTypeList.size());
    }

    public ApPlayListType getNextPlayType() {
        return mPlayTypeList.get((mCurPlayTypePos + 1) % mPlayTypeList.size());
    }

    public ApPlayListType goNextPlayType() {
        return mPlayTypeList.get((++mCurPlayTypePos) % mPlayTypeList.size());
    }

    public String getMediaCode(@NonNull ApSheetMusic music) {
        return music.getSongId() + "";
    }

    public PineMediaPlayerBean transferMediaBean(@NonNull ApSheetMusic music) {
        PineMediaPlayerBean mediaBean = new PineMediaPlayerBean(getMediaCode(music),
                music.getName(), Uri.parse(music.getFilePath()),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null,
                null, null);
        return mediaBean;
    }

    public void setMusicList(List<ApSheetMusic> list, boolean startPlay) {
        mMusicList = new ArrayList<>();
        mCodeMediaListMap = new HashMap<>();
        mCodeMusicListMap = new HashMap<>();
        if (list == null || list.size() < 1) {
            mPlayer.release();
            mPlayer.setPlayingMedia(null);
            mCurrentMediaCode = "";
        } else {
            addMusicList(list, startPlay);
        }
    }

    public void addMusic(ApSheetMusic music, boolean startPlay) {
        if (music == null) {
            return;
        }
        String mediaCode = getMediaCode(music);
        if (mCodeMusicListMap.containsKey(mediaCode)) {
            mMusicList.remove(mCodeMusicListMap.get(mediaCode));
        }
        PineMediaPlayerBean mediaBean = transferMediaBean(music);
        mMusicList.add(0, music);
        mCodeMusicListMap.put(mediaBean.getMediaCode(), music);
        mCodeMediaListMap.put(mediaBean.getMediaCode(), mediaBean);
        onMediaSelect(getMediaCode(music), startPlay);
    }

    public void addMusicList(List<ApSheetMusic> list, boolean startPlay) {
        if (list == null && list.size() < 1) {
            return;
        }
        for (ApSheetMusic music : list) {
            String mediaCode = getMediaCode(music);
            if (mCodeMusicListMap.containsKey(mediaCode)) {
                mMusicList.remove(mCodeMusicListMap.get(mediaCode));
            }
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            ApSheetMusic music = list.get(i);
            PineMediaPlayerBean mediaBean = transferMediaBean(music);
            mMusicList.add(0, music);
            mCodeMediaListMap.put(mediaBean.getMediaCode(), mediaBean);
            mCodeMusicListMap.put(mediaBean.getMediaCode(), music);
        }
        onMediaSelect(getMediaCode(mMusicList.get(0)), startPlay);
    }

    public void removeMusic(ApSheetMusic music) {
        int curPos = findMusicPosition(mCurrentMediaCode);
        String musicMediaCode = getMediaCode(music);
        if (!musicMediaCode.equals(mCurrentMediaCode)
                && findMusicPosition(getMediaCode(music)) < curPos) {
            curPos--;
        }
        if (mCodeMusicListMap.containsKey(musicMediaCode)) {
            mMusicList.remove(mCodeMusicListMap.get(musicMediaCode));
            mCodeMediaListMap.remove(musicMediaCode);
            mCodeMusicListMap.remove(musicMediaCode);
        }
        if (mMusicList.size() < 1) {
            mPlayer.release();
            mPlayer.setPlayingMedia(null);
            mCurrentMediaCode = "";
        } else {
            playMedia(curPos, mPlayer.isPlaying());
        }
    }

    private void refreshPreNextBtnState(int curPosition) {
        if (mControllerViewHolder != null) {
            if (mControllerViewHolder.getPrevButton() != null) {
                mControllerViewHolder.getPrevButton().setEnabled(curPosition > 0);
            }
            if (mControllerViewHolder.getNextButton() != null) {
                mControllerViewHolder.getNextButton().setEnabled(curPosition < mMusicList.size() - 1);
            }
        }
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
        viewHolder.setPausePlayButton(root.findViewById(R.id.sapv_play_pause_btn));
        viewHolder.setNextButton(root.findViewById(R.id.sapv_next_btn));
//        SeekBar seekBar = root.findViewById(R.id.progress_bar);
//        viewHolder.setPlayProgressBar(seekBar);
    }

    @Override
    public PineControllerViewHolder onCreateOutRootControllerViewHolder(
            PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode) {
        if (mControllerViewHolder == null) {
            mControllerViewHolder = new PineControllerViewHolder();
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


    private int findMusicPosition(String mediaCode) {
        if (mMusicList != null && mMusicList.size() > 0) {
            for (int i = 0; i < mMusicList.size(); i++) {
                if (mediaCode.equals(getMediaCode(mMusicList.get(i)))) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public boolean onPreMediaSelect(@NonNull String curMediaCode, boolean startPlay) {
        int position = findMusicPosition(curMediaCode);
        if (position == -1) {
            return false;
        }
        position--;
        return playMedia(position, startPlay);
    }

    @Override
    public boolean onNextMediaSelect(@NonNull String curMediaCode, boolean startPlay) {
        int position = findMusicPosition(curMediaCode);
        if (position == -1) {
            return false;
        }
        position++;
        return playMedia(position, startPlay);
    }

    @Override
    public boolean onMediaSelect(String mediaCode, boolean startPlay) {
        int position = findMusicPosition(mediaCode);
        if (position == -1) {
            return false;
        }
        return playMedia(position, startPlay);
    }

    private boolean playMedia(int position, boolean startPlay) {
        position = position % mMusicList.size();
        refreshPreNextBtnState(position);
        if (mPlayer != null) {
            if (position >= 0 && position < mMusicList.size()) {
                String mediaCode = getMediaCode(mMusicList.get(position));
                if (mCurrentMediaCode != mediaCode) {
                    mPlayer.setPlayingMedia(mCodeMediaListMap.get(mediaCode));
                }
                if (startPlay) {
                    mPlayer.start();
                }
                mCurrentMediaCode = mediaCode;
                return true;
            }
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

