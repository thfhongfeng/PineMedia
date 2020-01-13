package com.pine.audioplayer.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.pine.audioplayer.R;
import com.pine.audioplayer.bean.ApPlayListType;
import com.pine.audioplayer.databinding.ApItemSimpleAudioDialogBinding;
import com.pine.audioplayer.databinding.ApSimpleAudioDialogTitleBinding;
import com.pine.audioplayer.db.entity.ApMusic;
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

import java.util.List;

public abstract class AudioPlayerView extends RelativeLayout {
    protected final String TAG = LogUtils.makeLogTag(this.getClass());

    private ViewGroup mControllerRoot;

    private PineMediaPlayerView mMediaPlayerView;
    private PineMediaController mMediaController;
    protected ApAudioControllerAdapter mControllerAdapter;
    private PineMediaWidget.IPineMediaPlayer mMediaPlayer;

    protected int[] mPlayTypeResIds = {R.mipmap.res_ic_play_order_1_1,
            R.mipmap.res_ic_play_all_loop_1_1,
            R.mipmap.res_ic_play_single_loop_1_1,
            R.mipmap.res_ic_play_random_1_1};
    protected int[] mDialogPlayTypeResIds = {R.mipmap.res_ic_play_order_1_2,
            R.mipmap.res_ic_play_all_loop_1_2,
            R.mipmap.res_ic_play_single_loop_1_2,
            R.mipmap.res_ic_play_random_1_2};

    private CustomListDialog mMusicListDialog;

