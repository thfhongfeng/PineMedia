package com.pine.videoplayer.db.entity;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "vp_video")
public class VpVideo implements Serializable {
    // id
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "_id")
    private long id;

    @NonNull
    @ColumnInfo(name = "video_id")
    private long videoId;

    @NonNull
    private String name;

    @NonNull
    @ColumnInfo(name = "file_path")
    private String filePath;

    @ColumnInfo(name = "image_url")
    private String imageUrl;

    @ColumnInfo(name = "lyric_file_path")
    private String lyricFilePath;

    @ColumnInfo(name = "lyric_charset", defaultValue = "GBK")
    private String lyricCharset;

    @ColumnInfo(name = "is_favourite")
    private boolean isFavourite;

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

    @NonNull
    @ColumnInfo(name = "update_time_stamp")
    private long updateTimeStamp;

    @NonNull
    @ColumnInfo(name = "create_time_stamp")
    private long createTimeStamp;

    public VpVideo() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVideoId() {
        return videoId;
    }

    public void setVideoId(long videoId) {
        this.videoId = videoId;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLyricFilePath() {
        return lyricFilePath;
    }

    public void setLyricFilePath(String lyricFilePath) {
        this.lyricFilePath = lyricFilePath;
    }

    public String getLyricCharset() {
        if (TextUtils.isEmpty(lyricCharset)) {
            return "GBK";
        }
        return lyricCharset;
    }

    public void setLyricCharset(String lyricCharset) {
        this.lyricCharset = lyricCharset;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
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

    public boolean mediaInfoChange(VpVideo video) {
        return !TextUtils.equals(this.filePath, video.getFilePath()) ||
                !TextUtils.equals(this.lyricFilePath, video.getLyricFilePath()) ||
                !TextUtils.equals(this.lyricCharset, video.getLyricCharset()) ||
                !TextUtils.equals(this.mimeType, video.getMimeType());
    }

    public void copyDataFrom(VpVideo video) {
        this.id = video.id;
        this.videoId = video.videoId;
        this.name = video.name;
        this.filePath = video.filePath;
        this.lyricFilePath = video.lyricFilePath;
        this.lyricCharset = video.lyricCharset;
        this.isFavourite = video.isFavourite;
        this.author = video.author;
        this.duration = video.duration;
        this.album = video.album;
        this.albumId = video.albumId;
        this.year = video.year;
        this.mimeType = video.mimeType;
        this.size = video.size;
        this.description = video.description;
        this.updateTimeStamp = video.updateTimeStamp;
        this.createTimeStamp = video.createTimeStamp;
    }

    @Override
    public String toString() {
        return "VpVideo{" +
                "id=" + id +
                ", videoId=" + videoId +
                ", name='" + name + '\'' +
                ", filePath='" + filePath + '\'' +
                ", lyricFilePath='" + lyricFilePath + '\'' +
                ", lyricCharset='" + lyricCharset + '\'' +
                ", isFavourite=" + isFavourite +
                ", author='" + author + '\'' +
                ", duration=" + duration +
                ", album='" + album + '\'' +
                ", albumId=" + albumId +
                ", year='" + year + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", size=" + size +
                ", description='" + description + '\'' +
                ", updateTimeStamp=" + updateTimeStamp +
                ", createTimeStamp=" + createTimeStamp +
                '}';
    }
}
