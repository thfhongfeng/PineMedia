package com.pine.audioplayer.model;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.pine.audioplayer.ApConstants;
import com.pine.audioplayer.db.entity.ApMusic;
import com.pine.audioplayer.db.entity.ApSheet;
import com.pine.audioplayer.db.repository.ApMusicRepository;
import com.pine.audioplayer.db.repository.ApSheetMusicRepository;
import com.pine.audioplayer.db.repository.ApSheetRepository;
import com.pine.audioplayer.util.ApLocalMusicUtils;

import java.util.List;

public class ApMusicModel {
    public ApSheet getRecentSheet(Context context) {
        return ApSheetRepository.getInstance(context).querySheetByType(ApConstants.MUSIC_SHEET_TYPE_RECENT);
    }

    public LiveData<ApSheet> syncRecentSheet(Context context) {
        return ApSheetRepository.getInstance(context).syncSheetByType(ApConstants.MUSIC_SHEET_TYPE_RECENT);
    }

    public ApSheet getPlayListSheet(Context context) {
        return ApSheetRepository.getInstance(context).querySheetByType(ApConstants.MUSIC_SHEET_TYPE_PLAY_LIST);
    }

    public ApSheet getCustomSheet(Context context, long sheetId) {
        return ApSheetRepository.getInstance(context).querySheetById(sheetId);
    }

    public List<ApSheet> getCustomMusicSheetList(Context context) {
        return ApSheetRepository.getInstance(context).querySheetListByType(ApConstants.MUSIC_SHEET_TYPE_CUSTOM);
    }

    public List<ApSheet> getCustomMusicSheetList(Context context, long... excludeSheetIds) {
        return ApSheetRepository.getInstance(context).querySheetListByType(ApConstants.MUSIC_SHEET_TYPE_CUSTOM, excludeSheetIds);
    }

    public long addMusicSheet(Context context, ApSheet apSheet) {
        apSheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_CUSTOM);
        return ApSheetRepository.getInstance(context).addMusicSheet(apSheet);
    }

    public void removeMusicSheet(Context context, ApSheet apSheet) {
        ApSheetRepository.getInstance(context).deleteMusicSheet(apSheet);
    }

    public int getAllMusicListCount(Context context) {
        return getAllMusicList(context).size();
    }

    public List<ApMusic> getAllMusicList(Context context) {
        List<ApMusic> list = ApLocalMusicUtils.getAllMusicList(context);
        return list;
    }

    public List<ApMusic> getFavouriteMusicList(Context context) {
        return ApMusicRepository.getInstance(context).getFavouriteMusicList();
    }

    public int getFavouriteMusicListCount(Context context) {
        return ApMusicRepository.getInstance(context).getFavouriteMusicListCount();
    }

    public void updateMusicFavourite(Context context, ApMusic music, boolean isFavourite) {
        ApMusicRepository.getInstance(context).updateMusicFavourite(music.getSongId(), isFavourite);
    }

    public void updateMusicListFavourite(Context context, List<ApMusic> musicList, boolean isFavourite) {
        ApMusicRepository.getInstance(context).updateMusicListFavourite(musicList, isFavourite);
    }

    public void updateMusicLyric(Context context, ApMusic music, String lrcFilePath, String charset) {
        ApMusicRepository.getInstance(context).updateMusicLyric(music.getSongId(), lrcFilePath, charset);
    }

    public ApMusic getSheetMusic(Context context, long sheetId, long songId) {
        return ApSheetMusicRepository.getInstance(context).querySheetMusic(sheetId, songId);
    }

    public int getSheetMusicListCount(Context context, long sheetId) {
        return ApSheetMusicRepository.getInstance(context).getSheetMusicListCount(sheetId);
    }

    public List<ApMusic> getSheetMusicList(Context context, long sheetId) {
        return ApSheetMusicRepository.getInstance(context).querySheetMusicList(sheetId);
    }

    public ApMusic addSheetMusic(Context context, ApMusic apMusic, long sheetId) {
        return ApSheetMusicRepository.getInstance(context).addSheetMusic(apMusic, sheetId);
    }

    public List<ApMusic> addSheetMusicList(Context context, List<ApMusic> list, long sheetId) {
        return ApSheetMusicRepository.getInstance(context).addSheetMusicList(list, sheetId);
    }

    public void removeSheetMusic(Context context, ApMusic music, long sheetId) {
        ApSheetMusicRepository.getInstance(context).deleteSheetMusic(music, sheetId);
    }

    public void removeSheetMusic(Context context, long sheetId, long songId) {
        ApSheetMusicRepository.getInstance(context).deleteSheetMusic(sheetId, songId);
    }

    public void clearSheetMusic(Context context, long sheetId) {
        ApSheetMusicRepository.getInstance(context).deleteSheetAllMusics(sheetId);
    }

    public void removeSheetMusicList(Context context, List<ApMusic> list, long sheetId) {
        ApSheetMusicRepository.getInstance(context).deleteSheetMusicList(list, sheetId);
    }
}
