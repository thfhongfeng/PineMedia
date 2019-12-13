package com.pine.config.model;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pine.config.BuildConfig;
import com.pine.config.ConfigApplication;
import com.pine.config.ConfigKey;
import com.pine.config.Constants;
import com.pine.config.UrlConstants;
import com.pine.config.bean.ConfigSwitcherEntity;
import com.pine.tool.architecture.mvp.model.IModelAsyncResponse;
import com.pine.tool.exception.MessageException;
import com.pine.tool.request.RequestBean;
import com.pine.tool.request.RequestManager;
import com.pine.tool.request.Response;
import com.pine.tool.request.callback.JsonCallback;
import com.pine.tool.util.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tanghongfeng on 2018/9/14
 */

public class ConfigSwitcherModel {
    private final String TAG = LogUtils.makeLogTag(this.getClass());
    private static final int REQUEST_REQUEST_QUERY_BUNDLE_SWITCHER = 1;

    public boolean requestBundleSwitcherData(HashMap<String, String> params,
                                             @NonNull IModelAsyncResponse<ArrayList<ConfigSwitcherEntity>> callback) {
        String url = UrlConstants.Query_BundleSwitcher_Data;
        JsonCallback httpStringCallback = handleResponse(callback);
        RequestBean requestBean = new RequestBean(url, REQUEST_REQUEST_QUERY_BUNDLE_SWITCHER, params);
        requestBean.setModuleTag(TAG);
        return RequestManager.setJsonRequest(requestBean, httpStringCallback);
    }

    private <T> JsonCallback handleResponse(final IModelAsyncResponse<T> callback) {
        return new JsonCallback() {
            @Override
            public void onResponse(int what, JSONObject jsonObject, Response response) {
                if (REQUEST_REQUEST_QUERY_BUNDLE_SWITCHER == what) {
                    if (jsonObject.optBoolean(Constants.SUCCESS)) {
                        T retData = new Gson().fromJson(jsonObject.optString(Constants.DATA),
                                new TypeToken<ArrayList<ConfigSwitcherEntity>>() {
                                }.getType());
                        if (callback != null) {
                            callback.onResponse(retData);
                        }
                    } else {
                        if (callback != null) {
                            callback.onFail(new MessageException(jsonObject.optString("message")));
                        }
                    }
                }
            }

            @Override
            public boolean onFail(int what, Exception e, Response response) {
                if (callback != null) {
                    return callback.onFail(e);
                }
                return false;
            }

            @Override
            public void onCancel(int what) {
                if (callback != null) {
                    callback.onCancel();
                }
            }
        };
    }
}
