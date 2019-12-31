package com.pine.audioplayer.widget.plugin;

import android.content.Context;

import com.pine.audioplayer.widget.IAudioPlayerView;
import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.applet.subtitle.bean.PineSubtitleBean;
import com.pine.player.applet.subtitle.plugin.PineLrcParserPlugin;
import com.pine.player.widget.viewholder.PinePluginViewHolder;

import java.util.List;

public class ApOutRootLrcPlugin<T extends List> extends PineLrcParserPlugin<T> {
    private IAudioPlayerView.ILyricUpdateListener mLyricUpdateListener;

    public ApOutRootLrcPlugin(Context context, String subtitleFilePath, String charset) {
        super(context, subtitleFilePath, charset);
    }

    public ApOutRootLrcPlugin(Context context, String subtitleFilePath, int pathType, String charset) {
        super(context, subtitleFilePath, pathType, charset);
    }

    public void setLyricUpdateListener(IAudioPlayerView.ILyricUpdateListener lyricUpdateListener) {
        mLyricUpdateListener = lyricUpdateListener;
    }

    @Override
    public int getContainerType() {
        return IPinePlayerPlugin.TYPE_OUT_ROOT;
    }

    @Override
    public PinePluginViewHolder createViewHolder(Context context, boolean isFullScreen) {
        return null;
    }

    @Override
    public void updateSubtitleText(PineSubtitleBean subtitle, int position) {
        if (mPlayer != null && mLyricUpdateListener != null) {
            mLyricUpdateListener.updateLyricText(mPlayer.getMediaPlayerBean(), mSubtitleBeanList, subtitle, position);
        }
    }

    @Override
    public void clearSubtitleText() {
        if (mLyricUpdateListener != null) {
            mLyricUpdateListener.clearLyricText();
        }
    }
}
