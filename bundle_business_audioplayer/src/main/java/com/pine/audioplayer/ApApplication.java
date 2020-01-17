package com.pine.audioplayer;

import com.pine.audioplayer.db.entity.ApSheet;
import com.pine.audioplayer.db.repository.ApSheetRepository;
import com.pine.base.BaseApplication;
import com.pine.tool.util.LogUtils;
import com.pine.tool.util.SharePreferenceUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by tanghongfeng on 2018/9/14
 */

public class ApApplication extends BaseApplication {
    private final static String TAG = LogUtils.makeLogTag(ApApplication.class);

    public static void attach() {
        if (!SharePreferenceUtils.readBooleanFromConfig("ap_database_init", false)) {
            List<ApSheet> list = new ArrayList<>();
            ApSheet recentSheet = new ApSheet();
            recentSheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_RECENT);
            recentSheet.setName(mApplication.getString(R.string.ap_home_recent_music_name));
            recentSheet.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            recentSheet.setCreateTimeStamp(Calendar.getInstance().getTimeInMillis());
            list.add(recentSheet);
            ApSheet tmpPlaySheet = new ApSheet();
            tmpPlaySheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_PLAY_LIST);
            tmpPlaySheet.setName(mApplication.getString(R.string.ap_home_play_list_name));
            tmpPlaySheet.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            tmpPlaySheet.setCreateTimeStamp(Calendar.getInstance().getTimeInMillis());
            list.add(tmpPlaySheet);
            ApSheetRepository.getInstance(mApplication).addMusicSheetList(list);
            SharePreferenceUtils.saveToConfig("ap_database_init", true);
        }
    }
}
