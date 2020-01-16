package com.pine.audioplayer.vm;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pine.audioplayer.bean.ApSheetListDetail;
import com.pine.audioplayer.db.entity.ApSheet;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.tool.architecture.mvp.model.IModelAsyncResponse;
import com.pine.tool.architecture.mvvm.vm.ViewModel;

import java.util.List;

public class ApSheetListVm extends ViewModel {
    public static final int LIVE_DATA_TAG_RECENT_SHEET = 1;

    protected ApMusicModel mModel = new ApMusicModel();

    public MutableLiveData<ApSheet> mAllMusicSheetData = new MutableLiveData<>();
    public MutableLiveData<ApSheet> mFavouriteSheetData = new MutableLiveData<>();
    public LiveData<ApSheet> mRecentSheetData;
    public MutableLiveData<List<ApSheet>> mCustomSheetListData = new MutableLiveData<>();

    @Override
    public boolean parseIntentData(Context activity, @NonNull Bundle bundle) {
        return false;
    }

    @Override
    public void afterViewInit(Context activity) {
        super.afterViewInit(activity);
        mModel.syncRecentSheet(activity, new IModelAsyncResponse<LiveData<ApSheet>>() {
            @Override
            public void onResponse(LiveData<ApSheet> apSheetLiveData) {
                mRecentSheetData = apSheetLiveData;
                setSyncLiveDataTag(LIVE_DATA_TAG_RECENT_SHEET);
            }

            @Override
            public boolean onFail(Exception e) {
                return false;
            }

            @Override
            public void onCancel() {

            }
        });
    }

    public void createSheet(final Context context, String sheetName) {
        final ApSheet sheet = new ApSheet();
        sheet.setName(sheetName);
        mModel.addMusicSheet(context, sheet, new IModelAsyncResponse<Long>() {
            @Override
            public void onResponse(Long id) {
                sheet.setId(id);
                refreshData(context);
            }

            @Override
            public boolean onFail(Exception e) {
                return false;
            }

            @Override
            public void onCancel() {

            }
        });
    }

    public void refreshData(Context context) {
        mModel.getMusicSheetList(context, new IModelAsyncResponse<ApSheetListDetail>() {
            @Override
            public void onResponse(ApSheetListDetail sheetListDetail) {
                mAllMusicSheetData.setValue(sheetListDetail.getAllMusicSheet());
                mFavouriteSheetData.setValue(sheetListDetail.getFavouriteSheet());
                mCustomSheetListData.setValue(sheetListDetail.getCustomSheetList());
            }

            @Override
            public boolean onFail(Exception e) {
                return false;
            }

            @Override
            public void onCancel() {

            }
        }, -1);
    }
}
