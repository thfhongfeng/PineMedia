package com.pine.audioplayer.vm;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pine.audioplayer.ApConstants;
import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.tool.architecture.mvvm.vm.ViewModel;

import java.util.List;

public class ApMusicListVm extends ViewModel {

    private ApMusicModel mModel = new ApMusicModel();

    public MutableLiveData<List<PineMediaPlayerBean>> mMediaListData = new MutableLiveData<>();
    public MutableLiveData<List<ApSheetMusic>> mSheetMusicListData = new MutableLiveData<>();
    public MutableLiveData<ApMusicSheet> mSheetData = new MutableLiveData<>();
    public MutableLiveData<Boolean> mActionData = new MutableLiveData<>();

    private ApMusicSheet mMusicSheet;
    private long mSheetId;

    @Override
    public boolean parseIntentData(@NonNull Bundle bundle) {
        mMusicSheet = (ApMusicSheet) bundle.getSerializable("musicSheet");
        if (mMusicSheet == null) {
            return true;
        }
        mActionData.setValue(bundle.getBoolean("action", false));
        mSheetId = mMusicSheet.getId();
        mSheetData.setValue(mMusicSheet);
        return false;
    }

    public void refreshData() {
        switch (mMusicSheet.getSheetType()) {
            case ApConstants.MUSIC_SHEET_TYPE_ALL:
                mSheetData.setValue(mMusicSheet);
                mSheetMusicListData.setValue(mModel.getAllMusicList(getContext()));
                break;
            case ApConstants.MUSIC_SHEET_TYPE_FAVOURITE:
                mMusicSheet = mModel.getFavouriteSheet(getContext());
                mSheetData.setValue(mMusicSheet);
                mSheetMusicListData.setValue(mModel.getSheetMusicList(getContext(), mSheetId));
                break;
            case ApConstants.MUSIC_SHEET_TYPE_RECENT:
                mMusicSheet = mModel.getRecentSheet(getContext());
                mSheetData.setValue(mMusicSheet);
                mSheetMusicListData.setValue(mModel.getSheetMusicList(getContext(), mSheetId));
                break;
            case ApConstants.MUSIC_SHEET_TYPE_CUSTOM:
                mMusicSheet = mModel.getCustomSheet(getContext(), mMusicSheet.getId());
                mSheetData.setValue(mMusicSheet);
                mSheetMusicListData.setValue(mModel.getSheetMusicList(getContext(), mSheetId));
                break;
        }
    }

    public void deleteMusicSheet() {
        mModel.removeMusicSheet(getContext(), mMusicSheet);
    }

    public void deleteSheetMusics(List<ApSheetMusic> selectList) {
        mModel.removeSheetMusicList(getContext(), selectList, mSheetId);
    }
}
