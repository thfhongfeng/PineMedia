package com.pine.audioplayer.widget.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.pine.audioplayer.ApConstants;
import com.pine.audioplayer.R;
import com.pine.audioplayer.bean.ApPlayListType;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.widget.AudioPlayerView;
import com.pine.audioplayer.widget.plugin.ApOutRootLrcPlugin;
import com.pine.player.PineConstants;
import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.view.PineProgressBar;
import com.pine.player.widget.viewholder.PineBackgroundViewHolder;
import com.pine.player.widget.viewholder.PineControllerViewHolder;
import com.pine.player.widget.viewholder.PineRightViewHolder;
import com.pine.player.widget.viewholder.PineWaitingProgressViewHolder;
import com.pine.tool.util.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class ApAudioControllerAdapter extends PineMediaController.AbstractMediaControllerAdapter {
    private Context mContext;
    private ViewGroup mControllerView;
    private PineBackgroundViewHolder mBackgroundViewHolder;
    private PineControllerViewHolder mControllerViewHolder;
    private RelativeLayout mBackgroundView;

    public PineMediaWidget.IPineMediaPlayer mPlayer;

    private List<ApSheetMusic> mMusicList = new LinkedList<>();
    private HashMap<String, PineMediaPlayerBean> mCodeMediaListMap = new HashMap<>();
    private HashMap<String, ApSheetMusic> mCodeMusicListMap = new HashMap<>();
    private String mCurrentMediaCode = "";

    private List<ApPlayListType> mPlayTypeList;
    private int mCurPlayTypePos = 0;

    private boolean mReleaseAfterComplete;

    private AudioPlayerView.IPlayerViewListener mPlayerViewListener;
    private ApOutRootLrcPlugin.ILyricUpdateListener mLyricUpdateListener;

    private PineMediaWidget.PineMediaPlayerListener mPlayerListener = new PineMediaWidget.PineMediaPlayerListener() {
        @Override
        public boolean onComplete(PineMediaPlayerBean playerBean) {
            if (mReleaseAfterComplete) {
                release(true);
                mReleaseAfterComplete = false;
            } else {
                switch (getCurPlayType().getType()) {
                    case ApPlayListType.TYPE_ORDER:
                        onNextMediaSelect(mCurrentMediaCode, true);
                        break;
                    case ApPlayListType.TYPE_ALL_LOOP:
                        onNextMediaSelect(mCurrentMediaCode, true);
                        break;
                    case ApPlayListType.TYPE_SING_LOOP:
                        onMediaSelect(mCurrentMediaCode, true);
                        break;
                    case ApPlayListType.TYPE_RANDOM:
                        int randomPos = new Random().nextInt(10000) % mMusicList.size();
                        onMediaSelect(getMediaCode(mMusicList.get(randomPos)), true);
                        break;
                }
            }
            return false;
        }
    };

    public ApAudioControllerAdapter(Context context) {
        mContext = context;
        mPlayTypeList = ApPlayListType.getDefaultList(mContext);
    }

    public ApAudioControllerAdapter(Context context, ViewGroup root) {
        mContext = context;
        mControllerView = root;
        mPlayTypeList = ApPlayListType.getDefaultList(mContext);
    }

    public void setupPlayer(PineMediaWidget.IPineMediaPlayer player) {
        mPlayer = player;
        mPlayer.addMediaPlayerListener(mPlayerListener);
    }

    public void cancelDelayRelease() {
        mReleaseAfterComplete = false;
    }

    /*
     * @param immediately  true:立即停止播放, false:播放完当前内容后停止
     */
    public void release(boolean immediately) {
        LogUtils.d(TAG, "release player immediately:" + immediately);
        if (immediately) {
            mPlayer.release();
        } else {
            mReleaseAfterComplete = true;
        }
    }

    public void destroy() {
        mPlayer.removeMediaPlayerListener(mPlayerListener);
        setMusicList(null, false);
    }

    public void setControllerView(ViewGroup root, List<ApPlayListType> playTypeList) {
        mPlayTypeList = playTypeList;
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

    public ApPlayListType getAndGoNextPlayType() {
        ApPlayListType apPlayListType = mPlayTypeList.get((++mCurPlayTypePos) % mPlayTypeList.size());
        refreshPreNextBtnState();
        return apPlayListType;
    }

    public String getMediaCode(@NonNull ApSheetMusic music) {
        return music.getSongId() + "";
    }

    public void setPlayerViewListener(@NonNull AudioPlayerView.IPlayerViewListener playerViewListener) {
        mPlayerViewListener = playerViewListener;
    }

    public ApSheetMusic onLyricDownloaded(String mediaCode, String filePath, String charset) {
        ApSheetMusic music = mCodeMusicListMap.get(mediaCode);
        if (music == null) {
            return null;
        }
        music.setLyricFilePath(filePath);
        music.setLyricCharset(charset);
        PineMediaPlayerBean bean = mCodeMediaListMap.get(mediaCode);
        HashMap<Integer, IPinePlayerPlugin> pluginHashMap = bean.getPlayerPluginMap();
        if (pluginHashMap != null) {
            IPinePlayerPlugin plugin = pluginHashMap.get(ApConstants.PLUGIN_LRC_SUBTITLE);
            if (plugin != null && plugin instanceof ApOutRootLrcPlugin) {
                ((ApOutRootLrcPlugin) plugin).setSubtitle(filePath, PineConstants.PATH_STORAGE,
                        music.getLyricCharset());
            }
        }
        return music;
    }

    public void setLyricUpdateListener(@NonNull ApOutRootLrcPlugin.ILyricUpdateListener lyricUpdateListener) {
        mLyricUpdateListener = lyricUpdateListener;
        if (mCodeMediaListMap != null && mCodeMediaListMap.size() > 0) {
            Iterator<Map.Entry<String, PineMediaPlayerBean>> iterator = mCodeMediaListMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, PineMediaPlayerBean> entity = iterator.next();
                HashMap<Integer, IPinePlayerPlugin> pluginHashMap = entity.getValue().getPlayerPluginMap();
                if (pluginHashMap != null) {
                    IPinePlayerPlugin plugin = pluginHashMap.get(ApConstants.PLUGIN_LRC_SUBTITLE);
                    if (plugin != null && plugin instanceof ApOutRootLrcPlugin) {
                        ((ApOutRootLrcPlugin) plugin).setLyricUpdateListener(mLyricUpdateListener);
                    }
                }
            }
        }
    }

    public PineMediaPlayerBean transferMediaBean(@NonNull ApSheetMusic music) {
        PineMediaPlayerBean mediaBean = new PineMediaPlayerBean(getMediaCode(music),
                music.getName(), Uri.parse(music.getFilePath()),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null,
                null, null);
        HashMap<Integer, IPinePlayerPlugin> pluginHashMap = new HashMap<>();
        ApOutRootLrcPlugin playerPlugin = new ApOutRootLrcPlugin(mContext, music.getLyricFilePath(),
                music.getLyricCharset());
        playerPlugin.setLyricUpdateListener(mLyricUpdateListener);
        pluginHashMap.put(ApConstants.PLUGIN_LRC_SUBTITLE, playerPlugin);
        mediaBean.setPlayerPluginMap(pluginHashMap);
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

    public void updateMusicData(ApSheetMusic music) {
        if (music == null) {
            return;
        }
        String mediaCode = getMediaCode(music);
        if (mCodeMusicListMap.containsKey(mediaCode)) {
            ApSheetMusic listedMusic = mCodeMusicListMap.get(mediaCode);
            if (listedMusic != null) {
                if (listedMusic.mediaInfoChange(music)) {
                    PineMediaPlayerBean mediaBean = transferMediaBean(music);
                    mCodeMediaListMap.put(mediaBean.getMediaCode(), mediaBean);
                }
                listedMusic.copyDataFrom(music);
            }
        }
    }

    public void updateMusicListData(List<ApSheetMusic> list) {
        if (list == null && list.size() < 1) {
            return;
        }
        for (ApSheetMusic music : list) {
            updateMusicData(music);
        }
    }

    public void removeMusic(ApSheetMusic music) {
        int curPos = findMusicPosition(mCurrentMediaCode);
        String removeMediaCode = getMediaCode(music);
        if (mCodeMusicListMap.containsKey(removeMediaCode)) {
            mMusicList.remove(mCodeMusicListMap.get(removeMediaCode));
            mCodeMediaListMap.remove(removeMediaCode);
            mCodeMusicListMap.remove(removeMediaCode);
        }
        if (mMusicList.size() < 1) {
            mPlayer.release();
            mPlayer.setPlayingMedia(null);
            mCurrentMediaCode = "";
        } else {
            if (removeMediaCode.equals(mCurrentMediaCode)) {
                playMedia(curPos, mPlayer.isPlaying());
            }
        }
    }

    public void refreshPreNextBtnState() {
        int position = findMusicPosition(mCurrentMediaCode);
        if (mControllerViewHolder != null) {
            if (mControllerViewHolder.getPrevButton() != null) {
                mControllerViewHolder.getPrevButton().setEnabled(isLoopMode() || position > 0);
            }
            if (mControllerViewHolder.getNextButton() != null) {
                mControllerViewHolder.getNextButton().setEnabled(isLoopMode() || position < mMusicList.size() - 1 && position >= 0);
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
        if (root.findViewById(R.id.player_play_pause_btn) != null) {
            viewHolder.setPausePlayButton(root.findViewById(R.id.player_play_pause_btn));
        }
        if (root.findViewById(R.id.player_next_btn) != null) {
            viewHolder.setNextButton(root.findViewById(R.id.player_next_btn));
        }
        if (root.findViewById(R.id.player_pre_btn) != null) {
            viewHolder.setPrevButton(root.findViewById(R.id.player_pre_btn));
        }
        if (root.findViewById(R.id.player_progress_bar) != null) {
            View progressBarView = root.findViewById(R.id.player_progress_bar);
            if (progressBarView instanceof PineProgressBar) {
                viewHolder.setCustomProgressBar((PineProgressBar) progressBarView);
            } else if (progressBarView instanceof ProgressBar) {
                viewHolder.setPlayProgressBar((ProgressBar) progressBarView);
            }
        }
        if (root.findViewById(R.id.player_cur_time_tv) != null) {
            viewHolder.setCurrentTimeText(root.findViewById(R.id.player_cur_time_tv));
        }
        if (root.findViewById(R.id.player_end_time_tv) != null) {
            viewHolder.setEndTimeText(root.findViewById(R.id.player_end_time_tv));
        }
    }

    @Override
    public PineControllerViewHolder onCreateOutRootControllerViewHolder(
            PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode) {
        if (mControllerViewHolder == null) {
            mControllerViewHolder = new PineControllerViewHolder();
            initControllerViewHolder(mControllerViewHolder, mControllerView);
        }
        mControllerViewHolder.setContainer(mControllerView);
        refreshPreNextBtnState();
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

    private boolean isLoopMode() {
        int curPlayType = getCurPlayType().getType();
        if (curPlayType == ApPlayListType.TYPE_ALL_LOOP ||
                curPlayType == ApPlayListType.TYPE_RANDOM) {
            return true;
        }
        return false;
    }

    private boolean playMedia(int position, boolean startPlay) {
        if (isLoopMode()) {
            position = position % mMusicList.size();
        }
        if (position >= 0 && position < mMusicList.size()) {
            if (mPlayer != null) {
                ApSheetMusic music = mMusicList.get(position);
                String mediaCode = getMediaCode(music);
                PineMediaPlayerBean bean = mCodeMediaListMap.get(mediaCode);
                if (mCurrentMediaCode != mediaCode) {
                    mPlayer.setPlayingMedia(bean);
                }
                if (startPlay) {
                    mPlayer.start();
                }
                String oldMediaCode = mCurrentMediaCode;
                mCurrentMediaCode = mediaCode;
                if (mPlayerViewListener != null) {
                    mPlayerViewListener.onPlayMusic(mPlayer, mCodeMusicListMap.get(oldMediaCode), music);
                }
                refreshPreNextBtnState();
                return true;
            }
        }
        refreshPreNextBtnState();
        return false;
    }

    @Override
    protected PineMediaController.ControllersActionListener onCreateControllersActionListener() {
        return new PineMediaController.ControllersActionListener() {
            @Override
            public boolean onPreBtnClick(View preBtn, PineMediaWidget.IPineMediaPlayer player) {
                PineMediaPlayerBean mediaPlayerBean = player.getMediaPlayerBean();
                if (mediaPlayerBean != null) {
                    onPreMediaSelect(mediaPlayerBean.getMediaCode(), true);
                    return true;
                }
                return false;
            }

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
}

