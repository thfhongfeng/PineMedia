package com.pine.media.audio.remote.server.arouter;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.pine.media.audio.remote.server.AudioRemoteService;
import com.pine.tool.router.impl.arouter.ARouterBundleRemote;

/**
 * Created by tanghongfeng on 2019/2/21
 */

@Route(path = "/audio/service")
public class AudioARouterRemote extends ARouterBundleRemote<AudioRemoteService> {

}
