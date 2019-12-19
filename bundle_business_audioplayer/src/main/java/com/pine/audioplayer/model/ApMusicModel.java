package com.pine.audioplayer.model;

import android.content.Context;

import com.pine.audioplayer.ApConstants;
import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.db.repository.ApMusicSheetRepository;
import com.pine.audioplayer.db.repository.ApSheetMusicRepository;

import java.util.ArrayList;
import java.util.List;

public class ApMusicModel {

    public ApMusicSheet getFavouriteSheet(Context context) {
        return ApMusicSheetRepository.getInstance(context).querySheetBySheetId(ApConstants.MUSIC_FAVOURITE_SHEET_ID);
    }

    public ApMusicSheet getRecentSheet(Context context) {
        return ApMusicSheetRepository.getInstance(context).querySheetBySheetId(ApConstants.MUSIC_RECENT_SHEET_ID);
    }

    public List<ApMusicSheet> getCustomMusicSheetList(Context context) {
        List<Integer> excludeIds = new ArrayList<>();
        excludeIds.add(ApConstants.MUSIC_FAVOURITE_SHEET_ID);
        excludeIds.add(ApConstants.MUSIC_RECENT_SHEET_ID);
        return ApMusicSheetRepository.getInstance(context).querySheetListExcludeIds(excludeIds);
    }

    public void addMusicSheet(Context context, ApMusicSheet apMusicSheet) {
        int curMaxSheetId = ApMusicSheetRepository.getInstance(context).queryMaxSheetId();
        apMusicSheet.setSheetId(++curMaxSheetId);
        ApMusicSheetRepository.getInstance(context).addMusicSheet(apMusicSheet);
    }

    public void removeMusicSheet(Context context, ApMusicSheet apMusicSheet) {
        ApMusicSheetRepository.getInstance(context).deleteMusicSheet(apMusicSheet);
    }

    public List<ApSheetMusic> getSheetMusicList(Context context, int sheetId) {
        return ApSheetMusicRepository.getInstance(context).querySheetMusicList(sheetId);
    }

    public void addSheetMusic(Context context, ApSheetMusic apSheetMusic) {
        ApSheetMusicRepository.getInstance(context).addSheetMusic(apSheetMusic);
    }

    public void addSheetMusicList(Context context, List<ApSheetMusic> list) {
        ApSheetMusicRepository.getInstance(context).addSheetMusicList(list);
    }

    public void removeSheetMusic(Context context, ApSheetMusic apSheetMusic) {
        ApSheetMusicRepository.getInstance(context).deleteSheetMusic(apSheetMusic);
    }

    public void removeSheetMusicList(Context context, List<ApSheetMusic> list) {
        ApSheetMusicRepository.getInstance(context).deleteSheetMusicList(list);
    }
}
