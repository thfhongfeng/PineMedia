package com.pine.audioplayer;

import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.audioplayer.db.repository.ApMusicSheetRepository;
import com.pine.base.BaseApplication;
import com.pine.tool.util.LogUtils;
import com.pine.tool.util.SharePreferenceUtils;

import java.util.ArrayList;
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
            favouriteSheet.setSheetId(ApConstants.MUSIC_FAVOURITE_SHEET_ID);
            favouriteSheet.setName(mApplication.getString(R.string.ap_home_my_favourite_name));
            list.add(favouriteSheet);
            ApMusicSheet recentSheet = new ApMusicSheet();
            recentSheet.setSheetId(ApConstants.MUSIC_RECENT_SHEET_ID);
            recentSheet.setName(mApplication.getString(R.string.ap_home_recent_music_name));
            list.add(recentSheet);
            ApMusicSheetRepository.getInstance(mApplication).addMusicSheetList(list);
            SharePreferenceUtils.saveToConfig("ap_database_init", true);
        }
    }
}
