package com.pine.main.vm;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pine.base.router.command.RouterAudioPlayerCommand;
import com.pine.base.router.command.RouterPictureViewerCommand;
import com.pine.base.router.command.RouterVideoPlayerCommand;
import com.pine.config.ConfigKey;
import com.pine.config.switcher.ConfigSwitcherServer;
import com.pine.main.R;
import com.pine.main.bean.MainBusinessItemEntity;
import com.pine.main.model.MainHomeModel;
import com.pine.tool.architecture.mvvm.vm.ViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainHomeVm extends ViewModel {
    private MainHomeModel mHomeModel = new MainHomeModel();

    public void loadBusinessBundleData(Context context) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        try {
            if (ConfigSwitcherServer.getInstance().isEnable(ConfigKey.BUNDLE_VIDEO_PLAYER_KEY)) {
                jsonObject = new JSONObject();
                jsonObject.put("name", context.getString(R.string.main_home_video_player));
                jsonObject.put("imageResId", R.mipmap.res_ic_video_player);
                jsonObject.put("bundle", ConfigKey.BUNDLE_VIDEO_PLAYER_KEY);
                jsonObject.put("command", RouterVideoPlayerCommand.goVideoPlayerHomeActivity);
                jsonArray.put(jsonObject);
            }
            if (ConfigSwitcherServer.getInstance().isEnable(ConfigKey.BUNDLE_AUDIO_PLAYER_KEY)) {
                jsonObject = new JSONObject();
                jsonObject.put("name", context.getString(R.string.main_home_audio_player));
                jsonObject.put("imageResId", R.mipmap.res_ic_audio_player);
                jsonObject.put("bundle", ConfigKey.BUNDLE_AUDIO_PLAYER_KEY);
                jsonObject.put("command", RouterAudioPlayerCommand.goAudioPlayerHomeActivity);
                jsonArray.put(jsonObject);
            }
            if (ConfigSwitcherServer.getInstance().isEnable(ConfigKey.BUNDLE_PICTURE_VIEWER_KEY)) {
                jsonObject = new JSONObject();
                jsonObject.put("name", context.getString(R.string.main_home_picture_viewer));
                jsonObject.put("imageResId", R.mipmap.res_ic_picture_viewer);
                jsonObject.put("bundle", ConfigKey.BUNDLE_PICTURE_VIEWER_KEY);
                jsonObject.put("command", RouterPictureViewerCommand.goPictureViewerHomeActivity);
                jsonArray.put(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<MainBusinessItemEntity> entityList = new Gson().fromJson(jsonArray.toString(),
                new TypeToken<ArrayList<MainBusinessItemEntity>>() {
                }.getType());
        setBusinessBundleList(entityList);
    }

    private MutableLiveData<ArrayList<MainBusinessItemEntity>> businessBundleListData = new MutableLiveData<>();

    public MutableLiveData<ArrayList<MainBusinessItemEntity>> getBusinessBundleListData() {
        return businessBundleListData;
    }

    public void setBusinessBundleList(ArrayList<MainBusinessItemEntity> list) {
        businessBundleListData.setValue(list);
    }
}
