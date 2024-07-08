package com.pine.media.video.remote.server.arouter;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.pine.media.video.remote.server.VideoRemoteService;
import com.pine.tool.router.impl.arouter.ARouterBundleRemote;

/**
 * Created by tanghongfeng on 2019/2/21
 */

@Route(path = "/video/service")
public class VideoARouterRemote extends ARouterBundleRemote<VideoRemoteService> {

}
