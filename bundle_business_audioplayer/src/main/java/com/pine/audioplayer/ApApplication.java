package com.pine.audioplayer;

import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.repository.ApMusicSheetRepository;
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
            List<ApMusicSheet> list = new ArrayList<>();
            ApMusicSheet favouriteSheet = new ApMusicSheet();
            favouriteSheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_FAVOURITE);
            favouriteSheet.setName(mApplication.getString(R.string.ap_home_my_favourite_name));
            favouriteSheet.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            favouriteSheet.setCreateTimeStamp(Calendar.getInstance().getTimeInMillis());
            list.add(favouriteSheet);
            ApMusicSheet recentSheet = new ApMusicSheet();
            recentSheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_RECENT);
            recentSheet.setName(mApplication.getString(R.string.ap_home_recent_music_name));
            recentSheet.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            recentSheet.setCreateTimeStamp(Calendar.getInstance().getTimeInMillis());
            list.add(recentSheet);
            ApMusicSheet tmpPlaySheet = new ApMusicSheet();
            tmpPlaySheet.setSheetType(ApConstants.MUSIC_SHEET_TYPE_TMP_PLAY);
            tmpPlaySheet.setName(mApplication.getString(R.string.ap_home_tmp_play_name));
            recentSheet.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            recentSheet.setCreateTimeStamp(Calendar.getInstance().getTimeInMillis());
            list.add(tmpPlaySheet);
            ApMusicSheetRepository.getInstance(mApplication).addMusicSheetList(list);
            SharePreferenceUtils.saveToConfig("ap_database_init", true);
        }
    }
}
