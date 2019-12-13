package com.pine.audioplayer.remote.server.arouter;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.pine.audioplayer.remote.server.AudioPlayerRemoteService;
import com.pine.tool.router.impl.arouter.ARouterBundleRemote;

/**
 * Created by tanghongfeng on 2019/2/21
 */

@Route(path = "/audioPlayer/service")
public class AudioPlayerARouterRemote extends ARouterBundleRemote<AudioPlayerRemoteService> {

}
