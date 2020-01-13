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

public class ApAddMusicToSheetVm extends ViewModel {
    protected ApMusicModel mModel = new ApMusicModel();

    public MutableLiveData<List<ApSheet>> mSheetListData = new MutableLiveData<>();
    public MutableLiveData<List<ApMusic>> mSelectMusicListData = new MutableLiveData<>();

    private long mExcludeCustomSheetId = -1;

    @Override
    public boolean parseIntentData(@NonNull Bundle bundle) {
        mExcludeCustomSheetId = bundle.getLong("excludeSheetId", -1);
        List<ApMusic> selectList = (List<ApMusic>) bundle.getSerializable("selectList");
        if (selectList == null) {
            finishUi();
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
        ApSheet sheet = new ApSheet();
        sheet.setName(sheetName);
        long sheetId = mModel.addMusicSheet(getContext(), sheet);
        sheet.setId(sheetId);
        addMusicToSheet(sheet);
    }

    public void addMusicToSheet(ApSheet sheet) {
        List<ApMusic> selectList = mSelectMusicListData.getValue();
        if (sheet.getSheetType() == ApConstants.MUSIC_SHEET_TYPE_FAVOURITE) {
            mModel.updateMusicListFavourite(getContext(), selectList, true);
        } else {
            mModel.addSheetMusicList(getContext(), selectList, sheet.getId());
        }
    }

    public void refreshData() {
        ApSheet favouriteMusicSheet = new ApSheet();
        favouriteMusicSheet.setName(getContext().getString(R.string.ap_home_my_favourite_name));
        favouriteMusicSheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_FAVOURITE);
        favouriteMusicSheet.setCount(mModel.getFavouriteMusicListCount(getContext()));
        List<ApSheet> sheetList = mExcludeCustomSheetId == -1 ?
                mModel.getCustomMusicSheetList(getContext()) :
                mModel.getCustomMusicSheetList(getContext(), mExcludeCustomSheetId);
        sheetList.add(0, favouriteMusicSheet);
        mSheetListData.setValue(sheetList);
    }
}
