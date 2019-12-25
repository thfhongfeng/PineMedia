package com.pine.audioplayer.widget.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
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
import com.pine.audioplayer.widget.adapter.ApSimpleAudioControllerAdapter;
import com.pine.base.util.DialogUtils;
import com.pine.base.widget.dialog.CustomListDialog;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.PineMediaPlayerView;
import com.pine.tool.ui.Activity;

import java.util.ArrayList;
import java.util.List;

public class ApSimpleAudioPlayerView extends RelativeLayout {
    private View mRoot;
    private View controller_view;
    private ImageView cover_iv, list_btn;
    private TextView name_tv, desc_tv;

    private PineMediaPlayerView mMediaPlayerView;
    private PineMediaController mMediaController;
    private PineMediaWidget.IPineMediaPlayer mMediaPlayer;
    private ApSimpleAudioControllerAdapter mControllerAdapter;

    private List<ApPlayListType> mPlayTypeList = new ArrayList<>();
    private int mCurPlayTypePos = -1;

    private IOnMediaListChangeListener mListener;

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
        list_btn = mRoot.findViewById(R.id.list_btn);

        list_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = DialogUtils.createCustomListDialog(getContext(), R.layout.ap_simple_audio_dialog_title_layout,
                        R.layout.ap_item_simple_audio_dialog, mControllerAdapter.getMediaList(),
                        new CustomListDialog.IOnViewBindCallback<PineMediaPlayerBean>() {
                            @Override
                            public void onTitleBind(View titleView, final CustomListDialog.DialogListAdapter adapter) {
                                final ApSimpleAudioDialogTitleBinding binding = DataBindingUtil.bind(titleView);
                                binding.setPlayType(mPlayTypeList.get((++mCurPlayTypePos) % mPlayTypeList.size()));
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
                                        adapter.setData(null);
                                        onMediaListClear(list);
                                    }
                                });
                                binding.executePendingBindings();
                            }

                            @Override
                            public void onItemBind(View itemView, final CustomListDialog.DialogListAdapter adapter,
                                                   int position, final PineMediaPlayerBean data) {
                                ApItemSimpleAudioDialogBinding binding = DataBindingUtil.bind(itemView);
                                binding.setMediaBean(data);
                                PineMediaPlayerBean playerBean = mMediaPlayer.getMediaPlayerBean();
                                int playState = playerBean != null && playerBean.getMediaCode().equals(data.getMediaCode()) ? (mMediaPlayer.isInPlaybackState() ? 2 : 1) : 0;
                                binding.setPlayingState(playState);
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
                                        adapter.setData(mControllerAdapter.getMediaList());
                                        onMediaRemove(data);
                                    }
                                });
                                binding.getRoot().setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                });
                                binding.executePendingBindings();
                            }
                        });
                dialog.show();
            }
        });
        cover_iv.setImageResource(R.mipmap.res_iv_top_bg);
        name_tv.setText(R.string.ap_sad_play_pine_name);
        desc_tv.setText(R.string.ap_sad_play_pine_desc);

        refreshAudioListRelateState();
    }

    public void init(Activity context, String tag, IOnMediaListChangeListener listener) {
        mListener = listener;
        mMediaController = new PineMediaController(getContext());
        mMediaPlayerView.init(tag, mMediaController);
        mMediaPlayer = mMediaPlayerView.getMediaPlayer();
        mMediaPlayer.setAutocephalyPlayMode(true);
        mControllerAdapter = new ApSimpleAudioControllerAdapter(getContext(), mMediaPlayer,
                (ViewGroup) controller_view);
        mMediaController.setMediaControllerAdapter(mControllerAdapter);
        refreshAudioListRelateState();
    }

    public void playMediaList(@NonNull List<PineMediaPlayerBean> mediaBeanList,
                              @NonNull PineMediaPlayerBean playerBean, Bitmap musicAlbumArtBitmap,
                              boolean startPlay) {
        if (mediaBeanList == null && mediaBeanList.size() < 1) {
            return;
        }
        mControllerAdapter.addMediaList(mediaBeanList);
        mControllerAdapter.onMediaSelect(playerBean.getMediaCode(), startPlay);

        setupControllerView(playerBean, musicAlbumArtBitmap);
    }

    public void playMedia(@NonNull PineMediaPlayerBean mediaBean, Bitmap musicAlbumArtBitmap,
                          boolean startPlay) {
        if (mediaBean == null) {
            return;
        }
        mControllerAdapter.addMedia(mediaBean);
        mControllerAdapter.onMediaSelect(mediaBean.getMediaCode(), startPlay);

        setupControllerView(mediaBean, musicAlbumArtBitmap);
    }

    private void onMediaRemove(PineMediaPlayerBean playerBean) {
        if (mListener != null) {
            mListener.onMediaRemove(playerBean);
        }
        refreshAudioListRelateState();
    }

    private void onMediaListClear(List<PineMediaPlayerBean> mediaBeanList) {
        if (mListener != null) {
            mListener.onMediaListClear(mediaBeanList);
        }
        refreshAudioListRelateState();
    }

    private void setupControllerView(PineMediaPlayerBean playerBean, Bitmap musicAlbumArtBitmap) {
        if (musicAlbumArtBitmap != null) {
            cover_iv.setImageBitmap(musicAlbumArtBitmap);
        } else {
            cover_iv.setImageResource(R.mipmap.res_iv_top_bg);
        }
        name_tv.setText(playerBean.getMediaName());
        desc_tv.setText(playerBean.getMediaDesc());
        refreshAudioListRelateState();
    }

    private void refreshAudioListRelateState() {
        list_btn.setEnabled(mControllerAdapter != null && mControllerAdapter.getMediaList().size() > 0);
    }

    public interface IOnMediaListChangeListener {
        void onMediaRemove(PineMediaPlayerBean mediaBean);

        void onMediaListClear(List<PineMediaPlayerBean> mediaBeanList);
    }
}
