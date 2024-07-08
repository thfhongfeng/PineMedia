package com.pine.media.audio.db.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pine.media.audio.db.ApRoomDatabase;
import com.pine.media.audio.db.dao.ApMusicDao;
import com.pine.media.audio.db.dao.ApSheetDao;
import com.pine.media.audio.db.entity.ApMusic;
import com.pine.tool.util.LogUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ApMusicRepository {
    private final String TAG = LogUtils.makeLogTag(this.getClass());

    private static volatile ApMusicRepository mInstance = null;
    private ApSheetDao apSheetDao;
    private ApMusicDao apMusicDao;

    public static ApMusicRepository getInstance(Context application) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            if (mInstance == null) {
                mInstance = new ApMusicRepository(application);
            }
            return mInstance;
        }
    }

    private ApRoomDatabase roomDatabase;

    private ApMusicRepository(Context application) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            LogUtils.d(TAG, "new");
            roomDatabase = ApRoomDatabase.getINSTANCE(application);
            apSheetDao = roomDatabase.apSheetDao();
            apMusicDao = roomDatabase.apMusicDao();
        }
    }

    public static void reset() {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            mInstance = null;
        }
    }

    public int getAllMusicCount() {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apMusicDao.getAllMusicCount();
        }
    }

    public List<ApMusic> getAllMusicList() {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apMusicDao.getAllMusicList();
        }
    }

    public void addMusicList(@NonNull List<ApMusic> list) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            for (ApMusic music : list) {
                insertOrUpdateMusic(music);
            }
        }
    }

    public List<ApMusic> getFavouriteMusicList() {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apMusicDao.getFavouriteMusicList();
        }
    }

    public int getFavouriteMusicListCount() {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            return apMusicDao.getFavouriteMusicCount();
        }
    }

    public void updateMusicFavourite(final long songId, final boolean isFavourite) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            apMusicDao.updateMusicFavourite(songId, isFavourite);
        }
    }

    public void updateMusicListFavourite(@NonNull List<ApMusic> musicList, boolean isFavourite) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            List<Long> idList = new ArrayList<>();
            for (ApMusic music : musicList) {
                idList.add(music.getSongId());
            }
            apMusicDao.updateMusicListFavourite(idList, isFavourite);
        }
    }

    public void updateMusicLyric(long songId, String lyricFilePath, String charset) {
        synchronized (ApRoomDatabase.DB_SYNC_LOCK) {
            apMusicDao.updateMusicLyric(songId, lyricFilePath, charset, Calendar.getInstance().getTimeInMillis());
        }
    }

    private void insertOrUpdateMusic(@NonNull ApMusic music) {
        ApMusic dbMusic = apMusicDao.checkMusic(music.getSongId());
        if (dbMusic == null) {
            music.setId(0);
            music.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            music.setCreateTimeStamp(Calendar.getInstance().getTimeInMillis());
            apMusicDao.insert(music);
        } else if (dbMusic.mediaInfoChange(music)) {
            dbMusic.setFilePath(music.getFilePath());
            dbMusic.setLyricFilePath(music.getLyricFilePath());
            dbMusic.setLyricCharset(music.getLyricCharset());
            dbMusic.setMimeType(music.getMimeType());
            dbMusic.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            apMusicDao.update(dbMusic);
        }
    }
}
