package com.pine.audioplayer.vm;

import android.os.Bundle;

import com.pine.audioplayer.ApConstants;
import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.tool.architecture.mvvm.vm.ViewModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

public class ApMusicListVm extends ViewModel {
    private ApMusicModel mModel = new ApMusicModel();

    public MutableLiveData<List<ApSheetMusic>> mSheetMusicListData = new MutableLiveData<>();
    private ApMusicSheet mMusicSheet;
    public MutableLiveData<ApMusicSheet> mSheetData = new MutableLiveData<>();
    public MutableLiveData<Boolean> mActionData = new MutableLiveData<>();

    private ApMusicSheet mRecentSheet;

    @Override
    public boolean parseIntentData(@NonNull Bundle bundle) {
        mMusicSheet = (ApMusicSheet) bundle.getSerializable("musicSheet");
        if (mMusicSheet == null) {
            return true;
        }
        mActionData.setValue(bundle.getBoolean("action", false));
        mSheetData.setValue(mMusicSheet);
        mRecentSheet = mModel.getRecentSheet(getContext());
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
                mSheetMusicListData.setValue(mModel.getSheetMusicList(getContext(), mMusicSheet.getId()));
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

    public void addMusicToFavourite(ApSheetMusic music) {
        mModel.addSheetMusic(getContext(), music, mModel.getFavouriteSheet(getContext()).getId());
    }

    public void addMusicToRecent(ApSheetMusic music) {
        mModel.addSheetMusic(getContext(), music, mRecentSheet.getId());
    }

    public void addAllMusicsToRecent() {
        mModel.addSheetMusicList(getContext(), mSheetMusicListData.getValue(), mRecentSheet.getId());
    }

    public void removeMusicFromRecent(long songId) {
        mModel.removeSheetMusic(getContext(), mRecentSheet.getId(), songId);
    }

    public void clearRecentSheetMusic() {
        mModel.clearSheetMusic(getContext(), mRecentSheet.getId());
    }

    public void deleteMusicSheet() {
        mModel.removeMusicSheet(getContext(), mMusicSheet);
    }

    public void deleteSheetMusic(ApSheetMusic sheetMusic) {
        mModel.removeSheetMusic(getContext(), sheetMusic);
    }

    public void deleteSheetMusics(List<ApSheetMusic> selectList) {
        mModel.removeSheetMusicList(getContext(), selectList, mMusicSheet.getId());
    }
}
