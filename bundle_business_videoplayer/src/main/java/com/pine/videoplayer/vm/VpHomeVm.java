package com.pine.videoplayer.vm;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.applet.subtitle.plugin.PineLrcParserPlugin;
import com.pine.player.applet.subtitle.plugin.PineSrtParserPlugin;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.tool.architecture.mvvm.vm.ViewModel;
import com.pine.tool.binding.data.ParametricLiveData;
import com.pine.tool.util.FileUtils;
import com.pine.tool.util.MediaFileUtils;
import com.pine.tool.util.SharePreferenceUtils;
import com.pine.videoplayer.VpConstants;
import com.pine.videoplayer.bean.VpFileBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

public class VpHomeVm extends ViewModel {
    private final String LRC_SUFFIX = "lrc";
    private final String SRT_SUFFIX = "srt";

    private Set<String> mAllPlayableFileSet = new HashSet<>();
    private HashMap<String, HashSet<String>> mChosenFileMap = new HashMap<>();
    private HashMap<String, String> mChosenLrcFileSubtitleMap = new HashMap<>();
    private HashMap<String, String> mChosenSrtFileSubtitleMap = new HashMap<>();
    private HashMap<String, Uri> mChosenMediaImageMap = new HashMap<>();
    public ParametricLiveData<ArrayList<VpFileBean>, String> mFileListData = new ParametricLiveData<>();
    public ParametricLiveData<ArrayList<PineMediaPlayerBean>, String> mMediaListData = new ParametricLiveData<>();

    @Override
    public void afterViewInit(Context activity) {
        super.afterViewInit(activity);
        mAllPlayableFileSet = SharePreferenceUtils.readStringSetFromCache("recentAllPlayableFileSet", null);
        if (mAllPlayableFileSet != null && mAllPlayableFileSet.size() > 0) {
            setupAddedMediaData(activity, mAllPlayableFileSet, null);
        } else {
            mAllPlayableFileSet = new HashSet<>();
        }
    }

    public void deleteMedia(VpFileBean fileBean) {
        ArrayList<VpFileBean> fileList = mFileListData.getValue();
        ArrayList<PineMediaPlayerBean> mediaList = mMediaListData.getValue();

        fileList.remove(fileBean);
        int pos = 0;
        while (mediaList.size() > pos && !mediaList.get(pos).getMediaCode().equals(fileBean.getMediaCode())) {
            pos++;
        }
        mediaList.remove(pos);
        String nextMediaCode = mediaList.get(pos % mediaList.size()).getMediaCode();
        mFileListData.setValue(fileList, nextMediaCode);
        mMediaListData.setValue(mediaList, nextMediaCode);
    }

