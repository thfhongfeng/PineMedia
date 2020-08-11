package com.pine.videoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.pine.base.recycle_view.BaseListViewHolder;
import com.pine.base.recycle_view.adapter.BaseNoPaginationTreeListAdapter;
import com.pine.base.recycle_view.bean.BaseListAdapterItemEntity;
import com.pine.base.recycle_view.bean.BaseListAdapterItemProperty;
import com.pine.videoplayer.R;
import com.pine.videoplayer.bean.VpFileBean;
import com.pine.videoplayer.databinding.VpChooseFileItemBinding;
import com.pine.videoplayer.databinding.VpChooseFolderItemBinding;

import java.util.ArrayList;
import java.util.List;

public class VpPlayFilesAdapter extends BaseNoPaginationTreeListAdapter<VpFileBean> {
    public final static int FOLDER_VIEW_HOLDER = 1;
    public final static int FILE_VIEW_HOLDER = 2;

    private String mCurMediaCode = "";

    public String getCurMediaCode() {
        return mCurMediaCode;
    }

    public void setCurMediaCode(String curMediaCode) {
        mCurMediaCode = curMediaCode;
    }

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
    public List<BaseListAdapterItemEntity<VpFileBean>> parseTreeData(List<VpFileBean> data, boolean reset) {
        List<BaseListAdapterItemEntity<VpFileBean>> adapterData = new ArrayList<>();
        if (data != null) {
            BaseListAdapterItemEntity adapterEntity;
            int fileCount = 0;
            for (int i = data.size() - 1; i >= 0; i--) {
                VpFileBean entity = data.get(i);
                adapterEntity = new BaseListAdapterItemEntity();
                adapterEntity.setData(entity);
                if (entity.getType() == VpFileBean.TYPE_FOLDER) {
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

    public class FolderViewHolder extends BaseListViewHolder<VpFileBean> {
        private VpChooseFolderItemBinding mBinding;

        public FolderViewHolder(Context context, View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }

        @Override
        public void updateData(VpFileBean content, final BaseListAdapterItemProperty propertyEntity, final int position) {
            mBinding.setFileBean(content);
            mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean subWillShow = !propertyEntity.isItemViewSpread();
                    propertyEntity.setItemViewSpread(subWillShow);
                    for (int i = position + 1; i < propertyEntity.getSubItemViewCount() + position + 1; i++) {
                        mData.get(i).getPropertyEntity().setItemViewNeedShow(subWillShow);
                    }
                    notifyItemRangeChanged(position + getHeadViewCount(), propertyEntity.getSubItemViewCount() + 1);
                }
            });
            // 数据改变时立即刷新数据，解决DataBinding导致的刷新闪烁问题
            mBinding.executePendingBindings();
        }
    }

    private int lastFileSelectPosition = -1;

    public class FileViewHolder extends BaseListViewHolder<VpFileBean> {
        private VpChooseFileItemBinding mBinding;

        public FileViewHolder(Context context, View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }

        @Override
        public void updateData(final VpFileBean content, BaseListAdapterItemProperty propertyEntity, final int position) {
            mBinding.setFileBean(content);
            if (!propertyEntity.isItemViewNeedShow()) {
                mBinding.container.setVisibility(View.GONE);
                return;
            }
            if (mCurMediaCode.equals(content.getMediaCode())) {
                lastFileSelectPosition = position;
                mBinding.nameTv.setSelected(true);
            } else {
                mBinding.nameTv.setSelected(false);
            }
            mBinding.container.setVisibility(View.VISIBLE);

            mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(v, position, "root", content);
                        notifyItemChangedSafely(lastFileSelectPosition);
                        notifyItemChangedSafely(position);
                    }
                }
            });
            mBinding.deleteIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(v, position, "delete", content);
                        notifyDataSetChangedSafely();
                    }
                }
            });
            // 数据改变时立即刷新数据，解决DataBinding导致的刷新闪烁问题
            mBinding.executePendingBindings();
        }
    }
}
