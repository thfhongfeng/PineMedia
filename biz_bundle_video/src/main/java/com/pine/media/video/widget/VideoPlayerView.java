package com.pine.media.video.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pine.media.video.R;
import com.pine.media.video.bean.VpPlayListType;
import com.pine.media.video.db.entity.VpVideo;
import com.pine.media.video.widget.adapter.VpVideoControllerAdapter;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.component.PinePlayState;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.PineMediaPlayerView;
import com.pine.template.base.widget.dialog.CustomListDialog;
import com.pine.tool.util.LogUtils;

import java.util.List;

public abstract class VideoPlayerView extends RelativeLayout {
    protected final String TAG = LogUtils.makeLogTag(this.getClass());

    private ViewGroup mControllerRoot, mFullControllerRoot;

    private PineMediaPlayerView mMediaPlayerView;
    private PineMediaController mMediaController;
    protected VpVideoControllerAdapter mControllerAdapter;
    private PineMediaWidget.IPineMediaPlayer mMediaPlayer;

    protected int[] mPlayTypeResIds = {R.mipmap.vp_ic_play_order_1_1,
            R.mipmap.vp_ic_play_all_loop_1_1,
            R.mipmap.vp_ic_play_single_loop_1_1,
            R.mipmap.vp_ic_play_random_1_1};
    protected int[] mDialogPlayTypeResIds = {R.mipmap.vp_ic_play_order_1_2,
            R.mipmap.vp_ic_play_all_loop_1_2,
            R.mipmap.vp_ic_play_single_loop_1_2,
            R.mipmap.vp_ic_play_random_1_2};

    private CustomListDialog mVideoListDialog;

    private PlayerViewListener mPlayerViewListener;
    private IOnListDialogListener mListDialogListener;

    public void setOnListDialogListener(IOnListDialogListener listDialogListener) {
        mListDialogListener = listDialogListener;
    }

    public VideoPlayerView(Context context) {
        super(context);
        initVideoPlayerView();
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVideoPlayerView();
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoPlayerView();
    }

    private void initVideoPlayerView() {
        mControllerRoot = initView();
        mFullControllerRoot = initFullView();
        mMediaPlayerView = getMediaPlayerView();
        setBackgroundColor(Color.TRANSPARENT);
    }

    public void init(String tag, VpVideoControllerAdapter controllerAdapter) {
        LogUtils.d(TAG, "init playerView:" + VideoPlayerView.this);
        mMediaController = new PineMediaController(getContext());
        mMediaPlayerView.disableBackPressTip();
        mMediaPlayerView.init(tag, mMediaController, false);
        mMediaPlayerView.mForbidIdleControllerWhenDetach = true;
        mMediaPlayer = mMediaPlayerView.getMediaPlayer();
        mMediaPlayer.setAutocephalyPlayMode(true);
        mControllerAdapter = controllerAdapter;
        mControllerAdapter.setupPlayer(mMediaPlayer);
    }

