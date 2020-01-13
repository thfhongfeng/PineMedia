package com.pine.audioplayer.vm;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pine.audioplayer.ApConstants;
import com.pine.audioplayer.R;
import com.pine.audioplayer.db.entity.ApMusic;
import com.pine.audioplayer.db.entity.ApSheet;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.tool.architecture.mvvm.vm.ViewModel;

import java.util.List;

public class ApSheetListVm extends ViewModel {
    public static final int LIVE_DATA_TAG_RECENT_SHEET = 1;

    protected ApMusicModel mModel = new ApMusicModel();

    public MutableLiveData<ApSheet> mAllMusicSheetData = new MutableLiveData<>();
    public MutableLiveData<ApSheet> mFavouriteSheetData = new MutableLiveData<>();
    public LiveData<ApSheet> mRecentSheetData;
    public MutableLiveData<List<ApSheet>> mCustomSheetListData = new MutableLiveData<>();

    private ApSheet mAllMusicSheet = new ApSheet();
    private ApSheet mFavouriteSheet = new ApSheet();

    private ApSheet mRecentSheet;

    @Override
    public boolean parseIntentData(@NonNull Bundle bundle) {
        mRecentSheet = mModel.getRecentSheet(getContext());
        return false;
    }

    @Override
    public void afterViewInit() {
        super.afterViewInit();
        mAllMusicSheet.setName(getContext().getString(R.string.ap_home_all_music_name));
        mAllMusicSheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_ALL);
        mFavouriteSheet.setName(getContext().getString(R.string.ap_home_my_favourite_name));
        mFavouriteSheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_FAVOURITE);

        mRecentSheetData = mModel.syncRecentSheet(getContext());
        setSyncLiveDataTag(LIVE_DATA_TAG_RECENT_SHEET);
    }

    public void createSheet(String sheetName) {
        ApSheet sheet = new ApSheet();
        sheet.setName(sheetName);
        mModel.addMusicSheet(getContext(), sheet);
        mCustomSheetListData.setValue(mModel.getCustomMusicSheetList(getContext()));
    }

    public void refreshData() {
        mAllMusicSheet.setCount(mModel.getAllMusicListCount(getContext()));
        mAllMusicSheetData.setValue(mAllMusicSheet);
        mFavouriteSheet.setCount(mModel.getFavouriteMusicListCount(getContext()));
        mFavouriteSheetData.setValue(mFavouriteSheet);
        mCustomSheetListData.setValue(mModel.getCustomMusicSheetList(getContext()));
    }

    public List<ApMusic> getRecentMusicList() {
        return mModel.getSheetMusicList(getContext(), mRecentSheet.getId());
    }
}