    private PlayerViewListener mPlayerViewListener;
    private IOnListDialogListener mListDialogListener;

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
        setBackgroundColor(Color.TRANSPARENT);
    }

    public void init(String tag, ApAudioControllerAdapter controllerAdapter) {
        LogUtils.d(TAG, "init playerView:" + AudioPlayerView.this);
        mMediaController = new PineMediaController(getContext());
        mMediaPlayerView.disableBackPressTip();
        mMediaPlayerView.init(tag, mMediaController);
        mMediaPlayerView.mForbidIdleControllerWhenDetach = true;
        mMediaPlayer = mMediaPlayerView.getMediaPlayer();
        mMediaPlayer.setAutocephalyPlayMode(true);
        mControllerAdapter = controllerAdapter;
        mControllerAdapter.setupPlayer(mMediaPlayer);
    }

    public void attachView(
            PlayerViewListener playerViewListener,
            ApOutRootLrcPlugin.ILyricUpdateListener lyricUpdateListener) {
        LogUtils.d(TAG, "attachView playerView:" + AudioPlayerView.this);
        mControllerAdapter.setControllerView(mControllerRoot, ApPlayListType.getDefaultList(getContext()));
        mPlayerViewListener = playerViewListener;
        mControllerAdapter.setLyricUpdateListener(lyricUpdateListener);
        mControllerAdapter.addListener(this, mPlayerViewListener, new ApAudioControllerAdapter.IControllerAdapterListener() {
            @Override
            public void onMusicStateChange(ApMusic music, PinePlayState state) {
                LogUtils.d(TAG, "onMusicStateChange state:" + state + ",music:" + music + ", playerView:" + AudioPlayerView.this);
                refreshPlayerView();
                if (mMusicListDialog != null && mMusicListDialog.isShowing()) {
                    mMusicListDialog.getListAdapter().notifyDataSetChangedSafely();
                }
            }

            @Override
            public void onAlbumArtPrepare(String mediaCode, ApMusic music, Bitmap smallBitmap,
                                          Bitmap bigBitmap, int mainColor) {
                LogUtils.d(TAG, "onAlbumArtPrepare mediaCode:" + mediaCode + ", playerView:" + AudioPlayerView.this);
                setupAlbumArt(smallBitmap, bigBitmap, mainColor);
            }
        });

        ApMusic curMusic = getCurMusic();
        List<ApMusic> playList = mControllerAdapter.getMusicList();
        if (curMusic == null) {      // 播放器release状态
            if (playList == null || playList.size() < 1) {  // 播放列表为空
                mControllerAdapter.loadAlbumArtAndLyric(null);
                playerViewListener.onPlayMusic(mMediaPlayer, null);
                refreshPlayerView();
                mMediaController.resetOutRootControllerIdleState();
            } else { // 播放列表不为空则选择第一个music
                mControllerAdapter.onMediaSelect(mControllerAdapter.getMediaCode(playList.get(0)), false);
            }
        } else {    // 播放器处于工作中，进行状态刷新
            mControllerAdapter.loadAlbumArtAndLyric(curMusic);
            refreshPlayerView();
            playerViewListener.onPlayMusic(mMediaPlayer, curMusic);
        }
        mMediaController.setMediaControllerAdapter(mControllerAdapter);
    }

    @Override
    protected void onDetachedFromWindow() {
        LogUtils.d(TAG, "onDetachedFromWindow playerView:" + this);
        detachView();
        super.onDetachedFromWindow();
    }

    public void detachView() {
        LogUtils.d(TAG, "detachView playerView:" + AudioPlayerView.this);
        if (mMusicListDialog != null && mMusicListDialog.isShowing()) {
            mMusicListDialog.dismiss();
        }
        mPlayerViewListener = null;
        if (mControllerAdapter != null) {
            mControllerAdapter.removeListener(this);
        }
    }

    public void showMusicListDialog() {
        if (mMusicListDialog == null) {
            mMusicListDialog = DialogUtils.createBottomCustomListDialog(RootApplication.mCurResumedActivity,
                    R.layout.ap_audio_dialog_title_layout, R.layout.ap_item_audio_dialog,
                    true, mControllerAdapter.getMusicList(),
                    new CustomListDialog.IOnViewBindCallback<ApMusic>() {
                        ApSimpleAudioDialogTitleBinding binding;

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
                        public void onViewBind(View titleView, View actionView, final CustomListDialog dialog) {
                            binding = DataBindingUtil.bind(titleView);
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
                                    mControllerAdapter.setMusicList(null, false);
                                    dialog.getListAdapter().setData(null);
                                    refreshPlayerView();
                                    mMediaController.resetOutRootControllerIdleState();
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
                        public void onItemViewUpdate(View itemView, int position,
                                                     final ApMusic data,
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
                                    List<ApMusic> musicList = mControllerAdapter.getMusicList();
                                    dialog.getListAdapter().setData(musicList);
                                    if (musicList == null || musicList.size() < 1) {
                                        mMediaController.resetOutRootControllerIdleState();
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

                        @Override
                        public void onListDataChange(View titleView, View actionView, CustomListDialog dialog) {
                            binding.setPlayType(mControllerAdapter.getCurPlayType());
                            setupPlayTypeImageInDialog(binding.loopTypeBtn);
                            binding.setMediaCount(mControllerAdapter.getMusicList().size());
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
            mMusicListDialog.getListAdapter().setData(mControllerAdapter.getMusicList());
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
            ApMusic music = mControllerAdapter.getCurMusic();
            mPlayerViewListener.onViewClick(view, music, tag);
        }
    }

    public void playMusicList(@NonNull List<ApMusic> musicList, boolean startPlay) {
        if (musicList == null && musicList.size() < 1) {
            return;
        }
        mControllerAdapter.addMusicList(musicList, startPlay);
    }

    public void playMusic(@NonNull ApMusic music, boolean startPlay) {
        if (music == null) {
            return;
        }
        mControllerAdapter.addMusic(music, startPlay);
    }

    public void playMusicFromPlayList(@NonNull ApMusic music, boolean startPlay) {
        if (music == null) {
            return;
        }
        mControllerAdapter.onMediaSelect(mControllerAdapter.getMediaCode(music), startPlay);
    }

    public void updateMusicData(ApMusic music) {
        if (mControllerAdapter != null) {
            mControllerAdapter.updateMusicData(music);
        }
    }

    public void updateMusicListData(List<ApMusic> list) {
        if (mControllerAdapter != null) {
            mControllerAdapter.updateMusicListData(list);
        }
    }

    public ApMusic getCurMusic() {
        return mControllerAdapter.getCurMusic();
    }

    private void refreshPlayerView() {
        LogUtils.d(TAG, "refreshPlayerView playerView:" + this);
        setupPlayTypeImage(mControllerAdapter.getCurPlayType());
        boolean hasMedia = mControllerAdapter.getMusicList().size() > 0;
        setupMusicView(mControllerAdapter.getCurMusic(), hasMedia);
        if (!hasMedia) {
            dismissMusicListDialog();
        }
    }

    public abstract ViewGroup initView();

    public abstract PineMediaPlayerView getMediaPlayerView();

    public abstract void setupPlayTypeImage(ApPlayListType playListType);

    public abstract void setupAlbumArt(Bitmap smallBitmap, Bitmap bigBitmap, int mainColor);

    public abstract void setupMusicView(ApMusic music, boolean hasMedia);

    public interface PlayerViewListener extends IPlayerViewListener {
        void onLyricDownloaded(String mediaCode, ApMusic music, String filePath, String charset);

        void onMusicRemove(ApMusic music);

        void onMusicListClear();

        void onViewClick(View view, ApMusic music, String tag);
    }

    public interface IPlayerViewListener {
        void onPlayMusic(PineMediaWidget.IPineMediaPlayer mPlayer, @Nullable ApMusic newMusic);

        void onPlayStateChange(ApMusic music, PinePlayState fromState, PinePlayState toState);

        void onAlbumArtChange(String mediaCode, ApMusic music, Bitmap smallBitmap,
                              Bitmap bigBitmap, int mainColor);
    }

    public interface IOnListDialogListener {
        void onListDialogStateChange(boolean isShown);
    }
}
