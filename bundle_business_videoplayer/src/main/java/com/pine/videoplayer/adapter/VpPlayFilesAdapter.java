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

    private int mCurMediaPosition = -1;

    public int getCurMediaPosition() {
        return mCurMediaPosition;
    }

    public void setCurMediaPosition(int curMediaPosition) {
        mCurMediaPosition = curMediaPosition;
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
            if (mCurMediaPosition == content.getMediaPosition()) {
                lastFileSelectPosition = position;
                mBinding.nameTv.setSelected(true);
            } else {
                mBinding.nameTv.setSelected(false);
            }
            mBinding.container.setVisibility(View.VISIBLE);

            mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMediaItemClickListener != null) {
                        mMediaItemClickListener.onMediaItemClick(v, content.getMediaPosition());
                        mCurMediaPosition = content.getMediaPosition();
                        notifyItemChangedSafely(lastFileSelectPosition);
                        notifyItemChangedSafely(position);
                    }
                }
            });
        }
    }

    private IOnMediaItemClick mMediaItemClickListener;

    public void setMediaItemClick(IOnMediaItemClick listener) {
        mMediaItemClickListener = listener;
    }

    public interface IOnMediaItemClick {
        void onMediaItemClick(View view, int position);
    }
}
