package com.pine.audioplayer.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.palette.graphics.Palette;

import com.pine.audioplayer.R;
import com.pine.audioplayer.bean.ApPlayListType;
import com.pine.audioplayer.databinding.ApItemSimpleAudioDialogBinding;
import com.pine.audioplayer.databinding.ApSimpleAudioDialogTitleBinding;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.util.ApLocalMusicUtils;
import com.pine.audioplayer.widget.adapter.ApAudioControllerAdapter;
import com.pine.audioplayer.widget.plugin.ApOutRootLrcPlugin;
import com.pine.base.util.DialogUtils;
import com.pine.base.widget.dialog.CustomListDialog;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.component.PinePlayState;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.PineMediaPlayerView;
import com.pine.tool.RootApplication;
import com.pine.tool.util.CharsetUtils;
import com.pine.tool.util.ImageUtils;
import com.pine.tool.util.LogUtils;

import java.io.File;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class AudioPlayerView extends RelativeLayout {
    protected final String TAG = LogUtils.makeLogTag(this.getClass());

    private ViewGroup mControllerRoot;

    private PineMediaPlayerView mMediaPlayerView;
    private PineMediaController mMediaController;
    protected ApAudioControllerAdapter mControllerAdapter;
    private PineMediaWidget.IPineMediaPlayer mMediaPlayer;

    private HandlerThread mWorkThread;
    private Handler mAlbumArtWorkHandler, mLrcWorkHandler;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    private final int MAX_BIG_ALBUM_ART_CACHE_COUNT = 10;
    private final int MAX_ALBUM_ART_REQUEST_COUNT = 2;
    private int DEFAULT_ALBUM_ART_MAIN_COLOR = Color.TRANSPARENT;
    private Bitmap mDefaultSmallAlbumArtBitmap = null;
    private Bitmap mDefaultBigAlbumArtBitmap = null;
    private HashMap<String, Bitmap> mSmallAlbumArtBitmapMap = new HashMap<>();
    private HashMap<String, Bitmap> mBigAlbumArtBitmapMap = new HashMap<>();
    private HashMap<String, Integer> mMainAlbumArtColorMap = new HashMap<>();
    private ArrayDeque<String> mBigAlbumArtDeque = new ArrayDeque<>();
    private HashMap<String, Boolean> mAlbumArtBitmapRequestingMap = new HashMap<>();
    protected int mPreMainColor = DEFAULT_ALBUM_ART_MAIN_COLOR;
    protected boolean mEnableSmallAlbumArt = false;
    protected boolean mEnableBigAlbumArt = false;
    protected boolean mEnableMainAlbumArtColor = false;

    protected int[] mPlayTypeResIds = {R.mipmap.res_ic_play_order_1_1,
            R.mipmap.res_ic_play_all_loop_1_1,
            R.mipmap.res_ic_play_single_loop_1_1,
            R.mipmap.res_ic_play_random_1_1};
    protected int[] mDialogPlayTypeResIds = {R.mipmap.res_ic_play_order_1_2,
            R.mipmap.res_ic_play_all_loop_1_2,
            R.mipmap.res_ic_play_single_loop_1_2,
            R.mipmap.res_ic_play_random_1_2};

    private CustomListDialog mMusicListDialog;

    private IPlayerViewListener mPlayerViewListener;
    private IOnListDialogListener mListDialogListener;

    private IPlayerListener mPlayerListener;

    private PineMediaWidget.PineMediaPlayerListener mMediaPlayerListener = new PineMediaWidget.PineMediaPlayerListener() {
        @Override
        public void onStateChange(PineMediaPlayerBean playerBean, PinePlayState fromState, PinePlayState toState) {
            boolean mediaChang = TextUtils.isEmpty(mControllerAdapter.getCurMediaCode()) ||
                    mMediaPlayer.isInPlaybackState() && !playerBean.getMediaCode().equals(mControllerAdapter.getCurMediaCode());
            boolean playStateChange = fromState != toState;
            if (mediaChang || playStateChange) {
                setupControllerView(mControllerAdapter.getCurMusic());
                if (mMusicListDialog != null && mMusicListDialog.isShowing()) {
                    mMusicListDialog.getListAdapter().notifyDataSetChangedSafely();
                }
                if (mPlayerListener != null) {
                    mPlayerListener.onPlayMusic(mControllerAdapter.getCurMusic(), toState == PinePlayState.STATE_PLAYING);
                }
            }
        }
    };

    public void setOnListDialogListener(IOnListDialogListener listDialogListener) {
        mListDialogListener = listDialogListener;
    }

    public AudioPlayerView(Context context) {
        super(context);
        initAudioPlayerView();
    }

    public AudioPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAudioPlayerView();
    }

    public AudioPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAudioPlayerView();
    }

    private void initAudioPlayerView() {
        mControllerRoot = initView();
        mMediaPlayerView = getMediaPlayerView();
        setBackgroundColor(mPreMainColor);
    }

    @Override
    protected void onDetachedFromWindow() {
        LogUtils.d(TAG, "onDetachedFromWindow");
        detachView();
        super.onDetachedFromWindow();
    }

    public void attachView() {
        if (mDefaultSmallAlbumArtBitmap != null) {
            mDefaultSmallAlbumArtBitmap.recycle();
            mDefaultSmallAlbumArtBitmap = null;
        }
        if (mDefaultBigAlbumArtBitmap != null) {
            mDefaultBigAlbumArtBitmap.recycle();
            mDefaultBigAlbumArtBitmap = null;
        }
        mDefaultSmallAlbumArtBitmap = ImageUtils.getBitmap(getContext(),
                R.mipmap.res_iv_top_bg_vertical);
        mDefaultBigAlbumArtBitmap = ImageUtils.getBitmap(getContext(),
                R.mipmap.res_iv_top_bg_vertical);
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

        ApSheetMusic curMusic = mControllerAdapter.getCurMusic();
        if (curMusic == null) {
            if (mControllerAdapter.getMusicList() != null && mControllerAdapter.getMusicList().size() > 0) {
                curMusic = mControllerAdapter.getMusicList().get(0);
                mControllerAdapter.onMediaSelect(mControllerAdapter.getMediaCode(curMusic), false);
            }
        }
        setupControllerView(curMusic);
    }

    public void detachView() {
        if (mMusicListDialog != null && mMusicListDialog.isShowing()) {
            mMusicListDialog.dismiss();
        }
        mPlayerListener = null;
        mMediaPlayer.removeMediaPlayerListener(mMediaPlayerListener);
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

    public void init(Context context, String tag, ApAudioControllerAdapter controllerAdapter,
                     IPlayerViewListener playerViewListener, IPlayerListener playerListener,
                     ApOutRootLrcPlugin.ILyricUpdateListener lyricUpdateListener) {
        mPlayerViewListener = playerViewListener;
        mPlayerListener = playerListener;
        mMediaController = new PineMediaController(getContext());
        mMediaPlayerView.disableBackPressTip();
        mMediaPlayerView.init(tag, mMediaController);
        mMediaPlayer = mMediaPlayerView.getMediaPlayer();
        mMediaPlayer.setAutocephalyPlayMode(true);
        mControllerAdapter = controllerAdapter;
        mControllerAdapter.setPlayerViewListener(mPlayerViewListener);
        mControllerAdapter.setLyricUpdateListener(lyricUpdateListener);
        mControllerAdapter.setControllerView(mControllerRoot, ApPlayListType.getDefaultList(getContext()));
        mMediaController.setMediaControllerAdapter(mControllerAdapter);
        mControllerAdapter.setupPlayer(mMediaPlayer);
        mMediaPlayer.addMediaPlayerListener(mMediaPlayerListener);

        if (mPlayerListener != null && mControllerAdapter.getCurMusic() != null) {
            mPlayerListener.onPlayMusic(mControllerAdapter.getCurMusic(), mMediaPlayer.isPlaying());
        }

        attachView();
    }

    public void showMusicListDialog() {
        if (mMusicListDialog == null) {
            mMusicListDialog = DialogUtils.createCustomListDialog(RootApplication.mCurResumedActivity, R.layout.ap_audio_dialog_title_layout,
                    R.layout.ap_item_audio_dialog, mControllerAdapter.getMusicList(),
                    new CustomListDialog.IOnViewBindCallback<ApSheetMusic>() {
                        private void setupPlayTypeImageInDialog(ImageView imageView) {
                            if (mControllerAdapter == null) {
                                imageView.setImageResource(mDialogPlayTypeResIds[0]);
                                return;
                            }
                            ApPlayListType playListType = mControllerAdapter.getCurPlayType();
                            switch (playListType.getType()) {
                                case ApPlayListType.TYPE_ORDER:
                                    imageView.setImageResource(mDialogPlayTypeResIds[0]);
                                    break;
                                case ApPlayListType.TYPE_ALL_LOOP:
                                    imageView.setImageResource(mDialogPlayTypeResIds[1]);
                                    break;
                                case ApPlayListType.TYPE_SING_LOOP:
                                    imageView.setImageResource(mDialogPlayTypeResIds[2]);
                                    break;
                                case ApPlayListType.TYPE_RANDOM:
                                    imageView.setImageResource(mDialogPlayTypeResIds[3]);
                                    break;
                            }
                        }

                        @Override
                        public void onTitleBind(View titleView, final CustomListDialog dialog) {
                            final ApSimpleAudioDialogTitleBinding binding = DataBindingUtil.bind(titleView);
                            binding.setPlayType(mControllerAdapter.getCurPlayType());
                            setupPlayTypeImageInDialog(binding.loopTypeBtn);
                            binding.setMediaCount(mControllerAdapter.getMusicList().size());
                            binding.playTypeLl.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    binding.setPlayType(mControllerAdapter.getAndGoNextPlayType());
                                    setupPlayTypeImageInDialog(binding.loopTypeBtn);
                                    if (mControllerAdapter != null) {
                                        setupPlayTypeImage(mControllerAdapter.getCurPlayType());
                                    }
                                }
                            });
                            binding.clearIv.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    List<ApSheetMusic> list = mControllerAdapter.getMusicList();
                                    mControllerAdapter.setMusicList(null, false);
                                    dialog.getListAdapter().setData(null);
                                    onMusicListClear(list);
                                    dialog.dismiss();
                                }
                            });
                            binding.downLl.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });
                            binding.executePendingBindings();
                        }

                        @Override
                        public void onItemBind(View itemView, int position,
                                               final ApSheetMusic data,
                                               final CustomListDialog dialog) {
                            ApItemSimpleAudioDialogBinding binding = DataBindingUtil.bind(itemView);
                            binding.setMusic(data);
                            PineMediaPlayerBean playerBean = mMediaPlayer.getMediaPlayerBean();
                            int playState = playerBean != null && playerBean.getMediaCode().equals(mControllerAdapter.getMediaCode(data)) ? (mMediaPlayer.isPlaying() ? 2 : 1) : 0;
                            binding.setPlayingState(playState);
                            if (binding.playStateIv.getDrawable() instanceof AnimationDrawable) {
                                ((AnimationDrawable) binding.playStateIv.getDrawable()).stop();
                            }
                            if (playState == 2) {
                                binding.playStateIv.setImageResource(R.drawable.res_anim_playing);
                                AnimationDrawable playAnim = (AnimationDrawable) binding.playStateIv.getDrawable();
                                playAnim.start();
                            } else {
                                binding.playStateIv.setImageResource(R.mipmap.res_ic_playing_1_1);
                            }
                            binding.deleteIv.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mControllerAdapter.removeMusic(data);
                                    List<ApSheetMusic> musicList = mControllerAdapter.getMusicList();
                                    dialog.getListAdapter().setData(musicList);
                                    onMusicRemove(data);
                                    if (musicList == null || musicList.size() < 1) {
                                        dialog.dismiss();
                                    }
                                }
                            });
                            binding.getRoot().setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    playMusicFromPlayList(data, true);
                                }
                            });
                            binding.executePendingBindings();
                        }
                    });
            mMusicListDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    if (mListDialogListener != null) {
                        mListDialogListener.onListDialogStateChange(true);
                    }
                }
            });
            mMusicListDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mListDialogListener != null) {
                        mListDialogListener.onListDialogStateChange(false);
                    }
                }
            });
        }
        if (!mMusicListDialog.isShowing()) {
            mMusicListDialog.show();
        }
    }

    public void dismissMusicListDialog() {
        if (mMusicListDialog != null && mMusicListDialog.isShowing()) {
            mMusicListDialog.dismiss();
        }
    }

    public void onLoopTypeClick() {
        mControllerAdapter.getAndGoNextPlayType();
        setupPlayTypeImage(mControllerAdapter.getCurPlayType());
    }

    public void onViewClick(View view, String tag) {
        view.setSelected(!view.isSelected());
        if (mPlayerViewListener != null) {
            ApSheetMusic music = mControllerAdapter.getCurMusic();
            mPlayerViewListener.onViewClick(view, music, tag);
        }
    }

    public void playMusicList(@NonNull List<ApSheetMusic> musicList, boolean startPlay) {
        if (musicList == null && musicList.size() < 1) {
            return;
        }
        mControllerAdapter.addMusicList(musicList, startPlay);
    }

    public void playMusic(@NonNull ApSheetMusic music, boolean startPlay) {
        if (music == null) {
            return;
        }
        mControllerAdapter.addMusic(music, startPlay);
    }

    public void playMusicFromPlayList(@NonNull ApSheetMusic music, boolean startPlay) {
        if (music == null) {
            return;
        }
        mControllerAdapter.onMediaSelect(mControllerAdapter.getMediaCode(music), startPlay);
    }

    public ApSheetMusic getCurMusic() {
        if (mControllerAdapter != null) {
            return mControllerAdapter.getCurMusic();
        }
        return null;
    }

    public void enableAlbumArt(boolean enableSmallAlbumArt, boolean enableBigAlbumArt, boolean enableMainAlbumArtColor) {
        mEnableSmallAlbumArt = enableSmallAlbumArt;
        mEnableBigAlbumArt = enableBigAlbumArt;
        mEnableMainAlbumArtColor = enableMainAlbumArtColor;
    }

    public Bitmap getBigAlbumArtBitmap() {
        String mediaCode = mControllerAdapter.getCurMediaCode();
        if (mAlbumArtBitmapRequestingMap.containsKey(mediaCode)) {
            return mDefaultBigAlbumArtBitmap;
        } else {
            if (mBigAlbumArtBitmapMap.containsKey(mediaCode)) {
                return mBigAlbumArtBitmapMap.get(mediaCode);
            } else {
                return mDefaultBigAlbumArtBitmap;
            }
        }
    }

    public int getMainAlbumArtColor() {
        if (mControllerAdapter == null || !mMainAlbumArtColorMap.containsKey(mControllerAdapter.getCurMediaCode())) {
            return DEFAULT_ALBUM_ART_MAIN_COLOR;
        }
        return mMainAlbumArtColorMap.get(mControllerAdapter.getCurMediaCode());
    }

    public void onMusicRemove(ApSheetMusic music) {
        if (mPlayerViewListener != null) {
            mPlayerViewListener.onMusicRemove(music);
        }
    }

    public void onMusicListClear(List<ApSheetMusic> musicList) {
        if (mPlayerViewListener != null) {
            mPlayerViewListener.onMusicListClear(musicList);
        }
        setupControllerView(null);
    }

    private void setupControllerView(ApSheetMusic music) {
        if (music != null) {
            final String mediaCode = music.getSongId() + "";
            if (mEnableSmallAlbumArt && !mSmallAlbumArtBitmapMap.containsKey(mediaCode) ||
                    mEnableBigAlbumArt && !mBigAlbumArtBitmapMap.containsKey(mediaCode) ||
                    mEnableMainAlbumArtColor && !mMainAlbumArtColorMap.containsKey(mediaCode)) {
                LogUtils.d(TAG, "setupControllerView getAlbumArtBitmapInBackground mediaCode:" + mediaCode);
                mSmallAlbumArtBitmapMap.put(mediaCode, mDefaultSmallAlbumArtBitmap);
                mBigAlbumArtBitmapMap.put(mediaCode, mDefaultBigAlbumArtBitmap);
                mMainAlbumArtColorMap.put(mediaCode, DEFAULT_ALBUM_ART_MAIN_COLOR);
                getAlbumArtBitmapInBackground(mediaCode, music.getSongId(), music.getAlbumId());
            } else {
                if (!mAlbumArtBitmapRequestingMap.containsKey(mediaCode)) {
                    onAlbumArtPrepare(music, mSmallAlbumArtBitmapMap.get(mediaCode),
                            mBigAlbumArtBitmapMap.get(mediaCode),
                            mMainAlbumArtColorMap.get(mediaCode));
                }
            }
            if ((TextUtils.isEmpty(music.getLyricFilePath()) || !new File(music.getLyricFilePath()).exists())) {
                getLyricInBackground(mediaCode, music);
            }
        }
        if (mControllerAdapter != null) {
            setupPlayTypeImage(mControllerAdapter.getCurPlayType());
            mControllerAdapter.refreshPreNextBtnState();
        }
        boolean hasMedia = mControllerAdapter != null && mControllerAdapter.getMusicList().size() > 0;
        setupMusicView(music, hasMedia);
        if (!hasMedia) {
            dismissMusicListDialog();
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
        mAlbumArtBitmapRequestingMap.put(mediaCode, true);
        mAlbumArtWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                LogUtils.d(TAG, "getAlbumArtBitmapInBackground start --- mediaCode:" + mediaCode);
                Bitmap smallBitmap = null;
                if (mEnableSmallAlbumArt) {
                    smallBitmap = ApLocalMusicUtils.getAlbumArtBitmap(getContext(),
                            songId, albumId, true);
                }
                Bitmap bigBitmap = null;
                if (mEnableBigAlbumArt) {
                    bigBitmap = ApLocalMusicUtils.getAlbumArtBitmap(getContext(),
                            songId, albumId, false);
                }
                int color = DEFAULT_ALBUM_ART_MAIN_COLOR;
                if (smallBitmap != null || bigBitmap != null) {
                    Palette palette = new Palette.Builder(smallBitmap != null ? smallBitmap : bigBitmap).generate();
                    color = palette.getDominantColor(DEFAULT_ALBUM_ART_MAIN_COLOR);
                }
                final Bitmap finalSmallBitmap = smallBitmap == null ? mDefaultSmallAlbumArtBitmap : smallBitmap;
                final Bitmap finalBigBitmap = bigBitmap == null ? mDefaultBigAlbumArtBitmap : bigBitmap;
                final int finalColor = color;
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        LogUtils.d(TAG, "getAlbumArtBitmapInBackground end --- mediaCode:" + mediaCode);
                        mAlbumArtBitmapRequestingMap.remove(mediaCode);
                        mSmallAlbumArtBitmapMap.put(mediaCode, finalSmallBitmap);

                        if (mBigAlbumArtDeque.size() >= MAX_BIG_ALBUM_ART_CACHE_COUNT) {
                            String mediaCode = mBigAlbumArtDeque.poll();
                            Bitmap bitmap = mBigAlbumArtBitmapMap.remove(mediaCode);
                            if (bitmap != null) {
                                bitmap.recycle();
                            }
                        }
                        mBigAlbumArtBitmapMap.put(mediaCode, finalBigBitmap);
                        if (finalBigBitmap != null) {
                            mBigAlbumArtDeque.add(mediaCode);
                        }
                        mMainAlbumArtColorMap.put(mediaCode, finalColor);
                        if (mControllerAdapter != null && mediaCode.equals(mControllerAdapter.getCurMediaCode())) {
                            onAlbumArtPrepare(mControllerAdapter.getCurMusic(),
                                    finalSmallBitmap, finalBigBitmap, mMainAlbumArtColorMap.get(mediaCode));
                        }
                    }
                });
            }
        });
    }

    private void onAlbumArtPrepare(ApSheetMusic music, Bitmap smallBitmap, Bitmap bigBitmap, int mainColor) {
        setupAlbumArt(smallBitmap, bigBitmap, mainColor);
        if (mPlayerListener != null) {
            mPlayerListener.onAlbumArtThemeChange(music, mainColor);
        }
        mPreMainColor = mainColor;
    }

    private void getLyricInBackground(final String mediaCode, final ApSheetMusic music) {
        if (mLrcWorkHandler == null) {
            return;
        }
        mLrcWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                final String filePath = ApLocalMusicUtils.getLyric(getContext(), music);
                if (TextUtils.isEmpty(filePath)) {
                    return;
                }
                String charset = CharsetUtils.getCharset(filePath);
                onLyricPrepared(mediaCode, music, filePath, charset);
            }
        });
    }

    private void onLyricPrepared(final String mediaCode, final ApSheetMusic music,
                                 final String lrcFilePath, final String charset) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mPlayerViewListener != null && mControllerAdapter != null) {
                    ApSheetMusic music = mControllerAdapter.onLyricDownloaded(mediaCode, lrcFilePath, charset);
                    if (music != null) {
                        mPlayerViewListener.onLyricDownloaded(music, lrcFilePath, charset);
                    }
                }
            }
        });
    }

    public abstract ViewGroup initView();

    public abstract PineMediaPlayerView getMediaPlayerView();

    public abstract void setupPlayTypeImage(ApPlayListType playListType);

    public abstract void setupAlbumArt(Bitmap smallBitmap, Bitmap bigBitmap, int mainColor);

    public abstract void setupMusicView(ApSheetMusic music, boolean hasMedia);

    public interface IPlayerViewListener {
        void onPlayMusic(PineMediaWidget.IPineMediaPlayer player, ApSheetMusic oldPlayMusic, ApSheetMusic newPlayMusic);

        void onLyricDownloaded(ApSheetMusic music, String filePath, String charset);

        void onMusicRemove(ApSheetMusic music);

        void onMusicListClear(List<ApSheetMusic> musicList);

        void onViewClick(View view, ApSheetMusic music, String tag);
    }

    public interface IOnListDialogListener {
        void onListDialogStateChange(boolean isShown);
    }

    public interface IPlayerListener {
        void onPlayMusic(ApSheetMusic music, boolean isPlaying);

        void onAlbumArtThemeChange(ApSheetMusic music, int mainColor);
    }
}
