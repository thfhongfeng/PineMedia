package com.pine.media.videoplayer.remote.server;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.pine.media.base.router.command.RouterVideoPlayerCommand;
import com.pine.tool.router.IServiceCallback;
import com.pine.tool.router.annotation.RouterCommand;
import com.pine.media.videoplayer.ui.activity.VpHomeActivity;

/**
 * Created by tanghongfeng on 2018/9/13
 */

public class VpRemoteService {

    @RouterCommand(CommandName = RouterVideoPlayerCommand.goVideoPlayerHomeActivity)
    public void goVideoPlayerHomeActivity(@NonNull Context context, Bundle args, @NonNull final IServiceCallback callback) {
        Bundle responseBundle = new Bundle();
        Intent intent = new Intent(context, VpHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        callback.onResponse(responseBundle);
    }
}
