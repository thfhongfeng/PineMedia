package com.pine.media.audioplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.pine.media.audioplayer.R;
import com.pine.media.audioplayer.databinding.ApItemMusicSheetBinding;
import com.pine.media.audioplayer.db.entity.ApSheet;
import com.pine.media.base.recycle_view.BaseListViewHolder;
import com.pine.media.base.recycle_view.adapter.BaseNoPaginationListAdapter;
import com.pine.media.base.recycle_view.bean.BaseListAdapterItemProperty;

public class ApMusicSheetAdapter extends BaseNoPaginationListAdapter<ApSheet> {

    private void setOnItemClickListener() {

    }

    @Override
    public BaseListViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new MusicSheetViewHolder(parent.getContext(), LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ap_item_music_sheet, parent, false));
    }

    public class MusicSheetViewHolder extends BaseListViewHolder<ApSheet> {
        private ApItemMusicSheetBinding mBinding;

        public MusicSheetViewHolder(Context context, View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }

        @Override
        public void updateData(final ApSheet content, final BaseListAdapterItemProperty propertyEntity, final int position) {
            mBinding.setSheetBean(content);
            mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(v, position, null, content);
                    }
                }
            });
            // 数据改变时立即刷新数据，解决DataBinding导致的刷新闪烁问题
            mBinding.executePendingBindings();
        }
    }
}
