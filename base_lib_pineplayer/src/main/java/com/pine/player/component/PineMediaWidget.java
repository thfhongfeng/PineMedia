package com.pine.player.component;

import android.media.MediaPlayer;
import android.view.SurfaceView;

import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.widget.PineMediaPlayerView;
import com.pine.player.widget.PineSurfaceView;

import java.util.Map;

/**
 * Created by tanghongfeng on 2017/8/28.
 */

public class PineMediaWidget {
    /**
     * 播放控制器接口，主要提供给播放器使用
     */
    public static interface IPineMediaController {

        /**
         * 设置播放器
         *
         * @param player 播放器
         */
        void setMediaPlayer(PineMediaPlayerProxy player);

        /**
         * 设置播放器控件Anchor View
         *
         * @param playerView
         */
        void setMediaPlayerView(PineMediaPlayerView playerView);

        /**
         * 设置播放内容
         *
         * @param pineMediaPlayerBean
         * @param videoViewTag
         */
        void setPlayingMedia(PineMediaPlayerBean pineMediaPlayerBean,
                             String videoViewTag);

        /**
         * 挂载控制器到播放器上
         *
         * @param isPlayerReset 本此attach是否重置了MediaPlayer
         * @param isResumeState 本此attach是否是为了恢复状态
         */
        void attachToParentView(boolean isPlayerReset, boolean isResumeState);

        /**
         * 播放和暂停切换
         */
        void doPauseResume();

        /**
         * 切换控制器显示状态
         */
        void toggleMediaControllerVisibility();

        /**
         * 显示控制器
         */
        void show();

        /**
         * 显示控制器
         *
         * @param timeout 控制器自动隐藏时间
         */
        void show(int timeout);

        /**
         * 隐藏控制器
         */
        void hide();

        /**
         * 隐藏右侧View容器
         */
        void hideRightView();

        /**
         * 恢复非内置控制器到IDlE状态
         */
        void resetOutRootControllerIdleState();

        /**
         * 播放器播放回调
         */
        void onMediaPlayerStart();

        /**
         * 播放器暂停回调
         */
        void onMediaPlayerPause();

        /**
         * 播放器装备就绪回调
         */
        void onMediaPlayerPrepared();

        /**
         * 播放器信息更新回调
         *
         * @param what
         * @param extra
         */
        void onMediaPlayerInfo(int what, int extra);

        /**
         * 播放器缓冲更新回调
         *
         * @param percent 当前缓冲百分比
         */
        void onBufferingUpdate(int percent);

        /**
         * 播放器播放完毕回调
         */
        void onMediaPlayerComplete();

        /**
         * 播放器发生错误回调
         *
         * @param framework_err 参考MediaPlayer.OnErrorListener what参数
         * @param impl_err      参考MediaPlayer.OnErrorListener extra参数
         */
        void onMediaPlayerError(int framework_err, int impl_err);

        /**
         * 播放器非正常播放结束回调
         */
        void onAbnormalComplete();

        /**
         * 播放器释放回调
         */
        void onMediaPlayerRelease(boolean clearTargetState);

        /**
         * 播放器播放速度改编回调
         */
        void onMediaPlayerSpeedChange();

        /**
         * 更新音量显示
         */
        void updateVolumesText();

        /**
         * 更新全屏模式
         */
        void updateFullScreenMode();

        /**
         * 播放按键RequestFocus
         */
        void pausePlayBtnRequestFocus();

        /**
         * 设置控制器是否可用
         *
         * @param enabled
         */
        void setControllerEnabled(boolean enabled);

        /**
         * 分别设置各个控制器部件是否可用
         *
         * @param enabledSpeed
         * @param enabledRightView
         * @param enabledPlayerPause
         * @param enabledProgressBar
         * @param enabledToggleFullScreen
         * @param enabledLock
         * @param enabledFastForward
         * @param enabledFastBackward
         */
        void setControllerEnabled(boolean enabledSpeed, boolean enabledRightView,
                                  boolean enabledPlayerPause, boolean enabledProgressBar,
                                  boolean enabledToggleFullScreen, boolean enabledLock,
                                  boolean enabledFastForward, boolean enabledFastBackward);