    public void onFileChosen(Context context, @NonNull Intent data) {
        List<String> chooseFilePathList = new ArrayList<>();
        if (data.getData() != null) {
            // 单次点击未使用多选的情况
            try {
                chooseFilePathList.add(addToChooseFileMap(context, data.getData()));
            } catch (Exception e) {
            }
        } else {
            // 长按使用多选的情况
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    chooseFilePathList.add(addToChooseFileMap(context, item.getUri()));
                }
            }
        }
        if (chooseFilePathList.size() < 1) {
            return;
        }
        HashSet<String> similarFileSet = new HashSet<>();
        String[] suffixList = {"", LRC_SUFFIX, SRT_SUFFIX, "jpg", "png"};
        for (String chooseFilePath : chooseFilePathList) {
            String similarStr = Pattern.compile("[\\d]").matcher(FileUtils.getFileNameWithoutExtension(chooseFilePath)).replaceAll("");
            suffixList[0] = (FileUtils.getFileExtension(chooseFilePath));
            similarFileSet.addAll(FileUtils.getFileList(FileUtils.getFolderName(chooseFilePath), similarStr, suffixList));
        }
        mAllPlayableFileSet.addAll(similarFileSet);
        setupAddedMediaData(context, similarFileSet, chooseFilePathList.get(0));
        SharePreferenceUtils.saveToCache("recentAllPlayableFileSet", new HashSet<String>(mAllPlayableFileSet));
    }

    private void setupAddedMediaData(Context context, Set<String> similarFileList, String willPlayFilePath) {
        String[] similarFiles = similarFileList.toArray(new String[0]);
        for (int i = 0; i < similarFiles.length; i++) {
            String filePath = similarFiles[i];
            if (filePath.endsWith("." + LRC_SUFFIX)) {
                mChosenLrcFileSubtitleMap.put(FileUtils.getFileNameWithoutExtension(filePath), filePath);
            } else if (filePath.endsWith("." + SRT_SUFFIX)) {
                mChosenSrtFileSubtitleMap.put(FileUtils.getFileNameWithoutExtension(filePath), filePath);
            } else if (filePath.endsWith(".jpg") || filePath.endsWith(".png")) {
                mChosenMediaImageMap.put(FileUtils.getFileNameWithoutExtension(filePath), Uri.parse(filePath));
            } else {
                addToChooseFileMap(filePath);
            }
        }
        ArrayList<VpFileBean> fileList = new ArrayList<>();
        ArrayList<PineMediaPlayerBean> mediaList = new ArrayList<>();
        String playMediaCode = "";
        Iterator<HashMap.Entry<String, HashSet<String>>> iterator = mChosenFileMap.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry<String, HashSet<String>> entry = iterator.next();
            VpFileBean folder = new VpFileBean(VpFileBean.TYPE_FOLDER, entry.getKey());
            fileList.add(folder);
            String[] filePaths = entry.getValue().toArray(new String[0]);
            for (String filePath : filePaths) {
                VpFileBean file = new VpFileBean(VpFileBean.TYPE_FILE, filePath);
                HashMap<Integer, IPinePlayerPlugin> playerPluginMap = new HashMap<>();
                String fileNameWithoutExtension = FileUtils.getFileNameWithoutExtension(filePath);
                if (mChosenLrcFileSubtitleMap.containsKey(fileNameWithoutExtension)) {
                    IPinePlayerPlugin playerPlugin = new PineLrcParserPlugin(context, mChosenLrcFileSubtitleMap.get(fileNameWithoutExtension), "GBK");
                    playerPluginMap.put(VpConstants.PLUGIN_LRC_SUBTITLE, playerPlugin);
                } else if (mChosenSrtFileSubtitleMap.containsKey(fileNameWithoutExtension)) {
                    IPinePlayerPlugin playerPlugin = new PineSrtParserPlugin(context, mChosenSrtFileSubtitleMap.get(fileNameWithoutExtension), "UTF-8");
                    playerPluginMap.put(VpConstants.PLUGIN_SRT_SUBTITLE, playerPlugin);
                }
                Uri mediaImgUri = mChosenMediaImageMap.containsKey(fileNameWithoutExtension) ? mChosenMediaImageMap.get(fileNameWithoutExtension) : null;
                String mediaCode = file.hashCode() + "";
                PineMediaPlayerBean bean = new PineMediaPlayerBean(mediaCode, file.getFileName(), Uri.parse(file.getFilePath()),
                        MediaFileUtils.isVideoFileType(MediaFileUtils.getFileTypeForFile(filePath)) ? PineMediaPlayerBean.MEDIA_TYPE_VIDEO : PineMediaPlayerBean.MEDIA_TYPE_AUDIO,
                        mediaImgUri, playerPluginMap, null);
                file.setMediaCode(mediaCode);
                file.setMediaPlayerBean(bean);
                fileList.add(file);
                mediaList.add(bean);
                if (filePath.equals(willPlayFilePath)) {
                    playMediaCode = mediaCode;
                }
            }
        }
        mFileListData.setValue(fileList, playMediaCode);
        mMediaListData.setValue(mediaList, playMediaCode);
    }

    private String addToChooseFileMap(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            String folderPath = FileUtils.getFolderName(filePath);
            HashSet<String> hashSet = mChosenFileMap.get(folderPath);
            if (hashSet == null) {
                hashSet = new HashSet<>();
                mChosenFileMap.put(folderPath, hashSet);
            }
            hashSet.add(filePath);
            return filePath;
        }
        return "";
    }

    private String addToChooseFileMap(Context context, Uri uri) {
        if (uri != null) {
            String filePath = FileUtils.getFileAbsolutePath(context, uri);
            String folderPath = FileUtils.getFolderName(filePath);
            HashSet<String> hashSet = mChosenFileMap.get(folderPath);
            if (hashSet == null) {
                hashSet = new HashSet<>();
                mChosenFileMap.put(folderPath, hashSet);
            }
            hashSet.add(filePath);
            return filePath;
        }
        return "";
    }
}
