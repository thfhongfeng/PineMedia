package com.pine.audioplayer.db.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ap_sheet_music")
public class ApSheetMusic implements Parcelable {
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

    protected ApSheetMusic(Parcel in) {
        id = in.readLong();
        sheetId = in.readLong();
        songId = in.readLong();
        updateTimeStamp = in.readLong();
        createTimeStamp = in.readLong();
    }

    public static final Creator<ApSheetMusic> CREATOR = new Creator<ApSheetMusic>() {
        @Override
        public ApSheetMusic createFromParcel(Parcel in) {
            return new ApSheetMusic(in);
        }

        @Override
        public ApSheetMusic[] newArray(int size) {
            return new ApSheetMusic[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(sheetId);
        dest.writeLong(songId);
        dest.writeLong(updateTimeStamp);
        dest.writeLong(createTimeStamp);
    }

    public void copyDataFrom(ApSheetMusic music) {
        this.id = music.id;
        this.sheetId = music.sheetId;
        this.songId = music.songId;
        this.updateTimeStamp = music.updateTimeStamp;
        this.createTimeStamp = music.createTimeStamp;
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
