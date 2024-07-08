package com.pine.media.audio.vm;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pine.media.audio.ApConstants;
import com.pine.media.audio.bean.ApSheetListDetail;
import com.pine.media.audio.db.entity.ApMusic;
import com.pine.media.audio.db.entity.ApSheet;
import com.pine.media.audio.model.ApMusicModel;
import com.pine.tool.architecture.mvp.model.IModelAsyncResponse;
import com.pine.tool.architecture.mvvm.vm.ViewModel;

import java.util.List;

public class ApAddMusicVm extends ViewModel {
    protected ApMusicModel mModel = new ApMusicModel();

    public MutableLiveData<ApSheet> mAllMusicSheetData = new MutableLiveData<>();
    public MutableLiveData<ApSheet> mFavouriteSheetData = new MutableLiveData<>();
    public MutableLiveData<ApSheet> mRecentSheetData = new MutableLiveData<>();
    public MutableLiveData<List<ApSheet>> mCustomSheetListData = new MutableLiveData<>();

    public ApSheet mSheetBeAddTo;

    @Override
    public boolean parseIntentData(Context activity, @NonNull Bundle bundle) {
        mSheetBeAddTo = (ApSheet) bundle.getSerializable("musicSheet");
        if (mSheetBeAddTo == null) {
            finishUi();
            return true;
        }
        return false;
    }

    @Override
    public void afterViewInit(Context activity) {
        super.afterViewInit(activity);
    }

    public void refreshData(Context context) {
        mModel.getRecentSheet(context, new IModelAsyncResponse<ApSheet>() {
            @Override
            public void onResponse(ApSheet sheet) {
                mRecentSheetData.setValue(sheet);
            }

            @Override
            public boolean onFail(Exception e) {
                return false;
            }

            @Override
            public void onCancel() {

            }
        });
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
        }, mSheetBeAddTo.getId());
    }

    public void addMusicList(final Context context, List<ApMusic> selectList) {
        if (mSheetBeAddTo.getSheetType() == ApConstants.MUSIC_SHEET_TYPE_FAVOURITE) {
            mModel.updateMusicListFavourite(context, selectList, true, new IModelAsyncResponse<Boolean>() {
                @Override
                public void onResponse(Boolean success) {
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
        } else {
            mModel.addSheetMusicList(context, selectList, mSheetBeAddTo.getId(), new IModelAsyncResponse<List<ApMusic>>() {
                @Override
                public void onResponse(List<ApMusic> list) {
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
    }
}
