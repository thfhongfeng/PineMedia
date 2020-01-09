package com.pine.welcome.ui.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.pine.base.architecture.mvvm.ui.activity.BaseMvvmNoActionBarActivity;
import com.pine.base.util.DialogUtils;
import com.pine.base.widget.dialog.ProgressDialog;
import com.pine.config.ConfigKey;
import com.pine.config.switcher.ConfigSwitcherServer;
import com.pine.tool.permission.PermissionsAnnotation;
import com.pine.tool.router.IRouterCallback;
import com.pine.tool.util.SharePreferenceUtils;
import com.pine.welcome.R;
import com.pine.welcome.WelcomeApplication;
import com.pine.welcome.WelcomeSPKeyConstants;
import com.pine.welcome.databinding.LoadingActivityBinding;
import com.pine.welcome.remote.WelcomeRouterClient;
import com.pine.welcome.vm.LoadingVm;

@PermissionsAnnotation(Permissions = {Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE})
public class LoadingActivity extends BaseMvvmNoActionBarActivity<LoadingActivityBinding, LoadingVm> {
    private final static int REQUEST_CODE_USER_PRIVACY = 9999;
    private final static int LOADING_STAY_MIN_TIME = 1000;
    private final static int GO_NEXT_DELAY = 100;
    private long mStartTimeMillis;
    private Dialog mUpdateConfirmDialog;
    private ProgressDialog mUpdateProgressDialog;

    @Override
    protected boolean beforeInitOnCreate(@Nullable Bundle savedInstanceState) {
        super.beforeInitOnCreate(savedInstanceState);
        return !isTaskRoot();
    }

