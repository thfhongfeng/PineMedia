package com.pine.media.audio.bean;

import com.pine.media.audio.db.entity.ApMusic;
import com.pine.media.audio.db.entity.ApSheet;

import java.util.List;

public class ApSheetMusicDetail {
    private ApSheet sheet;
    private List<ApMusic> musicList;

    public ApSheet getSheet() {
        return sheet;
    }

    public void setSheet(ApSheet sheet) {
        this.sheet = sheet;
    }

    public List<ApMusic> getMusicList() {
        return musicList;
    }

    public void setMusicList(List<ApMusic> musicList) {
        this.musicList = musicList;
    }
}