        /**
         * 控制器是否显示
         *
         * @return
         */
        boolean isShowing();

        /**
         * 当前控制器是否锁住
         *
         * @return
         */
        boolean isLocked();
    }

    /**
     * 播放器接口，主要提供给播放控制器使用
     */
    public static interface IPineMediaPlayer {
        String getMediaPlayerTag();

        /**
         * 获取播放倍速
         */
        float getSpeed();

        /**
         * 设置播放倍速
         *
         * @param speed
         */
        void setSpeed(float speed);

        /**
         * 开始播放
         */
        void start();

        /**
         * 暂停播放
         */
        void pause();

        /**
         * 挂起
         */
        void suspend();

        /**
         * 恢复播放
         */
        void resume();

        /**
         * 释放
         */
        void release();

        /**
         * 是否本地视频流服务方式播放
         */
        boolean isLocalStreamMode();

        /**
         * 设置多媒体播放参数
         *
         * @param pineMediaPlayerBean 多媒体播放参数对象
         */
        void setPlayingMedia(PineMediaPlayerBean pineMediaPlayerBean);

        /**
         * 设置多媒体播放参数
         *
         * @param pineMediaPlayerBean 多媒体播放参数对象
         * @param headers             多媒体播放信息头
         */
        void setPlayingMedia(PineMediaPlayerBean pineMediaPlayerBean, Map<String, String> headers);

        /**
         * 设置多媒体播放参数
         *
         * @param pineMediaPlayerBean   多媒体播放参数对象
         * @param isAutocephalyPlayMode 是否独立播放模式
         */
        void setPlayingMedia(PineMediaPlayerBean pineMediaPlayerBean, boolean isAutocephalyPlayMode);

        /**
         * 设置多媒体播放参数
         *
         * @param pineMediaPlayerBean   多媒体播放参数对象
         * @param headers               多媒体播放信息头
         * @param isAutocephalyPlayMode 是否独立播放模式
         */
        void setPlayingMedia(PineMediaPlayerBean pineMediaPlayerBean, Map<String, String> headers,
                             boolean isAutocephalyPlayMode);

        /**
         * 重新设置播放参数并恢复到之前的播放状态
         *
         * @param pineMediaPlayerBean 播放参数对象
         * @param headers             播放头
         */
        void resetPlayingMediaAndResume(PineMediaPlayerBean pineMediaPlayerBean,
                                        Map<String, String> headers);

        /**
         * 保存播放状态和进度
         */
        void savePlayMediaState();

        /**
         * 清除播放状态和进度
         */
        void clearPlayMediaState();

        /**
         * 获取播放总时长
         *
         * @return
         */
        int getDuration();

        /**
         * 获取当前播放位置
         *
         * @return
         */
        int getCurrentPosition();

        /**
         * 获取准备播放的media实体
         *
         * @return
         */
        PineMediaPlayerBean getMediaPlayerBean();

        /**
         * 获取Track信息
         *
         * @return
         */
        MediaPlayer.TrackInfo[] getTrackInfo();

        /**
         * 跳到指定的播放位置
         *
         * @param pos 指定的播放位置
         */
        void seekTo(int pos);

        /**
         * 当前是否处于播放状态
         *
         * @return
         */
        boolean isPlaying();

        /**
         * 当前是否处于播暂停状态
         *
         * @return
         */
        boolean isPause();


        /**
         * 获取缓冲百分比
         *
         * @return
         */
        int getBufferPercentage();

        /**
         * 播放器是否处于可播放状态
         *
         * @return
         */
        boolean isInPlaybackState();

        /**
         * 是否是独立播放模式
         *
         * @return
         */
        boolean isAutocephalyPlayMode();

        /**
         * 设置是否为独立播放模式（是否与播放界面共生命周期）
         *
         * @param isAutocephalyPlayMode 设置是否为独立播放模式, 默认为false
         */
        void setAutocephalyPlayMode(boolean isAutocephalyPlayMode);

