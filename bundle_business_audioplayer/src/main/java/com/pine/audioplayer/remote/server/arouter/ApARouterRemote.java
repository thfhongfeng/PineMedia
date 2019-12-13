package com.pine.audioplayer.remote.server.arouter;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.pine.audioplayer.remote.server.ApRemoteService;
import com.pine.tool.router.impl.arouter.ARouterBundleRemote;

/**
 * Created by tanghongfeng on 2019/2/21
 */

@Route(path = "/audioPlayer/service")
public class ApARouterRemote extends ARouterBundleRemote<ApRemoteService> {

}
