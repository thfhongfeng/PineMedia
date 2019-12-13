package com.pine.videoplayer.bean;

import com.pine.tool.util.FileUtils;

public class VpChooseFileBean {
    public final static int TYPE_UNKNOWN = 0;
    public final static int TYPE_FOLDER = 1;
    public final static int TYPE_FILE = 2;

    // 类别：0-未知；1-文件夹；2-文件
    private int type;
    private String filePath;
    private String fileName;

    public VpChooseFileBean(int type, String filePath) {
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
}
