package com.pine.videoplayer.remote.server.arouter;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.pine.tool.router.impl.arouter.ARouterBundleRemote;
import com.pine.videoplayer.remote.server.VideoPlayerRemoteService;

/**
 * Created by tanghongfeng on 2019/2/21
 */

@Route(path = "/videoPlayer/service")
public class VideoPlayerARouterRemote extends ARouterBundleRemote<VideoPlayerRemoteService> {

}
