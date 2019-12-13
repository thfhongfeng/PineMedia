package com.pine.tool.request.callback;

import android.app.Application;
import android.widget.Toast;

import com.pine.tool.R;
import com.pine.tool.exception.MessageException;
import com.pine.tool.request.Response;
import com.pine.tool.util.AppUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tanghongfeng on 2018/9/10.
 */

public abstract class JsonCallback extends DataResponseCallback {

    @Override
    public void onResponse(int what, Response response) {
        if (response.getData() instanceof String) {
            String res = (String) response.getData();
            try {
                JSONObject jsonObject = new JSONObject(res);
                onResponse(what, jsonObject, response);
            } catch (JSONException e) {
                e.printStackTrace();
                Application application = AppUtils.getApplication();
                String errMsg = application.getString(R.string.tool_data_type_err);
                if (!onFail(what, new MessageException(errMsg), response)) {
                    Toast.makeText(application, errMsg, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Application application = AppUtils.getApplication();
            String errMsg = application.getString(R.string.tool_data_type_err);
            if (!onFail(what, new MessageException(errMsg), response)) {
                Toast.makeText(application, errMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public abstract void onResponse(int what, JSONObject jsonObject, Response response);
}
