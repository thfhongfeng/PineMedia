package com.pine.videoplayer.bean;

import android.content.Context;

import com.pine.videoplayer.R;

import java.util.ArrayList;
import java.util.List;

public class VpPlayListType {
    public static final int TYPE_ORDER = 1;
    public static final int TYPE_ALL_LOOP = 2;
    public static final int TYPE_SING_LOOP = 3;
    public static final int TYPE_RANDOM = 4;

    private int type;
    private String name;

    public VpPlayListType(int type, String name) {
        this.type = type;
        this.name = name;
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

    public static List<VpPlayListType> getDefaultList(Context context) {
        List<VpPlayListType> playTypeList = new ArrayList<>();
        VpPlayListType playType = new VpPlayListType(VpPlayListType.TYPE_ALL_LOOP,
                context.getString(R.string.vp_play_loop_all));
        playTypeList.add(playType);
        playType = new VpPlayListType(VpPlayListType.TYPE_SING_LOOP,
                context.getString(R.string.vp_play_single_loop));
        playTypeList.add(playType);
        playType = new VpPlayListType(VpPlayListType.TYPE_ORDER,
                context.getString(R.string.vp_play_ord));
        playTypeList.add(playType);
        playType = new VpPlayListType(VpPlayListType.TYPE_RANDOM,
                context.getString(R.string.vp_play_random));
        playTypeList.add(playType);
        return playTypeList;
    }
}
