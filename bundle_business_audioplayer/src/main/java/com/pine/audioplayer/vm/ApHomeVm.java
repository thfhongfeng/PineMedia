package com.pine.audioplayer.vm;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.lifecycle.MutableLiveData;

import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.tool.architecture.mvvm.vm.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class ApHomeVm extends ViewModel {
    private ApMusicModel mModel = new ApMusicModel();

    public MutableLiveData<ApMusicSheet> mFavouriteSheetData = new MutableLiveData<>();
    public MutableLiveData<ApMusicSheet> mRecentSheetData = new MutableLiveData<>();
    public MutableLiveData<List<ApMusicSheet>> mCustomSheetListData = new MutableLiveData<>();
    public MutableLiveData<List<ApSheetMusic>> mMusicListData = new MutableLiveData<>();

    @Override
    public void afterViewInit() {
        super.afterViewInit();
        mFavouriteSheetData.setValue(mModel.getFavouriteSheet(getContext()));
        mRecentSheetData.setValue(mModel.getRecentSheet(getContext()));
        mCustomSheetListData.setValue(mModel.getCustomMusicSheetList(getContext()));

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.YEAR, MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media.DATA};
        Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);
        List<ApSheetMusic> musicList = new ArrayList<>();
        while (cursor.moveToNext()) {
            ApSheetMusic music = new ApSheetMusic();
            music.setName(cursor.getString(2));
            music.setDuration(cursor.getString(2));
            music.setAuthor(cursor.getString(3));
            music.setAlbum(cursor.getString(4));
            music.setName(cursor.getString(5));
            music.setYear(cursor.getString(6));
            music.setMimeType(cursor.getString(7));
            music.setSize(cursor.getString(8));
            music.setFilePath(cursor.getString(9));
            musicList.add(music);
        }
        mMusicListData.setValue(musicList);
    }
}
