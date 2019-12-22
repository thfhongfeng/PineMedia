package com.pine.audioplayer.vm;

import androidx.lifecycle.MutableLiveData;

import com.pine.audioplayer.ApConstants;
import com.pine.audioplayer.R;
import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.tool.architecture.mvvm.vm.ViewModel;

import java.util.List;

public class ApSheetListVm extends ViewModel {
    private ApMusicModel mModel = new ApMusicModel();

    public MutableLiveData<ApMusicSheet> mAllMusicSheetData = new MutableLiveData<>();
    public MutableLiveData<ApMusicSheet> mFavouriteSheetData = new MutableLiveData<>();
    public MutableLiveData<ApMusicSheet> mRecentSheetData = new MutableLiveData<>();
    public MutableLiveData<List<ApMusicSheet>> mCustomSheetListData = new MutableLiveData<>();

    @Override
    public void afterViewInit() {
        super.afterViewInit();
        mFavouriteSheetData.setValue(mModel.getFavouriteSheet(getContext()));
        mRecentSheetData.setValue(mModel.getRecentSheet(getContext()));
        mCustomSheetListData.setValue(mModel.getCustomMusicSheetList(getContext()));

        ApMusicSheet allMusicSheet = new ApMusicSheet();
        allMusicSheet.setName(getContext().getString(R.string.ap_home_all_music_name));
        allMusicSheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_ALL);
        allMusicSheet.setCount(mModel.getAllMusicListCount(getContext()));
        mAllMusicSheetData.setValue(allMusicSheet);
    }

    public void createSheet(String sheetName) {
        ApMusicSheet sheet = new ApMusicSheet();
        sheet.setName(sheetName);
        mModel.addMusicSheet(getContext(), sheet);
        mCustomSheetListData.setValue(mModel.getCustomMusicSheetList(getContext()));
    }

    public void onRefresh() {
        mFavouriteSheetData.setValue(mModel.getFavouriteSheet(getContext()));
        mRecentSheetData.setValue(mModel.getRecentSheet(getContext()));
        mCustomSheetListData.setValue(mModel.getCustomMusicSheetList(getContext()));
    }
}
