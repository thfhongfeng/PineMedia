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

    @Query("SELECT * FROM ap_music_sheet WHERE sheet_type IN (:sheetTypes) ORDER BY update_time_stamp DESC")
    List<ApMusicSheet> querySheetListByTypes(List<Integer> sheetTypes);

    @Query("SELECT *FROM ap_music_sheet WHERE sheet_type=:sheetType ORDER BY update_time_stamp DESC")
    List<ApMusicSheet> querySheetListByType(int sheetType);

    @Query("SELECT *FROM ap_music_sheet WHERE sheet_type=:sheetType AND _id NOT IN (:excludeIds) ORDER BY update_time_stamp DESC")
    List<ApMusicSheet> querySheetListByType(int sheetType, long... excludeIds);

    @Query("SELECT *FROM ap_music_sheet WHERE _id=:sheetId")
    ApMusicSheet querySheetById(long sheetId);

    @Query("SELECT *FROM ap_music_sheet WHERE sheet_type=:sheetType")
    ApMusicSheet querySheetByType(int sheetType);

    @Query("UPDATE ap_music_sheet SET count=:count WHERE _id=:sheetId")
    int updateSheetCount(long sheetId, int count);

    @Delete
    int delete(ApMusicSheet apMusicSheet);

    @Delete
    int delete(List<ApMusicSheet> list);
}
