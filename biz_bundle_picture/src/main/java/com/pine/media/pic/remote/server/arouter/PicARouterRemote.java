package com.pine.media.pic.remote.server.arouter;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.pine.media.pic.remote.server.PicRemoteService;
import com.pine.tool.router.impl.arouter.ARouterBundleRemote;

/**
 * Created by tanghongfeng on 2019/2/21
 */

@Route(path = "/pic/service")
public class PicARouterRemote extends ARouterBundleRemote<PicRemoteService> {

}
