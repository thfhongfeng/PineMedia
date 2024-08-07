package com.pine.template.login.vm;

import android.content.Context;
import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;

import com.pine.template.login.LoginKeyConstants;
import com.pine.template.login.R;
import com.pine.template.login.bean.LoginBean;
import com.pine.template.login.manager.LoginManager;
import com.pine.template.login.model.ILoginResponse;
import com.pine.tool.architecture.mvvm.vm.ViewModel;
import com.pine.tool.util.RegexUtils;
import com.pine.tool.util.SharePreferenceUtils;

public class LoginVm extends ViewModel {
    @Override
    public void afterViewInit(Context activity) {
        LoginBean loginBean = new LoginBean();
        String account = SharePreferenceUtils.readStringFromCache(LoginKeyConstants.ACCOUNT_ACCOUNT, "");
        String password = SharePreferenceUtils.readStringFromCache(LoginKeyConstants.ACCOUNT_PASSWORD, "");
        loginBean.setMobile(account);
        loginBean.setPassword(password);
        setLoginBean(loginBean);
    }

    public void login() {
        if (isUiLoading()) {
            return;
        }
        LoginBean loginBean = loginBeanData.getValue();
        if (TextUtils.isEmpty(loginBean.getMobile()) || TextUtils.isEmpty(loginBean.getPassword())) {
            setToastResId(R.string.login_input_empty_msg);
            return;
        }
        if (!RegexUtils.isMobilePhoneNumber(loginBean.getMobile())) {
            setToastResId(R.string.login_mobile_incorrect_format);
            return;
        }
        setUiLoading(true);
        LoginManager.login(loginBean.getMobile(), loginBean.getPassword(), new ILoginResponse() {
            @Override
            public boolean onLoginResponse(boolean isSuccess, String msg) {
                setUiLoading(false);
                if (!isSuccess) {
                    if (TextUtils.isEmpty(msg)) {
                        return false;
                    } else {
                        setToastMsg(msg);
                    }
                } else {
                    finishUi();
                }
                return true;
            }

            @Override
            public void onCancel() {
                setUiLoading(false);
            }
        });
    }

    private MutableLiveData<LoginBean> loginBeanData = new MutableLiveData<>();

    public MutableLiveData<LoginBean> getLoginBeanData() {
        return loginBeanData;
    }

    public void setLoginBean(LoginBean loginBean) {
        loginBeanData.setValue(loginBean);
    }
}
