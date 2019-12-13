package com.pine.pictureviewer.remote.server.arouter;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.pine.pictureviewer.remote.server.PvRemoteService;
import com.pine.tool.router.impl.arouter.ARouterBundleRemote;

/**
 * Created by tanghongfeng on 2019/2/21
 */

@Route(path = "/pictureViewer/service")
public class PvARouterRemote extends ARouterBundleRemote<PvRemoteService> {

}
