package com.pine.audioplayer.db.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.pine.audioplayer.db.ApRoomDatabase;
import com.pine.audioplayer.db.dao.ApMusicSheetDao;
import com.pine.audioplayer.db.dao.ApSheetMusicDao;
import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.tool.util.LogUtils;

import java.util.Calendar;
import java.util.List;

public class ApMusicSheetRepository {
    private final String TAG = LogUtils.makeLogTag(this.getClass());

    private static volatile ApMusicSheetRepository mInstance = null;

    private ApSheetMusicDao apSheetMusicDao;
    private ApMusicSheetDao apMusicSheetDao;

    public static ApMusicSheetRepository getInstance(Context application) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            if (mInstance == null) {
                mInstance = new ApMusicSheetRepository(application);
            }
            return mInstance;
        }
    }

    private ApRoomDatabase roomDatabase;

    private ApMusicSheetRepository(Context application) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            LogUtils.d(TAG, "new");
            roomDatabase = ApRoomDatabase.getINSTANCE(application);
            apSheetMusicDao = roomDatabase.apSheetMusicDao();
            apMusicSheetDao = roomDatabase.apMusicSheetDao();
        }
    }

    public static void reset() {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            mInstance = null;
        }
    }

    public ApMusicSheet querySheetById(long sheetId) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apMusicSheetDao.querySheetById(sheetId);
        }
    }

    public LiveData<ApMusicSheet> syncSheetById(long sheetId) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apMusicSheetDao.syncSheetById(sheetId);
        }
    }

    public ApMusicSheet querySheetByType(int sheetType) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apMusicSheetDao.querySheetByType(sheetType);
        }
    }

    public LiveData<ApMusicSheet> syncSheetByType(int sheetType) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apMusicSheetDao.syncSheetByType(sheetType);
        }
    }

    public List<ApMusicSheet> querySheetListByType(int sheetType) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apMusicSheetDao.querySheetListByType(sheetType);
        }
    }

    public List<ApMusicSheet> querySheetListByType(int sheetType, long... excludeIds) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apMusicSheetDao.querySheetListByType(sheetType, excludeIds);
        }
    }

    public List<ApMusicSheet> querySheetListByTypes(List<Integer> sheetTypes) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apMusicSheetDao.querySheetListByTypes(sheetTypes);
        }
    }

    public long addMusicSheet(@NonNull ApMusicSheet apMusicSheet) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            apMusicSheet.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            apMusicSheet.setCreateTimeStamp(Calendar.getInstance().getTimeInMillis());
            return apMusicSheetDao.insert(apMusicSheet);
        }
    }

    public void addMusicSheetList(@NonNull List<ApMusicSheet> list) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            apMusicSheetDao.insert(list);
        }
    }

    public void deleteMusicSheet(final @NonNull ApMusicSheet apMusicSheet) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            roomDatabase.runInTransaction(new Runnable() {
                @Override
                public void run() {
                    apMusicSheetDao.delete(apMusicSheet);
                    apSheetMusicDao.deleteBySheetId(apMusicSheet.getId());
                }
            });
        }
    }
}
