package com.pine.videoplayer.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "vp_sheet_video")
public class VpSheetVideo implements Serializable {
    // id
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "_id")
    private long id;

    @NonNull
    @ColumnInfo(name = "sheet_id")
    private long sheetId;

    @NonNull
    @ColumnInfo(name = "video_id")
    private long videoId;

    @NonNull
    @ColumnInfo(name = "update_time_stamp")
    private long updateTimeStamp;

    @NonNull
    @ColumnInfo(name = "create_time_stamp")
    private long createTimeStamp;

    public VpSheetVideo() {

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

    public long getVideoId() {
        return videoId;
    }

    public void setVideoId(long videoId) {
        this.videoId = videoId;
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
        return "VpSheetVideo{" +
                "id=" + id +
                ", sheetId=" + sheetId +
                ", videoId=" + videoId +
                ", updateTimeStamp=" + updateTimeStamp +
                ", createTimeStamp=" + createTimeStamp +
                '}';
    }
}
