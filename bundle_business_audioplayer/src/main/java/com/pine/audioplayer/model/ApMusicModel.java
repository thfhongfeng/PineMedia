package com.pine.audioplayer.model;

import android.content.Context;

import com.pine.audioplayer.ApConstants;
import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.db.repository.ApMusicSheetRepository;
import com.pine.audioplayer.db.repository.ApSheetMusicRepository;
import com.pine.audioplayer.util.ApLocalMusicUtils;

import java.util.Calendar;
import java.util.List;

import androidx.lifecycle.LiveData;

public class ApMusicModel {

    public ApMusicSheet getFavouriteSheet(Context context) {
        return ApMusicSheetRepository.getInstance(context).querySheetByType(ApConstants.MUSIC_SHEET_TYPE_FAVOURITE);
    }

    public ApMusicSheet getRecentSheet(Context context) {
        return ApMusicSheetRepository.getInstance(context).querySheetByType(ApConstants.MUSIC_SHEET_TYPE_RECENT);
    }

    public LiveData<ApMusicSheet> syncRecentSheet(Context context) {
        return ApMusicSheetRepository.getInstance(context).syncSheetByType(ApConstants.MUSIC_SHEET_TYPE_RECENT);
    }

    public ApMusicSheet getPlayListSheet(Context context) {
        return ApMusicSheetRepository.getInstance(context).querySheetByType(ApConstants.MUSIC_SHEET_TYPE_PLAY_LIST);
    }

    public ApMusicSheet getCustomSheet(Context context, long sheetId) {
        return ApMusicSheetRepository.getInstance(context).querySheetById(sheetId);
    }

    public List<ApMusicSheet> getCustomMusicSheetList(Context context) {
        return ApMusicSheetRepository.getInstance(context).querySheetListByType(ApConstants.MUSIC_SHEET_TYPE_CUSTOM);
    }

    public List<ApMusicSheet> getCustomMusicSheetList(Context context, long... excludeSheetIds) {
        return ApMusicSheetRepository.getInstance(context).querySheetListByType(ApConstants.MUSIC_SHEET_TYPE_CUSTOM, excludeSheetIds);
    }

    public long addMusicSheet(Context context, ApMusicSheet apMusicSheet) {
        apMusicSheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_CUSTOM);
        apMusicSheet.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
        apMusicSheet.setCreateTimeStamp(Calendar.getInstance().getTimeInMillis());
        return ApMusicSheetRepository.getInstance(context).addMusicSheet(apMusicSheet);
    }

    public void removeMusicSheet(Context context, ApMusicSheet apMusicSheet) {
        ApMusicSheetRepository.getInstance(context).deleteMusicSheet(apMusicSheet);
    }


    public int getAllMusicListCount(Context context) {
        return ApLocalMusicUtils.getAllMusicListCount(context);
    }

    public List<ApSheetMusic> getAllMusicList(Context context) {
        return ApLocalMusicUtils.getAllMusicList(context);
    }

    public List<ApSheetMusic> getSheetMusicList(Context context, long sheetId) {
        return ApSheetMusicRepository.getInstance(context).querySheetMusicList(sheetId);
    }

    public void addSheetMusic(Context context, ApSheetMusic apSheetMusic, long sheetId) {
        ApSheetMusicRepository.getInstance(context).addSheetMusic(apSheetMusic, sheetId);
    }

    public void addSheetMusicList(Context context, List<ApSheetMusic> list, long sheetId) {
        ApSheetMusicRepository.getInstance(context).addSheetMusicList(list, sheetId);
    }

    public void updateMusicLyric(Context context, ApSheetMusic music, String filePath) {
        music.setLyricFilePath(filePath);
        music.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
        ApSheetMusicRepository.getInstance(context).updateSheetMusic(music);
    }

    public void removeSheetMusic(Context context, ApSheetMusic apSheetMusic) {
        ApSheetMusicRepository.getInstance(context).deleteSheetMusic(apSheetMusic);
    }

    public void removeSheetMusic(Context context, long sheetId, long songId) {
        ApSheetMusicRepository.getInstance(context).deleteSheetMusic(sheetId, songId);
    }

    public void clearSheetMusic(Context context, long sheetId) {
        ApSheetMusicRepository.getInstance(context).deleteSheetAllMusics(sheetId);
    }

    public void removeSheetMusicList(Context context, List<ApSheetMusic> list, long sheetId) {
        ApSheetMusicRepository.getInstance(context).deleteSheetMusicList(list, sheetId);
    }
}
