package com.pine.audioplayer.widget.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.pine.audioplayer.R;
import com.pine.audioplayer.bean.ApPlayListType;
import com.pine.audioplayer.databinding.ApItemSimpleAudioDialogBinding;
import com.pine.audioplayer.databinding.ApSimpleAudioDialogTitleBinding;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.util.ApLocalMusicUtils;
import com.pine.audioplayer.widget.IAudioPlayerView;
import com.pine.audioplayer.widget.adapter.ApAudioControllerAdapter;
import com.pine.base.util.DialogUtils;
import com.pine.base.widget.dialog.CustomListDialog;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.component.PinePlayState;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.PineMediaPlayerView;
import com.pine.tool.RootApplication;
import com.pine.tool.util.LogUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

public class ApMainAudioPlayerView extends RelativeLayout implements IAudioPlayerView {
    private final String TAG = LogUtils.makeLogTag(this.getClass());

    private View mRoot;
    private ImageView mapv_loop_type_btn, mapv_media_list_btn, mapv_favourite_btn;

    private PineMediaPlayerView mMediaPlayerView;
    private PineMediaController mMediaController;
    private PineMediaWidget.IPineMediaPlayer mMediaPlayer;
    private ApAudioControllerAdapter mControllerAdapter;

    private HandlerThread mWorkThread;
    private Handler mWorkThreadHandler;
    private HashMap<String, Bitmap> mAlbumArtBitmapMap = new HashMap<>();
    private HashMap<String, String> mLyricMap = new HashMap<>();

    private CustomListDialog mMusicListDialog;

    private IPlayerViewListener mPlayerViewListener;
    private IOnListDialogListener mListDialogListener;

