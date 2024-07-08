package com.pine.media.video.remote.server;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.pine.app.template.biz_bundle_video.router.RouterVideoCommand;
import com.pine.media.video.VpApplication;
import com.pine.media.video.ui.activity.VpHomeActivity;
import com.pine.tool.router.IServiceCallback;
import com.pine.tool.router.annotation.RouterCommand;

/**
 * Created by tanghongfeng on 2018/9/13
 */

public class VideoRemoteService {
    @RouterCommand(CommandName = "onAppCreate")
    public void onAppCreate(@NonNull Context context, Bundle args) {
        VpApplication.onCreate();
    }

    @RouterCommand(CommandName = "onAppAttach")
    public void onAppAttach(@NonNull Context context, Bundle args) {
        VpApplication.attach();
    }

    @RouterCommand(CommandName = RouterVideoCommand.goVideoHomeActivity)
    public void goVideoHomeActivity(@NonNull Context context, Bundle args, @NonNull final IServiceCallback callback) {
        Bundle responseBundle = new Bundle();
        context.startActivity(new Intent(context, VpHomeActivity.class));
        callback.onResponse(responseBundle);
    }
}
