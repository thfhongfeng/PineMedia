package com.pine.audioplayer.vm;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.tool.architecture.mvvm.vm.ViewModel;

import java.util.List;

public class ApAddMusicToSheetVm extends ViewModel {
    protected ApMusicModel mModel = new ApMusicModel();

    public MutableLiveData<List<ApMusicSheet>> mSheetListData = new MutableLiveData<>();
    public MutableLiveData<List<ApSheetMusic>> mSelectMusicListData = new MutableLiveData<>();

    private long mExcludeCustomSheetId = -1;

    @Override
    public boolean parseIntentData(@NonNull Bundle bundle) {
        mExcludeCustomSheetId = bundle.getLong("excludeSheetId", -1);
        List<ApSheetMusic> selectList = bundle.getParcelableArrayList("selectList");
        if (selectList == null) {
            return true;
        }
        mSelectMusicListData.setValue(selectList);
        return false;
    }

    @Override
    public void afterViewInit() {
        super.afterViewInit();
    }

    public void createAndAddMusicToSheet(String sheetName) {
        ApMusicSheet sheet = new ApMusicSheet();
        sheet.setName(sheetName);
        long sheetId = mModel.addMusicSheet(getContext(), sheet);
        sheet.setId(sheetId);
        addMusicToSheet(sheet);
    }

    public void addMusicToSheet(ApMusicSheet sheet) {
        List<ApSheetMusic> selectList = mSelectMusicListData.getValue();
        mModel.addSheetMusicList(getContext(), selectList, sheet.getId());
    }

    public void refreshData() {
        ApMusicSheet favouriteMusicSheet = mModel.getFavouriteSheet(getContext());
        List<ApMusicSheet> sheetList = mExcludeCustomSheetId == -1?
                mModel.getCustomMusicSheetList(getContext()) :
                mModel.getCustomMusicSheetList(getContext(), mExcludeCustomSheetId);
        sheetList.add(0, favouriteMusicSheet);
        mSheetListData.setValue(sheetList);
    }
}
