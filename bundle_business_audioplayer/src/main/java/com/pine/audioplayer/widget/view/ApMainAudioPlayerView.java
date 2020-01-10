package com.pine.audioplayer.widget.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.pine.audioplayer.R;
import com.pine.audioplayer.bean.ApPlayListType;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.widget.AudioPlayerView;
import com.pine.audioplayer.widget.plugin.ApOutRootLrcPlugin;
import com.pine.player.widget.PineMediaPlayerView;
import com.pine.tool.util.ColorUtils;

public class ApMainAudioPlayerView extends AudioPlayerView {
    private View mRoot;
    private ImageView mapv_loop_type_btn, mapv_media_list_btn, mapv_favourite_btn;
    private SeekBar player_progress_bar;
    private TextView player_cur_time_tv, player_end_time_tv;
    private ImageView player_pre_btn, player_play_pause_btn, player_next_btn;

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

        player_cur_time_tv = mRoot.findViewById(R.id.player_cur_time_tv);
        player_end_time_tv = mRoot.findViewById(R.id.player_end_time_tv);
        player_progress_bar = mRoot.findViewById(R.id.player_progress_bar);
        player_pre_btn = mRoot.findViewById(R.id.player_pre_btn);
        player_play_pause_btn = mRoot.findViewById(R.id.player_play_pause_btn);
        player_next_btn = mRoot.findViewById(R.id.player_next_btn);

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
    public void attachView(PlayerViewListener playerViewListener,
                           ApOutRootLrcPlugin.ILyricUpdateListener lyricUpdateListener) {
        mControllerAdapter.enableAlbumArt(false, true);
        super.attachView(playerViewListener, lyricUpdateListener);
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
                mapv_loop_type_btn.setImageResource(mPlayTypeResIds[0]);
                break;
            case ApPlayListType.TYPE_ALL_LOOP:
                mapv_loop_type_btn.setImageResource(mPlayTypeResIds[1]);
                break;
            case ApPlayListType.TYPE_SING_LOOP:
                mapv_loop_type_btn.setImageResource(mPlayTypeResIds[2]);
                break;
            case ApPlayListType.TYPE_RANDOM:
                mapv_loop_type_btn.setImageResource(mPlayTypeResIds[3]);
                break;
        }
    }

    @Override
    public void setupAlbumArt(Bitmap smallBitmap, Bitmap bigBitmap, int mainColor) {
        boolean isLightColor = ColorUtils.isLightColor(mainColor);
        setBackgroundColor(mainColor);
        changeBgTheme(isLightColor);
    }

    private void changeBgTheme(boolean isLightColor) {
        if (isLightColor) {
            mPlayTypeResIds = new int[]{R.mipmap.res_ic_play_order_1_2,
                    R.mipmap.res_ic_play_all_loop_1_2,
                    R.mipmap.res_ic_play_single_loop_1_2,
                    R.mipmap.res_ic_play_random_1_2};
            mapv_media_list_btn.setImageResource(R.mipmap.res_ic_music_list_enable_1_2);
            mapv_favourite_btn.setImageResource(R.drawable.res_selector_is_favourite_1_2);
            player_cur_time_tv.setTextColor(getResources().getColor(R.color.dark_gray_black));
            player_end_time_tv.setTextColor(getResources().getColor(R.color.dark_gray_black));
            player_progress_bar.setThumb(getResources().getDrawable(R.drawable.res_selector_seek_bar_thumb_1_2));
            player_progress_bar.setProgressDrawable(getResources().getDrawable(R.drawable.res_shape_seek_bar_progress_2));
            player_pre_btn.setImageResource(R.drawable.res_selector_previous_btn_1_2);
            player_play_pause_btn.setImageResource(R.drawable.res_selector_play_pause_btn_big_1_2);
            player_next_btn.setImageResource(R.drawable.res_selector_next_btn_1_2);
        } else {
            mPlayTypeResIds = new int[]{R.mipmap.res_ic_play_order_1_1,
                    R.mipmap.res_ic_play_all_loop_1_1,
                    R.mipmap.res_ic_play_single_loop_1_1,
                    R.mipmap.res_ic_play_random_1_1};
            mapv_media_list_btn.setImageResource(R.mipmap.res_ic_music_list_enable_1_1);
            mapv_favourite_btn.setImageResource(R.drawable.res_selector_is_favourite_1_1);
            player_cur_time_tv.setTextColor(getResources().getColor(R.color.white));
            player_end_time_tv.setTextColor(getResources().getColor(R.color.white));
            player_progress_bar.setThumb(getResources().getDrawable(R.drawable.res_selector_seek_bar_thumb_1_1));
            player_progress_bar.setProgressDrawable(getResources().getDrawable(R.drawable.res_shape_seek_bar_progress_1));
            player_pre_btn.setImageResource(R.drawable.res_selector_previous_btn_1_1);
            player_play_pause_btn.setImageResource(R.drawable.res_selector_play_pause_btn_big_1_1);
            player_next_btn.setImageResource(R.drawable.res_selector_next_btn_1_1);
        }
        if (mControllerAdapter != null) {
            setupPlayTypeImage(mControllerAdapter.getCurPlayType());
        }
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
