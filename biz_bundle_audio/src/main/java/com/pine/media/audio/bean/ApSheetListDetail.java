package com.pine.media.audio.bean;

import com.pine.media.audio.db.entity.ApSheet;

import java.util.List;

public class ApSheetListDetail {
    private ApSheet allMusicSheet;
    private ApSheet favouriteSheet;
    private List<ApSheet> customSheetList;

    public ApSheet getAllMusicSheet() {
        return allMusicSheet;
    }

    public void setAllMusicSheet(ApSheet allMusicSheet) {
        this.allMusicSheet = allMusicSheet;
    }

    public ApSheet getFavouriteSheet() {
        return favouriteSheet;
    }

    public void setFavouriteSheet(ApSheet favouriteSheet) {
        this.favouriteSheet = favouriteSheet;
    }

    public List<ApSheet> getCustomSheetList() {
        return customSheetList;
    }

    public void setCustomSheetList(List<ApSheet> customSheetList) {
        this.customSheetList = customSheetList;
    }
}
