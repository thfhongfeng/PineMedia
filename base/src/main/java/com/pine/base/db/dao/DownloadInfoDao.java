package com.pine.base.db.dao;

import com.pine.base.db.entity.DownloadInfo;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;

@Dao
public interface DownloadInfoDao {
    @Insert
    long insert(DownloadInfo downloadInfo);

    @Update
    int update(DownloadInfo downloadInfo);
}
