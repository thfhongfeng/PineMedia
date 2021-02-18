package com.pine.media.login;

import com.pine.media.base.BaseApplication;
import com.pine.media.config.BuildConfig;
import com.pine.media.login.model.interceptor.LoginResponseInterceptor;
import com.pine.tool.request.RequestManager;
import com.pine.tool.util.LogUtils;

/**
 * Created by tanghongfeng on 2018/9/14
 */

public class LoginApplication extends BaseApplication {
    private final static String TAG = LogUtils.makeLogTag(LoginApplication.class);

    public static void attach() {
        switch (BuildConfig.APP_THIRD_DATA_SOURCE_PROVIDER) {
            case "local":
                break;
            default:
                RequestManager.addGlobalResponseInterceptor(new LoginResponseInterceptor());
                break;
        }
    }
}
