package com.pine.media.video.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.pine.media.video.db.dao.VpSheetDao;
import com.pine.media.video.db.dao.VpSheetVideoDao;
import com.pine.media.video.db.dao.VpVideoDao;
import com.pine.media.video.db.entity.VpSheet;
import com.pine.media.video.db.entity.VpSheetVideo;
import com.pine.media.video.db.entity.VpVideo;
import com.pine.tool.util.AppUtils;
import com.pine.tool.util.LogUtils;
import com.pine.tool.util.PathUtils;
import com.tencent.wcdb.database.SQLiteCipherSpec;
import com.tencent.wcdb.room.db.WCDBOpenHelperFactory;

import java.io.File;

@Database(entities = {VpVideo.class, VpSheet.class, VpSheetVideo.class}, version = 1, exportSchema = false)
public abstract class VpRoomDatabase extends RoomDatabase {
    private static final String TAG = LogUtils.makeLogTag(VpRoomDatabase.class);

    public static final Object DB_SYNC_LOCK = new Object();

    public abstract VpVideoDao vpVideoDao();

    public abstract VpSheetDao vpSheetDao();

    public abstract VpSheetVideoDao vpSheetVideoDao();

    private static final String PASSPHRASE = "pine123";

    // volatile关键字，确保不会被编译器优化
    private static volatile VpRoomDatabase INSTANCE;

    private static SQLiteCipherSpec cipherSpec = new SQLiteCipherSpec()
            .setPageSize(4096)
            .setKDFIteration(64000);

    private static WCDBOpenHelperFactory factory = new WCDBOpenHelperFactory()
            .passphrase(PASSPHRASE.getBytes())  // passphrase to the database, remove this line for plain-text
            .cipherSpec(cipherSpec);               // cipher to use, remove for default settings

    public static VpRoomDatabase getINSTANCE(final Context context) {
        synchronized (DB_SYNC_LOCK) {
            if (INSTANCE == null) {
                String path = PathUtils.getAppFilePath("db") + File.separator + "video_player.db";
                LogUtils.d(TAG, "open or create database with path:" + path);
                Builder<VpRoomDatabase> builder = Room.databaseBuilder(context.getApplicationContext(), VpRoomDatabase.class, path);
                if (!AppUtils.isApkDebuggable(context)) {
                    LogUtils.d(TAG, "on debug apk, disable encrypt database");
                    builder.openHelperFactory(factory);  // encrypt
                }
                builder.allowMainThreadQueries();   // 允许主线程操作
                builder.addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        LogUtils.d(TAG, "onCreate");
                    }

                    @Override
                    public void onOpen(@NonNull SupportSQLiteDatabase db) {
                        LogUtils.d(TAG, "onOpen");
                    }
                });
                INSTANCE = builder.build();
            }
            return INSTANCE;
        }
    }

    public static void resetDatabase() {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            if (INSTANCE != null) {
                INSTANCE.close();
                INSTANCE = null;
            }
        }
    }
}
