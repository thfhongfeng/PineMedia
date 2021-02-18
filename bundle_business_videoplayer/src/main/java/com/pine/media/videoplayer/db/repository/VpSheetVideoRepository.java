package com.pine.media.videoplayer.db.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pine.tool.util.LogUtils;
import com.pine.media.videoplayer.db.VpRoomDatabase;
import com.pine.media.videoplayer.db.dao.VpSheetDao;
import com.pine.media.videoplayer.db.dao.VpSheetVideoDao;
import com.pine.media.videoplayer.db.entity.VpSheetVideo;
import com.pine.media.videoplayer.db.entity.VpVideo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class VpSheetVideoRepository {
    private final String TAG = LogUtils.makeLogTag(this.getClass());

    private static volatile VpSheetVideoRepository mInstance = null;

    private VpSheetVideoDao vpSheetVideoDao;
    private VpSheetDao vpSheetDao;

    public static VpSheetVideoRepository getInstance(Context application) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            if (mInstance == null) {
                mInstance = new VpSheetVideoRepository(application);
            }
            return mInstance;
        }
    }

    private VpRoomDatabase roomDatabase;

    private VpSheetVideoRepository(Context application) {
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

    public int getSheetVideoListCount(long sheetId) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            return vpSheetVideoDao.querySheetVideoCount(sheetId);
        }
    }

    public VpVideo querySheetVideo(long sheetId, long videoId) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            return vpSheetVideoDao.querySheetVideo(sheetId, videoId);
        }
    }

    public List<VpVideo> querySheetVideoList(long sheetId) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            return vpSheetVideoDao.querySheetVideo(sheetId);
        }
    }

    public VpVideo addSheetVideo(final @NonNull VpVideo video, final long sheetId) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            roomDatabase.runInTransaction(new Runnable() {
                @Override
                public void run() {
                    if (insertOrUpdateSheetVideo(video, sheetId)) {
                        updateVideoSheetCount(sheetId);
                    }
                }
            });
            return vpSheetVideoDao.querySheetVideo(sheetId, video.getVideoId());
        }
    }

    public List<VpVideo> addSheetVideoList(final @NonNull List<VpVideo> list, final long sheetId) {
        final List<VpVideo> retList = new ArrayList<>();
        if (list != null && list.size() > 0) {
            synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
                roomDatabase.runInTransaction(new Runnable() {
                    @Override
                    public void run() {
                        for (VpVideo video : list) {
                            insertOrUpdateSheetVideo(video, sheetId);
                            retList.add(vpSheetVideoDao.querySheetVideo(sheetId, video.getVideoId()));
                        }
                        updateVideoSheetCount(sheetId);
                    }
                });
            }
        }
        return retList;
    }

    private boolean insertOrUpdateSheetVideo(@NonNull VpVideo video, final long sheetId) {
        VpSheetVideo sheetVideo = vpSheetVideoDao.checkSheetVideo(sheetId, video.getVideoId());
        if (sheetVideo == null) {
            sheetVideo = new VpSheetVideo();
            sheetVideo.setSheetId(sheetId);
            sheetVideo.setVideoId(video.getVideoId());
            sheetVideo.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            sheetVideo.setCreateTimeStamp(Calendar.getInstance().getTimeInMillis());
            vpSheetVideoDao.insert(sheetVideo);
            return true;
        } else {
            sheetVideo.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            vpSheetVideoDao.update(sheetVideo);
            return false;
        }
    }

    public void deleteSheetVideo(final @NonNull VpVideo video, final long sheetId) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            roomDatabase.runInTransaction(new Runnable() {
                @Override
                public void run() {
                    vpSheetVideoDao.deleteBySheetIdVideoId(sheetId, video.getVideoId());
                    updateVideoSheetCount(sheetId);
                }
            });
        }
    }

    public void deleteSheetVideo(final @NonNull long sheetId, final long videoId) {
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            roomDatabase.runInTransaction(new Runnable() {
                @Override
                public void run() {
                    vpSheetVideoDao.deleteBySheetIdVideoId(sheetId, videoId);
                    updateVideoSheetCount(sheetId);
                }
            });
        }
    }

    public void deleteSheetVideoList(@NonNull final List<VpVideo> list, final long sheetId) {
        if (list.size() < 1) {
            return;
        }
        synchronized (VpRoomDatabase.DB_SYNC_LOCK) {
            roomDatabase.runInTransaction(new Runnable() {
                @Override
                public void run() {
                    List<Long> videoIdList = new ArrayList<>();
                    for (VpVideo video : list) {
                        videoIdList.add(video.getVideoId());
                    }
                    vpSheetVideoDao.deleteBySheetIdVideoIdList(sheetId, videoIdList);
                    updateVideoSheetCount(sheetId);
                }
            });
        }
    }

    public void deleteSheetAllVideos(final @NonNull long sheetId) {
        roomDatabase.runInTransaction(new Runnable() {
            @Override
            public void run() {
                vpSheetVideoDao.deleteBySheetId(sheetId);
                updateVideoSheetCount(sheetId);
            }
        });
    }

    private void updateVideoSheetCount(long sheetId) {
        int count = vpSheetVideoDao.querySheetVideoCount(sheetId);
        vpSheetDao.updateSheetCount(sheetId, count);
    }
}
