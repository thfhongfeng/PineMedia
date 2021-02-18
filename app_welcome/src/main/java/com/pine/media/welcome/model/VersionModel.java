package com.pine.media.welcome.model;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pine.tool.architecture.mvp.model.IModelAsyncResponse;
import com.pine.tool.exception.MessageException;
import com.pine.tool.request.RequestBean;
import com.pine.tool.request.RequestManager;
import com.pine.tool.request.Response;
import com.pine.tool.request.callback.JsonCallback;
import com.pine.tool.util.LogUtils;
import com.pine.media.welcome.WelcomeConstants;
import com.pine.media.welcome.WelcomeUrlConstants;
import com.pine.media.welcome.bean.VersionEntity;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by tanghongfeng on 2018/9/16
 */

public class VersionModel {
    private final String TAG = LogUtils.makeLogTag(this.getClass());
    private static final int REQUEST_QUERY_VERSION_INFO = 1;

    public boolean requestUpdateVersionData(final HashMap<String, String> params,
                                            @NonNull IModelAsyncResponse<VersionEntity> callback) {
        String url = WelcomeUrlConstants.Query_Version_Data;
        RequestBean requestBean = new RequestBean(url, REQUEST_QUERY_VERSION_INFO, params);
        requestBean.setModuleTag(TAG);
        return RequestManager.setJsonRequest(requestBean, handleResponse(callback));
    }

    private <T> JsonCallback handleResponse(final IModelAsyncResponse<T> callback) {
        return new JsonCallback() {
            @Override
            public void onResponse(int what, JSONObject jsonObject, Response response) {
                if (REQUEST_QUERY_VERSION_INFO == what) {
                    if (jsonObject.optBoolean(WelcomeConstants.SUCCESS)) {
                        T retData = new Gson().fromJson(jsonObject.optString(WelcomeConstants.DATA), new TypeToken<VersionEntity>() {
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
