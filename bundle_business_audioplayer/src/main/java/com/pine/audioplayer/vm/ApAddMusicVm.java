package com.pine.audioplayer.vm;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pine.audioplayer.ApConstants;
import com.pine.audioplayer.R;
import com.pine.audioplayer.db.entity.ApMusic;
import com.pine.audioplayer.db.entity.ApSheet;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.tool.architecture.mvvm.vm.ViewModel;

import java.util.List;

public class ApAddMusicVm extends ViewModel {
    protected ApMusicModel mModel = new ApMusicModel();

    public MutableLiveData<ApSheet> mAllMusicSheetData = new MutableLiveData<>();
    public MutableLiveData<ApSheet> mFavouriteSheetData = new MutableLiveData<>();
    public MutableLiveData<ApSheet> mRecentSheetData = new MutableLiveData<>();
    public MutableLiveData<List<ApSheet>> mCustomSheetListData = new MutableLiveData<>();

    private ApSheet mAllMusicSheet = new ApSheet();
    private ApSheet mFavouriteSheet = new ApSheet();
    public ApSheet mSheetBeAddTo;

    @Override
    public boolean parseIntentData(@NonNull Bundle bundle) {
        mSheetBeAddTo = (ApSheet) bundle.getSerializable("musicSheet");
        if (mSheetBeAddTo == null) {
            finishUi();
            return true;
        }
        return false;
    }

    @Override
    public void afterViewInit() {
        super.afterViewInit();
        mAllMusicSheet.setName(getContext().getString(R.string.ap_home_all_music_name));
        mAllMusicSheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_ALL);
        mFavouriteSheet.setName(getContext().getString(R.string.ap_home_my_favourite_name));
        mFavouriteSheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_FAVOURITE);
    }

    public void refreshData() {
        mAllMusicSheet.setCount(mModel.getAllMusicListCount(getContext()));
        mAllMusicSheetData.setValue(mAllMusicSheet);
        mFavouriteSheet.setCount(mModel.getFavouriteMusicListCount(getContext()));
        mFavouriteSheetData.setValue(mFavouriteSheet);
        mRecentSheetData.setValue(mModel.getRecentSheet(getContext()));
        mCustomSheetListData.setValue(mModel.getCustomMusicSheetList(getContext(), mSheetBeAddTo.getId()));
    }

    public void addMusicList(List<ApMusic> selectList) {
        if (mSheetBeAddTo.getSheetType() == ApConstants.MUSIC_SHEET_TYPE_FAVOURITE) {
            mModel.updateMusicListFavourite(getContext(), selectList, true);
        } else {
            mModel.addSheetMusicList(getContext(), selectList, mSheetBeAddTo.getId());
        }
        refreshData();
    }
}
