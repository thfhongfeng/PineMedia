package com.pine.media.audio.vm;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pine.media.audio.ApConstants;
import com.pine.media.audio.db.entity.ApMusic;
import com.pine.media.audio.db.entity.ApSheet;
import com.pine.media.audio.model.ApMusicModel;
import com.pine.tool.architecture.mvp.model.IModelAsyncResponse;
import com.pine.tool.architecture.mvvm.vm.ViewModel;

import java.util.List;

public class ApMusicListVm extends ViewModel {
    private ApMusicModel mModel = new ApMusicModel();

    public MutableLiveData<List<ApMusic>> mSheetMusicListData = new MutableLiveData<>();
    private ApSheet mMusicSheet;
    public MutableLiveData<ApSheet> mSheetData = new MutableLiveData<>();
    public MutableLiveData<Boolean> mActionData = new MutableLiveData<>();

    @Override
    public boolean parseIntentData(Context activity, @NonNull Bundle bundle) {
        mMusicSheet = (ApSheet) bundle.getSerializable("musicSheet");
        if (mMusicSheet == null) {
            finishUi();
            return true;
        }
        mActionData.setValue(bundle.getBoolean("action", false));
        mSheetData.setValue(mMusicSheet);
        return false;
    }

    public void refreshData(Context context) {
        mModel.getMusicListDetail(context, mMusicSheet, new IModelAsyncResponse<List<ApMusic>>() {
            @Override
            public void onResponse(List<ApMusic> list) {
                mSheetMusicListData.setValue(list);
            }

            @Override
            public boolean onFail(Exception e) {
                mSheetMusicListData.setValue(null);
                return false;
            }

            @Override
            public void onCancel() {
                mSheetMusicListData.setValue(null);
            }
        });
    }

    public void updateMusicFavourite(Context context, final ApMusic music, final boolean isFavourite) {
        mModel.updateMusicFavourite(context, music, isFavourite, new IModelAsyncResponse<Boolean>() {
            @Override
            public void onResponse(Boolean success) {
                music.setFavourite(isFavourite);
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

    public void deleteMusicSheet(Context context) {
        mModel.removeMusicSheet(context, mMusicSheet, new IModelAsyncResponse<Boolean>() {
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
    }

    public void deleteSheetMusic(Context context, ApMusic music) {
        if (mMusicSheet.getSheetType() == ApConstants.MUSIC_SHEET_TYPE_FAVOURITE) {
            updateMusicFavourite(context, music, false);
        } else {
            mModel.removeSheetMusic(context, mMusicSheet.getId(), music.getSongId(),
                    new IModelAsyncResponse<Boolean>() {
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
        }
    }

    public void deleteSheetMusics(Context context, List<ApMusic> selectList) {
        if (mMusicSheet.getSheetType() == ApConstants.MUSIC_SHEET_TYPE_FAVOURITE) {
            mModel.updateMusicListFavourite(context, selectList, false, new IModelAsyncResponse<Boolean>() {
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
            mModel.removeSheetMusicList(context, selectList, mMusicSheet.getId(), new IModelAsyncResponse<Boolean>() {
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
        }
    }
}
