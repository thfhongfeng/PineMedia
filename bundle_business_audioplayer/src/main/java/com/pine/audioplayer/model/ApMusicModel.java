package com.pine.audioplayer.model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pine.audioplayer.ApConstants;
import com.pine.audioplayer.R;
import com.pine.audioplayer.bean.ApSheetListDetail;
import com.pine.audioplayer.bean.ApSheetMusicDetail;
import com.pine.audioplayer.db.entity.ApMusic;
import com.pine.audioplayer.db.entity.ApSheet;
import com.pine.audioplayer.db.repository.ApMusicRepository;
import com.pine.audioplayer.db.repository.ApSheetMusicRepository;
import com.pine.audioplayer.db.repository.ApSheetRepository;
import com.pine.audioplayer.util.ApLocalMusicUtils;
import com.pine.tool.architecture.mvp.model.IModelAsyncResponse;

import java.util.ArrayList;
import java.util.List;

public class ApMusicModel {
    private ApSheet getAllMusicSheet(Context context) {
        ApSheet sheet = new ApSheet();
        sheet.setName(context.getString(R.string.ap_home_all_music_name));
        sheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_ALL);
        sheet.setCount(ApLocalMusicUtils.getAllMusicListCount(context));
        return sheet;
    }

    private ApSheet getFavouriteMusicSheet(Context context) {
        ApSheet sheet = new ApSheet();
        sheet.setName(context.getString(R.string.ap_home_my_favourite_name));
        sheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_FAVOURITE);
        sheet.setCount(ApMusicRepository.getInstance(context).getFavouriteMusicListCount());
        return sheet;
    }

    public void getRecentSheet(Context context, @NonNull IModelAsyncResponse<ApSheet> callback) {
        ApSheet sheet = ApSheetRepository.getInstance(context).querySheetByType(ApConstants.MUSIC_SHEET_TYPE_RECENT);
        callback.onResponse(sheet == null ? new ApSheet() : sheet);
    }

    public void syncRecentSheet(Context context, @NonNull IModelAsyncResponse<LiveData<ApSheet>> callback) {
        LiveData<ApSheet> sheetData = ApSheetRepository.getInstance(context).syncSheetByType(ApConstants.MUSIC_SHEET_TYPE_RECENT);
        callback.onResponse(sheetData == null ? new MutableLiveData<ApSheet>() : sheetData);
    }

    public void getMusicSheetList(Context context, @NonNull IModelAsyncResponse<ApSheetListDetail> callback, long... excludeSheetIds) {
        ApSheetListDetail sheetListDetail = new ApSheetListDetail();
        sheetListDetail.setAllMusicSheet(getAllMusicSheet(context));
        sheetListDetail.setFavouriteSheet(getFavouriteMusicSheet(context));
        if (excludeSheetIds[0] == -1) {
            sheetListDetail.setCustomSheetList(ApSheetRepository.getInstance(context).querySheetListByType(ApConstants.MUSIC_SHEET_TYPE_CUSTOM));
        } else {
            sheetListDetail.setCustomSheetList(ApSheetRepository.getInstance(context).querySheetListByType(ApConstants.MUSIC_SHEET_TYPE_CUSTOM, excludeSheetIds));
        }
        callback.onResponse(sheetListDetail);
    }

    public void addMusicSheet(Context context, ApSheet apSheet, @NonNull IModelAsyncResponse<Long> callback) {
        apSheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_CUSTOM);
        callback.onResponse(ApSheetRepository.getInstance(context).addMusicSheet(apSheet));
    }

    public void removeMusicSheet(Context context, ApSheet apSheet, @NonNull IModelAsyncResponse<Boolean> callback) {
        ApSheetRepository.getInstance(context).deleteMusicSheet(apSheet);
        callback.onResponse(true);
    }

    public void getPlayListDetail(Context context, @NonNull IModelAsyncResponse<ApSheetMusicDetail> callback) {
        ApSheet sheet = ApSheetRepository.getInstance(context).querySheetByType(ApConstants.MUSIC_SHEET_TYPE_PLAY_LIST);
        ApSheetMusicDetail sheetDetail = null;
        if (sheet != null) {
            List<ApMusic> playList = ApSheetMusicRepository.getInstance(context).querySheetMusicList(sheet.getId());
            sheetDetail = new ApSheetMusicDetail();
            sheetDetail.setSheet(sheet);
            sheetDetail.setMusicList(playList);
        }
        callback.onResponse(sheetDetail);
    }

    public void getMusicListDetail(Context context, @NonNull ApSheet apSheet, @NonNull IModelAsyncResponse<List<ApMusic>> callback) {
        List<ApMusic> list = new ArrayList<>();
        switch (apSheet.getSheetType()) {
            case ApConstants.MUSIC_SHEET_TYPE_ALL:
                list = ApLocalMusicUtils.getAllMusicList(context);
                ApMusicRepository.getInstance(context).addMusicList(list);
                break;
            case ApConstants.MUSIC_SHEET_TYPE_FAVOURITE:
                list = ApMusicRepository.getInstance(context).getFavouriteMusicList();
                break;
            case ApConstants.MUSIC_SHEET_TYPE_RECENT:
            case ApConstants.MUSIC_SHEET_TYPE_CUSTOM:
                list = ApSheetMusicRepository.getInstance(context).querySheetMusicList(apSheet.getId());
                break;
        }
        callback.onResponse(list);
    }

    public void updateMusicFavourite(Context context, @NonNull ApMusic music, boolean isFavourite,
                                     @NonNull IModelAsyncResponse<Boolean> callback) {
        ApMusicRepository.getInstance(context).updateMusicFavourite(music.getSongId(), isFavourite);
        callback.onResponse(true);
    }

    public void updateMusicListFavourite(Context context, @NonNull List<ApMusic> musicList, boolean isFavourite,
                                         @NonNull IModelAsyncResponse<Boolean> callback) {
        ApMusicRepository.getInstance(context).updateMusicListFavourite(musicList, isFavourite);
        callback.onResponse(true);
    }

    public void updateMusicLyric(Context context, @NonNull ApMusic music, String lrcFilePath, String charset,
                                 @NonNull IModelAsyncResponse<Boolean> callback) {
        ApMusicRepository.getInstance(context).updateMusicLyric(music.getSongId(), lrcFilePath, charset);
        callback.onResponse(true);
    }

    public void getSheetMusic(Context context, long sheetId, long songId,
                              @NonNull IModelAsyncResponse<ApMusic> callback) {
        ApMusic music = ApSheetMusicRepository.getInstance(context).querySheetMusic(sheetId, songId);
        callback.onResponse(music);
    }

    public void addSheetMusic(Context context, @NonNull ApMusic apMusic, long sheetId,
                              @NonNull IModelAsyncResponse<ApMusic> callback) {
        ApMusic music = ApSheetMusicRepository.getInstance(context).addSheetMusic(apMusic, sheetId);
        callback.onResponse(music);
    }

    public void addSheetMusicList(Context context, List<ApMusic> list, long sheetId,
                                  @NonNull IModelAsyncResponse<List<ApMusic>> callback) {
        List<ApMusic> musicList = ApSheetMusicRepository.getInstance(context).addSheetMusicList(list, sheetId);
        callback.onResponse(musicList);
    }

    public void removeSheetMusic(Context context, long sheetId, long songId,
                                 @NonNull IModelAsyncResponse<Boolean> callback) {
        ApSheetMusicRepository.getInstance(context).deleteSheetMusic(sheetId, songId);
        callback.onResponse(true);
    }

    public void clearSheetMusic(Context context, long sheetId, @NonNull IModelAsyncResponse<Boolean> callback) {
        ApSheetMusicRepository.getInstance(context).deleteSheetAllMusics(sheetId);
        callback.onResponse(true);
    }

    public void removeSheetMusicList(Context context, List<ApMusic> list, long sheetId,
                                     @NonNull IModelAsyncResponse<Boolean> callback) {
        ApSheetMusicRepository.getInstance(context).deleteSheetMusicList(list, sheetId);
        callback.onResponse(true);
    }
}
