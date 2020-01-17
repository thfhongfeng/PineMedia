package com.pine.videoplayer.db.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.pine.tool.util.LogUtils;
import com.pine.videoplayer.db.VpRoomDatabase;
import com.pine.videoplayer.db.dao.VpSheetDao;
import com.pine.videoplayer.db.dao.VpSheetVideoDao;
import com.pine.videoplayer.db.entity.VpSheet;

import java.util.Calendar;
import java.util.List;

public class VpSheetRepository {
    private final String TAG = LogUtils.makeLogTag(this.getClass());

    private static volatile VpSheetRepository mInstance = null;

    private VpSheetVideoDao vpSheetVideoDao;
    private VpSheetDao vpSheetDao;

    public static VpSheetRepository getInstance(Context application) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            if (mInstance == null) {
                mInstance = new VpSheetRepository(application);
            }
            return mInstance;
        }
    }

    private VpRoomDatabase roomDatabase;

    private VpSheetRepository(Context application) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            LogUtils.d(TAG, "new");
            roomDatabase = VpRoomDatabase.getINSTANCE(application);
            vpSheetVideoDao = roomDatabase.vpSheetVideoDao();
            vpSheetDao = roomDatabase.vpSheetDao();
        }
    }

    public static void reset() {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            mInstance = null;
        }
    }

    public VpSheet querySheetById(long sheetId) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            return vpSheetDao.querySheetById(sheetId);
        }
    }

    public LiveData<VpSheet> syncSheetById(long sheetId) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            return vpSheetDao.syncSheetById(sheetId);
        }
    }

    public VpSheet querySheetByType(int sheetType) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            return vpSheetDao.querySheetByType(sheetType);
        }
    }

    public LiveData<VpSheet> syncSheetByType(int sheetType) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            return vpSheetDao.syncSheetByType(sheetType);
        }
    }

    public List<VpSheet> querySheetListByType(int sheetType) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            return vpSheetDao.querySheetListByType(sheetType);
        }
    }

    public List<VpSheet> querySheetListByType(int sheetType, long... excludeIds) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            return vpSheetDao.querySheetListByType(sheetType, excludeIds);
        }
    }

    public List<VpSheet> querySheetListByTypes(List<Integer> sheetTypes) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            return vpSheetDao.querySheetListByTypes(sheetTypes);
        }
    }

    public long addVideoSheet(@NonNull VpSheet sheet) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            sheet.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            sheet.setCreateTimeStamp(Calendar.getInstance().getTimeInMillis());
            return vpSheetDao.insert(sheet);
        }
    }

    public void addVideoSheetList(@NonNull List<VpSheet> list) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            vpSheetDao.insert(list);
        }
    }

    public void deleteVideoSheet(final @NonNull VpSheet sheet) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            roomDatabase.runInTransaction(new Runnable() {
                @Override
                public void run() {
                    vpSheetDao.delete(sheet);
                    vpSheetVideoDao.deleteBySheetId(sheet.getId());
                }
            });
        }
    }
}
