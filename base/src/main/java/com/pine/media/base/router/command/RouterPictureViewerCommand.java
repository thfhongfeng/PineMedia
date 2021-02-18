package com.pine.media.base.router.command;

import com.pine.media.config.ConfigKey;
import com.pine.tool.router.annotation.ARouterRemoteAction;

/**
 * Created by tanghongfeng on 2019/1/25
 */

@ARouterRemoteAction(Key = ConfigKey.BUNDLE_PICTURE_VIEWER_KEY, RemoteAction = "/pictureViewer/service")
public interface RouterPictureViewerCommand {
    String goPictureViewerHomeActivity = "goPictureViewerHomeActivity";
}
