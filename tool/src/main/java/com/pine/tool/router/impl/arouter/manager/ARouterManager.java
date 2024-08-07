package com.pine.tool.router.impl.arouter.manager;

import static com.pine.tool.router.RouterCommandType.TYPE_DATA_COMMAND;
import static com.pine.tool.router.RouterCommandType.TYPE_OP_COMMAND;
import static com.pine.tool.router.RouterCommandType.TYPE_UI_COMMAND;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.callback.NavigationCallback;
import com.alibaba.android.arouter.launcher.ARouter;
import com.pine.tool.R;
import com.pine.tool.router.IRouterCallback;
import com.pine.tool.router.IRouterManager;
import com.pine.tool.router.RouterException;
import com.pine.tool.router.RouterManager;
import com.pine.tool.router.annotation.ARouterRemoteAction;
import com.pine.tool.router.impl.arouter.ARouterBundleRemote;
import com.pine.tool.util.AppUtils;
import com.pine.tool.util.LogUtils;

import java.util.HashMap;
import java.util.List;

/**
 * Created by tanghongfeng on 2019/2/21
 */

public class ARouterManager implements IRouterManager {
    private final String TAG = LogUtils.makeLogTag(this.getClass());

    private static HashMap<String, String> mBundleActionMap = new HashMap<>();
    private static volatile ARouterManager mInstance;

    private ARouterManager() {

    }

    public synchronized static ARouterManager getInstance() {
        if (mInstance == null) {
            mInstance = new ARouterManager();
        }
        return mInstance;
    }

    @Override
    public void init(Application application, List<String> commandClassNameList) {
        if (AppUtils.isApkDebuggable(AppUtils.getApplication())) {
            ARouter.openLog();
            ARouter.openDebug();
        }
        ARouter.init(AppUtils.getApplication());

        for (int i = 0; i < commandClassNameList.size(); i++) {
            try {
                Class<?> clazz = Class.forName(commandClassNameList.get(i));
                ARouterRemoteAction routerRemoteAction = clazz.getAnnotation(ARouterRemoteAction.class);
                if (routerRemoteAction != null) {
                    mBundleActionMap.put(routerRemoteAction.Key(), routerRemoteAction.RemoteAction());
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        LogUtils.d(TAG, "init bundle action map:" + mBundleActionMap);
    }

    @Override
    public void init(Application application, HashMap<String, String> bundlePathMap) {
        if (AppUtils.isApkDebuggable(AppUtils.getApplication())) {
            ARouter.openLog();
            ARouter.openDebug();
        }
        ARouter.init(AppUtils.getApplication());
        mBundleActionMap = bundlePathMap;
        LogUtils.d(TAG, "init bundle action map:" + mBundleActionMap);
    }

    @Override
    public void callCommand(final Context context, final String bundleKey, final String commandType, String commandName,
                            Bundle args, final IRouterCallback callback) {
        if (!checkBundleValidity(bundleKey, commandType, context, callback)) {
            return;
        }
        ARouterBundleRemote routerService = ((ARouterBundleRemote) ARouter.getInstance().build(mBundleActionMap.get(bundleKey))
                .navigation(context, new NavigationCallback() {
                    @Override
                    public void onFound(Postcard postcard) {
                        LogUtils.d(TAG, "callOpCommand path:'" + postcard.getPath() + "'onFound");
                    }

                    @Override
                    public void onLost(Postcard postcard) {
                        LogUtils.d(TAG, "callOpCommand path:'" + postcard.getPath() + "'onLost");
                        if (callback != null && !callback.onFail(IRouterManager.FAIL_CODE_LOST, "onLost")) {
                            onCommandFail(commandType, context, IRouterManager.FAIL_CODE_LOST, "onLost");
                        }
                    }

                    @Override
                    public void onArrival(Postcard postcard) {
                        LogUtils.d(TAG, "callOpCommand path:'" + postcard.getPath() + "'onArrival");
                    }

                    @Override
                    public void onInterrupt(Postcard postcard) {
                        LogUtils.d(TAG, "callOpCommand path:'" + postcard.getPath() + "'onInterrupt");
                        if (callback != null && !callback.onFail(IRouterManager.FAIL_CODE_INTERRUPT, "onInterrupt")) {
                            onCommandFail(commandType, context, IRouterManager.FAIL_CODE_INTERRUPT, "onInterrupt");
                        }
                    }
                }));
        if (routerService != null) {
            routerService.call(context, commandName, args, callback);
        }
    }

    @Override
    public <R> R callCommandDirect(final Context context, final String bundleKey, final String commandType,
                                   String commandName, Bundle args) throws RouterException {
        if (!checkBundleValidity(bundleKey, commandType, context, null)) {
            throw new RouterException("bundle no valid");
        }
        ARouterBundleRemote routerService = ((ARouterBundleRemote) ARouter.getInstance().build(mBundleActionMap.get(bundleKey))
                .navigation(context, null));
        if (routerService != null) {
            return (R) routerService.callDirect(context, commandName, args);
        }
        throw new RouterException("bundle no valid");
    }

    private boolean checkBundleValidity(final String bundleKey, final String commandType, final Context context,
                                        final IRouterCallback callback) {
        if (TextUtils.isEmpty(mBundleActionMap.get(bundleKey))) {
            LogUtils.releaseLog(TAG, "remote action is null");
            if (callback != null && !callback.onFail(IRouterManager.FAIL_CODE_INVALID,
                    context.getString(R.string.tool_remote_action_empty))) {
                onCommandFail(commandType, context, IRouterManager.FAIL_CODE_INVALID,
                        context.getString(R.string.tool_remote_action_empty));
            }
            return false;
        }
        if (!RouterManager.isBundleEnable(bundleKey)) {
            LogUtils.releaseLog(TAG, bundleKey + " is not opened");
            if (callback != null && !callback.onFail(IRouterManager.FAIL_CODE_INVALID,
                    context.getString(R.string.tool_bundle_not_open))) {
                onCommandFail(commandType, context, IRouterManager.FAIL_CODE_INVALID,
                        context.getString(R.string.tool_bundle_not_open));
            }
            return false;
        }
        return true;
    }

    private void onCommandFail(String commandType, Context context, int failCode, String message) {
        switch (commandType) {
            case TYPE_UI_COMMAND:
                if (!TextUtils.isEmpty(message)) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
                break;
            case TYPE_DATA_COMMAND:
                if (!TextUtils.isEmpty(message)) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
                break;
            case TYPE_OP_COMMAND:
                if (!TextUtils.isEmpty(message)) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
