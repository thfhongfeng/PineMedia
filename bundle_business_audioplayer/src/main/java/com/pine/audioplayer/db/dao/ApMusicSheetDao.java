package com.pine.audioplayer.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.pine.audioplayer.db.entity.ApMusicSheet;

import java.util.List;

@Dao
public interface ApMusicSheetDao {
    @Insert
    long insert(ApMusicSheet apMusicSheet);

    @Insert
    void insert(List<ApMusicSheet> list);

    @Update
    int update(ApMusicSheet apMusicSheet);

    @Query("SELECT * FROM ap_music_sheet WHERE sheet_id NOT IN (:excludeSheetIds)")
    List<ApMusicSheet> querySheetListExcludeIds(List<Integer> excludeSheetIds);

    @Query("SELECT *FROM ap_music_sheet WHERE sheet_id=:sheetId")
    ApMusicSheet querySheetBySheetId(int sheetId);

    @Query("SELECT MAX(sheet_id) FROM ap_music_sheet")
    int queryMaxSheetId();

    @Delete
    int delete(ApMusicSheet apMusicSheet);

    @Delete
    int delete(List<ApMusicSheet> list);
}
