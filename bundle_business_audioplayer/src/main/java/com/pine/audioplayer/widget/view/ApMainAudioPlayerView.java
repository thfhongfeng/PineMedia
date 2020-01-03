package com.pine.audioplayer.widget.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.pine.audioplayer.R;
import com.pine.audioplayer.bean.ApPlayListType;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.widget.AudioPlayerView;
import com.pine.player.widget.PineMediaPlayerView;

public class ApMainAudioPlayerView extends AudioPlayerView {
    private View mRoot;
    private ImageView mapv_loop_type_btn, mapv_media_list_btn, mapv_favourite_btn;

    public ApMainAudioPlayerView(Context context) {
        super(context);
    }

    public ApMainAudioPlayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ApMainAudioPlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public ViewGroup initView() {
        mRoot = LayoutInflater.from(getContext()).inflate(R.layout.ap_main_audio_player_view, this, true);
        mapv_loop_type_btn = mRoot.findViewById(R.id.mapv_loop_type_btn);
        mapv_media_list_btn = mRoot.findViewById(R.id.mapv_media_list_btn);
        mapv_favourite_btn = mRoot.findViewById(R.id.mapv_favourite_btn);

        mapv_loop_type_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoopTypeClick();
            }
        });
        mapv_media_list_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showMusicListDialog();
            }
        });
        mapv_favourite_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClick(mapv_favourite_btn, "favourite");
            }
        });

        return (ViewGroup) mRoot;
    }

    @Override
    public PineMediaPlayerView getMediaPlayerView() {
        return mRoot.findViewById(R.id.media_player_view);
    }

    @Override
    public void setupPlayTypeImage(ApPlayListType playListType) {
        if (playListType == null) {
            mapv_loop_type_btn.setImageResource(R.mipmap.res_ic_play_all_loop_1_1);
            return;
        }
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

    @Override
    public void setupAlbumArtBitmapView(Bitmap smallBitmap, Bitmap bigBitmap) {

    }

    @Override
    public void setupMusicView(ApSheetMusic music, boolean hasMedia) {
        mapv_loop_type_btn.setEnabled(hasMedia);
        mapv_media_list_btn.setEnabled(hasMedia);
        mapv_favourite_btn.setEnabled(hasMedia);
        if (music != null) {
            mapv_favourite_btn.setSelected(music.isFavourite());
        } else {
            mapv_favourite_btn.setSelected(false);
        }
    }
}
