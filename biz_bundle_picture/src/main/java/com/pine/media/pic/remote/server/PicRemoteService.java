package com.pine.media.pic.remote.server;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.pine.app.template.biz_bundle_picture.router.RouterPicCommand;
import com.pine.media.pic.PicApplication;
import com.pine.media.pic.ui.activity.PicHomeActivity;
import com.pine.tool.router.IServiceCallback;
import com.pine.tool.router.annotation.RouterCommand;

/**
 * Created by tanghongfeng on 2018/9/13
 */

public class PicRemoteService {
    @RouterCommand(CommandName = "onAppCreate")
    public void onAppCreate(@NonNull Context context, Bundle args) {
        PicApplication.onCreate();
    }

    @RouterCommand(CommandName = "onAppAttach")
    public void onAppAttach(@NonNull Context context, Bundle args) {
        PicApplication.attach();
    }

    @RouterCommand(CommandName = RouterPicCommand.goPicHomeActivity)
    public void goPicHomeActivity(@NonNull Context context, Bundle args, @NonNull final IServiceCallback callback) {
        Bundle responseBundle = new Bundle();
        context.startActivity(new Intent(context, PicHomeActivity.class));
        callback.onResponse(responseBundle);
    }
}
