package com.pine.media.base.router.command;

import com.pine.media.config.ConfigKey;
import com.pine.tool.router.annotation.ARouterRemoteAction;

/**
 * Created by tanghongfeng on 2019/1/25
 */

@ARouterRemoteAction(Key = ConfigKey.BUNDLE_AUDIO_PLAYER_KEY, RemoteAction = "/audioPlayer/service")
public interface RouterAudioPlayerCommand {
    String goAudioPlayerHomeActivity = "goAudioPlayerHomeActivity";
    String goAudioPlayerMainActivity = "goAudioPlayerMainActivity";
}
