package com.pine.audioplayer.db.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pine.audioplayer.ApConstants;
import com.pine.audioplayer.db.ApRoomDatabase;
import com.pine.audioplayer.db.dao.ApMusicSheetDao;
import com.pine.audioplayer.db.dao.ApSheetMusicDao;
import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.tool.util.LogUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ApSheetMusicRepository {
    private final String TAG = LogUtils.makeLogTag(this.getClass());

    private static volatile ApSheetMusicRepository mInstance = null;

    private ApSheetMusicDao apSheetMusicDao;
    private ApMusicSheetDao apMusicSheetDao;

    private ApMusicSheet mFavouriteSheet;

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

            mFavouriteSheet = apMusicSheetDao.querySheetByType(ApConstants.MUSIC_SHEET_TYPE_FAVOURITE);
        }
    }

    public static void reset() {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            mInstance = null;
        }
    }

    public ApSheetMusic querySheetMusic(long sheetId, long songId) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apSheetMusicDao.querySheetMusic(sheetId, songId);
        }
    }

    public List<ApSheetMusic> querySheetMusicList(long songId, String filePath) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apSheetMusicDao.checkSheetMusicList(songId, filePath);
        }
    }

    public List<ApSheetMusic> querySheetMusicList(long sheetId) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apSheetMusicDao.querySheetMusic(sheetId);
        }
    }

    public ApSheetMusic addSheetMusic(final @NonNull ApSheetMusic music, final long sheetId) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            roomDatabase.runInTransaction(new Runnable() {
                @Override
                public void run() {
                    if (insertOrUpdateSheetMusic(music, sheetId)) {
                        updateMusicSheetCount(music.getSheetId());
                        if (mFavouriteSheet != null && mFavouriteSheet.getId() == sheetId) {
                            apSheetMusicDao.updateMusicFavourite(music.getSongId(), true);
                        }
                    }
                }
            });
            return apSheetMusicDao.querySheetMusic(sheetId, music.getSongId());
        }
    }

    public List<ApSheetMusic> addSheetMusicList(final @NonNull List<ApSheetMusic> list, final long sheetId) {
        final List<ApSheetMusic> retList = new ArrayList<>();
        if (list != null && list.size() > 0) {
            synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
                roomDatabase.runInTransaction(new Runnable() {
                    @Override
                    public void run() {
                        for (ApSheetMusic music : list) {
                            insertOrUpdateSheetMusic(music, sheetId);
                            retList.add(apSheetMusicDao.querySheetMusic(sheetId, music.getSongId()));
                            if (mFavouriteSheet != null && mFavouriteSheet.getId() == sheetId) {
                                apSheetMusicDao.updateMusicFavourite(music.getSongId(), true);
                            }
                        }
                        updateMusicSheetCount(sheetId);
                    }
                });
            }
        }
        return retList;
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
            music.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            apSheetMusicDao.update(music);
            return false;
        }
    }

    public void updateMusicLyric(@NonNull ApSheetMusic music, String lrcFilePath, String charset) {
        apSheetMusicDao.updateMusicLyric(music.getSongId(), lrcFilePath, charset, Calendar.getInstance().getTimeInMillis());
    }

    public void deleteSheetMusic(final @NonNull ApSheetMusic music) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            roomDatabase.runInTransaction(new Runnable() {
                @Override
                public void run() {
                    apSheetMusicDao.delete(music);
                    updateMusicSheetCount(music.getSheetId());
                    if (mFavouriteSheet != null && mFavouriteSheet.getId() == music.getSheetId()) {
                        apSheetMusicDao.updateMusicFavourite(music.getSongId(), false);
                    }
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
                    if (mFavouriteSheet != null && mFavouriteSheet.getId() == sheetId) {
                        apSheetMusicDao.updateMusicFavourite(songId, false);
                    }
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
                    if (mFavouriteSheet != null && mFavouriteSheet.getId() == sheetId) {
                        for (ApSheetMusic music : list) {
                            apSheetMusicDao.updateMusicFavourite(music.getSongId(), false);
                        }
                    }
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
                if (mFavouriteSheet != null && mFavouriteSheet.getId() == sheetId) {
                    apSheetMusicDao.updateAllMusicFavourite(false);
                }
            }
        });
    }

    private void updateMusicSheetCount(long sheetId) {
        int count = apSheetMusicDao.querySheetMusicCount(sheetId);
        apMusicSheetDao.updateSheetCount(sheetId, count);
    }
}
