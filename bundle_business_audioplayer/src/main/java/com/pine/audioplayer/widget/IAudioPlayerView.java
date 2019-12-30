package com.pine.audioplayer.widget;

import android.content.Context;
import android.view.View;

import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.widget.adapter.ApAudioControllerAdapter;
import com.pine.player.applet.subtitle.bean.PineSubtitleBean;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;

import java.util.List;

import androidx.annotation.NonNull;

public interface IAudioPlayerView {
    void init(Context context, String tag, ApAudioControllerAdapter controllerAdapter,
              IPlayerViewListener playerViewListener,
              ILyricUpdateListener lyricUpdateListener);

    void playMusic(@NonNull ApSheetMusic music, boolean startPlay);

    void playMusicList(@NonNull List<ApSheetMusic> musicList, boolean startPlay);

    interface IPlayerViewListener {
        void onPlayMusic(PineMediaWidget.IPineMediaPlayer player, ApSheetMusic oldPlayMusic, ApSheetMusic newPlayMusic);

        void onLyricDownloaded(ApSheetMusic music, String filePath);

        void onMusicRemove(ApSheetMusic music);

        void onMusicListClear(List<ApSheetMusic> musicList);

        void onViewClick(View view, String tag);
    }

    interface IOnListDialogListener {
        void onListDialogStateChange(boolean isShown);
    }

    interface ILyricUpdateListener {
        void updateLyricText(PineMediaPlayerBean mediaBean, PineSubtitleBean subtitle);

        void clearLyricText();
    }
}
