package com.pine.media.audio.remote.server;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.pine.app.template.biz_bundle_audio.router.RouterAudioCommand;
import com.pine.media.audio.ApApplication;
import com.pine.media.audio.ApKeyConstants;
import com.pine.media.audio.ui.activity.ApHomeActivity;
import com.pine.media.audio.ui.activity.ApMainActivity;
import com.pine.tool.router.IServiceCallback;
import com.pine.tool.router.annotation.RouterCommand;
import com.pine.tool.ui.Activity;

/**
 * Created by tanghongfeng on 2018/9/13
 */

public class AudioRemoteService {
    @RouterCommand(CommandName = "onAppCreate")
    public void onAppCreate(@NonNull Context context, Bundle args) {
        ApApplication.onCreate();
    }

    @RouterCommand(CommandName = "onAppAttach")
    public void onAppAttach(@NonNull Context context, Bundle args) {
        ApApplication.attach();
    }

    @RouterCommand(CommandName = RouterAudioCommand.goAudioHomeActivity)
    public void goAudioHomeActivity(@NonNull Context context, Bundle args, @NonNull final IServiceCallback callback) {
        Bundle responseBundle = new Bundle();
        context.startActivity(new Intent(context, ApHomeActivity.class));
        callback.onResponse(responseBundle);
    }

    @RouterCommand(CommandName = RouterAudioCommand.goAudioPlayerMainActivity)
    public void goAudioPlayerMainActivity(@NonNull Context context, Bundle args, @NonNull final IServiceCallback callback) {
        Bundle responseBundle = new Bundle();
        Intent intent = new Intent(context, ApMainActivity.class);
        Intent startupIntent = args.getParcelable(ApKeyConstants.STARTUP_INTENT);
        Object requestCode = args.get(ApKeyConstants.REQUEST_CODE);
        if (startupIntent == null || requestCode == null) {
            return;
        }
        intent.putExtra("isStartupMode", true);
        intent.putExtra("data", startupIntent.getData());
        intent.putExtra("playing", true);
        if (context instanceof Activity && requestCode != null) {
            ((Activity) context).startActivityForResult(intent, args.getInt(ApKeyConstants.REQUEST_CODE));
        } else {
            context.startActivity(intent);
        }
        callback.onResponse(responseBundle);
    }
}