        /**
         * 设置是否为独立播放模式（是否与播放界面共生命周期）
         *
         * @param isAutocephalyPlayMode   设置是否为独立播放模式, 默认为false
         * @param shouldDestroyWhenDetach 在非独立模式下，当控件View从上下文环境中（Context）移除时，
         *                                播放器是销毁(destroy)还是释放(release), 默认为false
         *                                true: destroy模式下，从Context中移除后，非独立播放器所有状态清除，对象销毁，无法使用resume来恢复播放状态
         *                                false: release模式下，从Context中移除后，非独立播放器所有状态清除，对象不会销毁，可以使用resume来恢复播放状态
         */
        void setAutocephalyPlayMode(boolean isAutocephalyPlayMode, boolean shouldDestroyWhenDetach);

        /**
         * 获取播放器具体状态
         *
         * @return
         */
        PinePlayState getMediaPlayerState();

        /**
         * 移除播放状态监听器
         *
         * @param listener
         */
        void removeMediaPlayerListener(IPineMediaPlayerListener listener);

        /**
         * 添加播放状态监听器
         *
         * @param listener
         */
        void addMediaPlayerListener(IPineMediaPlayerListener listener);
    }

    /**
     * 播放器组件接口，适配不同的播放器内核
     */
    public static interface IPineMediaPlayerComponent {

        /**
         * 设置多媒体播放参数
         *
         * @param pineMediaPlayerBean 多媒体播放参数对象
         * @param headers             多媒体播放信息头
         * @param resumeState         此次播放是否恢复到之前的播放状态(用于被动中断后的恢复)
         */
        void setPlayingMedia(PineMediaPlayerBean pineMediaPlayerBean, Map<String, String> headers,
                             boolean resumeState);

        /**
         * 设置多媒体播放参数
         *
         * @param pineMediaPlayerBean   多媒体播放参数对象
         * @param headers               多媒体播放信息头
         * @param resumeState           此次播放是否恢复到之前的播放状态(用于被动中断后的恢复)
         * @param isAutocephalyPlayMode 是否独立播放模式
         */
        void setPlayingMedia(PineMediaPlayerBean pineMediaPlayerBean, Map<String, String> headers,
                             boolean resumeState, boolean isAutocephalyPlayMode);

        /**
         * 重新设置播放参数并恢复到之前的播放状态
         *
         * @param pineMediaPlayerBean 播放参数对象
         * @param headers             播放头
         */
        void resetPlayingMediaAndResume(PineMediaPlayerBean pineMediaPlayerBean,
                                        Map<String, String> headers);

        /**
         * 获取播放倍速
         */
        float getSpeed();

        /**
         * 设置播放倍速
         *
         * @param speed
         */
        void setSpeed(float speed);

        /**
         * 开始播放
         */
        void start();

        /**
         * 暂停播放
         */
        void pause();

        /**
         * 挂起
         */
        void suspend();

        /**
         * 恢复播放
         */
        void resume();

        /**
         * 释放
         */
        void release();

        /**
         * 销毁
         */
        void onDestroy();

        /**
         * 保存播放状态和进度
         */
        void savePlayMediaState();

        /**
         * 清除播放状态和进度
         */
        void clearPlayMediaState();

        /**
         * 获取播放总时长
         *
         * @return
         */
        int getDuration();

        /**
         * 获取当前播放位置
         *
         * @return the current position in milliseconds
         */
        int getCurrentPosition();

        /**
         * 获取准备播放的media实体
         *
         * @return
         */
        PineMediaPlayerBean getMediaPlayerBean();

        /**
         * 获取Track信息
         *
         * @return
         */
        MediaPlayer.TrackInfo[] getTrackInfo();

        /**
         * 跳到指定的播放位置
         *
         * @param pos 指定的播放位置
         */
        void seekTo(int pos);

        /**
         * 当前是否处于播放状态
         *
         * @return
         */
        boolean isPlaying();

        /**
         * 当前是否处于播暂停状态
         *
         * @return
         */
        boolean isPause();

        /**
         * 获取缓冲百分比
         *
         * @return
         */
        int getBufferPercentage();

        /**
         * 播放的media是否支持暂停
         *
         * @return
         */
        boolean canPause();

        /**
         * 播放的media是否支持回退
         *
         * @return
         */
        boolean canSeekBackward();

