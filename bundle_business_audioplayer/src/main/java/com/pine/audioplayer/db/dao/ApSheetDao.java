package com.pine.audioplayer.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.pine.audioplayer.db.entity.ApSheet;

import java.util.List;

@Dao
public interface ApSheetDao {
    @Insert
    long insert(ApSheet apSheet);

    @Insert
    void insert(List<ApSheet> list);

    @Update
    int update(ApSheet apSheet);

    @Query("SELECT * FROM ap_sheet WHERE sheet_type IN (:sheetTypes) ORDER BY update_time_stamp DESC")
    List<ApSheet> querySheetListByTypes(List<Integer> sheetTypes);

    @Query("SELECT *FROM ap_sheet WHERE sheet_type=:sheetType ORDER BY update_time_stamp DESC")
    List<ApSheet> querySheetListByType(int sheetType);

    @Query("SELECT *FROM ap_sheet WHERE sheet_type=:sheetType AND _id NOT IN (:excludeIds) ORDER BY update_time_stamp DESC")
    List<ApSheet> querySheetListByType(int sheetType, long... excludeIds);

    @Query("SELECT *FROM ap_sheet WHERE _id=:sheetId")
    ApSheet querySheetById(long sheetId);

    @Query("SELECT *FROM ap_sheet WHERE _id=:sheetId")
    LiveData<ApSheet> syncSheetById(long sheetId);

    @Query("SELECT *FROM ap_sheet WHERE sheet_type=:sheetType")
    ApSheet querySheetByType(int sheetType);

    @Query("SELECT *FROM ap_sheet WHERE sheet_type=:sheetType")
    LiveData<ApSheet> syncSheetByType(int sheetType);

    @Query("UPDATE ap_sheet SET count=:count WHERE _id=:sheetId")
    int updateSheetCount(long sheetId, int count);

    @Delete
    int delete(ApSheet apSheet);

    @Delete
    int delete(List<ApSheet> list);
}
