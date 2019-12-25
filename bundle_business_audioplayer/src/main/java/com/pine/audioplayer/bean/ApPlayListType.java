package com.pine.audioplayer.bean;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;

import com.pine.audioplayer.R;

import java.util.ArrayList;
import java.util.List;

public class ApPlayListType {
    public static final int TYPE_ORDER = 1;
    public static final int TYPE_ALL_LOOP = 2;
    public static final int TYPE_SING_LOOP = 3;
    public static final int TYPE_RANDOM = 4;

    private int type;
    private String name;
    @IdRes
    private int imageResId;

    public ApPlayListType(int type, String name, @DrawableRes int imageResId) {
        this.type = type;
        this.name = name;
        this.imageResId = imageResId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

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

    public static List<ApPlayListType> getDefaultList(Context context) {
        List<ApPlayListType> playTypeList = new ArrayList<>();
        ApPlayListType playType = new ApPlayListType(ApPlayListType.TYPE_ALL_LOOP,
                context.getString(R.string.ap_sad_play_loop_all), R.mipmap.res_ic_play_all_loop);
        playTypeList.add(playType);
        playType = new ApPlayListType(ApPlayListType.TYPE_SING_LOOP,
                context.getString(R.string.ap_sad_play_single_loop), R.mipmap.res_ic_play_single_loop);
        playTypeList.add(playType);
        playType = new ApPlayListType(ApPlayListType.TYPE_ORDER,
                context.getString(R.string.ap_sad_play_ord), R.mipmap.res_ic_play_order);
        playTypeList.add(playType);
        playType = new ApPlayListType(ApPlayListType.TYPE_RANDOM,
                context.getString(R.string.ap_sad_play_loop_all), R.mipmap.res_ic_play_random);
        playTypeList.add(playType);
        return playTypeList;
    }
}
