package com.pine.audioplayer.vm;

import android.os.Bundle;

import com.pine.audioplayer.ApConstants;
import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.audioplayer.model.ApMusicModel;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.tool.architecture.mvvm.vm.ViewModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

public class ApMusicListVm extends ViewModel {

    private ApMusicModel mModel = new ApMusicModel();

    public MutableLiveData<List<PineMediaPlayerBean>> mMediaListData = new MutableLiveData<>();
    public MutableLiveData<List<ApSheetMusic>> mSheetMusicListData = new MutableLiveData<>();
    public MutableLiveData<ApMusicSheet> mSheetData = new MutableLiveData<>();

    private ApMusicSheet mMusicSheet;
    private long mSheetId;

    @Override
    public boolean parseIntentData(@NonNull Bundle bundle) {
        mMusicSheet = (ApMusicSheet) bundle.getSerializable("musicSheet");
        if (mMusicSheet == null) {
            return true;
        }
        mSheetId = mMusicSheet.getId();
        mSheetData.setValue(mMusicSheet);
        return false;
    }

    @Override
    public void afterViewInit() {
        switch (mMusicSheet.getSheetType()) {
            case ApConstants.MUSIC_SHEET_TYPE_ALL:
                mSheetMusicListData.setValue(mModel.getAllMusicList(getContext()));
                break;
            case ApConstants.MUSIC_SHEET_TYPE_FAVOURITE:
            case ApConstants.MUSIC_SHEET_TYPE_RECENT:
            case ApConstants.MUSIC_SHEET_TYPE_CUSTOM:
                mSheetMusicListData.setValue(mModel.getSheetMusicList(getContext(), mSheetId));
                break;
        }
    }

    public void onRefresh() {
        switch (mMusicSheet.getSheetType()) {
            case ApConstants.MUSIC_SHEET_TYPE_ALL:
                mMusicSheet = mModel.getRecentSheet(getContext());
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
}
