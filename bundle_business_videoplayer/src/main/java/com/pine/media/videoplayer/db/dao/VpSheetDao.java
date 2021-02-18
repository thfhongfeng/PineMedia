package com.pine.media.videoplayer.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.pine.media.videoplayer.db.entity.VpSheet;

import java.util.List;

@Dao
public interface VpSheetDao {
    @Insert
    long insert(VpSheet vpSheet);

    @Insert
    void insert(List<VpSheet> list);

    @Update
    int update(VpSheet vpSheet);

    @Query("SELECT * FROM vp_sheet WHERE sheet_type IN (:sheetTypes) ORDER BY update_time_stamp DESC")
    List<VpSheet> querySheetListByTypes(List<Integer> sheetTypes);

    @Query("SELECT *FROM vp_sheet WHERE sheet_type=:sheetType ORDER BY update_time_stamp DESC")
    List<VpSheet> querySheetListByType(int sheetType);

    @Query("SELECT *FROM vp_sheet WHERE sheet_type=:sheetType AND _id NOT IN (:excludeIds) ORDER BY update_time_stamp DESC")
    List<VpSheet> querySheetListByType(int sheetType, long... excludeIds);

    @Query("SELECT *FROM vp_sheet WHERE _id=:sheetId")
    VpSheet querySheetById(long sheetId);

    @Query("SELECT *FROM vp_sheet WHERE _id=:sheetId")
    LiveData<VpSheet> syncSheetById(long sheetId);

    @Query("SELECT *FROM vp_sheet WHERE sheet_type=:sheetType")
    VpSheet querySheetByType(int sheetType);

    @Query("SELECT *FROM vp_sheet WHERE sheet_type=:sheetType")
    LiveData<VpSheet> syncSheetByType(int sheetType);

    @Query("UPDATE vp_sheet SET count=:count WHERE _id=:sheetId")
    int updateSheetCount(long sheetId, int count);

    @Delete
    int delete(VpSheet vpSheet);

    @Delete
    int delete(List<VpSheet> list);
}
