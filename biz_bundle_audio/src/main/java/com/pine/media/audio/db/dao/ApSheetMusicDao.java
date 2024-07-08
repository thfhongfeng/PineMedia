package com.pine.media.audio.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.pine.media.audio.db.entity.ApMusic;
import com.pine.media.audio.db.entity.ApSheetMusic;

import java.util.List;

@Dao
public interface ApSheetMusicDao {
    @Insert
    long insert(ApSheetMusic sheetMusic);

    @Insert
    void insert(List<ApSheetMusic> list);

    @Update
    int update(ApSheetMusic sheetMusic);

    @Query("SELECT * FROM ap_sheet_music WHERE sheet_id=:sheetId AND song_id=:songId")
    ApSheetMusic checkSheetMusic(long sheetId, long songId);

    @Query("SELECT m.* FROM ap_sheet_music AS sm LEFT JOIN ap_music AS m ON sm.song_id=m.song_id WHERE sm.sheet_id=:sheetId AND sm.song_id=:songId AND m.file_path IS NOT NULL")
    ApMusic querySheetMusic(long sheetId, long songId);

    @Query("SELECT m.* FROM ap_sheet_music AS sm LEFT JOIN ap_music AS m ON sm.song_id=m.song_id WHERE sm.sheet_id=:sheetId AND m.file_path IS NOT NULL ORDER BY sm.update_time_stamp DESC")
    List<ApMusic> querySheetMusic(long sheetId);

    @Query("SELECT COUNT(*) FROM ap_sheet_music WHERE sheet_id=:sheetId")
    int querySheetMusicCount(long sheetId);

    @Query("DELETE FROM ap_sheet_music WHERE sheet_id=:sheetId AND song_id=:songId")
    int deleteBySheetIdSongId(long sheetId, long songId);

    @Query("DELETE FROM ap_sheet_music WHERE sheet_id=:sheetId AND song_id IN (:songIdList)")
    int deleteBySheetIdSongIdList(long sheetId, List<Long> songIdList);

    @Query("DELETE FROM ap_sheet_music WHERE sheet_id=:sheetId")
    int deleteBySheetId(long sheetId);

    @Delete
    int delete(ApSheetMusic sheetMusicDetail);

    @Delete
    int delete(List<ApSheetMusic> list);
}
