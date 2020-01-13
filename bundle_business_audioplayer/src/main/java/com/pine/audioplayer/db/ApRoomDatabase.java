package com.pine.audioplayer.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.pine.audioplayer.db.dao.ApMusicDao;
import com.pine.audioplayer.db.dao.ApSheetDao;
import com.pine.audioplayer.db.dao.ApSheetMusicDao;
import com.pine.audioplayer.db.entity.ApMusic;
import com.pine.audioplayer.db.entity.ApSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.tool.util.AppUtils;
import com.pine.tool.util.LogUtils;
import com.pine.tool.util.PathUtils;
import com.tencent.wcdb.database.SQLiteCipherSpec;
import com.tencent.wcdb.room.db.WCDBOpenHelperFactory;

import java.io.File;

@Database(entities = {ApMusic.class, ApSheet.class, ApSheetMusic.class}, version = 1, exportSchema = false)
public abstract class ApRoomDatabase extends RoomDatabase {
    private static final String TAG = LogUtils.makeLogTag(ApRoomDatabase.class);

    public static final Object DB_SYNC_LOCK = new Object();

    public abstract ApMusicDao apMusicDao();

    public abstract ApSheetDao apSheetDao();

    public abstract ApSheetMusicDao apSheetMusicDao();

    private static final String PASSPHRASE = "pine123";

    // volatile关键字，确保不会被编译器优化
    private static volatile ApRoomDatabase INSTANCE;

    private static SQLiteCipherSpec cipherSpec = new SQLiteCipherSpec()
            .setPageSize(4096)
            .setKDFIteration(64000);

    private static WCDBOpenHelperFactory factory = new WCDBOpenHelperFactory()
            .passphrase(PASSPHRASE.getBytes())  // passphrase to the database, remove this line for plain-text
            .cipherSpec(cipherSpec);               // cipher to use, remove for default settings

    public static ApRoomDatabase getINSTANCE(final Context context) {
        synchronized (DB_SYNC_LOCK) {
            if (INSTANCE == null) {
                String path = PathUtils.getAppFilePath("db") + File.separator + "ap_music.db";
                LogUtils.d(TAG, "open or create database with path:" + path);
                Builder<ApRoomDatabase> builder = Room.databaseBuilder(context.getApplicationContext(), ApRoomDatabase.class, path);
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
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            if (INSTANCE != null) {
                INSTANCE.close();
                INSTANCE = null;
            }
        }
    }
}