        /**
         * 播放的media是否支持快进
         *
         * @return
         */
        boolean canSeekForward();

        /**
         * 获取AudioSessionId
         *
         * @return
         */
        int getAudioSessionId();

        /**
         * 播放器是否处于可播放状态
         *
         * @return
         */
        boolean isInPlaybackState();

        /**
         * 是否本地视频流服务方式播放
         */
        boolean isLocalStreamMode();

        /**
         * 是否是绑定View的播放模式
         *
         * @return
         */
        boolean isAttachViewMode();

        /**
         * 绑定View是否是显示状态
         *
         * @return
         */
        boolean isAttachViewShown();

        /**
         * 是否是独立播放模式
         *
         * @return
         */
        boolean isAutocephalyPlayMode();

        /**
         * 非独立播放模式下，当View从Context中移除时，是否销毁播放器
         *
         * @return
         */
        boolean shouldDestroyPlayerWhenDetach();

        /**
         * 设置是否为独立播放模式（是否与播放界面共生命周期）
         *
         * @param isAutocephalyPlayMode   设置是否为独立播放模式
         * @param shouldDestroyWhenDetach 在非独立模式下，当控件View从上下文环境中（Context）移除时，
         *                                播放器是销毁(destroy)还是释放(release)
         *                                true: destroy模式下，从Context中移除后，非独立播放器所有状态清除，对象销毁，无法使用resume来恢复播放状态
         *                                false: release模式下，从Context中移除后，非独立播放器所有状态清除，对象不会销毁，可以使用resume来恢复播放状态
         */
        void setAutocephalyPlayMode(boolean isAutocephalyPlayMode, boolean shouldDestroyWhenDetach);

        /**
         * 获取播放器具体状态
         *
         * @return
         */

        PinePlayState getMediaPlayerState();

        /**
         * 设置在player状态发生改变时是否暂时忽略其所引起的controller状态的变化，在重置或者更换新的media bean后准备播放，这个值会被重置为false
         *
         * @param ignoreControllerStateTemporary 这个参数用来在player状态发生改变时暂时忽略其所引起的controller状态的变化，直到重新播放media bean。
         *                                       （正常情况下，在切换controller的过程后，旧的media bean还未完全释放时，会将状态更新到新的controller中）
         *                                       适用场景：切换controller的同时可能会需要重置或者更换新的media bean，而新的controller不应该去表现旧的media bean的状态。
         *                                       这个时候可以通过设置mIgnoreControllerStateTemporary为true来避免这个问题。
         *                                       你不需要手动重置mIgnoreControllerStateTemporary为false，因为在重置或者更换新的media bean后准备播放，这个值会被重置为false。
         */
        void setIgnoreTemporaryControllerState(boolean ignoreControllerStateTemporary);

        boolean ignoreTemporaryControllerState();

        /**
         * 移除播放状态监听器
         *
         * @param listener
         */
        void removeMediaPlayerListener(IPineMediaPlayerListener listener);

        /**
         * 添加播放状态监听器
         *
         * @param listener
         */
        void addMediaPlayerListener(IPineMediaPlayerListener listener);

        /**
         * 设置播放器当前的播放MediaPlayerView
         *
         * @return
         */
        void setMediaPlayerView(PineMediaPlayerView playerView, boolean forResume);

        /**
         * 获取播放器当前的播放MediaPlayerView
         *
         * @return
         */
        PineMediaPlayerView getMediaPlayerView();

        /**
         * 卸载播放器当前的播放MediaPlayerView
         *
         * @return
         */
        void detachMediaPlayerView(PineMediaPlayerView view);

        /**
         * 挂载控制器界面
         *
         * @param isPlayerReset 本此attach是否重置了MediaPlayer
         * @param isResumeState 本此attach是否是为了恢复状态
         */
        void attachMediaController(boolean isPlayerReset, boolean isResumeState);

        /**
         * 当前播放器是否开启了SurfaceView
         *
         * @return
         */
        boolean isSurfaceEnabled();

        /**
         * 是否开启当前播放器的SurfaceView
         *
         * @return
         */
        void enableSurface(boolean enable);

