package com.pine.media.main.bean;

import androidx.annotation.IdRes;

/**
 * Created by tanghongfeng on 2018/9/13
 */

public class MainBizItemEntity {

    /**
     * name : Business
     * imageResId : R.mipmap.xxx
     * bundle : business_bundle
     * command : goBizAHomeActivity
     */

    private String name;
    @IdRes
    private int imageResId;
    private String bundle;
    private String command;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public String getBundle() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return "MainBizItemEntity{" +
                "name='" + name + '\'' +
                ", imageResId=" + imageResId +
                ", bundle='" + bundle + '\'' +
                ", command='" + command + '\'' +
                '}';
    }
}
