package com.pine.media.videoplayer.widget.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pine.player.PinePlayerConstants;
import com.pine.player.R;
import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.applet.subtitle.plugin.PineLrcParserPlugin;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.bean.PineMediaUriSource;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.component.PinePlayState;
import com.pine.player.util.LogUtils;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.view.AdvanceDecoration;
import com.pine.player.widget.viewholder.PineBackgroundViewHolder;
import com.pine.player.widget.viewholder.PineControllerViewHolder;
import com.pine.player.widget.viewholder.PineRightViewHolder;
import com.pine.player.widget.viewholder.PineWaitingProgressViewHolder;
import com.pine.tool.util.CharsetUtils;
import com.pine.media.videoplayer.VpConstants;
import com.pine.media.videoplayer.bean.VpPlayListType;
import com.pine.media.videoplayer.db.entity.VpVideo;
import com.pine.media.videoplayer.util.VpLocalVideoUtils;
import com.pine.media.videoplayer.widget.VideoPlayerView;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class VpVideoControllerAdapter extends PineMediaController.AbstractMediaControllerAdapter {
    private Context mContext;
    private ViewGroup mControllerView, mFullControllerView;
    private PineControllerViewHolder mControllerViewHolder, mFullControllerViewHolder;
    private RelativeLayout mBackgroundView;
    private PineBackgroundViewHolder mBackgroundViewHolder;
    private LinearLayout mWaitingProgressView;
    private PineWaitingProgressViewHolder mWaitingProgressViewHolder;

    private ViewGroup mDefinitionListContainerInPlayer;
    private ViewGroup mVideoListContainerInPlayer;
    private RecyclerView mDefinitionListInPlayerRv;
    private RecyclerView mVideoListInPlayerRv;
    private DefinitionListAdapter mDefinitionListInPlayerAdapter;
    private VideoListAdapter mVideoListInPlayerAdapter;
    private String[] mDefinitionNameArr;
    private TextView mDefinitionBtn;
    private boolean mEnableSpeed = true, mEnableMediaList = true, mEnableDefinition = true;
    private boolean mEnableCurTime = true, mEnableProgressBar = true, mEnableTotalTime = true;
    private boolean mEnableVolumeText = true, mEnableFullScreen = true;

    public PineMediaWidget.IPineMediaPlayer mPlayer;

    private List<VpVideo> mVideoList = new LinkedList<>();
    private HashMap<String, PineMediaPlayerBean> mCodeMediaListMap = new HashMap<>();
    private HashMap<String, VpVideo> mCodeVideoListMap = new HashMap<>();

    private List<VpPlayListType> mPlayTypeList;
    private int mCurPlayTypePos = 0;

    private boolean mReleaseAfterComplete;
    private long mSchemeReleaseDelay;

    private HashMap<Integer, IControllerAdapterListener> mAdapterListenerMap = new HashMap<>();
    private HashMap<Integer, VideoPlayerView.PlayerViewListener> mPlayerViewListenerMap = new HashMap<>();

    private PineMediaWidget.PineMediaPlayerListener mPlayerListener = new PineMediaWidget.PineMediaPlayerListener() {
        @Override
        public void onStateChange(PineMediaPlayerBean playerBean, PinePlayState fromState, PinePlayState toState) {
            LogUtils.d(TAG, "onStateChange mediaCode:" + playerBean.getMediaCode() + ",fromState:" + fromState + ",toState:" + toState);
            boolean mediaChang = TextUtils.isEmpty(getCurMediaCode()) ||
                    mPlayer.isInPlaybackState() && !playerBean.getMediaCode().equals(getCurMediaCode());
            boolean playStateChange = fromState != toState;
            if (!mediaChang && playStateChange) {
                refreshPreNextBtnState();
                if (mAdapterListenerMap.size() > 0) {
                    Iterator<Map.Entry<Integer, IControllerAdapterListener>> iterator = mAdapterListenerMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        iterator.next().getValue().onVideoStateChange(getCurVideo(), toState);
                    }
                }
                if (mPlayerViewListenerMap.size() > 0) {
                    Iterator<Map.Entry<Integer, VideoPlayerView.PlayerViewListener>> iterator = mPlayerViewListenerMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        iterator.next().getValue().onPlayStateChange(getCurVideo(), fromState, toState);
                    }
                }
            }
        }

        @Override
        public boolean onComplete(PineMediaPlayerBean playerBean) {
            if (mReleaseAfterComplete) {
                schemeRelease(-1);
            } else {
                switch (getCurPlayType().getType()) {
                    case VpPlayListType.TYPE_ORDER:
                        onNextMediaSelect(playerBean.getMediaCode(), true);
                        break;
                    case VpPlayListType.TYPE_ALL_LOOP:
                        onNextMediaSelect(playerBean.getMediaCode(), true);
                        break;
                    case VpPlayListType.TYPE_SING_LOOP:
                        onMediaSelect(playerBean.getMediaCode(), true);
                        break;
                    case VpPlayListType.TYPE_RANDOM:
                        int randomPos = new Random().nextInt(10000) % mVideoList.size();
                        onMediaSelect(getMediaCode(mVideoList.get(randomPos)), true);
                        break;
                }
            }
            return false;
        }

        @Override
        public void onProgress(PineMediaPlayerBean playerBean, int progressTime) {
            if (isInSchemaReleaseProcess() && mSchemeReleaseDelay < System.currentTimeMillis()) {
                schemeRelease(-1);
            }
        }
    };

    private HandlerThread mWorkThread;
    private Handler mAlbumArtWorkHandler, mLrcWorkHandler;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    private int DEFAULT_ALBUM_ART_MAIN_COLOR = Color.TRANSPARENT;
    private final int MAX_SMALL_ALBUM_ART_CACHE_COUNT = 50;
    private final int MAX_BIG_ALBUM_ART_CACHE_COUNT = 10;
    private final int MAX_ALBUM_ART_REQUEST_COUNT = 2;

    private Bitmap mDefaultSmallAlbumArtBitmap = null;
    private Bitmap mDefaultBigAlbumArtBitmap = null;
    private HashMap<String, Bitmap> mSmallAlbumArtBitmapMap = new HashMap<>();
    private HashMap<String, Bitmap> mBigAlbumArtBitmapMap = new HashMap<>();
    private HashMap<String, Integer> mMainAlbumArtColorMap = new HashMap<>();
    private ArrayDeque<String> mBigAlbumArtDeque = new ArrayDeque<>();
    private ArrayDeque<String> mSmallAlbumArtDeque = new ArrayDeque<>();
    private HashMap<String, Boolean> mAlbumArtBitmapRequestingMap = new HashMap<>();
    private HashMap<String, String> mLyricMap = new HashMap<>();
    private HashMap<String, Boolean> mLyricRequestingMap = new HashMap<>();
    protected boolean mEnableSmallAlbumArt = false;
    protected boolean mEnableBigAlbumArt = false;
    protected int mPreMainColor = DEFAULT_ALBUM_ART_MAIN_COLOR;

    public VpVideoControllerAdapter(Context context) {
        mContext = context;
        mPlayTypeList = VpPlayListType.getDefaultList(mContext);
        mDefinitionNameArr = context.getResources().getStringArray(R.array.pine_media_definition_text_arr);

        if (mDefaultSmallAlbumArtBitmap == null) {
            mDefaultSmallAlbumArtBitmap = getBitmap(context,
                    R.drawable.pine_player_iv_default_bg);
        }
        if (mDefaultBigAlbumArtBitmap == null) {
            mDefaultBigAlbumArtBitmap = getBitmap(context,
                    R.drawable.pine_player_iv_default_bg);
        }
        Palette palette = new Palette.Builder(mDefaultSmallAlbumArtBitmap).generate();
        DEFAULT_ALBUM_ART_MAIN_COLOR = palette.getDominantColor(DEFAULT_ALBUM_ART_MAIN_COLOR);

        if (mWorkThread == null) {
            mWorkThread = new HandlerThread(TAG);
            mWorkThread.start();
        }
        if (mAlbumArtWorkHandler == null) {
            mAlbumArtWorkHandler = new Handler(mWorkThread.getLooper());
        }
        if (mLrcWorkHandler == null) {
            mLrcWorkHandler = new Handler(mWorkThread.getLooper());
        }

        if (getVideoList() != null && getVideoList().size() > 0 && mEnableMediaList) {
            initVideoRecycleView();
        }
        if (mEnableDefinition) {
            initDefinitionRecycleView();
        }
    }

    public void setupPlayer(PineMediaWidget.IPineMediaPlayer player) {
        mPlayer = player;
        mPlayer.addMediaPlayerListener(mPlayerListener);
    }

    public void setControllerView(ViewGroup root, ViewGroup fullRoot, List<VpPlayListType> playTypeList) {
        mPlayTypeList = playTypeList;
        mControllerView = root;
        mFullControllerView = fullRoot;
        mControllerViewHolder = null;
        mFullControllerViewHolder = null;
        mBackgroundViewHolder = null;
        mBackgroundView = null;
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
        if (isFullScreenMode) {
            if (mFullControllerViewHolder == null) {
                mFullControllerViewHolder = new PineControllerViewHolder();
                if (mFullControllerView == null) {
                    mFullControllerView = (ViewGroup) View.inflate(mContext,
                            R.layout.pine_player_media_controller_full, null);
                }
                initControllerViewHolder(mFullControllerViewHolder, mFullControllerView);
                mFullControllerViewHolder.setTopControllerView(
                        mFullControllerView.findViewById(R.id.top_controller));
                mFullControllerViewHolder.setCenterControllerView(
                        mFullControllerView.findViewById(R.id.center_controller));
                mFullControllerViewHolder.setBottomControllerView(
                        mFullControllerView.findViewById(R.id.bottom_controller));
                mFullControllerViewHolder.setGoBackButton(
                        mFullControllerView.findViewById(R.id.go_back_btn));
            }
            List<View> rightViewControlBtnList = new ArrayList<View>();
            View mediaListBtn = mFullControllerView.findViewById(R.id.media_list_btn);
            if (getVideoList() != null && getVideoList().size() > 0) {
                rightViewControlBtnList.add(mediaListBtn);
                mediaListBtn.setVisibility(View.VISIBLE);
            } else {
                mediaListBtn.setVisibility(View.GONE);
            }
            mDefinitionBtn = mFullControllerView.findViewById(R.id.media_definition_text);
            PineMediaPlayerBean pineMediaPlayerBean = player.getMediaPlayerBean();
            if (hasDefinitionList(pineMediaPlayerBean)) {
                rightViewControlBtnList.add(mDefinitionBtn);
                mDefinitionBtn.setVisibility(View.VISIBLE);
            } else {
                mDefinitionBtn.setVisibility(View.GONE);
            }
            if (rightViewControlBtnList.size() > 0) {
                mFullControllerViewHolder.setRightViewControlBtnList(rightViewControlBtnList);
            }
            mFullControllerViewHolder.setContainer(mFullControllerView);
            return mFullControllerViewHolder;
        } else {
            if (mControllerViewHolder == null) {
                if (mControllerView == null) {
                    mControllerView = (ViewGroup) View.inflate(mContext,
                            R.layout.pine_player_media_controller, null);
                }
                mControllerViewHolder = new PineControllerViewHolder();
                initControllerViewHolder(mControllerViewHolder, mControllerView);
                mControllerViewHolder.setTopControllerView(mControllerView
                        .findViewById(R.id.top_controller));
                mControllerViewHolder.setCenterControllerView(mControllerView
                        .findViewById(R.id.center_controller));
                mControllerViewHolder.setBottomControllerView(mControllerView
                        .findViewById(R.id.bottom_controller));
            }
            mControllerViewHolder.setContainer(mControllerView);
            return mControllerViewHolder;
        }
    }

    private final void initControllerViewHolder(
            PineControllerViewHolder viewHolder, View root) {
        viewHolder.setPausePlayButton(root.findViewById(R.id.pause_play_btn));
        SeekBar seekBar = root.findViewById(R.id.media_progress);
        if (mEnableProgressBar) {
            viewHolder.setPlayProgressBar(seekBar);
        } else {
            seekBar.setVisibility(View.GONE);
        }
        View curTimeTv = root.findViewById(R.id.cur_time_text);
        if (mEnableCurTime) {
            viewHolder.setCurrentTimeText(curTimeTv);
        } else {
            curTimeTv.setVisibility(View.GONE);
        }
        View endTimeTv = root.findViewById(R.id.end_time_text);
        if (mEnableTotalTime) {
            viewHolder.setEndTimeText(endTimeTv);
        } else {
            endTimeTv.setVisibility(View.GONE);
        }
        View VolumesTv = root.findViewById(R.id.volumes_text);
        if (mEnableVolumeText) {
            viewHolder.setVolumesText(VolumesTv);
        } else {
            VolumesTv.setVisibility(View.GONE);
        }
        View fullScreenTv = root.findViewById(R.id.full_screen_btn);
        if (mEnableFullScreen) {
            viewHolder.setFullScreenButton(fullScreenTv);
        } else {
            fullScreenTv.setVisibility(View.GONE);
        }
        View speedTv = root.findViewById(R.id.media_speed_text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mEnableSpeed) {
            viewHolder.setSpeedButton(speedTv);
        } else {
            speedTv.setVisibility(View.GONE);
        }
        viewHolder.setMediaNameText(root.findViewById(R.id.media_name_text));
        viewHolder.setLockControllerButton(root.findViewById(R.id.lock_screen_btn));
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
        if (mWaitingProgressViewHolder == null) {
            mWaitingProgressViewHolder = new PineWaitingProgressViewHolder();
            if (mWaitingProgressView == null) {
                mWaitingProgressView = new LinearLayout(mContext);
                mWaitingProgressView.setGravity(Gravity.CENTER);
                mWaitingProgressView.setBackgroundColor(Color.argb(192, 255, 255, 255));
                ProgressBar progressBar = new ProgressBar(mContext);
                ViewGroup.LayoutParams progressBarParams = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                progressBar.setLayoutParams(progressBarParams);
                progressBar.setIndeterminateDrawable(mContext.getResources()
                        .getDrawable(R.drawable.pine_player_media_waiting_anim));
                progressBar.setIndeterminate(true);
                mWaitingProgressView.addView(progressBar, progressBarParams);
            }
        }
        mWaitingProgressViewHolder.setContainer(mWaitingProgressView);
        return mWaitingProgressViewHolder;
    }

    @Override
    protected final List<PineRightViewHolder> onCreateRightViewHolderList(
            PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode) {
        List<PineRightViewHolder> viewHolderList = new ArrayList<>();
        if (isFullScreenMode) {
            if (getVideoList() != null && getVideoList().size() > 0 && mEnableMediaList) {
                PineRightViewHolder mediaListViewHolder = new PineRightViewHolder();
                mediaListViewHolder.setContainer(mVideoListContainerInPlayer);
                viewHolderList.add(mediaListViewHolder);
            }
            PineMediaPlayerBean pineMediaPlayerBean = player.getMediaPlayerBean();
            if (hasDefinitionList(pineMediaPlayerBean) && mEnableDefinition) {
                PineRightViewHolder definitionViewHolder = new PineRightViewHolder();
                definitionViewHolder.setContainer(mDefinitionListContainerInPlayer);
                viewHolderList.add(definitionViewHolder);
                mDefinitionListInPlayerAdapter.setData(pineMediaPlayerBean);
                mDefinitionListInPlayerAdapter.notifyDataSetChanged();
            }
        }
        return viewHolderList.size() > 0 ? viewHolderList : null;
    }

    private PineMediaController.ControllerMonitor mControllerMonitor;

    public void setControllerMonitor(PineMediaController.ControllerMonitor controllerMonitor) {
        mControllerMonitor = controllerMonitor;
    }

    @Override
    protected PineMediaController.ControllerMonitor onCreateControllerMonitor() {
        if (mControllerMonitor != null) {
            return mControllerMonitor;
        }
        return super.onCreateControllerMonitor();
    }

    private PineMediaController.ControllersActionListener mActionListener;

    public void setActionListener(PineMediaController.ControllersActionListener actionListener) {
        mActionListener = actionListener;
    }

    @Override
    public PineMediaController.ControllersActionListener onCreateControllersActionListener() {
        if (mActionListener != null) {
            return mActionListener;
        }
        return new PineMediaController.ControllersActionListener() {
            @Override
            public boolean onGoBackBtnClick(View fullScreenBtn, PineMediaWidget.IPineMediaPlayer player,
                                            boolean isFullScreenMode) {
                if (isFullScreenMode && mEnableFullScreen) {
                    mFullControllerViewHolder.getFullScreenButton().performClick();
                } else if (mContext instanceof Activity) {
                    ((Activity) mContext).finish();
                }
                return false;
            }

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

    public void addListener(View view, @NonNull VideoPlayerView.PlayerViewListener playerViewListener,
                            IControllerAdapterListener adapterListener) {
        if (playerViewListener != null) {
            mPlayerViewListenerMap.put(view.hashCode(), playerViewListener);
        }
        if (adapterListener != null) {
            mAdapterListenerMap.put(view.hashCode(), adapterListener);
        }
    }

    public void removeListener(View view) {
        mPlayerViewListenerMap.remove(view.hashCode());
        mAdapterListenerMap.remove(view.hashCode());
    }

    public List<VpVideo> getVideoList() {
        return mVideoList;
    }

    public String getCurMediaCode() {
        return mPlayer != null && mPlayer.getMediaPlayerBean() != null ? mPlayer.getMediaPlayerBean().getMediaCode() : "";
    }

    public VpVideo getCurVideo() {
        return mCodeVideoListMap.get(getCurMediaCode());
    }

    public VpVideo getListedVideo(String mediaCode) {
        return mCodeVideoListMap.get(mediaCode);
    }

    public VpPlayListType getCurPlayType() {
        return mPlayTypeList.get(mCurPlayTypePos % mPlayTypeList.size());
    }

    public VpPlayListType getAndGoNextPlayType() {
        VpPlayListType playListType = mPlayTypeList.get((++mCurPlayTypePos) % mPlayTypeList.size());
        refreshPreNextBtnState();
        return playListType;
    }

    private boolean hasDefinitionList(PineMediaPlayerBean pineMediaPlayerBean) {
        return pineMediaPlayerBean != null && pineMediaPlayerBean.getMediaUriSourceList().size() > 1;
    }

    private void videoDefinitionSelected(PineMediaPlayerBean pineMediaPlayerBean, int oldPosition, int newPosition) {
        if (pineMediaPlayerBean == null) {
            return;
        }
        mDefinitionListInPlayerAdapter.notifyDataSetChanged();
        mPlayer.resetPlayingMediaAndResume(pineMediaPlayerBean, null);
        if (mDefinitionBtn != null) {
            mDefinitionBtn.setText(getDefinitionName(pineMediaPlayerBean.getCurrentDefinition()));
        }
        if (mAdapterListenerMap.size() > 0) {
            Iterator<Map.Entry<Integer, IControllerAdapterListener>> iterator = mAdapterListenerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                iterator.next().getValue().onDefinitionChange(mCodeVideoListMap.get(pineMediaPlayerBean.getMediaCode()), oldPosition, newPosition);
            }
        }
    }

    public void setVideoList(List<VpVideo> list, boolean startPlay) {
        VpVideo preVideo = getCurVideo();
        mVideoList = new ArrayList<>();
        mCodeMediaListMap = new HashMap<>();
        mCodeVideoListMap = new HashMap<>();
        if (list == null || list.size() < 1) {
            if (mPlayerViewListenerMap.size() > 0) {
                Iterator<Map.Entry<Integer, VideoPlayerView.PlayerViewListener>> iterator = mPlayerViewListenerMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    iterator.next().getValue().onVideoListClear();
                }
            }
            onVideoListClear(preVideo);
        } else {
            addVideoList(list, startPlay);
        }
    }

    private void onVideoListClear(VpVideo preVideo) {
        mPlayer.release();
        mPlayer.setPlayingMedia(null);
        loadAlbumArtAndLyric(null);
        if (mPlayerViewListenerMap.size() > 0) {
            Iterator<Map.Entry<Integer, VideoPlayerView.PlayerViewListener>> iterator = mPlayerViewListenerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                iterator.next().getValue().onPlayVideo(mPlayer, null);
            }
        }
    }

    public void addVideo(VpVideo video, boolean startPlay) {
        if (video == null) {
            return;
        }
        String mediaCode = getMediaCode(video);
        if (mCodeVideoListMap.containsKey(mediaCode)) {
            mVideoList.remove(mCodeVideoListMap.get(mediaCode));
        }
        PineMediaPlayerBean mediaBean = transferMediaBean(video);
        mVideoList.add(0, video);
        mCodeVideoListMap.put(mediaBean.getMediaCode(), video);
        mCodeMediaListMap.put(mediaBean.getMediaCode(), mediaBean);
        onMediaSelect(getMediaCode(video), startPlay);
    }

    public void addVideoList(List<VpVideo> list, boolean startPlay) {
        if (list == null && list.size() < 1) {
            return;
        }
        for (VpVideo video : list) {
            String mediaCode = getMediaCode(video);
            if (mCodeVideoListMap.containsKey(mediaCode)) {
                mVideoList.remove(mCodeVideoListMap.get(mediaCode));
            }
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            VpVideo video = list.get(i);
            PineMediaPlayerBean mediaBean = transferMediaBean(video);
            mVideoList.add(0, video);
            mCodeMediaListMap.put(mediaBean.getMediaCode(), mediaBean);
            mCodeVideoListMap.put(mediaBean.getMediaCode(), video);
        }
        onMediaSelect(getMediaCode(mVideoList.get(0)), startPlay);
    }

    public void updateVideoData(VpVideo video) {
        if (video == null) {
            return;
        }
        String mediaCode = getMediaCode(video);
        if (mCodeVideoListMap.containsKey(mediaCode)) {
            VpVideo listedVideo = mCodeVideoListMap.get(mediaCode);
            if (listedVideo != null) {
                if (listedVideo.mediaInfoChange(video)) {
                    PineMediaPlayerBean mediaBean = transferMediaBean(video);
                    mCodeMediaListMap.put(mediaBean.getMediaCode(), mediaBean);
                }
                listedVideo.copyDataFrom(video);
            }
        }
    }

    public void updateVideoListData(List<VpVideo> list) {
        if (list == null && list.size() < 1) {
            return;
        }
        for (VpVideo video : list) {
            updateVideoData(video);
        }
    }

    public void removeVideo(VpVideo video) {
        VpVideo preVideo = getCurVideo();
        int curPos = findVideoPosition(getCurMediaCode());
        String removeMediaCode = getMediaCode(video);
        if (mCodeVideoListMap.containsKey(removeMediaCode)) {
            mVideoList.remove(mCodeVideoListMap.get(removeMediaCode));
            mCodeMediaListMap.remove(removeMediaCode);
            mCodeVideoListMap.remove(removeMediaCode);
        }
        if (mPlayerViewListenerMap.size() > 0) {
            Iterator<Map.Entry<Integer, VideoPlayerView.PlayerViewListener>> iterator = mPlayerViewListenerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                iterator.next().getValue().onVideoRemove(video);
            }
        }
        if (mVideoList.size() < 1) {
            onVideoListClear(preVideo);
        } else {
            if (removeMediaCode.equals(getCurMediaCode())) {
                playMedia(curPos, mPlayer.isPlaying());
            }
        }
    }

    private boolean isInSchemaReleaseProcess() {
        return mSchemeReleaseDelay > 0;
    }

    public void clearDelayRelease() {
        mSchemeReleaseDelay = 0;
        mReleaseAfterComplete = false;
    }

    /**
     * 按计划停止播放器
     *
     * @param delay 小于0：立即停止播放；0：播放完当前内容后停止播放；大于0：delay时间后停止播放
     */
    public void schemeRelease(long delay) {
        LogUtils.d(TAG, "release player delay:" + delay);
        clearDelayRelease();
        if (delay < 0) {
            mPlayer.release();
        } else if (delay == 0) {
            mReleaseAfterComplete = true;
        } else {
            mSchemeReleaseDelay = System.currentTimeMillis() + delay;
        }
    }

    public void destroy() {
        mPlayer.removeMediaPlayerListener(mPlayerListener);
        setVideoList(null, false);

        clearAlbumArtBitmap();
        if (mAlbumArtWorkHandler != null) {
            mSmallAlbumArtBitmapMap.clear();
            mBigAlbumArtBitmapMap.clear();
            mAlbumArtWorkHandler.removeCallbacksAndMessages(null);
            mAlbumArtWorkHandler = null;
        }
        if (mLrcWorkHandler != null) {
            mLrcWorkHandler.removeCallbacksAndMessages(null);
            mLrcWorkHandler = null;
        }
        if (mWorkThread != null) {
            mWorkThread.quit();
            mWorkThread = null;
        }
    }

    private void clearAlbumArtBitmap() {
        Iterator<Map.Entry<String, Bitmap>> iterator = mSmallAlbumArtBitmapMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Bitmap bitmap = iterator.next().getValue();
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        iterator = mBigAlbumArtBitmapMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Bitmap bitmap = iterator.next().getValue();
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        mSmallAlbumArtDeque.clear();
        mBigAlbumArtDeque.clear();
        if (mDefaultSmallAlbumArtBitmap != null) {
            mDefaultSmallAlbumArtBitmap.recycle();
            mDefaultSmallAlbumArtBitmap = null;
        }
        if (mDefaultBigAlbumArtBitmap != null) {
            mDefaultBigAlbumArtBitmap.recycle();
            mDefaultBigAlbumArtBitmap = null;
        }
    }

    public void refreshPreNextBtnState() {
        int position = findVideoPosition(getCurMediaCode());
        if (mControllerViewHolder != null) {
            if (mControllerViewHolder.getPrevButton() != null) {
                mControllerViewHolder.getPrevButton().setEnabled(isLoopMode() || position > 0);
            }
            if (mControllerViewHolder.getNextButton() != null) {
                mControllerViewHolder.getNextButton().setEnabled(isLoopMode() || position < mVideoList.size() - 1 && position >= 0);
            }
        }
    }

    private int findVideoPosition(String mediaCode) {
        if (mVideoList != null && mVideoList.size() > 0) {
            for (int i = 0; i < mVideoList.size(); i++) {
                if (mediaCode.equals(getMediaCode(mVideoList.get(i)))) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public boolean onPreMediaSelect(@NonNull String curMediaCode, boolean startPlay) {
        int position = findVideoPosition(curMediaCode);
        if (position == -1) {
            return false;
        }
        position--;
        return playMedia(position, startPlay);
    }

    @Override
    public boolean onNextMediaSelect(@NonNull String curMediaCode, boolean startPlay) {
        int position = findVideoPosition(curMediaCode);
        if (position == -1) {
            return false;
        }
        position++;
        return playMedia(position, startPlay);
    }

    @Override
    public boolean onMediaSelect(String mediaCode, boolean startPlay) {
        int position = findVideoPosition(mediaCode);
        if (position == -1) {
            return false;
        }
        return playMedia(position, startPlay);
    }

    private boolean isLoopMode() {
        int curPlayType = getCurPlayType().getType();
        if (curPlayType == VpPlayListType.TYPE_ALL_LOOP ||
                curPlayType == VpPlayListType.TYPE_RANDOM) {
            return true;
        }
        return false;
    }

    private boolean playMedia(int position, boolean startPlay) {
        if (isLoopMode()) {
            position = (position + mVideoList.size()) % mVideoList.size();
        }
        if (position >= 0 && position < mVideoList.size()) {
            if (mPlayer != null) {
                LogUtils.d(TAG, "playMedia position:" + position + ",startPlay:" + startPlay);
                VpVideo video = mVideoList.get(position);
                String mediaCode = getMediaCode(video);
                PineMediaPlayerBean bean = mCodeMediaListMap.get(mediaCode);
                if (!mediaCode.equals(getCurMediaCode())) {
                    mPlayer.setPlayingMedia(bean);
                    loadAlbumArtAndLyric(video);
                }
                if (startPlay) {
                    mPlayer.start();
                }
                if (mPlayerViewListenerMap.size() > 0) {
                    Iterator<Map.Entry<Integer, VideoPlayerView.PlayerViewListener>> iterator = mPlayerViewListenerMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        iterator.next().getValue().onPlayVideo(mPlayer, video);
                    }
                }
                refreshPreNextBtnState();
                return true;
            }
        }
        refreshPreNextBtnState();
        return false;
    }

    public void enableAlbumArt(boolean enableSmallAlbumArt, boolean enableBigAlbumArt) {
        mEnableSmallAlbumArt = enableSmallAlbumArt;
        mEnableBigAlbumArt = enableBigAlbumArt;
    }

    public Bitmap getBigAlbumArtBitmap(String mediaCode) {
        if (!mAlbumArtBitmapRequestingMap.containsKey(mediaCode) &&
                mBigAlbumArtBitmapMap.containsKey(mediaCode)) {
            LogUtils.d(TAG, "getBigAlbumArtBitmap mediaCode :" + mediaCode + " found");
            return mBigAlbumArtBitmapMap.get(mediaCode);
        } else {
            LogUtils.d(TAG, "getBigAlbumArtBitmap mediaCode :" + mediaCode + " use default");
            return mDefaultBigAlbumArtBitmap;
        }
    }

    public Bitmap getSmallAlbumArtBitmap(String mediaCode) {
        if (!mAlbumArtBitmapRequestingMap.containsKey(mediaCode) &&
                mSmallAlbumArtBitmapMap.containsKey(mediaCode)) {
            LogUtils.d(TAG, "getSmallAlbumArtBitmap mediaCode :" + mediaCode + " found");
            return mSmallAlbumArtBitmapMap.get(mediaCode);
        } else {
            LogUtils.d(TAG, "getSmallAlbumArtBitmap mediaCode :" + mediaCode + " use default");
            return mDefaultSmallAlbumArtBitmap;
        }
    }

    public int getMainAlbumArtColor(String mediaCode) {
        if (!mAlbumArtBitmapRequestingMap.containsKey(mediaCode) &&
                mMainAlbumArtColorMap.containsKey(mediaCode)) {
            LogUtils.d(TAG, "getMainAlbumArtColor mediaCode :" + mediaCode + " found");
            return mMainAlbumArtColorMap.get(mediaCode);
        } else {
            LogUtils.d(TAG, "getMainAlbumArtColor mediaCode :" + mediaCode + " use default");
            return DEFAULT_ALBUM_ART_MAIN_COLOR;
        }
    }

    public void loadAlbumArtAndLyric(VpVideo video) {
        String mediaCode = "";
        if (video == null) {
            onAlbumArtPrepare(mediaCode, null, getSmallAlbumArtBitmap(mediaCode),
                    getBigAlbumArtBitmap(mediaCode), getMainAlbumArtColor(mediaCode));
            return;
        }
        mediaCode = getMediaCode(video);
        if (!mAlbumArtBitmapRequestingMap.containsKey(mediaCode)) {
            if ((mEnableSmallAlbumArt && !mSmallAlbumArtBitmapMap.containsKey(mediaCode) ||
                    mEnableBigAlbumArt && !mBigAlbumArtBitmapMap.containsKey(mediaCode) ||
                    !mMainAlbumArtColorMap.containsKey(mediaCode))) {
                LogUtils.d(TAG, "loadAlbumArtAndLyric getAlbumArtBitmapInBackground mediaCode:" + mediaCode);
                getAlbumArtBitmapInBackground(mediaCode, video.getVideoId(), video.getAlbumId());
            } else {
                onAlbumArtPrepare(mediaCode, video, getSmallAlbumArtBitmap(mediaCode),
                        getBigAlbumArtBitmap(mediaCode), getMainAlbumArtColor(mediaCode));
            }
            if (TextUtils.isEmpty(video.getLyricFilePath()) || !new File(video.getLyricFilePath()).exists()) {
                if (!mLyricMap.containsKey(mediaCode)) {
                    if (!mLyricRequestingMap.containsKey(mediaCode)) {
                        getLyricInBackground(mediaCode, video);
                    }
                } else {
                    onLyricDownloaded(mediaCode, mLyricMap.get(mediaCode), CharsetUtils.getCharset(mLyricMap.get(mediaCode)));
                }
            }
        }
    }

    private void getAlbumArtBitmapInBackground(final String mediaCode, final long songId, final long albumId) {
        if (mAlbumArtWorkHandler == null) {
            return;
        }
        if (mAlbumArtBitmapRequestingMap.size() > MAX_ALBUM_ART_REQUEST_COUNT) {
            LogUtils.d(TAG, "getAlbumArtBitmapInBackground removeCallbacksAndMessages for too much message delayed");
            mAlbumArtWorkHandler.removeCallbacksAndMessages(null);
            mAlbumArtBitmapRequestingMap.clear();
        }
        final boolean finaEnableSmallAlbumArt = mEnableSmallAlbumArt;
        final boolean finaEnableBigAlbumArt = mEnableBigAlbumArt;
        mAlbumArtWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                LogUtils.d(TAG, "getAlbumArtBitmapInBackground start --- mediaCode:" + mediaCode);
                Bitmap smallBitmap = null;
                if (finaEnableSmallAlbumArt && !mSmallAlbumArtBitmapMap.containsKey(mediaCode)) {
                    smallBitmap = VpLocalVideoUtils.getAlbumArtBitmap(mContext,
                            songId, albumId, true);
                }
                Bitmap bigBitmap = null;
                if (finaEnableBigAlbumArt && !mBigAlbumArtBitmapMap.containsKey(mediaCode)) {
                    bigBitmap = VpLocalVideoUtils.getAlbumArtBitmap(mContext,
                            songId, albumId, false);
                }
                int color = DEFAULT_ALBUM_ART_MAIN_COLOR;
                if (!mMainAlbumArtColorMap.containsKey(mediaCode) && (smallBitmap != null || bigBitmap != null)) {
                    Palette palette = new Palette.Builder(smallBitmap != null ? smallBitmap : bigBitmap).generate();
                    color = palette.getDominantColor(DEFAULT_ALBUM_ART_MAIN_COLOR);
                }
                final Bitmap finalSmallBitmap = smallBitmap;
                final Bitmap finalBigBitmap = bigBitmap;
                final int finalColor = color;
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        LogUtils.d(TAG, "getAlbumArtBitmapInBackground end --- mediaCode:" + mediaCode);
                        mAlbumArtBitmapRequestingMap.remove(mediaCode);

                        if (mSmallAlbumArtDeque.size() >= MAX_SMALL_ALBUM_ART_CACHE_COUNT) {
                            String oldestMediaCode = mSmallAlbumArtDeque.poll();
                            if (!oldestMediaCode.equals(mediaCode)) {
                                Bitmap bitmap = mSmallAlbumArtBitmapMap.remove(oldestMediaCode);
                                if (bitmap != null) {
                                    bitmap.recycle();
                                }
                            } else {
                                mSmallAlbumArtDeque.add(mediaCode);
                            }
                        }
                        if (finaEnableSmallAlbumArt && !mSmallAlbumArtBitmapMap.containsKey(mediaCode)) {
                            mSmallAlbumArtBitmapMap.put(mediaCode, finalSmallBitmap == null ? mDefaultSmallAlbumArtBitmap : finalSmallBitmap);
                            if (finalSmallBitmap != null) {
                                mSmallAlbumArtDeque.add(mediaCode);
                            }
                        }

                        if (mBigAlbumArtDeque.size() >= MAX_BIG_ALBUM_ART_CACHE_COUNT) {
                            String oldestMediaCode = mBigAlbumArtDeque.poll();
                            if (!oldestMediaCode.equals(mediaCode)) {
                                Bitmap bitmap = mBigAlbumArtBitmapMap.remove(oldestMediaCode);
                                if (bitmap != null) {
                                    bitmap.recycle();
                                }
                            } else {
                                mBigAlbumArtDeque.add(mediaCode);
                            }
                        }
                        if (finaEnableBigAlbumArt && !mBigAlbumArtBitmapMap.containsKey(mediaCode)) {
                            mBigAlbumArtBitmapMap.put(mediaCode, finalBigBitmap == null ? mDefaultBigAlbumArtBitmap : finalBigBitmap);
                            if (finalBigBitmap != null) {
                                mBigAlbumArtDeque.add(mediaCode);
                            }
                        }

                        if (!mMainAlbumArtColorMap.containsKey(mediaCode)) {
                            mMainAlbumArtColorMap.put(mediaCode, finalColor);
                        }

                        if (mediaCode.equals(getCurMediaCode())) {
                            onAlbumArtPrepare(mediaCode, getCurVideo(),
                                    getSmallAlbumArtBitmap(mediaCode), getBigAlbumArtBitmap(mediaCode),
                                    getMainAlbumArtColor(mediaCode));
                        }
                    }
                });
            }
        });
        mAlbumArtBitmapRequestingMap.put(mediaCode, true);
    }

    private void onAlbumArtPrepare(String mediaCode, VpVideo video, Bitmap smallBitmap, Bitmap bigBitmap, int mainColor) {
        if (mAdapterListenerMap.size() > 0) {
            Iterator<Map.Entry<Integer, IControllerAdapterListener>> iterator = mAdapterListenerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                iterator.next().getValue().onAlbumArtPrepare(mediaCode, video, smallBitmap, bigBitmap, mainColor);
            }
        }
        if (mPlayerViewListenerMap.size() > 0) {
            Iterator<Map.Entry<Integer, VideoPlayerView.PlayerViewListener>> iterator = mPlayerViewListenerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                iterator.next().getValue().onAlbumArtChange(mediaCode, video, smallBitmap, bigBitmap, mainColor);
            }
        }
        mPreMainColor = mainColor;
    }

    private void getLyricInBackground(final String mediaCode, final VpVideo video) {
        if (mLrcWorkHandler == null) {
            return;
        }
        mLrcWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                final String filePath = VpLocalVideoUtils.getLyric(mContext, video);
                if (TextUtils.isEmpty(filePath)) {
                    return;
                }
                String charset = CharsetUtils.getCharset(filePath);
                onLyricPrepared(mediaCode, video, filePath, charset);
            }
        });
        mLyricRequestingMap.put(mediaCode, true);
    }

    private void onLyricPrepared(final String mediaCode, final VpVideo video,
                                 final String lrcFilePath, final String charset) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mLyricRequestingMap.remove(mediaCode);
                mLyricMap.put(mediaCode, lrcFilePath);
                VpVideo video = onLyricDownloaded(mediaCode, lrcFilePath, charset);
                if (mPlayerViewListenerMap.size() > 0) {
                    Iterator<Map.Entry<Integer, VideoPlayerView.PlayerViewListener>> iterator = mPlayerViewListenerMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        iterator.next().getValue().onLyricDownloaded(mediaCode, video, lrcFilePath, charset);
                    }
                }
            }
        });
    }

    private VpVideo onLyricDownloaded(String mediaCode, String filePath, String charset) {
        VpVideo video = mCodeVideoListMap.get(mediaCode);
        if (video == null) {
            return null;
        }
        video.setLyricFilePath(filePath);
        video.setLyricCharset(charset);
        PineMediaPlayerBean bean = mCodeMediaListMap.get(mediaCode);
        HashMap<Integer, IPinePlayerPlugin> pluginHashMap = bean.getPlayerPluginMap();
        if (pluginHashMap != null) {
            IPinePlayerPlugin plugin = pluginHashMap.get(VpConstants.PLUGIN_LRC_SUBTITLE);
            if (plugin != null && plugin instanceof PineLrcParserPlugin) {
                ((PineLrcParserPlugin) plugin).setSubtitle(filePath, PinePlayerConstants.PATH_STORAGE,
                        video.getLyricCharset());
            }
        }
        return video;
    }

    private void initVideoRecycleView() {
        mVideoListContainerInPlayer = (ViewGroup) LayoutInflater.from(mContext)
                .inflate(R.layout.pine_player_media_list_recycler_view, null);
        mVideoListInPlayerRv = (RecyclerView) mVideoListContainerInPlayer
                .findViewById(R.id.video_recycler_view_in_player);

        // 播放器内置播放列表初始化
        // 设置固定大小
        mVideoListInPlayerRv.setHasFixedSize(true);
        // 创建线性布局管理器
        LinearLayoutManager MediaListLlm = new LinearLayoutManager(mContext);
        // 设置垂直方向
        MediaListLlm.setOrientation(RecyclerView.VERTICAL);
        // 给RecyclerView设置布局管理器
        mVideoListInPlayerRv.setLayoutManager(MediaListLlm);
        // 给RecyclerView添加装饰（比如divider）
        mVideoListInPlayerRv.addItemDecoration(
                new AdvanceDecoration(mContext,
                        R.drawable.pine_player_rv_divider, 2, AdvanceDecoration.VERTICAL, true));
        // 设置适配器
        mVideoListInPlayerAdapter = new VideoListAdapter(mVideoListInPlayerRv);
        mVideoListInPlayerRv.setAdapter(mVideoListInPlayerAdapter);
        mVideoListInPlayerAdapter.setData(mVideoList);
        mVideoListInPlayerAdapter.notifyDataSetChanged();
    }

    private void initDefinitionRecycleView() {
        mDefinitionListContainerInPlayer = (ViewGroup) LayoutInflater.from(mContext)
                .inflate(R.layout.pine_player_definition_recycler_view, null);
        mDefinitionListInPlayerRv = mDefinitionListContainerInPlayer
                .findViewById(R.id.definition_recycler_view_in_player);

        // 播放器内置清晰度列表初始化
        // 设置固定大小
        mDefinitionListInPlayerRv.setHasFixedSize(true);
        // 创建线性布局管理器
        LinearLayoutManager definitionListLlm = new LinearLayoutManager(mContext);
        // 设置垂直方向
        definitionListLlm.setOrientation(RecyclerView.VERTICAL);
        // 给RecyclerView设置布局管理器
        mDefinitionListInPlayerRv.setLayoutManager(definitionListLlm);
        // 给RecyclerView添加装饰（比如divider）
        mDefinitionListInPlayerRv.addItemDecoration(
                new AdvanceDecoration(mContext,
                        R.drawable.pine_player_rv_divider, 2, AdvanceDecoration.VERTICAL, true));
        // 设置适配器
        mDefinitionListInPlayerAdapter = new DefinitionListAdapter(mDefinitionListInPlayerRv);
        mDefinitionListInPlayerRv.setAdapter(mDefinitionListInPlayerAdapter);
    }

    private String getDefinitionName(int definition) {
        String definitionName = null;
        switch (definition) {
            case PineMediaUriSource.MEDIA_DEFINITION_SD:
                definitionName = mDefinitionNameArr[0];
                break;
            case PineMediaUriSource.MEDIA_DEFINITION_HD:
                definitionName = mDefinitionNameArr[1];
                break;
            case PineMediaUriSource.MEDIA_DEFINITION_VHD:
                definitionName = mDefinitionNameArr[2];
                break;
            case PineMediaUriSource.MEDIA_DEFINITION_1080:
                definitionName = mDefinitionNameArr[3];
                break;
            default:
                break;
        }
        return definitionName;
    }

    private Bitmap getBitmap(@NonNull Context context, @DrawableRes final int resId) {
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public PineMediaPlayerBean transferMediaBean(@NonNull VpVideo video) {
        PineMediaPlayerBean mediaBean = new PineMediaPlayerBean(getMediaCode(video),
                video.getName(), Uri.parse(video.getFilePath()),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null,
                null, null);
        HashMap<Integer, IPinePlayerPlugin> pluginHashMap = new HashMap<>();
        PineLrcParserPlugin playerPlugin = new PineLrcParserPlugin(mContext, video.getLyricFilePath(),
                video.getLyricCharset());
        pluginHashMap.put(VpConstants.PLUGIN_LRC_SUBTITLE, playerPlugin);
        mediaBean.setPlayerPluginMap(pluginHashMap);
        return mediaBean;
    }

    public String getMediaCode(@NonNull VpVideo video) {
        return video.getVideoId() + "";
    }

    // 自定义RecyclerView的数据Adapter
    class DefinitionListAdapter extends RecyclerView.Adapter {
        private PineMediaPlayerBean pineMediaPlayerBean;
        private List<PineMediaUriSource> mData;
        private RecyclerView mRecyclerView;

        public DefinitionListAdapter(RecyclerView view) {
            this.mRecyclerView = view;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.pine_player_item_definition_select_in_player, parent, false);
            DefinitionViewHolder viewHolder = new DefinitionViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final DefinitionViewHolder myHolder = (DefinitionViewHolder) holder;
            PineMediaUriSource itemData = mData.get(position);
            int definition = itemData.getMediaDefinition();
            if (myHolder.mItemTv != null) {
                myHolder.mItemTv.setText(getDefinitionName(definition));
            }
            boolean isSelected = position == pineMediaPlayerBean.getCurrentDefinitionPosition();
            myHolder.itemView.setSelected(isSelected);
            myHolder.mItemTv.setSelected(isSelected);
            myHolder.mTextPaint.setFakeBoldText(isSelected);
            // 为RecyclerView的item view设计事件监听机制
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    int oldPosition = pineMediaPlayerBean.getCurrentDefinitionPosition();
                    pineMediaPlayerBean.setCurrentDefinitionByPosition(position);
                    mPlayer.savePlayMediaState();
                    videoDefinitionSelected(pineMediaPlayerBean, oldPosition, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        public void setData(@NonNull PineMediaPlayerBean pineMediaPlayerBean) {
            this.pineMediaPlayerBean = pineMediaPlayerBean;
            this.mData = pineMediaPlayerBean.getMediaUriSourceList();
        }
    }

    // 自定义的ViewHolder，持有每个Item的的所有界面元素
    class DefinitionViewHolder extends RecyclerView.ViewHolder {
        public TextView mItemTv;
        public TextPaint mTextPaint;

        public DefinitionViewHolder(View view) {
            super(view);
            mItemTv = view.findViewById(R.id.rv_definition_item_text);
            mTextPaint = mItemTv.getPaint();
        }
    }

    // 自定义RecyclerView的数据Adapter
    class VideoListAdapter extends RecyclerView.Adapter {
        private List<VpVideo> mData;
        private RecyclerView mRecyclerView;

        public VideoListAdapter(RecyclerView view) {
            this.mRecyclerView = view;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.pine_player_item_video_in_player, parent, false);
            VideoViewHolder viewHolder = new VideoViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final VideoViewHolder myHolder = (VideoViewHolder) holder;
            final VpVideo itemData = mData.get(position);
            if (myHolder.mItemTv != null) {
                myHolder.mItemTv.setText(itemData.getName());
            }
            boolean isSelected = getMediaCode(itemData).equals(getCurMediaCode());
            myHolder.itemView.setSelected(isSelected);
            myHolder.mItemTv.setSelected(isSelected);
            myHolder.mTextPaint.setFakeBoldText(isSelected);
            // 为RecyclerView的item view设计事件监听机制
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    String oldMediaCode = getCurMediaCode();
                    if (onMediaSelect(getMediaCode(itemData), true)) {
                        notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        public void setData(List<VpVideo> data) {
            this.mData = data;
        }
    }

    // 自定义的ViewHolder，持有每个Item的的所有界面元素
    class VideoViewHolder extends RecyclerView.ViewHolder {
        public TextView mItemTv;
        public TextPaint mTextPaint;

        public VideoViewHolder(View view) {
            super(view);
            mItemTv = view.findViewById(R.id.rv_video_item_text);
            mTextPaint = mItemTv.getPaint();
        }
    }

    public interface IControllerAdapterListener {
        void onVideoStateChange(VpVideo video, PinePlayState state);

        void onDefinitionChange(VpVideo video, int oldPosition, int newPosition);

        void onAlbumArtPrepare(String mediaCode, VpVideo video, Bitmap smallBitmap,
                               Bitmap bigBitmap, int mainColor);
    }
}

