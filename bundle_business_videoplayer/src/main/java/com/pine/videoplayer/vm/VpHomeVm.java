package com.pine.videoplayer.vm;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pine.tool.architecture.mvvm.vm.ViewModel;
import com.pine.tool.util.FileUtils;
import com.pine.videoplayer.bean.VpChooseFileBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class VpHomeVm extends ViewModel {
    private HashMap<String, HashSet<String>> mChooseFileMap = new HashMap<>();
    public MutableLiveData<ArrayList<VpChooseFileBean>> mChooseFileList = new MutableLiveData<>();

    public void onFileChosen(@NonNull Intent data) {
        if (data.getData() != null) {
            // 单次点击未使用多选的情况
            try {
                addToChooseFileMap(data.getData());
            } catch (Exception e) {
            }
        } else {
            // 长按使用多选的情况
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    addToChooseFileMap(item.getUri());
                }
            }
        }
        ArrayList<VpChooseFileBean> list = new ArrayList<>();
        Iterator<HashMap.Entry<String, HashSet<String>>> iterator = mChooseFileMap.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry<String, HashSet<String>> entry = iterator.next();
            VpChooseFileBean folder = new VpChooseFileBean(VpChooseFileBean.TYPE_FOLDER, entry.getKey());
            list.add(folder);
            String[] filePaths = entry.getValue().toArray(new String[0]);
            for (String filePath : filePaths) {
                VpChooseFileBean file = new VpChooseFileBean(VpChooseFileBean.TYPE_FILE, filePath);
                list.add(file);
            }
        }
        mChooseFileList.setValue(list);
    }

    private void addToChooseFileMap(Uri uri) {
        if (uri != null) {
            String filePath = uri.getPath();
            String folderPath = FileUtils.getFolderName(filePath);
            HashSet<String> hashSet = mChooseFileMap.get(folderPath);
            if (hashSet == null) {
                hashSet = new HashSet<>();
                mChooseFileMap.put(folderPath, hashSet);
            }
            hashSet.add(filePath);
        }
    }
}
