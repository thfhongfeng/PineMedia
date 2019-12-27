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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.pine.audioplayer.R;
import com.pine.audioplayer.databinding.ApItemSimpleAudioDialogBinding;
import com.pine.audioplayer.databinding.ApSimpleAudioDialogTitleBinding;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.uitls.ApLocalMusicUtils;
import com.pine.audioplayer.widget.adapter.ApSimpleAudioControllerAdapter;
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

public class ApSimpleAudioPlayerView extends RelativeLayout {
    private final String TAG = LogUtils.makeLogTag(this.getClass());

    private View mRoot;
    private ViewGroup sapv_controller_view;
    private ImageView sapv_cover_iv, sapv_next_btn, sapv_list_btn;
    private TextView sapv_name_tv, sapv_desc_tv;

    private PineMediaPlayerView mMediaPlayerView;
    private PineMediaController mMediaController;
    private PineMediaWidget.IPineMediaPlayer mMediaPlayer;
    private ApSimpleAudioControllerAdapter mControllerAdapter;

    private HandlerThread mAlbumArtBitmapThread;
    private Handler mAlbumArtBitmapThreadHandler;
    private HashMap<String, Bitmap> mAlbumArtBitmapMap = new HashMap<>();

    private CustomListDialog mMusicListDialog;

    private IOnMediaListChangeListener mMediaListChangeListener;
    private IOnListDialogListener mListDialogListener;

    public ViewGroup getControllerView() {
        return sapv_controller_view;
    }

    public void setOnListDialogListener(IOnListDialogListener listDialogListener) {
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
        mRoot = LayoutInflater.from(getContext()).inflate(R.layout.ap_simple_audio_layout, this, true);
        mMediaPlayerView = mRoot.findViewById(R.id.sapv_player_view);
        sapv_controller_view = mRoot.findViewById(R.id.sapv_controller_view);

        sapv_cover_iv = mRoot.findViewById(R.id.sapv_cover_iv);
        sapv_name_tv = mRoot.findViewById(R.id.sapv_name_tv);
        sapv_desc_tv = mRoot.findViewById(R.id.sapv_desc_tv);
        sapv_next_btn = mRoot.findViewById(R.id.sapv_next_btn);
        sapv_list_btn = mRoot.findViewById(R.id.sapv_list_btn);

        sapv_list_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMusicListDialog != null && mMusicListDialog.isShowing()) {
                    mMusicListDialog.dismiss();
                }
                mMusicListDialog = DialogUtils.createCustomListDialog(RootApplication.mCurResumedActivity, R.layout.ap_simple_audio_dialog_title_layout,
                        R.layout.ap_item_simple_audio_dialog, mControllerAdapter.getMusicList(),
                        new CustomListDialog.IOnViewBindCallback<ApSheetMusic>() {
                            @Override
                            public void onTitleBind(View titleView, final CustomListDialog dialog) {
                                final ApSimpleAudioDialogTitleBinding binding = DataBindingUtil.bind(titleView);
                                binding.setPlayType(mControllerAdapter.getNextPlayType());
                                binding.setMediaCount(mControllerAdapter.getMusicList().size());
                                binding.playTypeIv.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        binding.setPlayType(mControllerAdapter.goNextPlayType());
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
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mMusicListDialog != null && mMusicListDialog.isShowing()) {
            mMusicListDialog.dismiss();
        }
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

    public void init(Context context, String tag, IOnMediaListChangeListener mediaListChangeListener) {
        init(context, tag, mediaListChangeListener);
    }

    public void init(Context context, String tag, ApSimpleAudioControllerAdapter controllerAdapter,
                     IOnMediaListChangeListener mediaListChangeListener) {
        mMediaListChangeListener = mediaListChangeListener;
        mMediaController = new PineMediaController(getContext());
        mMediaPlayerView.init(tag, mMediaController);
        mMediaPlayer = mMediaPlayerView.getMediaPlayer();
        mMediaPlayer.setAutocephalyPlayMode(true);
        mControllerAdapter = controllerAdapter;
        mMediaController.setMediaControllerAdapter(mControllerAdapter);
        mMediaPlayer.addMediaPlayerListener(new PineMediaWidget.PineMediaPlayerListener() {
            @Override
            public void onStateChange(PineMediaPlayerBean playerBean, PinePlayState fromState, PinePlayState toState) {
                boolean mediaChange = TextUtils.isEmpty(mControllerAdapter.getCurMediaCode()) ||
                        mMediaPlayer.isInPlaybackState() && !playerBean.getMediaCode().equals(mControllerAdapter.getCurMediaCode());
                boolean playStateChange = fromState != toState;
                if (mediaChange || playStateChange) {
                    mControllerAdapter.setCurMediaCode(playerBean.getMediaCode());
                    setupControllerView(mControllerAdapter.getCurMusic());
                    if (mMusicListDialog != null && mMusicListDialog.isShowing()) {
                        mMusicListDialog.getListAdapter().notifyDataSetChangedSafely();
                    }
                }
            }
        });

        ApSheetMusic curMusic = mControllerAdapter.getCurMusic();
        if (curMusic == null) {
            if (mControllerAdapter.getMusicList() != null && mControllerAdapter.getMusicList().size() > 0) {
                curMusic = mControllerAdapter.getMusicList().get(0);
                mControllerAdapter.onMediaSelect(mControllerAdapter.getMediaCode(curMusic), false);
            }
        }
        setupControllerView(curMusic);
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
        mControllerAdapter.onMediaSelect(mControllerAdapter.getMediaCode(music), startPlay);
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
        if (mMediaListChangeListener != null) {
            mMediaListChangeListener.onMediaRemove(music);
        }
        setupControllerView(mControllerAdapter.getCurMusic());
    }

    private void onMusicListClear(List<ApSheetMusic> musicList) {
        if (mMediaListChangeListener != null) {
            mMediaListChangeListener.onMediaListClear(musicList);
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

        boolean hasMedia = mControllerAdapter != null && mControllerAdapter.getMusicList().size() > 0;
        sapv_next_btn.setEnabled(hasMedia);
        sapv_next_btn.setSelected(hasMedia);
        sapv_list_btn.setEnabled(hasMedia);
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

    public interface IOnMediaListChangeListener {
        void onMediaRemove(ApSheetMusic music);

        void onMediaListClear(List<ApSheetMusic> musicList);
    }

    public interface IOnListDialogListener {
        void onListDialogStateChange(boolean isShown);
    }
}
