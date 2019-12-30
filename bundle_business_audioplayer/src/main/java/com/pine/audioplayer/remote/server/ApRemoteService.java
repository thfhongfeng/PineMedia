package com.pine.audioplayer.remote.server;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.pine.audioplayer.ui.activity.ApHomeActivity;
import com.pine.base.router.command.RouterAudioPlayerCommand;
import com.pine.tool.router.IServiceCallback;
import com.pine.tool.router.annotation.RouterCommand;

import androidx.annotation.NonNull;

/**
 * Created by tanghongfeng on 2018/9/13
 */

public class ApRemoteService {

    @RouterCommand(CommandName = RouterAudioPlayerCommand.goAudioPlayerHomeActivity)
    public void goAudioPlayerHomeActivity(@NonNull Context context, Bundle args, @NonNull final IServiceCallback callback) {
        Bundle responseBundle = new Bundle();
        Intent intent = new Intent(context, ApHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        callback.onResponse(responseBundle);
    }
}
