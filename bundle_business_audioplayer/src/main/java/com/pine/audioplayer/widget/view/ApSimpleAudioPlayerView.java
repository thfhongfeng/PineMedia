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
import android.widget.TextView;

import com.pine.audioplayer.R;
import com.pine.audioplayer.bean.ApPlayListType;
import com.pine.audioplayer.databinding.ApItemSimpleAudioDialogBinding;
import com.pine.audioplayer.databinding.ApSimpleAudioDialogTitleBinding;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.uitls.ApLocalMusicUtils;
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

public class ApSimpleAudioPlayerView extends RelativeLayout implements IAudioPlayerView {
    private final String TAG = LogUtils.makeLogTag(this.getClass());

    private View mRoot;
    private ViewGroup sapv_controller_view, sapv_content_ll;
    private ImageView sapv_cover_iv, sapv_media_list_btn;
    private TextView sapv_name_tv, sapv_desc_tv;

    private PineMediaPlayerView mMediaPlayerView;
    private PineMediaController mMediaController;
    private PineMediaWidget.IPineMediaPlayer mMediaPlayer;
    private ApAudioControllerAdapter mControllerAdapter;

    private HandlerThread mAlbumArtBitmapThread;
    private Handler mAlbumArtBitmapThreadHandler;
    private HashMap<String, Bitmap> mAlbumArtBitmapMap = new HashMap<>();

    private CustomListDialog mMusicListDialog;

    private IAudioPlayerView.IPlayerViewListener mPlayerViewListener;
    private IAudioPlayerView.IOnListDialogListener mListDialogListener;

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

    public void setOnListDialogListener(IAudioPlayerView.IOnListDialogListener listDialogListener) {
        mListDialogListener = listDialogListener;
    }

    public ApSimpleAudioPlayerView(Context context) {
        super(context);
        initView();
    }

    public ApSimpleAudioPlayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ApSimpleAudioPlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mRoot = LayoutInflater.from(getContext()).inflate(R.layout.ap_simple_audio_player_view, this, true);
        mMediaPlayerView = mRoot.findViewById(R.id.media_player_view);
        sapv_controller_view = mRoot.findViewById(R.id.sapv_controller_view);
        sapv_content_ll = mRoot.findViewById(R.id.sapv_content_ll);
        sapv_cover_iv = mRoot.findViewById(R.id.sapv_cover_iv);
        sapv_name_tv = mRoot.findViewById(R.id.sapv_name_tv);
        sapv_desc_tv = mRoot.findViewById(R.id.sapv_desc_tv);
        sapv_media_list_btn = mRoot.findViewById(R.id.sapv_media_list_btn);

        sapv_media_list_btn.setOnClickListener(new OnClickListener() {
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
        sapv_content_ll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerViewListener != null) {
                    mPlayerViewListener.onViewClick(sapv_content_ll, "content");
                }
            }
        });

        setupControllerView(null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mAlbumArtBitmapThread == null) {
            mAlbumArtBitmapThread = new HandlerThread(TAG);
            mAlbumArtBitmapThread.start();
        }
        if (mAlbumArtBitmapThreadHandler == null) {
            mAlbumArtBitmapThreadHandler = new Handler(mAlbumArtBitmapThread.getLooper());
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
        if (mAlbumArtBitmapThreadHandler != null) {
            mAlbumArtBitmapMap.clear();
            mAlbumArtBitmapThreadHandler.removeCallbacksAndMessages(null);
            mAlbumArtBitmapThreadHandler = null;
        }
        if (mAlbumArtBitmapThread != null) {
            mAlbumArtBitmapThread.quit();
            mAlbumArtBitmapThread = null;
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
                     IAudioPlayerView.IPlayerViewListener playerViewListener,
                     ILyricUpdateListener lyricUpdateListener) {
        mPlayerViewListener = playerViewListener;
        mMediaController = new PineMediaController(getContext());
        mMediaPlayerView.init(tag, mMediaController);
        mMediaPlayer = mMediaPlayerView.getMediaPlayer();
        mMediaPlayer.setAutocephalyPlayMode(true);
        mControllerAdapter = controllerAdapter;
        mControllerAdapter.setControllerView(sapv_controller_view, ApPlayListType.getDefaultList(getContext()));
        mControllerAdapter.setPlayerViewListener(mPlayerViewListener);
        mControllerAdapter.setLyricUpdateListener(lyricUpdateListener);
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
            } else {
                Bitmap bitmap = mAlbumArtBitmapMap.get(mediaCode);
                if (bitmap != null) {
                    sapv_cover_iv.setImageBitmap(bitmap);
                } else {
                    sapv_cover_iv.setImageResource(R.mipmap.res_iv_top_bg);
                }
            }
            sapv_name_tv.setText(music.getName());
            sapv_desc_tv.setText(music.getAuthor() + " - " + music.getAlbum());
        } else {
            sapv_cover_iv.setImageResource(R.mipmap.res_iv_top_bg);
            sapv_name_tv.setText(R.string.ap_sad_play_pine_name);
            sapv_desc_tv.setText(R.string.ap_sad_play_pine_desc);
        }
        if (mControllerAdapter != null) {
            mControllerAdapter.refreshPreNextBtnState();
        }
        boolean hasMedia = mControllerAdapter != null && mControllerAdapter.getMusicList().size() > 0;
        sapv_media_list_btn.setEnabled(hasMedia);
        if (!hasMedia) {
            if (mMusicListDialog != null && mMusicListDialog.isShowing()) {
                mMusicListDialog.dismiss();
            }
        }
    }

    private void getAlbumArtBitmapInBackground(final String mediaCode, final long songId, final long albumId) {
        if (mAlbumArtBitmapThreadHandler == null) {
            sapv_cover_iv.setImageResource(R.mipmap.res_iv_top_bg);
            return;
        }
        mAlbumArtBitmapThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = ApLocalMusicUtils.getAlbumArtBitmap(getContext(),
                        songId, albumId, true);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mAlbumArtBitmapMap.put(mediaCode, bitmap);
                        if (bitmap != null) {
                            sapv_cover_iv.setImageBitmap(bitmap);
                        } else {
                            sapv_cover_iv.setImageResource(R.mipmap.res_iv_top_bg);
                        }
                    }
                });
            }
        });
    }
}
