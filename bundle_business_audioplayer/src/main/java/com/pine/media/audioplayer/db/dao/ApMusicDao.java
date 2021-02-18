package com.pine.media.audioplayer.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.pine.media.audioplayer.db.entity.ApMusic;

import java.util.List;

@Dao
public interface ApMusicDao {
    @Insert
    long insert(ApMusic music);

    @Insert
    void insert(List<ApMusic> list);

    @Update
    int update(ApMusic music);

    @Query("SELECT * FROM ap_music")
    List<ApMusic> getAllMusicList();

    @Query("SELECT * FROM ap_music WHERE is_favourite")
    List<ApMusic> getFavouriteMusicList();

    @Query("SELECT * FROM ap_music WHERE song_id=:songId")
    ApMusic checkMusic(long songId);

    @Query("UPDATE ap_music SET lyric_file_path=:lrcFilePath,lyric_charset=:charset,update_time_stamp=:updateTimeStamp WHERE song_id=:songId")
    int updateMusicLyric(long songId, String lrcFilePath, String charset, long updateTimeStamp);

    @Query("UPDATE ap_music SET is_favourite=:isFavourite WHERE song_id=:songId")
    int updateMusicFavourite(long songId, boolean isFavourite);

    @Query("UPDATE ap_music SET is_favourite=:isFavourite WHERE song_id IN (:songIdList)")
    int updateMusicListFavourite(List<Long> songIdList, boolean isFavourite);

    @Query("SELECT COUNT(*) FROM ap_music")
    int getAllMusicCount();

    @Query("SELECT COUNT(*) FROM ap_music WHERE is_favourite")
    int getFavouriteMusicCount();

    @Delete
    int delete(ApMusic music);

    @Delete
    int delete(List<ApMusic> list);
}
