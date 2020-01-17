package com.pine.videoplayer.db.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pine.tool.util.LogUtils;
import com.pine.videoplayer.db.VpRoomDatabase;
import com.pine.videoplayer.db.dao.VpSheetDao;
import com.pine.videoplayer.db.dao.VpVideoDao;
import com.pine.videoplayer.db.entity.VpVideo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class VpVideoRepository {
    private final String TAG = LogUtils.makeLogTag(this.getClass());

    private static volatile VpVideoRepository mInstance = null;
    private VpSheetDao vpSheetDao;
    private VpVideoDao vpVideoDao;

    public static VpVideoRepository getInstance(Context application) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            if (mInstance == null) {
                mInstance = new VpVideoRepository(application);
            }
            return mInstance;
        }
    }

    private VpRoomDatabase roomDatabase;

    private VpVideoRepository(Context application) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            LogUtils.d(TAG, "new");
            roomDatabase = VpRoomDatabase.getINSTANCE(application);
            vpSheetDao = roomDatabase.vpSheetDao();
            vpVideoDao = roomDatabase.vpVideoDao();
        }
    }

    public static void reset() {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            mInstance = null;
        }
    }

    public int getAllVideoCount() {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            return vpVideoDao.getAllVideoCount();
        }
    }

    public List<VpVideo> getAllVideoList() {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            return vpVideoDao.getAllVideoList();
        }
    }

    public void addVideoList(@NonNull List<VpVideo> list) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            for (VpVideo video : list) {
                insertOrUpdateVideo(video);
            }
        }
    }

    public List<VpVideo> getFavouriteVideoList() {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            return vpVideoDao.getFavouriteVideoList();
        }
    }

    public int getFavouriteVideoListCount() {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            return vpVideoDao.getFavouriteVideoCount();
        }
    }

    public void updateVideoFavourite(final long videoId, final boolean isFavourite) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            vpVideoDao.updateVideoFavourite(videoId, isFavourite);
        }
    }

    public void updateVideoListFavourite(@NonNull List<VpVideo> videoList, boolean isFavourite) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            List<Long> idList = new ArrayList<>();
            for (VpVideo video : videoList) {
                idList.add(video.getVideoId());
            }
            vpVideoDao.updateVideoListFavourite(idList, isFavourite);
        }
    }

    public void updateVideoLyric(long videoId, String lyricFilePath, String charset) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            vpVideoDao.updateVideoLyric(videoId, lyricFilePath, charset, Calendar.getInstance().getTimeInMillis());
        }
    }

    private void insertOrUpdateVideo(@NonNull VpVideo video) {
        VpVideo dbVideo = vpVideoDao.checkVideo(video.getVideoId());
        if (dbVideo == null) {
            video.setId(0);
            video.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            video.setCreateTimeStamp(Calendar.getInstance().getTimeInMillis());
            vpVideoDao.insert(video);
        } else if (dbVideo.mediaInfoChange(video)) {
            dbVideo.setFilePath(video.getFilePath());
            dbVideo.setLyricFilePath(video.getLyricFilePath());
            dbVideo.setLyricCharset(video.getLyricCharset());
            dbVideo.setMimeType(video.getMimeType());
            dbVideo.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            vpVideoDao.update(dbVideo);
        }
    }
}
