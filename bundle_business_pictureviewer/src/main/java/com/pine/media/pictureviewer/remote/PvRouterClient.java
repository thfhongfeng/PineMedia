package com.pine.media.pictureviewer.remote;

import android.content.Context;
import android.os.Bundle;

import com.pine.tool.router.IRouterCallback;
import com.pine.tool.router.RouterManager;

public class PvRouterClient {
    public static void callCommand(Context context, String bundleKey, String commandType,
                                   String command, Bundle args, IRouterCallback callback) {
        RouterManager.callCommand(context, bundleKey, commandType, command, args, callback);
    }
}
