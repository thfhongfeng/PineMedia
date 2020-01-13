package com.pine.audioplayer.vm;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pine.audioplayer.ApConstants;
import com.pine.audioplayer.db.entity.ApMusic;
import com.pine.audioplayer.db.entity.ApSheet;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.tool.architecture.mvvm.vm.ViewModel;

import java.util.List;

public class ApMusicListVm extends ViewModel {
    private ApMusicModel mModel = new ApMusicModel();

    public MutableLiveData<List<ApMusic>> mSheetMusicListData = new MutableLiveData<>();
    private ApSheet mMusicSheet;
    public MutableLiveData<ApSheet> mSheetData = new MutableLiveData<>();
    public MutableLiveData<Boolean> mActionData = new MutableLiveData<>();

    @Override
    public boolean parseIntentData(@NonNull Bundle bundle) {
        mMusicSheet = (ApSheet) bundle.getSerializable("musicSheet");
        if (mMusicSheet == null) {
            finishUi();
            return true;
        }
        mActionData.setValue(bundle.getBoolean("action", false));
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
                mSheetData.setValue(mMusicSheet);
                mSheetMusicListData.setValue(mModel.getFavouriteMusicList(getContext()));
                break;
            case ApConstants.MUSIC_SHEET_TYPE_RECENT:
                mMusicSheet = mModel.getRecentSheet(getContext());
                mSheetData.setValue(mMusicSheet);
                mSheetMusicListData.setValue(mModel.getSheetMusicList(getContext(), mMusicSheet.getId()));
                break;
            case ApConstants.MUSIC_SHEET_TYPE_CUSTOM:
                mMusicSheet = mModel.getCustomSheet(getContext(), mMusicSheet.getId());
                mSheetData.setValue(mMusicSheet);
                mSheetMusicListData.setValue(mModel.getSheetMusicList(getContext(), mMusicSheet.getId()));
                break;
        }
    }

    public void addMusicToFavourite(ApMusic music) {
        mModel.updateMusicFavourite(getContext(), music, true);
    }

    public void deleteMusicSheet() {
        mModel.removeMusicSheet(getContext(), mMusicSheet);
    }

    public void deleteSheetMusic(ApMusic music) {
        if (mMusicSheet.getSheetType() == ApConstants.MUSIC_SHEET_TYPE_FAVOURITE) {
            mModel.updateMusicFavourite(getContext(), music, false);
        } else {
            mModel.removeSheetMusic(getContext(), mMusicSheet.getId(), music.getSongId());
        }
    }

    public void deleteSheetMusics(List<ApMusic> selectList) {
        if (mMusicSheet.getSheetType() == ApConstants.MUSIC_SHEET_TYPE_FAVOURITE) {
            mModel.updateMusicListFavourite(getContext(), selectList, false);
        } else {
            mModel.removeSheetMusicList(getContext(), selectList, mMusicSheet.getId());
        }
    }
}