    public void attachView(
            PlayerViewListener playerViewListener) {
        LogUtils.d(TAG, "attachView playerView:" + VideoPlayerView.this);
        mControllerAdapter.setControllerView(mControllerRoot, mFullControllerRoot, VpPlayListType.getDefaultList(getContext()));
        mPlayerViewListener = playerViewListener;
        mControllerAdapter.addListener(this, mPlayerViewListener, new VpVideoControllerAdapter.IControllerAdapterListener() {
            @Override
            public void onVideoStateChange(VpVideo video, PinePlayState state) {
                LogUtils.d(TAG, "onVideoStateChange state:" + state + ",video:" + video + ", playerView:" + VideoPlayerView.this);
                refreshPlayerView();
                if (mVideoListDialog != null && mVideoListDialog.isShowing()) {
                    mVideoListDialog.getListAdapter().notifyDataSetChangedSafely();
                }
            }

            @Override
            public void onDefinitionChange(VpVideo video, int oldPosition, int newPosition) {

            }

            @Override
            public void onAlbumArtPrepare(String mediaCode, VpVideo video, Bitmap smallBitmap,
                                          Bitmap bigBitmap, int mainColor) {
                LogUtils.d(TAG, "onAlbumArtPrepare mediaCode:" + mediaCode + ", playerView:" + VideoPlayerView.this);
                setupAlbumArt(smallBitmap, bigBitmap, mainColor);
            }
        });

        VpVideo curVideo = getCurVideo();
        List<VpVideo> playList = mControllerAdapter.getVideoList();
        if (curVideo == null) {      // 播放器release状态
            if (playList == null || playList.size() < 1) {  // 播放列表为空
                mControllerAdapter.loadAlbumArtAndLyric(null);
                playerViewListener.onPlayVideo(mMediaPlayer, null);
                refreshPlayerView();
                mMediaController.resetOutRootControllerIdleState();
            } else { // 播放列表不为空则选择第一个video
                mControllerAdapter.onMediaSelect(mControllerAdapter.getMediaCode(playList.get(0)), false);
            }
        } else {    // 播放器处于工作中，进行状态刷新
            mControllerAdapter.loadAlbumArtAndLyric(curVideo);
            refreshPlayerView();
            playerViewListener.onPlayVideo(mMediaPlayer, curVideo);
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
        LogUtils.d(TAG, "detachView playerView:" + VideoPlayerView.this);
        if (mVideoListDialog != null && mVideoListDialog.isShowing()) {
            mVideoListDialog.dismiss();
        }
        mPlayerViewListener = null;
        if (mControllerAdapter != null) {
            mControllerAdapter.removeListener(this);
        }
    }

    public void onLoopTypeClick() {
        mControllerAdapter.getAndGoNextPlayType();
        setupPlayTypeImage(mControllerAdapter.getCurPlayType());
    }

    public void onViewClick(View view, String tag) {
        view.setSelected(!view.isSelected());
        if (mPlayerViewListener != null) {
            VpVideo video = mControllerAdapter.getCurVideo();
            mPlayerViewListener.onViewClick(view, video, tag);
        }
    }

    public void playVideoList(@NonNull List<VpVideo> videoList, boolean startPlay) {
        if (videoList == null && videoList.size() < 1) {
            return;
        }
        mControllerAdapter.addVideoList(videoList, startPlay);
    }

    public void playVideo(@NonNull VpVideo video, boolean startPlay) {
        if (video == null) {
            return;
        }
        mControllerAdapter.addVideo(video, startPlay);
    }

    public void playVideoFromPlayList(@NonNull VpVideo video, boolean startPlay) {
        if (video == null) {
            return;
        }
        mControllerAdapter.onMediaSelect(mControllerAdapter.getMediaCode(video), startPlay);
    }

    public void updateVideoData(VpVideo video) {
        if (mControllerAdapter != null) {
            mControllerAdapter.updateVideoData(video);
        }
    }

    public void updateVideoListData(List<VpVideo> list) {
        if (mControllerAdapter != null) {
            mControllerAdapter.updateVideoListData(list);
        }
    }

    public VpVideo getCurVideo() {
        return mControllerAdapter.getCurVideo();
    }

    private void refreshPlayerView() {
        LogUtils.d(TAG, "refreshPlayerView playerView:" + this);
        setupPlayTypeImage(mControllerAdapter.getCurPlayType());
        boolean hasMedia = mControllerAdapter.getVideoList().size() > 0;
        setupVideoView(mControllerAdapter.getCurVideo(), hasMedia);
    }

    public abstract ViewGroup initView();

    public abstract ViewGroup initFullView();

    public abstract PineMediaPlayerView getMediaPlayerView();

    public abstract void setupPlayTypeImage(VpPlayListType playListType);

    public abstract void setupAlbumArt(Bitmap smallBitmap, Bitmap bigBitmap, int mainColor);

    public abstract void setupVideoView(VpVideo video, boolean hasMedia);

    public interface PlayerViewListener extends IPlayerViewListener {
        void onLyricDownloaded(String mediaCode, VpVideo video, String filePath, String charset);

        void onVideoRemove(VpVideo video);

        void onVideoListClear();

        void onViewClick(View view, VpVideo video, String tag);
    }

    public interface IPlayerViewListener {
        void onPlayVideo(PineMediaWidget.IPineMediaPlayer mPlayer, @Nullable VpVideo newVideo);

        void onPlayStateChange(VpVideo video, PinePlayState fromState, PinePlayState toState);

        void onAlbumArtChange(String mediaCode, VpVideo video, Bitmap smallBitmap,
                              Bitmap bigBitmap, int mainColor);
    }

    public interface IOnListDialogListener {
        void onListDialogStateChange(boolean isShown);
    }
}