    @Override
    public void observeInitLiveData(Bundle savedInstanceState) {
        mViewModel.getNewVersionNameData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String newVersionName) {
                if (TextUtils.isEmpty(newVersionName)) {
                    autoLogin(-1);
                } else {
                    showVersionUpdateConfirmDialog(newVersionName);
                }
            }
        });
        mViewModel.getVersionUpdateForceData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isForce) {
                showVersionUpdateProgressDialog(isForce);
            }
        });
        mViewModel.getVersionUpdateProgressData().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer progress) {
                updateVersionUpdateProgressDialog(progress);
            }
        });
        mViewModel.getVersionUpdateStateData().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer state) {
                onVersionUpdateStateChange(state);
            }
        });
    }

    @Override
    protected int getActivityLayoutResId() {
        return R.layout.wel_activity_loading;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        if (!SharePreferenceUtils.readBooleanFromConfig(WelcomeSPKeyConstants.USER_PRIVACY_AGREE, false)) {
            startActivityForResult(new Intent(this, UserPrivacyActivity.class), REQUEST_CODE_USER_PRIVACY);
        } else {
            doneAppStartTask();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_USER_PRIVACY == requestCode && resultCode == RESULT_OK) {
            doneAppStartTask();
        } else {
            finish();
        }
    }

    private void doneAppStartTask() {
        mStartTimeMillis = System.currentTimeMillis();
//        mViewModel.setupConfigSwitcher();
        autoLogin(GO_NEXT_DELAY);
    }

    @Override
    protected void onDestroy() {
        if (mUpdateConfirmDialog != null) {
            mUpdateConfirmDialog.dismiss();
            mUpdateConfirmDialog = null;
        }
        if (mUpdateProgressDialog != null) {
            mUpdateProgressDialog.dismiss();
            mUpdateProgressDialog = null;
        }
        super.onDestroy();
    }

    @Override
    public void observeSyncLiveData(int liveDataObjTag) {

    }

    private void showVersionUpdateConfirmDialog(@NonNull String newVersionName) {
        if (mUpdateConfirmDialog == null) {
            mUpdateConfirmDialog = new Dialog(this);
            mUpdateConfirmDialog.setContentView(R.layout.wel_dialog_version_update_confirm);
            mUpdateConfirmDialog.setCanceledOnTouchOutside(false);
            mUpdateConfirmDialog.setCancelable(false);
            mUpdateConfirmDialog.setOwnerActivity(this);
            ((TextView) mUpdateConfirmDialog.findViewById(R.id.reason_tv)).setText(String.format(getString(R.string.wel_new_version_available), newVersionName));
            mUpdateConfirmDialog.findViewById(R.id.cancel_btn_tv).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mUpdateConfirmDialog.dismiss();
                    autoLogin(-1);
                }
            });
            mUpdateConfirmDialog.findViewById(R.id.confirm_ll).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mUpdateConfirmDialog.dismiss();
                    mViewModel.updateVersion(false);
                }
            });
        }
        mUpdateConfirmDialog.show();
        CountDownTimer countDownTimer = new CountDownTimer(900000, 1000) {

            @Override
            public void onFinish() {
                if (mUpdateConfirmDialog != null && WelcomeApplication.mCurResumedActivity.getClass() == LoadingActivity.class &&
                        mUpdateConfirmDialog.isShowing()) {
                    mUpdateConfirmDialog.findViewById(R.id.confirm_ll).performClick();
                    mUpdateConfirmDialog.findViewById(R.id.count_time_tv).setVisibility(View.GONE);
                }
            }

            @Override
            public void onTick(long millisUntilFinished) {
                if (mUpdateConfirmDialog != null && WelcomeApplication.mCurResumedActivity.getClass() == LoadingActivity.class &&
                        mUpdateConfirmDialog.isShowing()) {
                    ((TextView) mUpdateConfirmDialog.findViewById(R.id.count_time_tv)).setText("(" + millisUntilFinished / 1000 + ")");
                }
            }
        };
        countDownTimer.start();
    }

    private void showVersionUpdateProgressDialog(boolean isForce) {
        if (mUpdateProgressDialog != null) {
            mUpdateProgressDialog.dismiss();
        }
        mUpdateProgressDialog = DialogUtils.createDownloadProgressDialog(this,
                0, isForce ? null : new ProgressDialog.IDialogActionListener() {
                    @Override
                    public void onCancel() {
                        mViewModel.cancelDownLoad();
                    }
                });
        mUpdateProgressDialog.show();
    }

    private void updateVersionUpdateProgressDialog(int progress) {
        if (mUpdateProgressDialog != null) {
            mUpdateProgressDialog.setProgress(progress);
        }
    }

    private void onVersionUpdateStateChange(int state) {
        if (mUpdateProgressDialog != null) {
            mUpdateProgressDialog.dismiss();
        }
        boolean isForce = mViewModel.getVersionUpdateStateData().getCustomData();
        if (state > 0) {
            finish();
        } else if (state == 0) {
            if (isForce) {
                finish();
            } else {
                autoLogin(-1);
            }
        } else {
            if (isForce) {
                finish();
            } else {
                autoLogin(GO_NEXT_DELAY);
            }
        }
    }

    public void autoLogin(final int delayTogo) {
        if (!ConfigSwitcherServer.getInstance().isEnable(ConfigKey.BUNDLE_LOGIN_KEY) ||
                WelcomeApplication.isLogin()) {
            gotoNext(delayTogo);
            return;
        }
        WelcomeRouterClient.autoLogin(this, null, new IRouterCallback() {
            @Override
            public void onSuccess(Bundle responseBundle) {
                gotoNext(delayTogo);
            }

            @Override
            public boolean onFail(int failCode, String errorInfo) {
                gotoNext(delayTogo);
                return true;
            }
        });
    }

    private void gotoNext(int delayTogo) {
        long delay = delayTogo;
        if (delayTogo <= 0) {
            delay = LOADING_STAY_MIN_TIME - (System.currentTimeMillis() - mStartTimeMillis);
            delay = delay > 0 ? delay : 0;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LoadingActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        }, delay);
    }
}
