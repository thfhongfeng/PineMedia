package com.pine.audioplayer.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
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
import com.pine.tool.util.LogUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class AudioPlayerView extends RelativeLayout {
    private final String TAG = LogUtils.makeLogTag(this.getClass());
    protected ApAudioControllerAdapter mControllerAdapter;
    protected PineMediaWidget.IPineMediaPlayer mMediaPlayer;

    private ViewGroup mControllerRoot;

    private PineMediaPlayerView mMediaPlayerView;
    public PineMediaController mMediaController;

    private HandlerThread mWorkThread;
    private Handler mWorkThreadHandler;
    private HashMap<String, Bitmap> mSmallAlbumArtBitmapMap = new HashMap<>();
    private HashMap<String, Bitmap> mBigAlbumArtBitmapMap = new HashMap<>();

    private CustomListDialog mMusicListDialog;

    private IPlayerViewListener mPlayerViewListener;
    private IOnListDialogListener mListDialogListener;

    private IPlayerListener mPlayerListener;

    private PineMediaWidget.PineMediaPlayerListener mMediaPlayerListener = new PineMediaWidget.PineMediaPlayerListener() {
        @Override
        public void onStateChange(PineMediaPlayerBean playerBean, PinePlayState fromState, PinePlayState toState) {
            boolean mediaChange = TextUtils.isEmpty(mControllerAdapter.getCurMediaCode()) ||
                    mMediaPlayer.isInPlaybackState() && !playerBean.getMediaCode().equals(mControllerAdapter.getCurMediaCode());
            boolean playStateChange = fromState != toState;
            if (mediaChange || playStateChange) {
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
        setupControllerView(null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mWorkThread == null) {
            mWorkThread = new HandlerThread(TAG);
            mWorkThread.start();
        }
        if (mWorkThreadHandler == null) {
            mWorkThreadHandler = new Handler(mWorkThread.getLooper());
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

    @Override
    protected void onDetachedFromWindow() {
        if (mMusicListDialog != null && mMusicListDialog.isShowing()) {
            mMusicListDialog.dismiss();
        }
        mPlayerListener = null;
        mMediaPlayer.removeMediaPlayerListener(mMediaPlayerListener);
        clearBitmap();
        if (mWorkThreadHandler != null) {
            mSmallAlbumArtBitmapMap.clear();
            mBigAlbumArtBitmapMap.clear();
            mWorkThreadHandler.removeCallbacksAndMessages(null);
            mWorkThreadHandler = null;
        }
        if (mWorkThread != null) {
            mWorkThread.quit();
            mWorkThread = null;
        }
        super.onDetachedFromWindow();
    }

    private void clearBitmap() {
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
    }

    public void showMusicListDialog() {
        if (mMusicListDialog == null) {
            mMusicListDialog = DialogUtils.createCustomListDialog(RootApplication.mCurResumedActivity, R.layout.ap_audio_dialog_title_layout,
                    R.layout.ap_item_audio_dialog, mControllerAdapter.getMusicList(),
                    new CustomListDialog.IOnViewBindCallback<ApSheetMusic>() {
                        private void setupPlayTypeImageInDialog(ImageView imageView) {
                            if (mControllerAdapter == null) {
                                imageView.setImageResource(R.mipmap.res_ic_play_all_loop);
                                return;
                            }
                            ApPlayListType playListType = mControllerAdapter.getCurPlayType();
                            switch (playListType.getType()) {
                                case ApPlayListType.TYPE_ORDER:
                                    imageView.setImageResource(R.mipmap.res_ic_play_order);
                                    break;
                                case ApPlayListType.TYPE_ALL_LOOP:
                                    imageView.setImageResource(R.mipmap.res_ic_play_all_loop);
                                    break;
                                case ApPlayListType.TYPE_SING_LOOP:
                                    imageView.setImageResource(R.mipmap.res_ic_play_single_loop);
                                    break;
                                case ApPlayListType.TYPE_RANDOM:
                                    imageView.setImageResource(R.mipmap.res_ic_play_random);
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
        setupControllerView(music);
    }

    public void playMusicFromPlayList(@NonNull ApSheetMusic music, boolean startPlay) {
        if (music == null) {
            return;
        }
        mControllerAdapter.onMediaSelect(mControllerAdapter.getMediaCode(music), startPlay);
        setupControllerView(music);
    }

    public void onMusicRemove(ApSheetMusic music) {
        if (mPlayerViewListener != null) {
            mPlayerViewListener.onMusicRemove(music);
        }
        setupControllerView(mControllerAdapter.getCurMusic());
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
            if (!mSmallAlbumArtBitmapMap.containsKey(mediaCode) || !mBigAlbumArtBitmapMap.containsKey(mediaCode)) {
                getAlbumArtBitmapInBackground(mediaCode, music.getSongId(), music.getAlbumId());
            } else {
                setupAlbumArtBitmapView(mSmallAlbumArtBitmapMap.get(mediaCode), mBigAlbumArtBitmapMap.get(mediaCode));
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
        if (mWorkThreadHandler == null) {
            return;
        }
        mWorkThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                final Bitmap smallBitmap = ApLocalMusicUtils.getAlbumArtBitmap(getContext(),
                        songId, albumId, true);
                final Bitmap bigBitmap = ApLocalMusicUtils.getAlbumArtBitmap(getContext(),
                        songId, albumId, true);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mSmallAlbumArtBitmapMap.put(mediaCode, smallBitmap);
                        mBigAlbumArtBitmapMap.put(mediaCode, bigBitmap);
                        setupAlbumArtBitmapView(smallBitmap, bigBitmap);
                    }
                });
            }
        });
    }

    private void getLyricInBackground(final String mediaCode, final ApSheetMusic music) {
        if (mWorkThreadHandler == null) {
            return;
        }
        mWorkThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                final String filePath = ApLocalMusicUtils.getLyric(getContext(),
                        music);
                if (TextUtils.isEmpty(filePath)) {
                    return;
                }
                onLyricPrepared(mediaCode, music, filePath);
            }
        });
    }

    private void onLyricPrepared(final String mediaCode, final ApSheetMusic music, final String lrcFilePath) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mPlayerViewListener != null && mControllerAdapter != null) {
                    ApSheetMusic music = mControllerAdapter.onLyricDownloaded(mediaCode, lrcFilePath);
                    if (music != null) {
                        mPlayerViewListener.onLyricDownloaded(music, lrcFilePath);
                    }
                }
            }
        });
    }

    public abstract ViewGroup initView();

    public abstract PineMediaPlayerView getMediaPlayerView();

    public abstract void setupPlayTypeImage(ApPlayListType playListType);

    public abstract void setupAlbumArtBitmapView(Bitmap smallBitmap, Bitmap bigBitmap);

    public abstract void setupMusicView(ApSheetMusic music, boolean hasMedia);

    public interface IPlayerViewListener {
        void onPlayMusic(PineMediaWidget.IPineMediaPlayer player, ApSheetMusic oldPlayMusic, ApSheetMusic newPlayMusic);

        void onLyricDownloaded(ApSheetMusic music, String filePath);

        void onMusicRemove(ApSheetMusic music);

        void onMusicListClear(List<ApSheetMusic> musicList);

        void onViewClick(View view, ApSheetMusic music, String tag);
    }

    public interface IOnListDialogListener {
        void onListDialogStateChange(boolean isShown);
    }

    public interface IPlayerListener {
        void onPlayMusic(ApSheetMusic music, boolean isPlaying);
    }
}
