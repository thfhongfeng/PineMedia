package com.pine.pictureviewer.remote.server;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.pine.base.router.command.RouterPictureViewerCommand;
import com.pine.pictureviewer.ui.activity.PictureViewerHomeActivity;
import com.pine.tool.router.IServiceCallback;
import com.pine.tool.router.annotation.RouterCommand;

/**
 * Created by tanghongfeng on 2018/9/13
 */

public class PictureViewerRemoteService {

    @RouterCommand(CommandName = RouterPictureViewerCommand.goPictureViewerHomeActivity)
    public void goPictureViewerHomeActivity(@NonNull Context context, Bundle args, @NonNull final IServiceCallback callback) {
        Bundle responseBundle = new Bundle();
        Intent intent = new Intent(context, PictureViewerHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        callback.onResponse(responseBundle);
    }
}
