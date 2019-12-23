package com.pine.audioplayer.db.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pine.audioplayer.db.ApRoomDatabase;
import com.pine.audioplayer.db.dao.ApMusicSheetDao;
import com.pine.audioplayer.db.dao.ApSheetMusicDao;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.tool.util.LogUtils;

import java.util.List;

public class ApSheetMusicRepository {
    private final String TAG = LogUtils.makeLogTag(this.getClass());

    private static volatile ApSheetMusicRepository mInstance = null;

    private ApSheetMusicDao apSheetMusicDao;
    private ApMusicSheetDao apMusicSheetDao;

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
            apMusicSheetDao = roomDatabase.apMusicSheetDao();
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

    public void addSheetMusic(final @NonNull ApSheetMusic apSheetMusic, final long sheetId) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            roomDatabase.runInTransaction(new Runnable() {
                @Override
                public void run() {
                    if (apSheetMusicDao.checkSheetMusic(sheetId, apSheetMusic.getSongId(), apSheetMusic.getFilePath()) == null) {
                        apSheetMusic.setId(0);
                        apSheetMusic.setSheetId(sheetId);
                        apSheetMusicDao.insert(apSheetMusic);
                        updateMusicSheetCount(apSheetMusic.getSheetId());
                    }
                }
            });
        }
    }

    public void addSheetMusicList(final @NonNull List<ApSheetMusic> list, final long sheetId) {
        if (list.size() < 1) {
            return;
        }
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            roomDatabase.runInTransaction(new Runnable() {
                @Override
                public void run() {
                    for (ApSheetMusic music : list) {
                        if (apSheetMusicDao.checkSheetMusic(sheetId, music.getSongId(), music.getFilePath()) == null) {
                            music.setId(0);
                            music.setSheetId(sheetId);
                            apSheetMusicDao.insert(music);
                        }
                    }
                    updateMusicSheetCount(sheetId);
                }
            });
        }
    }

    public void deleteSheetMusic(final @NonNull ApSheetMusic apSheetMusic) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            roomDatabase.runInTransaction(new Runnable() {
                @Override
                public void run() {
                    apSheetMusicDao.delete(apSheetMusic);
                    updateMusicSheetCount(apSheetMusic.getSheetId());
                }
            });
        }
    }

    public void deleteSheetMusicList(@NonNull final List<ApSheetMusic> list, final long sheetId) {
        if (list.size() < 1) {
            return;
        }
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            roomDatabase.runInTransaction(new Runnable() {
                @Override
                public void run() {
                    apSheetMusicDao.delete(list);
                    updateMusicSheetCount(sheetId);
                }
            });
        }
    }

    private void updateMusicSheetCount(long sheetId) {
        int count = apSheetMusicDao.querySheetMusicCount(sheetId);
        apMusicSheetDao.updateSheetCount(sheetId, count);
    }
}
