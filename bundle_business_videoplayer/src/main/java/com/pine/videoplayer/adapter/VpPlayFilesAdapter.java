package com.pine.videoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pine.base.recycle_view.BaseListViewHolder;
import com.pine.base.recycle_view.adapter.BaseNoPaginationTreeListAdapter;
import com.pine.base.recycle_view.bean.BaseListAdapterItemEntity;
import com.pine.base.recycle_view.bean.BaseListAdapterItemProperty;
import com.pine.videoplayer.R;
import com.pine.videoplayer.bean.VpChooseFileBean;

import java.util.ArrayList;
import java.util.List;

public class VpPlayFilesAdapter extends BaseNoPaginationTreeListAdapter<VpChooseFileBean> {
    public final static int FOLDER_VIEW_HOLDER = 1;
    public final static int FILE_VIEW_HOLDER = 2;

    @Override
    public BaseListViewHolder getViewHolder(ViewGroup parent, int viewType) {
        BaseListViewHolder viewHolder = null;
        switch (viewType) {
            case FOLDER_VIEW_HOLDER:
                viewHolder = new FolderViewHolder(parent.getContext(), LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.vp_item_choose_folder, parent, false));
                break;
            case FILE_VIEW_HOLDER:
                viewHolder = new FileViewHolder(parent.getContext(), LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.vp_item_choose_file, parent, false));
                break;
        }
        return viewHolder;
    }

    @Override
    public List<BaseListAdapterItemEntity<VpChooseFileBean>> parseTreeData(List<VpChooseFileBean> data, boolean reset) {
        List<BaseListAdapterItemEntity<VpChooseFileBean>> adapterData = new ArrayList<>();
        if (data != null) {
            BaseListAdapterItemEntity adapterEntity;
            int fileCount = 0;
            for (int i = data.size() - 1; i >= 0; i--) {
                VpChooseFileBean entity = data.get(i);
                adapterEntity = new BaseListAdapterItemEntity();
                adapterEntity.setData(entity);
                if (entity.getType() == VpChooseFileBean.TYPE_FOLDER) {
                    adapterEntity.getPropertyEntity().setItemViewType(FOLDER_VIEW_HOLDER);
                    adapterEntity.getPropertyEntity().setSubItemViewCount(fileCount);
                    fileCount = 0;
                } else {
                    fileCount++;
                    adapterEntity.getPropertyEntity().setItemViewType(FILE_VIEW_HOLDER);
                }
                adapterData.add(0, adapterEntity);
            }
        }
        return adapterData;
    }

    public class FolderViewHolder extends BaseListViewHolder<VpChooseFileBean> {

        public FolderViewHolder(Context context, View itemView) {
            super(itemView);
        }

        @Override
        public void updateData(VpChooseFileBean content, BaseListAdapterItemProperty propertyEntity, int position) {

        }
    }

    public class FileViewHolder extends BaseListViewHolder<VpChooseFileBean> {

        public FileViewHolder(Context context, View itemView) {
            super(itemView);
        }

        @Override
        public void updateData(VpChooseFileBean content, BaseListAdapterItemProperty propertyEntity, int position) {

        }
    }
}
