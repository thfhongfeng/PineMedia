package com.pine.videoplayer.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.pine.videoplayer.db.entity.VpVideo;

import java.util.List;

@Dao
public interface VpVideoDao {
    @Insert
    long insert(VpVideo video);

    @Insert
    void insert(List<VpVideo> list);

    @Update
    int update(VpVideo video);

    @Query("SELECT * FROM vp_video")
    List<VpVideo> getAllVideoList();

    @Query("SELECT * FROM vp_video WHERE is_favourite")
    List<VpVideo> getFavouriteVideoList();

    @Query("SELECT * FROM vp_video WHERE video_id=:videoId")
    VpVideo checkVideo(long videoId);

    @Query("UPDATE vp_video SET lyric_file_path=:lrcFilePath,lyric_charset=:charset,update_time_stamp=:updateTimeStamp WHERE video_id=:videoId")
    int updateVideoLyric(long videoId, String lrcFilePath, String charset, long updateTimeStamp);

    @Query("UPDATE vp_video SET is_favourite=:isFavourite WHERE video_id=:videoId")
    int updateVideoFavourite(long videoId, boolean isFavourite);

    @Query("UPDATE vp_video SET is_favourite=:isFavourite WHERE video_id IN (:videoIdList)")
    int updateVideoListFavourite(List<Long> videoIdList, boolean isFavourite);

    @Query("SELECT COUNT(*) FROM vp_video")
    int getAllVideoCount();

    @Query("SELECT COUNT(*) FROM vp_video WHERE is_favourite")
    int getFavouriteVideoCount();

    @Delete
    int delete(VpVideo video);

    @Delete
    int delete(List<VpVideo> list);
}
