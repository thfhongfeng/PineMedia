package com.pine.tool.request.callback;

import android.app.Application;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.pine.tool.R;
import com.pine.tool.exception.MessageException;
import com.pine.tool.request.Response;
import com.pine.tool.util.AppUtils;

/**
 * Created by tanghongfeng on 2018/9/10.
 */

public abstract class BitmapCallback extends DataResponseCallback {

    @Override
    public void onResponse(int what, Response response) {
        if (response.getData() instanceof Bitmap) {
            Bitmap bitmap = (Bitmap) response.getData();
            onResponse(what, bitmap, response);
        } else {
            Application application = AppUtils.getApplication();
            String errMsg = application.getString(R.string.tool_data_type_err);
            if (!onFail(what, new MessageException(errMsg), response)) {
                Toast.makeText(application, errMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public abstract void onResponse(int what, Bitmap bitmap, Response response);
}
