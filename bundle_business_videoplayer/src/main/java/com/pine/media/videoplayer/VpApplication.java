package com.pine.media.videoplayer;

import com.pine.media.base.BaseApplication;
import com.pine.tool.util.LogUtils;
import com.pine.tool.util.SharePreferenceUtils;
import com.pine.media.videoplayer.db.entity.VpSheet;
import com.pine.media.videoplayer.db.repository.VpSheetRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by tanghongfeng on 2018/9/14
 */

public class VpApplication extends BaseApplication {
    private final static String TAG = LogUtils.makeLogTag(VpApplication.class);

    public static void attach() {
        if (!SharePreferenceUtils.readBooleanFromConfig("ap_database_init", false)) {
            List<VpSheet> list = new ArrayList<>();
            VpSheet tmpPlaySheet = new VpSheet();
            tmpPlaySheet.setSheetType(VpConstants.VIDEO_SHEET_TYPE_PLAY_LIST);
            tmpPlaySheet.setName(mApplication.getString(R.string.vp_home_play_list_name));
            tmpPlaySheet.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            tmpPlaySheet.setCreateTimeStamp(Calendar.getInstance().getTimeInMillis());
            list.add(tmpPlaySheet);
            VpSheetRepository.getInstance(mApplication).addVideoSheetList(list);
            SharePreferenceUtils.saveToConfig("ap_database_init", true);
        }
    }
}
