package com.pine.media.audio.db.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pine.media.audio.db.ApRoomDatabase;
import com.pine.media.audio.db.dao.ApSheetDao;
import com.pine.media.audio.db.dao.ApSheetMusicDao;
import com.pine.media.audio.db.entity.ApMusic;
import com.pine.media.audio.db.entity.ApSheetMusic;
import com.pine.tool.util.LogUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ApSheetMusicRepository {
    private final String TAG = LogUtils.makeLogTag(this.getClass());

    private static volatile ApSheetMusicRepository mInstance = null;

    private ApSheetMusicDao apSheetMusicDao;
    private ApSheetDao apSheetDao;

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
            apSheetDao = roomDatabase.apSheetDao();
        }
    }

    public static void reset() {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            mInstance = null;
        }
    }

    public int getSheetMusicListCount(long sheetId) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apSheetMusicDao.querySheetMusicCount(sheetId);
        }
    }

    public ApMusic querySheetMusic(long sheetId, long songId) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apSheetMusicDao.querySheetMusic(sheetId, songId);
        }
    }

    public List<ApMusic> querySheetMusicList(long sheetId) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apSheetMusicDao.querySheetMusic(sheetId);
        }
    }

    public ApMusic addSheetMusic(final @NonNull ApMusic music, final long sheetId) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            roomDatabase.runInTransaction(new Runnable() {
                @Override
                public void run() {
                    if (insertOrUpdateSheetMusic(music, sheetId)) {
                        updateMusicSheetCount(sheetId);
                    }
                }
            });
            return apSheetMusicDao.querySheetMusic(sheetId, music.getSongId());
        }
    }

    public List<ApMusic> addSheetMusicList(final @NonNull List<ApMusic> list, final long sheetId) {
        final List<ApMusic> retList = new ArrayList<>();
        if (list != null && list.size() > 0) {
            synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
                roomDatabase.runInTransaction(new Runnable() {
                    @Override
                    public void run() {
                        for (ApMusic music : list) {
                            insertOrUpdateSheetMusic(music, sheetId);
                            retList.add(apSheetMusicDao.querySheetMusic(sheetId, music.getSongId()));
                        }
                        updateMusicSheetCount(sheetId);
                    }
                });
            }
        }
        return retList;
    }

    private boolean insertOrUpdateSheetMusic(@NonNull ApMusic music, final long sheetId) {
        ApSheetMusic sheetMusic = apSheetMusicDao.checkSheetMusic(sheetId, music.getSongId());
        if (sheetMusic == null) {
            sheetMusic = new ApSheetMusic();
            sheetMusic.setSheetId(sheetId);
            sheetMusic.setSongId(music.getSongId());
            sheetMusic.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            sheetMusic.setCreateTimeStamp(Calendar.getInstance().getTimeInMillis());
            apSheetMusicDao.insert(sheetMusic);
            return true;
        } else {
            sheetMusic.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            apSheetMusicDao.update(sheetMusic);
            return false;
        }
    }

    public void deleteSheetMusic(final @NonNull ApMusic music, final long sheetId) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            roomDatabase.runInTransaction(new Runnable() {
                @Override
                public void run() {
                    apSheetMusicDao.deleteBySheetIdSongId(sheetId, music.getSongId());
                    updateMusicSheetCount(sheetId);
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

    public void deleteSheetMusicList(@NonNull final List<ApMusic> list, final long sheetId) {
        if (list.size() < 1) {
            return;
        }
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            roomDatabase.runInTransaction(new Runnable() {
                @Override
                public void run() {
                    List<Long> songIdList = new ArrayList<>();
                    for (ApMusic music : list) {
                        songIdList.add(music.getSongId());
                    }
                    apSheetMusicDao.deleteBySheetIdSongIdList(sheetId, songIdList);
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
        apSheetDao.updateSheetCount(sheetId, count);
    }
}
