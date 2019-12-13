package com.pine.user.remote;

import android.content.Context;
import android.os.Bundle;

import com.pine.base.bean.AccountBean;
import com.pine.base.router.command.RouterLoginCommand;
import com.pine.config.ConfigKey;
import com.pine.tool.router.IRouterCallback;
import com.pine.tool.router.RouterManager;

public class UserRouterClient {

    public static void logout(Context context, Bundle args, IRouterCallback callback) {
        RouterManager.callOpCommand(context, ConfigKey.BUNDLE_LOGIN_KEY, RouterLoginCommand.logout, args, callback);
    }

    public static AccountBean getLoginAccount(Context context, Bundle args) {
        return RouterManager.callDataCommandDirect(context, ConfigKey.BUNDLE_LOGIN_KEY, RouterLoginCommand.getLoginAccount, args);
    }
}
