package com.pine.template.db_server;

import com.pine.template.base.BaseConstants;

/**
 * Created by tanghongfeng on 2018/9/13
 */

public interface DbConstants extends BaseConstants {
    // 数据库名
    String DATABASE_NAME = "pine.db";
    // 数据库版本
    int DATABASE_VERSION = 1;

    // 表名
    String ACCOUNT_TABLE_NAME = "db_account";
    String ACCOUNT_ACCESS_LOG_TABLE_NAME = "db_account_access_log";
    String SWITCHER_CONFIG_TABLE_NAME = "db_switcher_config";
    String APP_VERSION_TABLE_NAME = "db_app_version";

    String FILE_INFO_TABLE_NAME = "db_file_info";
}
