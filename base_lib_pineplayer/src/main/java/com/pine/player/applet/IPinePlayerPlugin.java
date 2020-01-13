package com.pine.player.applet;

import android.content.Context;

import com.pine.player.component.PineMediaWidget;
import com.pine.player.widget.viewholder.PinePluginViewHolder;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tanghongfeng on 2018/1/11.
 */

public interface IPinePlayerPlugin<T extends List> {
    // 插件View位于PlayerView内部，宽高与controller view匹配
    int TYPE_MATCH_CONTROLLER = 1;
    // 插件View位于PlayerView内部，宽高与surface view匹配
    int TYPE_MATCH_SURFACE = 2;
    // 插件View位于PlayerView外部
    int TYPE_OUT_ROOT = 3;

    PinePluginViewHolder createViewHolder(Context context, boolean isFullScreen);

    void onInit(Context context, PineMediaWidget.IPineMediaPlayer player,
                PineMediaWidget.IPineMediaController controller,
                boolean isPlayerReset, boolean isResumeState);

    void setData(T data);

    void addData(T data);

    int getContainerType();

    void onMediaPlayerPrepared();

    void onMediaPlayerStart();

    void onMediaPlayerInfo(int what, int extra);

    void onMediaPlayerPause();

    void onMediaPlayerComplete();

    void onAbnormalComplete();

    void onMediaPlayerError(int framework_err, int impl_err);

    /**
     *
     * @param position the current position in milliseconds
     */
    void onTime(long position);

    void onRelease();

    void openPlugin();

    void closePlugin();

    boolean isOpen();
}
