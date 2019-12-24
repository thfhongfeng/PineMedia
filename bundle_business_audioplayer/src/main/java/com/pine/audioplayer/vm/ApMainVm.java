package com.pine.audioplayer.vm;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.tool.architecture.mvvm.vm.ViewModel;
import com.pine.tool.binding.data.ParametricLiveData;

import java.util.List;

public class ApMainVm extends ViewModel {

    public MutableLiveData<List<PineMediaPlayerBean>> mMediaListData = new MutableLiveData<>();
    public ParametricLiveData<String, Boolean> mPlayStateData = new ParametricLiveData<>();

    @Override
    public boolean parseIntentData(@NonNull Bundle bundle) {
        List<PineMediaPlayerBean> list = (List<PineMediaPlayerBean>) bundle.getSerializable("mediaList");
        String mediaCode = bundle.getString("mediaCode");
        boolean playing = bundle.getBoolean("playing", false);
        if (list != null && !TextUtils.isEmpty(mediaCode)) {
            mMediaListData.setValue(list);
            mPlayStateData.setValue(mediaCode, playing);
        }
        return false;
    }
}
