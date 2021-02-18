package com.pine.media.audioplayer.remote.server;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.pine.media.audioplayer.ApConstants;
import com.pine.media.audioplayer.ui.activity.ApHomeActivity;
import com.pine.media.audioplayer.ui.activity.ApMainActivity;
import com.pine.media.base.router.command.RouterAudioPlayerCommand;
import com.pine.tool.router.IServiceCallback;
import com.pine.tool.router.annotation.RouterCommand;
import com.pine.tool.ui.Activity;

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

    @RouterCommand(CommandName = RouterAudioPlayerCommand.goAudioPlayerMainActivity)
    public void goAudioPlayerMainActivity(@NonNull Context context, Bundle args, @NonNull final IServiceCallback callback) {
        Bundle responseBundle = new Bundle();
        Intent intent = new Intent(context, ApMainActivity.class);
        Intent startupIntent = args.getParcelable(ApConstants.STARTUP_INTENT);
        Object requestCode = args.get(ApConstants.REQUEST_CODE);
        if (startupIntent == null || requestCode == null) {
            return;
        }
        intent.putExtra("data", startupIntent.getData());
        intent.putExtra("playing", true);
        if (context instanceof Activity && requestCode != null) {
            ((Activity) context).startActivityForResult(intent, args.getInt(ApConstants.REQUEST_CODE));
        } else {
            context.startActivity(intent);
        }
        callback.onResponse(responseBundle);
    }
}
