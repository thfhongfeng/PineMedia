package com.pine.audioplayer.db.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pine.audioplayer.db.ApRoomDatabase;
import com.pine.audioplayer.db.dao.ApSheetMusicDao;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.tool.util.LogUtils;

import java.util.List;

public class ApSheetMusicRepository {
    private final String TAG = LogUtils.makeLogTag(this.getClass());

    private static volatile ApSheetMusicRepository mInstance = null;

    private ApSheetMusicDao apSheetMusicDao;

    public static ApSheetMusicRepository getInstance(Context application) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            if (mInstance == null) {
                mInstance = new ApSheetMusicRepository(application);
            }
            return mInstance;
        }
    }

    private ApRoomDatabase roomDatabase;

    private ApSheetMusicRepository(Context application) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            LogUtils.d(TAG, "new");
            roomDatabase = ApRoomDatabase.getINSTANCE(application);
            apSheetMusicDao = roomDatabase.apSheetMusicDao();
        }
    }

    public static void reset() {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            mInstance = null;
        }
    }

    public List<ApSheetMusic> querySheetMusicList(long sheetId) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apSheetMusicDao.querySheetMusic(sheetId);
        }
    }

    public void addSheetMusicList(@NonNull List<ApSheetMusic> list) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            apSheetMusicDao.insert(list);
        }
    }

    public void addSheetMusic(@NonNull ApSheetMusic apSheetMusic) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            apSheetMusicDao.insert(apSheetMusic);
        }
    }

    public void deleteSheetMusic(@NonNull ApSheetMusic apSheetMusic) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            apSheetMusicDao.delete(apSheetMusic);
        }
    }

    public void deleteSheetMusicList(@NonNull List<ApSheetMusic> list) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            apSheetMusicDao.delete(list);
        }
    }
}
