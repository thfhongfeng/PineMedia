package com.pine.audioplayer.widget.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
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
import com.pine.audioplayer.bean.ApPlayListType;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ApSimpleAudioPlayerView extends RelativeLayout {
    private final String TAG = LogUtils.makeLogTag(this.getClass());

    private View mRoot;
    private View controller_view;
    private ImageView cover_iv, next_btn, list_btn;
    private TextView name_tv, desc_tv;

    private PineMediaPlayerView mMediaPlayerView;
    private PineMediaController mMediaController;
    private PineMediaWidget.IPineMediaPlayer mMediaPlayer;
    private ApSimpleAudioControllerAdapter mControllerAdapter;

    private List<ApPlayListType> mPlayTypeList = new ArrayList<>();
    private int mCurPlayTypePos = -1;

    private HashMap<String, ApSheetMusic> mMusicListMap = new HashMap<>();
    private ApSheetMusic mCurPlayMusic;

    private HandlerThread mAlbumArtBitmapThread;
    private Handler mAlbumArtBitmapThreadHandler;
    private HashMap<String, Bitmap> mAlbumArtBitmapMap = new HashMap<>();

    private CustomListDialog mMusicListDialog;

    private IOnMediaListChangeListener mMediaListChangeListener;
    private IOnListDialogListener mListDialogListener;

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
        mPlayTypeList = ApPlayListType.getDefaultList(getContext());

        mRoot = LayoutInflater.from(getContext()).inflate(R.layout.ap_simple_audio_layout, this, true);
        mMediaPlayerView = mRoot.findViewById(R.id.player_view);
        controller_view = mRoot.findViewById(R.id.controller_view);

        cover_iv = mRoot.findViewById(R.id.cover_iv);
        name_tv = mRoot.findViewById(R.id.name_tv);
        desc_tv = mRoot.findViewById(R.id.desc_tv);
        next_btn = mRoot.findViewById(R.id.next_btn);
        list_btn = mRoot.findViewById(R.id.list_btn);

        list_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMusicListDialog != null && mMusicListDialog.isShowing()) {
                    mMusicListDialog.dismiss();
                }
                mMusicListDialog = DialogUtils.createCustomListDialog(RootApplication.mCurResumedActivity, R.layout.ap_simple_audio_dialog_title_layout,
                        R.layout.ap_item_simple_audio_dialog, mControllerAdapter.getMediaList(),
                        new CustomListDialog.IOnViewBindCallback<PineMediaPlayerBean>() {
                            @Override
                            public void onTitleBind(View titleView, final CustomListDialog dialog) {
                                final ApSimpleAudioDialogTitleBinding binding = DataBindingUtil.bind(titleView);
                                binding.setPlayType(mPlayTypeList.get((mCurPlayTypePos + 1) % mPlayTypeList.size()));
                                binding.setMediaCount(mControllerAdapter.getMediaList().size());
                                binding.playTypeIv.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        binding.setPlayType(mPlayTypeList.get((++mCurPlayTypePos) % mPlayTypeList.size()));
                                    }
                                });
                                binding.clearIv.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        List<PineMediaPlayerBean> list = mControllerAdapter.getMediaList();
                                        mControllerAdapter.setMediaList(null);
                                        dialog.getListAdapter().setData(null);
                                        onMediaListClear(list);
                                        dialog.dismiss();
                                    }
                                });
                                binding.executePendingBindings();
                            }

                            @Override
                            public void onItemBind(View itemView, int position,
                                                   final PineMediaPlayerBean data,
                                                   final CustomListDialog dialog) {
                                ApItemSimpleAudioDialogBinding binding = DataBindingUtil.bind(itemView);
                                binding.setMusic(mMusicListMap.get(data.getMediaCode()));
                                PineMediaPlayerBean playerBean = mMediaPlayer.getMediaPlayerBean();
                                int playState = playerBean != null && playerBean.getMediaCode().equals(data.getMediaCode()) ? (mMediaPlayer.isPlaying() ? 2 : 1) : 0;
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
                                        mControllerAdapter.removeMedia(data);
                                        List<PineMediaPlayerBean> mediaList = mControllerAdapter.getMediaList();
                                        dialog.getListAdapter().setData(mediaList);
                                        onMediaRemove(data);
                                        if (mediaList == null || mediaList.size() < 1) {
                                            dialog.dismiss();
                                        }
                                    }
                                });
                                binding.getRoot().setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        playMusicFromPlayList(mMusicListMap.get(data.getMediaCode()), true);
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
        mMediaListChangeListener = mediaListChangeListener;
        mMediaController = new PineMediaController(getContext());
        mMediaPlayerView.init(tag, mMediaController);
        mMediaPlayer = mMediaPlayerView.getMediaPlayer();
        mMediaPlayer.setAutocephalyPlayMode(true);
        mControllerAdapter = new ApSimpleAudioControllerAdapter(getContext(), (ViewGroup) controller_view);
        mMediaController.setMediaControllerAdapter(mControllerAdapter);
        mMediaPlayer.addMediaPlayerListener(new PineMediaWidget.PineMediaPlayerListener() {
            @Override
            public void onStateChange(PineMediaPlayerBean playerBean, PinePlayState fromState, PinePlayState toState) {
                boolean mediaChange = mCurPlayMusic == null || mMediaPlayer.isInPlaybackState() && !playerBean.getMediaCode().equals(mCurPlayMusic.getSongId() + "");
                boolean playStateChange = fromState != toState;
                if (mediaChange || playStateChange) {
                    mCurPlayMusic = mMusicListMap.get(playerBean.getMediaCode());
                    setupControllerView(mCurPlayMusic);
                    if (mMusicListDialog != null && mMusicListDialog.isShowing()) {
                        mMusicListDialog.getListAdapter().notifyDataSetChangedSafely();
                    }
                }
            }
        });
    }

    public void playMusicList(@NonNull List<ApSheetMusic> musicList, boolean startPlay) {
        if (musicList == null && musicList.size() < 1) {
            return;
        }
        List<PineMediaPlayerBean> mediaList = new ArrayList<>();
        for (ApSheetMusic music : musicList) {
            PineMediaPlayerBean mediaBean = transferMediaBean(music);
            mediaBean.setMediaDesc(music.getAuthor() + " - " + music.getAlbum());
            mediaList.add(mediaBean);
            mMusicListMap.put(mediaBean.getMediaCode(), music);
        }
        mControllerAdapter.addMediaList(mediaList);
        mControllerAdapter.onMediaSelect(mediaList.get(0).getMediaCode(), startPlay);

        setupControllerView(musicList.get(0));
    }

    public void playMusic(@NonNull ApSheetMusic music, boolean startPlay) {
        if (music == null) {
            return;
        }
        PineMediaPlayerBean mediaBean = transferMediaBean(music);
        mMusicListMap.put(mediaBean.getMediaCode(), music);
        mControllerAdapter.addMedia(mediaBean);
        mControllerAdapter.onMediaSelect(mediaBean.getMediaCode(), startPlay);
        setupControllerView(music);
    }

    private void playMusicFromPlayList(@NonNull ApSheetMusic music, boolean startPlay) {
        if (music == null) {
            return;
        }
        PineMediaPlayerBean mediaBean = transferMediaBean(music);
        mMusicListMap.put(mediaBean.getMediaCode(), music);
        mControllerAdapter.onMediaSelect(mediaBean.getMediaCode(), startPlay);
        setupControllerView(music);
    }

    private PineMediaPlayerBean transferMediaBean(@NonNull ApSheetMusic music) {
        PineMediaPlayerBean mediaBean = new PineMediaPlayerBean(music.getSongId() + "",
                music.getName(), Uri.parse(music.getFilePath()),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null,
                null, null);
        return mediaBean;
    }

    private void onMediaRemove(PineMediaPlayerBean mediaBean) {
        if (mMediaListChangeListener != null) {
            ApSheetMusic music = mMusicListMap.get(mediaBean.getMediaCode());
            mMediaListChangeListener.onMediaRemove(music);
        }
        setupControllerView(mMusicListMap.get(mControllerAdapter.getCurMediaCode()));
    }

    private void onMediaListClear(List<PineMediaPlayerBean> mediaList) {
        if (mMediaListChangeListener != null) {
            List<ApSheetMusic> musicList = new ArrayList<>();
            if (mediaList != null && mediaList.size() > 0) {
                for (PineMediaPlayerBean mediaBean : mediaList) {
                    ApSheetMusic music = mMusicListMap.get(mediaBean.getMediaCode());
                    if (music != null) {
                        musicList.add(music);
                    }
                }
            }
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
                    cover_iv.setImageBitmap(bitmap);
                } else {
                    cover_iv.setImageResource(R.mipmap.res_iv_top_bg);
                }
            }
            name_tv.setText(music.getName());
            desc_tv.setText(music.getAuthor() + " - " + music.getAlbum());
        } else {
            cover_iv.setImageResource(R.mipmap.res_iv_top_bg);
            name_tv.setText(R.string.ap_sad_play_pine_name);
            desc_tv.setText(R.string.ap_sad_play_pine_desc);
        }

        boolean hasMedia = mControllerAdapter != null && mControllerAdapter.getMediaList().size() > 0;
        next_btn.setEnabled(hasMedia);
        next_btn.setSelected(hasMedia);
        list_btn.setEnabled(hasMedia);
        if (!hasMedia) {
            if (mMusicListDialog != null && mMusicListDialog.isShowing()) {
                mMusicListDialog.dismiss();
            }
        }
    }

    private void getAlbumArtBitmapInBackground(final String mediaCode, final long songId, final long albumId) {
        if (mAlbumArtBitmapThreadHandler == null) {
            cover_iv.setImageResource(R.mipmap.res_iv_top_bg);
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
                            cover_iv.setImageBitmap(bitmap);
                        } else {
                            cover_iv.setImageResource(R.mipmap.res_iv_top_bg);
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
