package com.pine.media.video.bean;

import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.tool.util.FileUtils;

public class VpFileBean {
    public final static int TYPE_UNKNOWN = 0;
    public final static int TYPE_FOLDER = 1;
    public final static int TYPE_FILE = 2;

    // 类别：0-未知；1-文件夹；2-文件
    private int type;
    private String filePath;
    private String fileName;
    private String mediaCode;
    private PineMediaPlayerBean mediaPlayerBean;

    public VpFileBean(int type, String filePath) {
        this.type = type;
        this.filePath = filePath;
        this.fileName = FileUtils.getFileName(filePath);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
        this.fileName = FileUtils.getFileName(filePath);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMediaCode() {
        return mediaCode;
    }

    public void setMediaCode(String mediaCode) {
        this.mediaCode = mediaCode;
    }

    public PineMediaPlayerBean getMediaPlayerBean() {
        return mediaPlayerBean;
    }

    public void setMediaPlayerBean(PineMediaPlayerBean mediaPlayerBean) {
        this.mediaPlayerBean = mediaPlayerBean;
    }
}
