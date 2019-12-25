package com.pine.audioplayer.vm;

import android.net.Uri;
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

    public MutableLiveData<List<ApSheetMusic>> mSheetMusicListData = new MutableLiveData<>();
    public MutableLiveData<ApMusicSheet> mSheetData = new MutableLiveData<>();
    public MutableLiveData<Boolean> mActionData = new MutableLiveData<>();

    private ApMusicSheet mMusicSheet;
    private long mSheetId, mRecentSheetId;

    @Override
    public boolean parseIntentData(@NonNull Bundle bundle) {
        mMusicSheet = (ApMusicSheet) bundle.getSerializable("musicSheet");
        if (mMusicSheet == null) {
            return true;
        }
        mActionData.setValue(bundle.getBoolean("action", false));
        mSheetId = mMusicSheet.getId();
        mSheetData.setValue(mMusicSheet);
        mRecentSheetId = mModel.getRecentSheet(getContext()).getId();
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

    public void getRecentMediaList(@NonNull List<ApSheetMusic> recentMusicList, @NonNull List<PineMediaPlayerBean> recentMediaList) {
        List<ApSheetMusic> list = mModel.getSheetMusicList(getContext(), mRecentSheetId);
        if (list != null && list.size() > 0) {
            for (ApSheetMusic music : list) {
                PineMediaPlayerBean bean = new PineMediaPlayerBean(music.getSongId() + "",
                        music.getName(), Uri.parse(music.getFilePath()),
                        PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null,
                        null, null);
                bean.setMediaDesc(music.getAuthor() + " - " + music.getAlbum());
                recentMediaList.add(bean);
                recentMusicList.add(music);
            }
        }
    }

    public void addMusicToFavourite(ApSheetMusic music) {
        mModel.addSheetMusic(getContext(), music, mModel.getFavouriteSheet(getContext()).getId());
    }

    public void addMusicToRecent(ApSheetMusic music) {
        mModel.addSheetMusic(getContext(), music, mRecentSheetId);
    }

    public void addAllMusicsToRecent() {
        mModel.addSheetMusicList(getContext(), mSheetMusicListData.getValue(), mRecentSheetId);
    }

    public void removeMusicFromRecent(String songIdStr) {
        long songId = Long.parseLong(songIdStr);
        mModel.removeSheetMusic(getContext(), songId, mRecentSheetId);
    }

    public void clearRecentSheetMusic() {
        mModel.clearSheetMusic(getContext(), mRecentSheetId);
    }

    public void deleteMusicSheet() {
        mModel.removeMusicSheet(getContext(), mMusicSheet);
    }

    public void deleteSheetMusic(ApSheetMusic sheetMusic) {
        mModel.removeSheetMusic(getContext(), sheetMusic);
    }

    public void deleteSheetMusics(List<ApSheetMusic> selectList) {
        mModel.removeSheetMusicList(getContext(), selectList, mSheetId);
    }
}
