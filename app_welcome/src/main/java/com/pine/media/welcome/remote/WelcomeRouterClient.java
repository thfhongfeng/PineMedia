package com.pine.media.welcome.remote;

import android.content.Context;
import android.os.Bundle;

import com.pine.media.base.router.command.RouterAudioPlayerCommand;
import com.pine.media.base.router.command.RouterLoginCommand;
import com.pine.media.base.router.command.RouterMainCommand;
import com.pine.media.config.ConfigKey;
import com.pine.tool.router.IRouterCallback;
import com.pine.tool.router.RouterManager;
import com.pine.tool.ui.Activity;

public class WelcomeRouterClient {

    public static void autoLogin(Context context, Bundle args, IRouterCallback callback) {
        RouterManager.callOpCommand(context, ConfigKey.BUNDLE_LOGIN_KEY,
                RouterLoginCommand.autoLogin, args, callback);
    }

    public static void goMainHomeActivity(Context context, Bundle args, IRouterCallback callback) {
        RouterManager.callUiCommand(context, ConfigKey.BUNDLE_MAIN_KEY,
                RouterMainCommand.goMainHomeActivity, args, callback);
    }

    public static void goAudioPlayerMainActivity(Activity context, Bundle args, IRouterCallback callback) {
        RouterManager.callUiCommand(context, ConfigKey.BUNDLE_AUDIO_PLAYER_KEY,
                RouterAudioPlayerCommand.goAudioPlayerMainActivity, args, callback);
    }
}
