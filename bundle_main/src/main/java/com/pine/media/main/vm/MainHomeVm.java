package com.pine.media.main.vm;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pine.app.template.bundle_main.BuildConfigKey;
import com.pine.app.template.bundle_main.router.RouterAudioCommand;
import com.pine.app.template.bundle_main.router.RouterPicCommand;
import com.pine.app.template.bundle_main.router.RouterVideoCommand;
import com.pine.media.main.R;
import com.pine.media.main.bean.MainBizItemEntity;
import com.pine.media.main.model.MainHomeModel;
import com.pine.tool.architecture.mvvm.vm.ViewModel;
import com.pine.tool.router.RouterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainHomeVm extends ViewModel {
    private MainHomeModel mHomeModel = new MainHomeModel();

    public void loadBizBundleData(Context context) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        try {
            if (RouterManager.isBundleEnable(BuildConfigKey.BIZ_BUNDLE_VIDEO)) {
                jsonObject = new JSONObject();
                jsonObject.put("name", context.getString(R.string.main_home_video_player));
                jsonObject.put("imageResId", R.mipmap.main_ic_video_player);
                jsonObject.put("bundle", BuildConfigKey.BIZ_BUNDLE_VIDEO);
                jsonObject.put("command", RouterVideoCommand.goVideoHomeActivity);
                jsonArray.put(jsonObject);
            }
            if (RouterManager.isBundleEnable(BuildConfigKey.BIZ_BUNDLE_AUDIO)) {
                jsonObject = new JSONObject();
                jsonObject.put("name", context.getString(R.string.main_home_audio_player));
                jsonObject.put("imageResId", R.mipmap.main_ic_audio_player);
                jsonObject.put("bundle", BuildConfigKey.BIZ_BUNDLE_AUDIO);
                jsonObject.put("command", RouterAudioCommand.goAudioHomeActivity);
                jsonArray.put(jsonObject);
            }
            if (RouterManager.isBundleEnable(BuildConfigKey.BIZ_BUNDLE_PICTURE)) {
                jsonObject = new JSONObject();
                jsonObject.put("name", context.getString(R.string.main_home_picture_viewer));
                jsonObject.put("imageResId", R.mipmap.main_ic_picture_viewer);
                jsonObject.put("bundle", BuildConfigKey.BIZ_BUNDLE_PICTURE);
                jsonObject.put("command", RouterPicCommand.goPicHomeActivity);
                jsonArray.put(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<MainBizItemEntity> entityList = new Gson().fromJson(jsonArray.toString(),
                new TypeToken<ArrayList<MainBizItemEntity>>() {
                }.getType());
        bizBundleListData.setValue(entityList);
    }

    public MutableLiveData<ArrayList<MainBizItemEntity>> bizBundleListData = new MutableLiveData<>();
}
