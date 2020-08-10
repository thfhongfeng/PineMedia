package com.pine.audioplayer;

import com.pine.audioplayer.db.entity.ApSheet;
import com.pine.audioplayer.db.repository.ApSheetRepository;
import com.pine.base.BaseApplication;
import com.pine.tool.util.LogUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by tanghongfeng on 2018/9/14
 */

public class ApApplication extends BaseApplication {
    private final static String TAG = LogUtils.makeLogTag(ApApplication.class);

    public static void attach() {
        addInitSheet();
    }

    private static void addInitSheet() {
        ApSheet recentSheet = ApSheetRepository.getInstance(mApplication).querySheetByType(ApConstants.MUSIC_SHEET_TYPE_RECENT);
        ApSheet tmpPlaySheet = ApSheetRepository.getInstance(mApplication).querySheetByType(ApConstants.MUSIC_SHEET_TYPE_PLAY_LIST);
        List<ApSheet> list = new ArrayList<>();
        if (recentSheet == null) {
            recentSheet = new ApSheet();
            recentSheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_RECENT);
            recentSheet.setName(mApplication.getString(R.string.ap_home_recent_music_name));
            recentSheet.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            recentSheet.setCreateTimeStamp(Calendar.getInstance().getTimeInMillis());
            list.add(recentSheet);
        }
        if (tmpPlaySheet == null) {
            tmpPlaySheet = new ApSheet();
            tmpPlaySheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_PLAY_LIST);
            tmpPlaySheet.setName(mApplication.getString(R.string.ap_home_play_list_name));
            tmpPlaySheet.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            tmpPlaySheet.setCreateTimeStamp(Calendar.getInstance().getTimeInMillis());
            list.add(tmpPlaySheet);
        }
        if (list.size() > 0) {
            ApSheetRepository.getInstance(mApplication).addMusicSheetList(list);
        }
    }
}
