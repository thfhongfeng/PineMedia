package com.pine.media.video.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.pine.media.video.db.entity.VpSheetVideo;
import com.pine.media.video.db.entity.VpVideo;

import java.util.List;

@Dao
public interface VpSheetVideoDao {
    @Insert
    long insert(VpSheetVideo sheetVideo);

    @Insert
    void insert(List<VpSheetVideo> list);

    @Update
    int update(VpSheetVideo sheetVideo);

    @Query("SELECT * FROM vp_sheet_video WHERE sheet_id=:sheetId AND video_id=:videoId")
    VpSheetVideo checkSheetVideo(long sheetId, long videoId);

    @Query("SELECT m.* FROM vp_sheet_video AS sm LEFT JOIN vp_video AS m ON sm.video_id=m.video_id WHERE sm.sheet_id=:sheetId AND sm.video_id=:videoId AND m.file_path IS NOT NULL")
    VpVideo querySheetVideo(long sheetId, long videoId);

    @Query("SELECT m.* FROM vp_sheet_video AS sm LEFT JOIN vp_video AS m ON sm.video_id=m.video_id WHERE sm.sheet_id=:sheetId AND m.file_path IS NOT NULL ORDER BY sm.update_time_stamp DESC")
    List<VpVideo> querySheetVideo(long sheetId);

    @Query("SELECT COUNT(*) FROM vp_sheet_video WHERE sheet_id=:sheetId")
    int querySheetVideoCount(long sheetId);

    @Query("DELETE FROM vp_sheet_video WHERE sheet_id=:sheetId AND video_id=:videoId")
    int deleteBySheetIdVideoId(long sheetId, long videoId);

    @Query("DELETE FROM vp_sheet_video WHERE sheet_id=:sheetId AND video_id IN (:videoIdList)")
    int deleteBySheetIdVideoIdList(long sheetId, List<Long> videoIdList);

    @Query("DELETE FROM vp_sheet_video WHERE sheet_id=:sheetId")
    int deleteBySheetId(long sheetId);

    @Delete
    int delete(VpSheetVideo sheetVideoDetail);

    @Delete
    int delete(List<VpSheetVideo> list);
}
