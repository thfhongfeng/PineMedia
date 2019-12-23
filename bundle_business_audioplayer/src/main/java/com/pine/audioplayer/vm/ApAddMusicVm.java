package com.pine.audioplayer.vm;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pine.audioplayer.ApConstants;
import com.pine.audioplayer.R;
import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.tool.architecture.mvvm.vm.ViewModel;

import java.util.List;

public class ApAddMusicVm extends ViewModel {
    protected ApMusicModel mModel = new ApMusicModel();

    public MutableLiveData<ApMusicSheet> mAllMusicSheetData = new MutableLiveData<>();
    public MutableLiveData<ApMusicSheet> mFavouriteSheetData = new MutableLiveData<>();
    public MutableLiveData<ApMusicSheet> mRecentSheetData = new MutableLiveData<>();
    public MutableLiveData<List<ApMusicSheet>> mCustomSheetListData = new MutableLiveData<>();

    private ApMusicSheet mAllMusicSheet = new ApMusicSheet();
    public ApMusicSheet mSheetBeAddTo;

    @Override
    public boolean parseIntentData(@NonNull Bundle bundle) {
        mSheetBeAddTo = (ApMusicSheet) bundle.getSerializable("musicSheet");
        if (mSheetBeAddTo == null) {
            return true;
        }
        return false;
    }

    @Override
    public void afterViewInit() {
        super.afterViewInit();
        mAllMusicSheet.setName(getContext().getString(R.string.ap_home_all_music_name));
        mAllMusicSheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_ALL);
    }

    public void refreshData() {
        mAllMusicSheet.setCount(mModel.getAllMusicListCount(getContext()));
        mAllMusicSheetData.setValue(mAllMusicSheet);
        mFavouriteSheetData.setValue(mModel.getFavouriteSheet(getContext()));
        mRecentSheetData.setValue(mModel.getRecentSheet(getContext()));
        mCustomSheetListData.setValue(mModel.getCustomMusicSheetList(getContext(), mSheetBeAddTo.getId()));
    }

    public void addMusicList(List<ApSheetMusic> selectList, long sheetId) {
        mModel.addSheetMusicList(getContext(), selectList, sheetId);
        refreshData();
    }
}
