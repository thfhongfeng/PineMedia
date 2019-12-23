package com.pine.audioplayer.vm;

import androidx.lifecycle.MutableLiveData;

import com.pine.audioplayer.ApConstants;
import com.pine.audioplayer.R;
import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.tool.architecture.mvvm.vm.ViewModel;

import java.util.List;

public class ApSheetListVm extends ViewModel {
    protected ApMusicModel mModel = new ApMusicModel();

    public MutableLiveData<ApMusicSheet> mAllMusicSheetData = new MutableLiveData<>();
    public MutableLiveData<ApMusicSheet> mFavouriteSheetData = new MutableLiveData<>();
    public MutableLiveData<ApMusicSheet> mRecentSheetData = new MutableLiveData<>();
    public MutableLiveData<List<ApMusicSheet>> mCustomSheetListData = new MutableLiveData<>();

    private ApMusicSheet mAllMusicSheet = new ApMusicSheet();

    @Override
    public void afterViewInit() {
        super.afterViewInit();
        mAllMusicSheet.setName(getContext().getString(R.string.ap_home_all_music_name));
        mAllMusicSheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_ALL);
    }

    public void createSheet(String sheetName) {
        ApMusicSheet sheet = new ApMusicSheet();
        sheet.setName(sheetName);
        mModel.addMusicSheet(getContext(), sheet);
        mCustomSheetListData.setValue(mModel.getCustomMusicSheetList(getContext()));
    }

    public void refreshData() {
        mAllMusicSheet.setCount(mModel.getAllMusicListCount(getContext()));
        mAllMusicSheetData.setValue(mAllMusicSheet);
        mFavouriteSheetData.setValue(mModel.getFavouriteSheet(getContext()));
        mRecentSheetData.setValue(mModel.getRecentSheet(getContext()));
        mCustomSheetListData.setValue(mModel.getCustomMusicSheetList(getContext()));
    }
}
