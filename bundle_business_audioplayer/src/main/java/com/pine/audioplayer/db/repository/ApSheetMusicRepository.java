package com.pine.audioplayer.db.repository;

import android.content.Context;

import com.pine.audioplayer.db.ApRoomDatabase;
import com.pine.audioplayer.db.dao.ApMusicSheetDao;
import com.pine.audioplayer.db.dao.ApSheetMusicDao;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.tool.util.LogUtils;

import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;

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
                    if (insertOrUpdateSheetMusic(apSheetMusic, sheetId)) {
                        updateMusicSheetCount(apSheetMusic.getSheetId());
                    }
                }
            });
        }
    }

    public void addSheetMusicList(final @NonNull List<ApSheetMusic> list, final long sheetId) {
        if (list == null && list.size() < 1) {
            return;
        }
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            roomDatabase.runInTransaction(new Runnable() {
                @Override
                public void run() {
                    for (ApSheetMusic music : list) {
                        insertOrUpdateSheetMusic(music, sheetId);
                    }
                    updateMusicSheetCount(sheetId);
                }
            });
        }
    }

    private boolean insertOrUpdateSheetMusic(@NonNull ApSheetMusic music, final long sheetId) {
        ApSheetMusic dbMusic = apSheetMusicDao.checkSheetMusic(sheetId, music.getSongId(), music.getFilePath());
        if (dbMusic == null) {
            music.setId(0);
            music.setSheetId(sheetId);
            music.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            music.setCreateTimeStamp(Calendar.getInstance().getTimeInMillis());
            apSheetMusicDao.insert(music);
            return true;
        } else {
            dbMusic.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            apSheetMusicDao.update(music);
            return false;
        }
    }

    public void updateSheetMusic(@NonNull ApSheetMusic music) {
        apSheetMusicDao.update(music);
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

    public void deleteSheetMusic(final @NonNull long sheetId, final long songId) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            roomDatabase.runInTransaction(new Runnable() {
                @Override
                public void run() {
                    apSheetMusicDao.deleteBySheetIdSongId(sheetId, songId);
                    updateMusicSheetCount(sheetId);
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

    public void deleteSheetAllMusics(final @NonNull long sheetId) {
        roomDatabase.runInTransaction(new Runnable() {
            @Override
            public void run() {
                apSheetMusicDao.deleteBySheetId(sheetId);
                updateMusicSheetCount(sheetId);
            }
        });
    }

    private void updateMusicSheetCount(long sheetId) {
        int count = apSheetMusicDao.querySheetMusicCount(sheetId);
        apMusicSheetDao.updateSheetCount(sheetId, count);
    }

}