    private PineMediaWidget.PineMediaPlayerListener mPlayerListener = new PineMediaWidget.PineMediaPlayerListener() {
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
            }
        }
    };

    public void setOnListDialogListener(IOnListDialogListener listDialogListener) {
        mListDialogListener = listDialogListener;
    }

    public ApMainAudioPlayerView(Context context) {
        super(context);
        initView();
    }

    public ApMainAudioPlayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ApMainAudioPlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mRoot = LayoutInflater.from(getContext()).inflate(R.layout.ap_main_audio_player_view, this, true);
        mMediaPlayerView = mRoot.findViewById(R.id.media_player_view);
        mapv_loop_type_btn = mRoot.findViewById(R.id.mapv_loop_type_btn);
        mapv_media_list_btn = mRoot.findViewById(R.id.mapv_media_list_btn);
        mapv_favourite_btn = mRoot.findViewById(R.id.mapv_favourite_btn);

        mapv_loop_type_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mControllerAdapter.getAndGoNextPlayType();
                setupPlayTypeImage(mapv_loop_type_btn);
            }
        });
        mapv_media_list_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMusicListDialog != null && mMusicListDialog.isShowing()) {
                    mMusicListDialog.dismiss();
                }
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
                                            setupPlayTypeImage(mapv_loop_type_btn);
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
                mMusicListDialog.show();
            }
        });
        mapv_favourite_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerViewListener != null) {
                    mPlayerViewListener.onViewClick(mapv_favourite_btn, "favourite");
                }
            }
        });

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
        mMediaPlayer.removeMediaPlayerListener(mPlayerListener);
        clearBitmap();
        if (mWorkThreadHandler != null) {
            mAlbumArtBitmapMap.clear();
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
        Iterator<Map.Entry<String, Bitmap>> iterator = mAlbumArtBitmapMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Bitmap bitmap = iterator.next().getValue();
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    @Override
    public void init(Context context, String tag, ApAudioControllerAdapter controllerAdapter,
                     IPlayerViewListener playerViewListener,
                     ILyricUpdateListener lyricUpdateListener) {
        mPlayerViewListener = playerViewListener;
        mMediaController = new PineMediaController(getContext());
        mMediaPlayerView.init(tag, mMediaController);
        mMediaPlayer = mMediaPlayerView.getMediaPlayer();
        mMediaPlayer.setAutocephalyPlayMode(true);
        mControllerAdapter = controllerAdapter;
        mControllerAdapter.setPlayerViewListener(mPlayerViewListener);
        mControllerAdapter.setLyricUpdateListener(lyricUpdateListener);
        mControllerAdapter.setControllerView((ViewGroup) mRoot, ApPlayListType.getDefaultList(getContext()));
        mMediaController.setMediaControllerAdapter(mControllerAdapter);
        mControllerAdapter.setupPlayer(mMediaPlayer);
        mMediaPlayer.addMediaPlayerListener(mPlayerListener);
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

    private void playMusicFromPlayList(@NonNull ApSheetMusic music, boolean startPlay) {
        if (music == null) {
            return;
        }
        mControllerAdapter.onMediaSelect(mControllerAdapter.getMediaCode(music), startPlay);
        setupControllerView(music);
    }

    private void onMusicRemove(ApSheetMusic music) {
        if (mPlayerViewListener != null) {
            mPlayerViewListener.onMusicRemove(music);
        }
        setupControllerView(mControllerAdapter.getCurMusic());
    }

    private void onMusicListClear(List<ApSheetMusic> musicList) {
        if (mPlayerViewListener != null) {
            mPlayerViewListener.onMusicListClear(musicList);
        }
        setupControllerView(null);
    }

    private void setupControllerView(ApSheetMusic music) {
        if (music != null) {
            final String mediaCode = music.getSongId() + "";
            if (!mAlbumArtBitmapMap.containsKey(mediaCode)) {
                getAlbumArtBitmapInBackground(mediaCode, music.getSongId(), music.getAlbumId());
            }
            if (!mLyricMap.containsKey(mediaCode)) {
                getLyricInBackground(mediaCode, music.getSongId(), music.getAlbumId());
            }
        }
        if (mControllerAdapter != null) {
            setupPlayTypeImage(mapv_loop_type_btn);
            mControllerAdapter.refreshPreNextBtnState();
        }
        boolean hasMedia = mControllerAdapter != null && mControllerAdapter.getMusicList().size() > 0;
        mapv_loop_type_btn.setEnabled(hasMedia);
        mapv_media_list_btn.setEnabled(hasMedia);
        mapv_favourite_btn.setEnabled(hasMedia);
        if (!hasMedia) {
            if (mMusicListDialog != null && mMusicListDialog.isShowing()) {
                mMusicListDialog.dismiss();
            }
        }
    }

    private void setupPlayTypeImage(ImageView imageView) {
        if (mControllerAdapter == null) {
            mapv_loop_type_btn.setImageResource(R.mipmap.res_ic_play_all_loop_1_1);
            return;
        }
        ApPlayListType playListType = mControllerAdapter.getCurPlayType();
        switch (playListType.getType()) {
            case ApPlayListType.TYPE_ORDER:
                mapv_loop_type_btn.setImageResource(R.mipmap.res_ic_play_order_1_1);
                break;
            case ApPlayListType.TYPE_ALL_LOOP:
                mapv_loop_type_btn.setImageResource(R.mipmap.res_ic_play_all_loop_1_1);
                break;
            case ApPlayListType.TYPE_SING_LOOP:
                mapv_loop_type_btn.setImageResource(R.mipmap.res_ic_play_single_loop_1_1);
                break;
            case ApPlayListType.TYPE_RANDOM:
                mapv_loop_type_btn.setImageResource(R.mipmap.res_ic_play_random_1_1);
                break;
        }
    }

    private void getAlbumArtBitmapInBackground(final String mediaCode, final long songId, final long albumId) {
        if (mWorkThreadHandler == null) {
            return;
        }
        mWorkThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = ApLocalMusicUtils.getAlbumArtBitmap(getContext(),
                        songId, albumId, true);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mAlbumArtBitmapMap.put(mediaCode, bitmap);
                    }
                });
            }
        });
    }

    private void getLyricInBackground(final String mediaCode, final long songId, final long albumId) {
        if (mWorkThreadHandler == null) {
            return;
        }
        mWorkThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                final String filePath = ApLocalMusicUtils.getLyric(getContext(),
                        songId, albumId);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mLyricMap.put(mediaCode, filePath);
                        if (mPlayerViewListener != null && mControllerAdapter != null) {
                            ApSheetMusic music = mControllerAdapter.onLyricDownloaded(mediaCode, filePath);
                            if (music != null) {
                                mPlayerViewListener.onLyricDownloaded(music, filePath);
                            }
                        }
                    }
                });
            }
        });
    }
}
