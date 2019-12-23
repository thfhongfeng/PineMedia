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

    @ColumnInfo(name = "music_img_uri")
    private String musicImgUri;

    @NonNull
    private String name;

    @NonNull
    @ColumnInfo(name = "file_path")
    private String filePath;

    private String author;

    private int duration;

    private String album;

    @ColumnInfo(name = "album_id")
    private long albumId;

    private String year;

    @ColumnInfo(name = "mime_type")
    private String mimeType;

    private long size;

    private String description;

    public ApSheetMusic() {

    }

    protected ApSheetMusic(Parcel in) {
        id = in.readLong();
        sheetId = in.readLong();
        songId = in.readLong();
        musicImgUri = in.readString();
        name = in.readString();
        filePath = in.readString();
        author = in.readString();
        duration = in.readInt();
        album = in.readString();
        albumId = in.readLong();
        year = in.readString();
        mimeType = in.readString();
        size = in.readLong();
        description = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(sheetId);
        dest.writeLong(songId);
        dest.writeString(musicImgUri);
        dest.writeString(name);
        dest.writeString(filePath);
        dest.writeString(author);
        dest.writeInt(duration);
        dest.writeString(album);
        dest.writeLong(albumId);
        dest.writeString(year);
        dest.writeString(mimeType);
        dest.writeLong(size);
        dest.writeString(description);
    }

    @Override
    public int describeContents() {
        return 0;
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

    public String getMusicImgUri() {
        return musicImgUri;
    }

    public void setMusicImgUri(String musicImgUri) {
        this.musicImgUri = musicImgUri;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(@NonNull String filePath) {
        this.filePath = filePath;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
