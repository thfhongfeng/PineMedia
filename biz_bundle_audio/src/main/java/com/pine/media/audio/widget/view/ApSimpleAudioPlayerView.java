package com.pine.media.audio.widget.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.pine.media.audio.R;
import com.pine.media.audio.bean.ApPlayListType;
import com.pine.media.audio.db.entity.ApMusic;
import com.pine.media.audio.widget.AudioPlayerView;
import com.pine.media.audio.widget.plugin.ApOutRootLrcPlugin;
import com.pine.player.widget.PineMediaPlayerView;

public class ApSimpleAudioPlayerView extends AudioPlayerView {
    private View mRoot;
    private ViewGroup sapv_controller_view, sapv_content_ll;
    private ImageView sapv_cover_iv, sapv_media_list_btn;
    private TextView sapv_name_tv, sapv_desc_tv;

    public ApSimpleAudioPlayerView(Context context) {
        super(context);
    }

    public ApSimpleAudioPlayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ApSimpleAudioPlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public ViewGroup initView() {
        mRoot = LayoutInflater.from(getContext()).inflate(R.layout.ap_simple_audio_player_view, this, true);
        sapv_controller_view = mRoot.findViewById(R.id.sapv_controller_view);
        sapv_content_ll = mRoot.findViewById(R.id.sapv_content_ll);
        sapv_cover_iv = mRoot.findViewById(R.id.sapv_cover_iv);
        sapv_name_tv = mRoot.findViewById(R.id.sapv_name_tv);
        sapv_desc_tv = mRoot.findViewById(R.id.sapv_desc_tv);
        sapv_media_list_btn = mRoot.findViewById(R.id.sapv_media_list_btn);

        sapv_media_list_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMusicListDialog();
            }
        });
        sapv_content_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClick(sapv_content_ll, "content");
            }
        });
        return (ViewGroup) mRoot;
    }

    @Override
    public void attachView(PlayerViewListener playerViewListener,
                           ApOutRootLrcPlugin.ILyricUpdateListener lyricUpdateListener) {
        mControllerAdapter.enableAlbumArt(true, false);
        super.attachView(playerViewListener, lyricUpdateListener);
    }

    @Override
    public PineMediaPlayerView getMediaPlayerView() {
        return mRoot.findViewById(R.id.media_player_view);
    }

    @Override
    public void setupPlayTypeImage(ApPlayListType playListType) {

    }

    @Override
    public void setupAlbumArt(Bitmap smallBitmap, Bitmap bigBitmap, int mainColor) {
        sapv_cover_iv.setImageBitmap(smallBitmap);
    }

    @Override
    public void setupMusicView(ApMusic music, boolean hasMedia) {
        if (music != null) {
            sapv_name_tv.setText(music.getName());
            sapv_desc_tv.setText(music.getAuthor() + " - " + music.getAlbum());
        } else {
            sapv_name_tv.setText(R.string.ap_sad_play_pine_name);
            sapv_desc_tv.setText(R.string.ap_sad_play_pine_desc);
        }
        sapv_media_list_btn.setEnabled(hasMedia);
    }
}