        /**
         * 获取播放器当前的播放SurfaceView
         *
         * @return
         */
        PineSurfaceView getSurfaceView();

        /**
         * 获取MediaView在onMeasure中调整后的布局属性，只有在onMeasure之后获取才有效
         *
         * @return
         */
        PineMediaPlayerView.PineMediaViewLayout getMediaAdaptionLayout();

        /**
         * 获取播放器控件宽度
         *
         * @return
         */
        int getMediaViewWidth();

        /**
         * 获取播放器控件高度
         *
         * @return
         */
        int getMediaViewHeight();

        void onSurfaceChanged(PineMediaPlayerView mediaPlayerView, SurfaceView surfaceView,
                              int format, int w, int h);

        void onSurfaceCreated(PineMediaPlayerView mediaPlayerView, SurfaceView surfaceView);

        void onSurfaceDestroyed(PineMediaPlayerView mediaPlayerView, SurfaceView surfaceView);
    }

    /**
     * 播放器播放状态监听器，主要提供给使用者使用，以对播放器播放状态进行监控
     */
    public interface IPineMediaPlayerListener {
        /**
         * 播放器放改变播
         *
         * @param playerBean
         * @param fromState  （STATE_ERROR, STATE_IDLE, STATE_PREPARING, STATE_PREPARED, STATE_PLAYING,
         *                   STATE_PAUSED, STATE_PLAYBACK_COMPLETED）
         * @param toState    （STATE_IDLE, STATE_PREPARING, STATE_PREPARED, STATE_PLAYING,
         *                   STATE_PAUSED, STATE_PLAYBACK_COMPLETED）
         */
        void onStateChange(PineMediaPlayerBean playerBean, PinePlayState fromState, PinePlayState toState);

        /**
         * 播放器播放信息更新
         *
         * @param playerBean
         * @param what
         * @param extra
         * @return
         */
        boolean onInfo(PineMediaPlayerBean playerBean, int what, int extra);

        /**
         * onBufferingUpdate回调
         *
         * @param playerBean
         * @param percent
         * @return
         */
        void onBufferingUpdate(PineMediaPlayerBean playerBean, int percent);

        /**
         * 播放器播放进度回调
         *
         * @param playerBean
         * @param progressTime the current position in milliseconds
         */
        void onProgress(PineMediaPlayerBean playerBean, int progressTime);

        /**
         * 播放器发生错误
         *
         * @param playerBean
         * @param framework_err 参考MediaPlayer.OnErrorListener what参数
         * @param impl_err      参考MediaPlayer.OnErrorListener extra参数
         * @return
         */
        boolean onError(PineMediaPlayerBean playerBean, int framework_err, int impl_err);

        /**
         * 播放器非正常播放结束（比如播放缓存或者暂停时session失效等问题）
         *
         * @param playerBean
         * @return
         */
        boolean onAbnormalComplete(PineMediaPlayerBean playerBean);

        /**
         * 播放器正常播放结束
         *
         * @param playerBean
         * @return
         */
        boolean onComplete(PineMediaPlayerBean playerBean);
    }

    /**
     * 默认播放器播放状态监听器，主要提供给使用者使用，以对播放器播放状态进行监控
     */
    public static class PineMediaPlayerListener implements IPineMediaPlayerListener {

        @Override
        public void onStateChange(PineMediaPlayerBean playerBean, PinePlayState fromState, PinePlayState toState) {

        }

        @Override
        public boolean onInfo(PineMediaPlayerBean playerBean, int what, int extra) {
            return false;
        }

        @Override
        public void onBufferingUpdate(PineMediaPlayerBean playerBean, int percent) {

        }

        @Override
        public void onProgress(PineMediaPlayerBean playerBean, int progressTime) {

        }

        @Override
        public boolean onError(PineMediaPlayerBean playerBean, int framework_err, int impl_err) {
            return false;
        }

        @Override
        public boolean onAbnormalComplete(PineMediaPlayerBean playerBean) {
            return false;
        }

        @Override
        public boolean onComplete(PineMediaPlayerBean playerBean) {
            return false;
        }
    }
}
