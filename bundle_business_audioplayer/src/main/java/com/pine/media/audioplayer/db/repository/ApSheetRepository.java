package com.pine.media.audioplayer.db.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.pine.media.audioplayer.db.ApRoomDatabase;
import com.pine.media.audioplayer.db.dao.ApSheetDao;
import com.pine.media.audioplayer.db.dao.ApSheetMusicDao;
import com.pine.media.audioplayer.db.entity.ApSheet;
import com.pine.tool.util.LogUtils;

import java.util.Calendar;
import java.util.List;

public class ApSheetRepository {
    private final String TAG = LogUtils.makeLogTag(this.getClass());

    private static volatile ApSheetRepository mInstance = null;

    private ApSheetMusicDao apSheetMusicDao;
    private ApSheetDao apSheetDao;

    public static ApSheetRepository getInstance(Context application) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            if (mInstance == null) {
                mInstance = new ApSheetRepository(application);
            }
            return mInstance;
        }
    }

    private ApRoomDatabase roomDatabase;

    private ApSheetRepository(Context application) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            LogUtils.d(TAG, "new");
            roomDatabase = ApRoomDatabase.getINSTANCE(application);
            apSheetMusicDao = roomDatabase.apSheetMusicDao();
            apSheetDao = roomDatabase.apSheetDao();
        }
    }

    public static void reset() {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            mInstance = null;
        }
    }

    public ApSheet querySheetById(long sheetId) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apSheetDao.querySheetById(sheetId);
        }
    }

    public LiveData<ApSheet> syncSheetById(long sheetId) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apSheetDao.syncSheetById(sheetId);
        }
    }

    public ApSheet querySheetByType(int sheetType) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apSheetDao.querySheetByType(sheetType);
        }
    }

    public LiveData<ApSheet> syncSheetByType(int sheetType) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apSheetDao.syncSheetByType(sheetType);
        }
    }

    public List<ApSheet> querySheetListByType(int sheetType) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apSheetDao.querySheetListByType(sheetType);
        }
    }

    public List<ApSheet> querySheetListByType(int sheetType, long... excludeIds) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apSheetDao.querySheetListByType(sheetType, excludeIds);
        }
    }

    public List<ApSheet> querySheetListByTypes(List<Integer> sheetTypes) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apSheetDao.querySheetListByTypes(sheetTypes);
        }
    }

    public long addMusicSheet(@NonNull ApSheet sheet) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            sheet.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            sheet.setCreateTimeStamp(Calendar.getInstance().getTimeInMillis());
            return apSheetDao.insert(sheet);
        }
    }

    public void addMusicSheetList(@NonNull List<ApSheet> list) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            apSheetDao.insert(list);
        }
    }

    public void deleteMusicSheet(final @NonNull ApSheet sheet) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            roomDatabase.runInTransaction(new Runnable() {
                @Override
                public void run() {
                    apSheetDao.delete(sheet);
                    apSheetMusicDao.deleteBySheetId(sheet.getId());
                }
            });
        }
    }
}
