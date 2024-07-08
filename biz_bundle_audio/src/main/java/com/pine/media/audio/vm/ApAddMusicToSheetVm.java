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

public class ApAddMusicToSheetVm extends ViewModel {
    protected ApMusicModel mModel = new ApMusicModel();

    public MutableLiveData<List<ApSheet>> mSheetListData = new MutableLiveData<>();
    public MutableLiveData<List<ApMusic>> mSelectMusicListData = new MutableLiveData<>();

    private long mExcludeCustomSheetId = -1;

    @Override
    public boolean parseIntentData(Context activity, @NonNull Bundle bundle) {
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
    public void afterViewInit(Context activity) {
        super.afterViewInit(activity);
    }

    public void createAndAddMusicToSheet(final Context context, String sheetName) {
        final ApSheet sheet = new ApSheet();
        sheet.setName(sheetName);
        mModel.addMusicSheet(context, sheet, new IModelAsyncResponse<Long>() {
            @Override
            public void onResponse(Long id) {
                sheet.setId(id);
                addMusicToSheet(context, sheet);
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

    public void addMusicToSheet(Context context, ApSheet sheet) {
        List<ApMusic> selectList = mSelectMusicListData.getValue();
        if (sheet.getSheetType() == ApConstants.MUSIC_SHEET_TYPE_FAVOURITE) {
            mModel.updateMusicListFavourite(context, selectList, true, new IModelAsyncResponse<Boolean>() {
                @Override
                public void onResponse(Boolean success) {

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
            mModel.addSheetMusicList(context, selectList, sheet.getId(), new IModelAsyncResponse<List<ApMusic>>() {
                @Override
                public void onResponse(List<ApMusic> list) {

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

    public void refreshData(Context context) {
        mModel.getMusicSheetList(context, new IModelAsyncResponse<ApSheetListDetail>() {
            @Override
            public void onResponse(ApSheetListDetail sheetListDetail) {
                if (sheetListDetail.getCustomSheetList() != null && sheetListDetail.getFavouriteSheet() != null) {
                    sheetListDetail.getCustomSheetList().add(0, sheetListDetail.getFavouriteSheet());
                }
                mSheetListData.setValue(sheetListDetail.getCustomSheetList());
            }

            @Override
            public boolean onFail(Exception e) {
                return false;
            }

            @Override
            public void onCancel() {

            }
        }, mExcludeCustomSheetId);
    }
}
