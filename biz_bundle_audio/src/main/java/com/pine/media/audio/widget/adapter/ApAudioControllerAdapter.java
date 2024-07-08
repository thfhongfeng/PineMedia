package com.pine.media.audio.widget.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.palette.graphics.Palette;

import com.pine.media.audio.ApConstants;
import com.pine.media.audio.R;
import com.pine.media.audio.bean.ApPlayListType;
import com.pine.media.audio.db.entity.ApMusic;
import com.pine.media.audio.util.ApLocalMusicUtils;
import com.pine.media.audio.widget.AudioPlayerView;
import com.pine.media.audio.widget.plugin.ApOutRootLrcPlugin;
import com.pine.player.PinePlayerConstants;
import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.component.PinePlayState;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.view.PineProgressBar;
import com.pine.player.widget.viewholder.PineBackgroundViewHolder;
import com.pine.player.widget.viewholder.PineControllerViewHolder;
import com.pine.player.widget.viewholder.PineRightViewHolder;
import com.pine.player.widget.viewholder.PineWaitingProgressViewHolder;
import com.pine.tool.util.CharsetUtils;
import com.pine.tool.util.ImageUtils;
import com.pine.tool.util.LogUtils;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ApAudioControllerAdapter extends PineMediaController.AbstractMediaControllerAdapter {
    private Context mContext;
    private ViewGroup mControllerView;
    private PineControllerViewHolder mControllerViewHolder;
    private RelativeLayout mBackgroundView;
    private PineBackgroundViewHolder mBackgroundViewHolder;

    public PineMediaWidget.IPineMediaPlayer mPlayer;

    private List<ApMusic> mMusicList = new LinkedList<>();
    private HashMap<String, PineMediaPlayerBean> mCodeMediaListMap = new HashMap<>();
    private HashMap<String, ApMusic> mCodeMusicListMap = new HashMap<>();

    private List<ApPlayListType> mPlayTypeList;
    private int mCurPlayTypePos = 0;

    private boolean mReleaseAfterComplete;
    private long mSchemeReleaseDelay;

    private HashMap<Integer, IControllerAdapterListener> mAdapterListenerMap = new HashMap<>();
    private HashMap<Integer, AudioPlayerView.PlayerViewListener> mPlayerViewListenerMap = new HashMap<>();
    private ApOutRootLrcPlugin.ILyricUpdateListener mLyricUpdateListener;

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
                        iterator.next().getValue().onMusicStateChange(getCurMusic(), toState);
                    }
                }
                if (mPlayerViewListenerMap.size() > 0) {
                    Iterator<Map.Entry<Integer, AudioPlayerView.PlayerViewListener>> iterator = mPlayerViewListenerMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        iterator.next().getValue().onPlayStateChange(getCurMusic(), fromState, toState);
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
                    case ApPlayListType.TYPE_ORDER:
                        onNextMediaSelect(playerBean.getMediaCode(), true);
                        break;
                    case ApPlayListType.TYPE_ALL_LOOP:
                        onNextMediaSelect(playerBean.getMediaCode(), true);
                        break;
                    case ApPlayListType.TYPE_SING_LOOP:
                        onMediaSelect(playerBean.getMediaCode(), true);
                        break;
                    case ApPlayListType.TYPE_RANDOM:
                        int randomPos = new Random().nextInt(10000) % mMusicList.size();
                        onMediaSelect(getMediaCode(mMusicList.get(randomPos)), true);
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

    public ApAudioControllerAdapter(Context context) {
        this(context, null);
        mContext = context;
        mPlayTypeList = ApPlayListType.getDefaultList(mContext);
    }

    public ApAudioControllerAdapter(Context context, ViewGroup root) {
        mContext = context;
        mControllerView = root;
        mPlayTypeList = ApPlayListType.getDefaultList(mContext);

        if (mDefaultSmallAlbumArtBitmap == null) {
            mDefaultSmallAlbumArtBitmap = ImageUtils.getBitmap(context,
                    R.mipmap.ap_iv_default_bg_1);
        }
        if (mDefaultBigAlbumArtBitmap == null) {
            mDefaultBigAlbumArtBitmap = ImageUtils.getBitmap(context,
                    R.mipmap.ap_iv_default_bg_1);
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
    }

    public void setupPlayer(PineMediaWidget.IPineMediaPlayer player) {
        mPlayer = player;
        mPlayer.addMediaPlayerListener(mPlayerListener);
    }

    public void setControllerView(ViewGroup root, List<ApPlayListType> playTypeList) {
        mPlayTypeList = playTypeList;
        mControllerView = root;
        mBackgroundViewHolder = null;
        mControllerViewHolder = null;
        mBackgroundView = null;
    }

    protected PineMediaController.ControllerMonitor onCreateControllerMonitor() {
        return new PineMediaController.ControllerMonitor() {
            public boolean judgeAndChangeRequestedOrientation(
                    Context context, PineMediaWidget.IPineMediaController controller,
                    PineMediaWidget.IPineMediaPlayer player, int mediaWidth,
                    int mediaHeight, int mediaType) {
                return true;
            }
        };
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

    public void addListener(View view, @NonNull AudioPlayerView.PlayerViewListener playerViewListener,
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

    public PineMediaPlayerBean transferMediaBean(@NonNull ApMusic music) {
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

    public List<ApMusic> getMusicList() {
        return mMusicList;
    }

    public String getCurMediaCode() {
        return mPlayer != null && mPlayer.getMediaPlayerBean() != null ? mPlayer.getMediaPlayerBean().getMediaCode() : "";
    }

    public ApMusic getCurMusic() {
        return mCodeMusicListMap.get(getCurMediaCode());
    }

    public ApMusic getListedMusic(String mediaCode) {
        return mCodeMusicListMap.get(mediaCode);
    }

    public ApPlayListType getCurPlayType() {
        return mPlayTypeList.get(mCurPlayTypePos % mPlayTypeList.size());
    }

    public ApPlayListType getAndGoNextPlayType() {
        ApPlayListType playListType = mPlayTypeList.get((++mCurPlayTypePos) % mPlayTypeList.size());
        refreshPreNextBtnState();
        return playListType;
    }

    public String getMediaCode(@NonNull ApMusic music) {
        return music.getSongId() + "";
    }

    public void setMusicList(List<ApMusic> list, boolean startPlay) {
        ApMusic preMusic = getCurMusic();
        mMusicList = new ArrayList<>();
        mCodeMediaListMap = new HashMap<>();
        mCodeMusicListMap = new HashMap<>();
        if (list == null || list.size() < 1) {
            if (mPlayerViewListenerMap.size() > 0) {
                Iterator<Map.Entry<Integer, AudioPlayerView.PlayerViewListener>> iterator = mPlayerViewListenerMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    iterator.next().getValue().onMusicListClear();
                }
            }
            onMusicListClear(preMusic);
        } else {
            addMusicList(list, startPlay);
        }
    }

    private void onMusicListClear(ApMusic preMusic) {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer.setPlayingMedia(null);
        }
        loadAlbumArtAndLyric(null);
        if (mPlayerViewListenerMap.size() > 0) {
            Iterator<Map.Entry<Integer, AudioPlayerView.PlayerViewListener>> iterator = mPlayerViewListenerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                iterator.next().getValue().onPlayMusic(mPlayer, null);
            }
        }
    }

    public void addMusic(ApMusic music, boolean startPlay) {
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

    public void addMusicList(List<ApMusic> list, boolean startPlay) {
        if (list == null && list.size() < 1) {
            return;
        }
        for (ApMusic music : list) {
            String mediaCode = getMediaCode(music);
            if (mCodeMusicListMap.containsKey(mediaCode)) {
                mMusicList.remove(mCodeMusicListMap.get(mediaCode));
            }
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            ApMusic music = list.get(i);
            PineMediaPlayerBean mediaBean = transferMediaBean(music);
            mMusicList.add(0, music);
            mCodeMediaListMap.put(mediaBean.getMediaCode(), mediaBean);
            mCodeMusicListMap.put(mediaBean.getMediaCode(), music);
        }
        onMediaSelect(getMediaCode(mMusicList.get(0)), startPlay);
    }

    public void updateMusicData(ApMusic music) {
        if (music == null) {
            return;
        }
        String mediaCode = getMediaCode(music);
        if (mCodeMusicListMap.containsKey(mediaCode)) {
            ApMusic listedMusic = mCodeMusicListMap.get(mediaCode);
            if (listedMusic != null) {
                if (listedMusic.mediaInfoChange(music)) {
                    PineMediaPlayerBean mediaBean = transferMediaBean(music);
                    mCodeMediaListMap.put(mediaBean.getMediaCode(), mediaBean);
                }
                listedMusic.copyDataFrom(music);
            }
        }
    }

    public void updateMusicListData(List<ApMusic> list) {
        if (list == null && list.size() < 1) {
            return;
        }
        for (ApMusic music : list) {
            updateMusicData(music);
        }
    }

    public void removeMusic(ApMusic music) {
        ApMusic preMusic = getCurMusic();
        int curPos = findMusicPosition(getCurMediaCode());
        String removeMediaCode = getMediaCode(music);
        if (mCodeMusicListMap.containsKey(removeMediaCode)) {
            mMusicList.remove(mCodeMusicListMap.get(removeMediaCode));
            mCodeMediaListMap.remove(removeMediaCode);
            mCodeMusicListMap.remove(removeMediaCode);
        }
        if (mPlayerViewListenerMap.size() > 0) {
            Iterator<Map.Entry<Integer, AudioPlayerView.PlayerViewListener>> iterator = mPlayerViewListenerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                iterator.next().getValue().onMusicRemove(music);
            }
        }
        if (mMusicList.size() < 1) {
            onMusicListClear(preMusic);
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
        if (mPlayer != null) {
            mPlayer.removeMediaPlayerListener(mPlayerListener);
        }
        setMusicList(null, false);

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
        int position = findMusicPosition(getCurMediaCode());
        if (mControllerViewHolder != null) {
            if (mControllerViewHolder.getPrevButton() != null) {
                mControllerViewHolder.getPrevButton().setEnabled(isLoopMode() || position > 0);
            }
            if (mControllerViewHolder.getNextButton() != null) {
                mControllerViewHolder.getNextButton().setEnabled(isLoopMode() || position < mMusicList.size() - 1 && position >= 0);
            }
        }
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
            position = (position + mMusicList.size()) % mMusicList.size();
        }
        if (position >= 0 && position < mMusicList.size()) {
            if (mPlayer != null) {
                LogUtils.d(TAG, "playMedia position:" + position + ",startPlay:" + startPlay);
                ApMusic music = mMusicList.get(position);
                String mediaCode = getMediaCode(music);
                PineMediaPlayerBean bean = mCodeMediaListMap.get(mediaCode);
                if (!mediaCode.equals(getCurMediaCode())) {
                    mPlayer.setPlayingMedia(bean);
                    loadAlbumArtAndLyric(music);
                }
                if (startPlay) {
                    mPlayer.start();
                }
                if (mPlayerViewListenerMap.size() > 0) {
                    Iterator<Map.Entry<Integer, AudioPlayerView.PlayerViewListener>> iterator = mPlayerViewListenerMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        iterator.next().getValue().onPlayMusic(mPlayer, music);
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

    public void loadAlbumArtAndLyric(ApMusic music) {
        String mediaCode = "";
        if (music == null) {
            onAlbumArtPrepare(mediaCode, null, getSmallAlbumArtBitmap(mediaCode),
                    getBigAlbumArtBitmap(mediaCode), getMainAlbumArtColor(mediaCode));
            return;
        }
        mediaCode = getMediaCode(music);
        if (!mAlbumArtBitmapRequestingMap.containsKey(mediaCode)) {
            if ((mEnableSmallAlbumArt && !mSmallAlbumArtBitmapMap.containsKey(mediaCode) ||
                    mEnableBigAlbumArt && !mBigAlbumArtBitmapMap.containsKey(mediaCode) ||
                    !mMainAlbumArtColorMap.containsKey(mediaCode))) {
                LogUtils.d(TAG, "loadAlbumArtAndLyric getAlbumArtBitmapInBackground mediaCode:" + mediaCode);
                getAlbumArtBitmapInBackground(mediaCode, music.getSongId(), music.getAlbumId());
            } else {
                onAlbumArtPrepare(mediaCode, music, getSmallAlbumArtBitmap(mediaCode),
                        getBigAlbumArtBitmap(mediaCode), getMainAlbumArtColor(mediaCode));
            }
            if (TextUtils.isEmpty(music.getLyricFilePath()) || !new File(music.getLyricFilePath()).exists()) {
                if (!mLyricMap.containsKey(mediaCode)) {
                    if (!mLyricRequestingMap.containsKey(mediaCode)) {
                        getLyricInBackground(mediaCode, music);
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
                    smallBitmap = ApLocalMusicUtils.getAlbumArtBitmap(mContext,
                            songId, albumId, true);
                }
                Bitmap bigBitmap = null;
                if (finaEnableBigAlbumArt && !mBigAlbumArtBitmapMap.containsKey(mediaCode)) {
                    bigBitmap = ApLocalMusicUtils.getAlbumArtBitmap(mContext,
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
                            onAlbumArtPrepare(mediaCode, getCurMusic(),
                                    getSmallAlbumArtBitmap(mediaCode), getBigAlbumArtBitmap(mediaCode),
                                    getMainAlbumArtColor(mediaCode));
                        }
                    }
                });
            }
        });
        mAlbumArtBitmapRequestingMap.put(mediaCode, true);
    }

    private void onAlbumArtPrepare(String mediaCode, ApMusic music, Bitmap smallBitmap, Bitmap bigBitmap, int mainColor) {
        if (mAdapterListenerMap.size() > 0) {
            Iterator<Map.Entry<Integer, IControllerAdapterListener>> iterator = mAdapterListenerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                iterator.next().getValue().onAlbumArtPrepare(mediaCode, music, smallBitmap, bigBitmap, mainColor);
            }
        }
        if (mPlayerViewListenerMap.size() > 0) {
            Iterator<Map.Entry<Integer, AudioPlayerView.PlayerViewListener>> iterator = mPlayerViewListenerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                iterator.next().getValue().onAlbumArtChange(mediaCode, music, smallBitmap, bigBitmap, mainColor);
            }
        }
        mPreMainColor = mainColor;
    }

    private void getLyricInBackground(final String mediaCode, final ApMusic music) {
        if (mLrcWorkHandler == null) {
            return;
        }
        mLrcWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                final String filePath = ApLocalMusicUtils.getLyric(mContext, music);
                if (TextUtils.isEmpty(filePath)) {
                    return;
                }
                String charset = CharsetUtils.getCharset(filePath);
                onLyricPrepared(mediaCode, music, filePath, charset);
            }
        });
        mLyricRequestingMap.put(mediaCode, true);
    }

    private void onLyricPrepared(final String mediaCode, final ApMusic music,
                                 final String lrcFilePath, final String charset) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mLyricRequestingMap.remove(mediaCode);
                mLyricMap.put(mediaCode, lrcFilePath);
                ApMusic music = onLyricDownloaded(mediaCode, lrcFilePath, charset);
                if (mPlayerViewListenerMap.size() > 0) {
                    Iterator<Map.Entry<Integer, AudioPlayerView.PlayerViewListener>> iterator = mPlayerViewListenerMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        iterator.next().getValue().onLyricDownloaded(mediaCode, music, lrcFilePath, charset);
                    }
                }
            }
        });
    }

    private ApMusic onLyricDownloaded(String mediaCode, String filePath, String charset) {
        ApMusic music = mCodeMusicListMap.get(mediaCode);
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
                ((ApOutRootLrcPlugin) plugin).setSubtitle(filePath, PinePlayerConstants.PATH_STORAGE,
                        music.getLyricCharset());
            }
        }
        return music;
    }

    public interface IControllerAdapterListener {
        void onMusicStateChange(ApMusic music, PinePlayState state);

        void onAlbumArtPrepare(String mediaCode, ApMusic music, Bitmap smallBitmap,
                               Bitmap bigBitmap, int mainColor);
    }
}

