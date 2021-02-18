package com.pine.media.audioplayer.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "ap_sheet_music")
public class ApSheetMusic implements Serializable {
    // id
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "_id")
    private long id;

    @NonNull
    @ColumnInfo(name = "sheet_id")
    private long sheetId;

    @NonNull
    @ColumnInfo(name = "song_id")
    private long songId;

    @NonNull
    @ColumnInfo(name = "update_time_stamp")
    private long updateTimeStamp;

    @NonNull
    @ColumnInfo(name = "create_time_stamp")
    private long createTimeStamp;

    public ApSheetMusic() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSheetId() {
        return sheetId;
    }

    public void setSheetId(long sheetId) {
        this.sheetId = sheetId;
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public long getUpdateTimeStamp() {
        return updateTimeStamp;
    }

    public void setUpdateTimeStamp(long updateTimeStamp) {
        this.updateTimeStamp = updateTimeStamp;
    }

    public long getCreateTimeStamp() {
        return createTimeStamp;
    }

    public void setCreateTimeStamp(long createTimeStamp) {
        this.createTimeStamp = createTimeStamp;
    }

    @Override
    public String toString() {
        return "ApMusic{" +
                "id=" + id +
                ", sheetId=" + sheetId +
                ", songId=" + songId +
                ", updateTimeStamp=" + updateTimeStamp +
                ", createTimeStamp=" + createTimeStamp +
                '}';
    }
}
