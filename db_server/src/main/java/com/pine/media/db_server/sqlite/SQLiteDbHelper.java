package com.pine.media.db_server.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.pine.media.config.ConfigKey;
import com.pine.tool.util.LogUtils;
import com.pine.tool.util.SecurityUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static com.pine.media.db_server.DbConstants.ACCOUNT_ACCESS_LOG_TABLE_NAME;
import static com.pine.media.db_server.DbConstants.ACCOUNT_TABLE_NAME;
import static com.pine.media.db_server.DbConstants.APP_VERSION_TABLE_NAME;
import static com.pine.media.db_server.DbConstants.DATABASE_NAME;
import static com.pine.media.db_server.DbConstants.DATABASE_VERSION;
import static com.pine.media.db_server.DbConstants.FILE_INFO_TABLE_NAME;
import static com.pine.media.db_server.DbConstants.SWITCHER_CONFIG_TABLE_NAME;

public class SQLiteDbHelper extends SQLiteOpenHelper {
    private final String TAG = LogUtils.makeLogTag(this.getClass());

    public SQLiteDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtils.d(TAG, "onCreate");
        createFileInfoTable(db);
        createConfigSwitcherTable(db);
        createAppVersionTable(db);
        createAccountTable(db);
        createAccountAccessLogTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void createFileInfoTable(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + FILE_INFO_TABLE_NAME +
                "(_id integer primary key autoincrement,fileName text not null," +
                "filePath text not null,bizType integer not null,fileType integer not null," +
                "descr text,orderNum integer not null," +
                "createTime text,updateTime text)");
    }

    private void createConfigSwitcherTable(SQLiteDatabase db) {
        try {
            db.execSQL("create table if not exists " + SWITCHER_CONFIG_TABLE_NAME +
                    "(_id integer primary key autoincrement,configType integer not null," +
                    "accountType integer not null,configKey text not null,parentConfigKey text," +
                    "state integer not null,createTime text,updateTime text)");
            List<ContentValues> list = new ArrayList<>();
            HashMap<Integer, Integer> accountTypeMap = new HashMap<>();
            accountTypeMap.put(0, 999999);
            for (int i = 10; i >= 1; i--) {
                accountTypeMap.put(i, 9000 + i * 10);
            }
            accountTypeMap.put(11, 100);
            accountTypeMap.put(12, 0);
            for (int i = 0; i < 13; i++) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("configType", 1); // 配置类型:0-缺省；1-模块开关；2-功能开关；3-配置开关
                contentValues.put("configKey", ConfigKey.BUNDLE_DB_SEVER_KEY);
                contentValues.put("state", 1); // 是否开放：0-关闭；1-开放
                contentValues.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                contentValues.put("updateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                contentValues.put("accountType", accountTypeMap.get(i)); // 账户类型:0-游客（临时账户），100-注册用户，999999-超级管理员，会员(9000-9999之间)
                list.add(contentValues);
                contentValues = new ContentValues();
                contentValues.put("configType", 1); // 配置类型:0-缺省；1-模块开关；2-功能开关；3-配置开关
                contentValues.put("configKey", ConfigKey.BUNDLE_WELCOME_KEY);
                contentValues.put("state", 1); // 是否开放：0-关闭；1-开放
                contentValues.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                contentValues.put("updateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                contentValues.put("accountType", accountTypeMap.get(i)); // 账户类型:0-游客（临时账户），100-注册用户，999999-超级管理员，会员(9000-9999之间)
                contentValues = new ContentValues();
                contentValues.put("configType", 1); // 配置类型:0-缺省；1-模块开关；2-功能开关；3-配置开关
                contentValues.put("configKey", ConfigKey.BUNDLE_LOGIN_KEY);
                contentValues.put("state", 1); // 是否开放：0-关闭；1-开放
                contentValues.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                contentValues.put("updateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                contentValues.put("accountType", accountTypeMap.get(i)); // 账户类型:0-游客（临时账户），100-注册用户，999999-超级管理员，会员(9000-9999之间)
                list.add(contentValues);
                contentValues = new ContentValues();
                contentValues.put("configType", 1); // 配置类型:0-缺省；1-模块开关；2-功能开关；3-配置开关
                contentValues.put("configKey", ConfigKey.BUNDLE_MAIN_KEY);
                contentValues.put("state", 1); // 是否开放：0-关闭；1-开放
                contentValues.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                contentValues.put("updateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                contentValues.put("accountType", accountTypeMap.get(i)); // 账户类型:0-游客（临时账户），100-注册用户，999999-超级管理员，会员(9000-9999之间)
                list.add(contentValues);
                contentValues = new ContentValues();
                contentValues.put("configType", 1); // 配置类型:0-缺省；1-模块开关；2-功能开关；3-配置开关
                contentValues.put("configKey", ConfigKey.BUNDLE_USER_KEY);
                contentValues.put("state", 1); // 是否开放：0-关闭；1-开放
                contentValues.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                contentValues.put("updateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                contentValues.put("accountType", accountTypeMap.get(i)); // 账户类型:0-游客（临时账户），100-注册用户，999999-超级管理员，会员(9000-9999之间)
                list.add(contentValues);

                contentValues = new ContentValues();
                contentValues.put("configType", 1); // 配置类型:0-缺省；1-模块开关；2-功能开关；3-配置开关
                contentValues.put("configKey", ConfigKey.BUNDLE_VIDEO_PLAYER_KEY);
                contentValues.put("state", 1); // 是否开放：0-关闭；1-开放
                contentValues.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                contentValues.put("updateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                contentValues.put("accountType", accountTypeMap.get(i)); // 账户类型:0-游客（临时账户），100-注册用户，999999-超级管理员，会员(9000-9999之间)
                list.add(contentValues);
                contentValues = new ContentValues();
                contentValues.put("configType", 1); // 配置类型:0-缺省；1-模块开关；2-功能开关；3-配置开关
                contentValues.put("configKey", ConfigKey.BUNDLE_PICTURE_VIEWER_KEY);
                contentValues.put("state", 1); // 是否开放：0-关闭；1-开放
                contentValues.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                contentValues.put("updateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                contentValues.put("accountType", accountTypeMap.get(i)); // 账户类型:0-游客（临时账户），100-注册用户，999999-超级管理员，会员(9000-9999之间)
                list.add(contentValues);
                contentValues = new ContentValues();
                contentValues.put("configType", 1); // 配置类型:0-缺省；1-模块开关；2-功能开关；3-配置开关
                contentValues.put("configKey", ConfigKey.BUNDLE_AUDIO_PLAYER_KEY);
                contentValues.put("state", 1); // 是否开放：0-关闭；1-开放
                contentValues.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                contentValues.put("updateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                contentValues.put("accountType", accountTypeMap.get(i)); // 账户类型:0-游客（临时账户），100-注册用户，999999-超级管理员，会员(9000-9999之间)
                list.add(contentValues);

                contentValues = new ContentValues();
                contentValues.put("configType", 3); // 配置类型:0-缺省；1-模块开关；2-功能开关；3-配置开关
                contentValues.put("configKey", ConfigKey.CONFIG_ADS_ALLOW_KEY);
                contentValues.put("state", 0); // 是否开放：0-关闭；1-开放
                contentValues.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                contentValues.put("updateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                contentValues.put("accountType", accountTypeMap.get(i)); // 账户类型:0-游客（临时账户），100-注册用户，999999-超级管理员，会员(9000-9999之间)
                list.add(contentValues);
            }
            boolean insertSuccess = true;
            db.beginTransaction();
            for (ContentValues cv : list) {
                long id = db.insert(SWITCHER_CONFIG_TABLE_NAME, "configKey", cv);
                if (id == -1) {
                    insertSuccess = false;
                }
            }
            db.setTransactionSuccessful();
            if (insertSuccess) {
                LogUtils.d(TAG, "createConfigSwitcherTable success");
            } else {
                LogUtils.d(TAG, "createConfigSwitcherTable fail: insert init data fail");
            }
        } catch (SQLException e) {
            LogUtils.d(TAG, "createConfigSwitcherTable fail: " + e.toString());
        }
        db.endTransaction();
    }

    private void createAppVersionTable(SQLiteDatabase db) {
        try {
            db.execSQL("create table if not exists " + APP_VERSION_TABLE_NAME +
                    "(_id integer primary key autoincrement,packageName text not null," +
                    "versionName text not null,versionCode integer not null," +
                    "minSupportedVersion text,force integer,fileName text,path text," +
                    "createTime text,updateTime text)");
            List<ContentValues> list = new ArrayList<>();
            ContentValues contentValues = new ContentValues();
            contentValues.put("packageName", "com.pine.media");
            contentValues.put("versionName", "1.0.1");
            contentValues.put("versionCode", 1);
            contentValues.put("minSupportedVersion", 1);
            contentValues.put("force", 0);  // 是否强制更新：0-不强制；1-强制
            contentValues.put("fileName", "pine_app_media-V1.0.1-release.apk");
            contentValues.put("path", "http://yanyangtian.purang.com/download/bsd_purang.apk");
            contentValues.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
            contentValues.put("updateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
            list.add(contentValues);
            contentValues = new ContentValues();
            contentValues.put("packageName", "com.pine.media");
            contentValues.put("versionName", "1.0.2");
            contentValues.put("versionCode", 2);
            contentValues.put("minSupportedVersion", 1);
            contentValues.put("force", 0);  // 是否强制更新：0-不强制；1-强制
            contentValues.put("fileName", "pine_app_media-V1.0.2-release.apk");
            contentValues.put("path", "http://yanyangtian.purang.com/download/bsd_purang.apk");
            contentValues.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
            contentValues.put("updateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
            list.add(contentValues);

            boolean insertSuccess = true;
            db.beginTransaction();
            for (ContentValues cv : list) {
                long id = db.insert(APP_VERSION_TABLE_NAME, "package", cv);
                if (id == -1) {
                    insertSuccess = false;
                }
            }
            db.setTransactionSuccessful();
            if (insertSuccess) {
                LogUtils.d(TAG, "createAppVersionTable success");
            } else {
                LogUtils.d(TAG, "createAppVersionTable fail: insert init data fail");
            }
        } catch (SQLException e) {
            LogUtils.d(TAG, "createAppVersionTable fail: " + e.toString());
        }
        db.endTransaction();
    }

    private final String[] HEAD_IMAGES = {"http://i1.sinaimg.cn/ent/d/2008-06-04/U105P28T3D2048907F326DT20080604225106.jpg",
            "https://c-ssl.duitang.com/uploads/item/201704/04/20170404153225_EiMHP.thumb.700_0.jpeg",
            "http://image2.sina.com.cn/IT/d/2005-10-31/U1235P2T1D752393F13DT20051031133235.jpg"};

    private void createAccountTable(SQLiteDatabase db) {
        try {
            db.execSQL("create table if not exists " + ACCOUNT_TABLE_NAME +
                    "(_id integer primary key autoincrement,id text not null unique," +
                    "account text not null,accountType integer not null," +
                    "name text not null,password text not null, headImgUrl text,state integer not null," +
                    "mobile text not null,curLoginTimeStamp integer not null,createTime text," +
                    "updateTime text)");
            int imageTotalCount = HEAD_IMAGES.length;
            Calendar calendar = Calendar.getInstance();
            List<ContentValues> list = new ArrayList<>();
            ContentValues contentValues = new ContentValues();
            contentValues.put("id", "1000" + "20190328102000000" + "000");
            contentValues.put("account", "admin");
            contentValues.put("accountType", 999999); // 账户类型:0-游客（临时账户），100-注册用户，999999-超级管理员，会员(9000-9999之间)
            contentValues.put("name", "admin");
            contentValues.put("password", SecurityUtils.generateMD5("111aaa"));
            contentValues.put("state", 1); // 账户状态:0-删除，1-激活，2-未激活
            contentValues.put("mobile", "18672943565");
            contentValues.put("headImgUrl", HEAD_IMAGES[0 % imageTotalCount]);
            contentValues.put("curLoginTimeStamp", 0);
            contentValues.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
            contentValues.put("updateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
            list.add(contentValues);
            contentValues = new ContentValues();
            contentValues.put("id", "1000" + "20190328102000000" + "001");
            contentValues.put("account", "15221464292");
            contentValues.put("accountType", 9100); // 账户类型:0-游客（临时账户），100-注册用户，999999-超级管理员，会员(9000-9999之间)
            contentValues.put("name", "15221464292");
            contentValues.put("password", SecurityUtils.generateMD5("111aaa"));
            contentValues.put("state", 1); // 账户状态:0-删除，1-激活，2-未激活
            contentValues.put("mobile", "15221464292");
            contentValues.put("headImgUrl", HEAD_IMAGES[1 % imageTotalCount]);
            contentValues.put("curLoginTimeStamp", 0);
            contentValues.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
            contentValues.put("updateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
            list.add(contentValues);
            contentValues = new ContentValues();
            contentValues.put("id", "1000" + "20190328102000000" + "002");
            contentValues.put("account", "15221464296");
            contentValues.put("accountType", 9010); // 账户类型:0-游客（临时账户），100-注册用户，999999-超级管理员，会员(9000-9999之间)
            contentValues.put("name", "15221464296");
            contentValues.put("password", SecurityUtils.generateMD5("111aaa"));
            contentValues.put("state", 1); // 账户状态:0-删除，1-激活，2-未激活
            contentValues.put("mobile", "15221464296");
            contentValues.put("headImgUrl", HEAD_IMAGES[2 % imageTotalCount]);
            contentValues.put("curLoginTimeStamp", 0);
            contentValues.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
            contentValues.put("updateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
            list.add(contentValues);
            boolean insertSuccess = true;
            db.beginTransaction();
            for (ContentValues cv : list) {
                long id = db.insert(ACCOUNT_TABLE_NAME, "typeName", cv);
                if (id == -1) {
                    insertSuccess = false;
                }
            }
            db.setTransactionSuccessful();
            if (insertSuccess) {
                LogUtils.d(TAG, "createAccountTable success");
            } else {
                LogUtils.d(TAG, "createAccountTable fail: insert init data fail");
            }
        } catch (SQLException e) {
            LogUtils.d(TAG, "createAccountTable fail: " + e.toString());
        }
        db.endTransaction();
    }

    private void createAccountAccessLogTable(SQLiteDatabase db) {
        try {
            db.execSQL("create table if not exists " + ACCOUNT_ACCESS_LOG_TABLE_NAME +
                    "(_id integer primary key autoincrement,accountId text not null," +
                    "loginTimeStamp integer,logoutTimeStamp integer)");
            LogUtils.d(TAG, "createAccountAccessLogTable success");
        } catch (SQLException e) {
            LogUtils.d(TAG, "createAccountAccessLogTable fail: " + e.toString());
        }
    }
}
