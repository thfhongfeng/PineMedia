package com.pine.audioplayer.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.pine.audioplayer.db.entity.ApSheetMusic;

import java.util.List;

@Dao
public interface ApSheetMusicDao {
    @Insert
    long insert(ApSheetMusic apSheetMusic);

    @Insert
    void insert(List<ApSheetMusic> list);

    @Update
    int update(ApSheetMusic apSheetMusic);

    @Query("SELECT * FROM ap_sheet_music WHERE sheet_id=:sheetId AND song_id=:songId AND file_path=:filePath")
    ApSheetMusic checkSheetMusic(long sheetId, long songId, String filePath);

    @Query("SELECT * FROM ap_sheet_music WHERE sheet_id=:sheetId")
    List<ApSheetMusic> querySheetMusic(long sheetId);

    @Query("SELECT COUNT(*) FROM ap_sheet_music WHERE sheet_id=:sheetId")
    int querySheetMusicCount(long sheetId);

    @Query("DELETE FROM ap_sheet_music WHERE sheet_id=:sheetId AND song_id=:songId")
    int deleteBySheetIdSongId(long sheetId, long songId);

    @Query("DELETE FROM ap_sheet_music WHERE sheet_id=:sheetId")
    int deleteBySheetId(long sheetId);

    @Delete
    int delete(ApSheetMusic apSheetMusic);

    @Delete
    int delete(List<ApSheetMusic> list);
}
