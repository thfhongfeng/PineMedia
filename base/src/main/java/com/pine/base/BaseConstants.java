package com.pine.base;

import com.pine.config.Constants;

/**
 * Created by tanghongfeng on 2018/9/10.
 */

public interface BaseConstants extends Constants {
    String LOGIN_ACCOUNT_ID = "accountId";
    String LOGIN_ACCOUNT = "mobile";
    String LOGIN_PASSWORD = "password";
    String LOGIN_VERIFY_CODE = "verifyCode";

    // list adapter key
    String PAGE_NO = "pageNo";
    String PAGE_SIZE = "pageSize";

    int PLUGIN_IMAGE_ADVERT = 1;
    int PLUGIN_LRC_SUBTITLE = 2;
    int PLUGIN_SRT_SUBTITLE = 3;
    int PLUGIN_BARRAGE = 4;
}
