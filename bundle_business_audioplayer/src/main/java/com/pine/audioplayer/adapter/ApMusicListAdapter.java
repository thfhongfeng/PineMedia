package com.pine.audioplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pine.audioplayer.R;
import com.pine.audioplayer.databinding.ApItemSheetMusicBinding;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.base.recycle_view.BaseListViewHolder;
import com.pine.base.recycle_view.adapter.BaseNoPaginationListAdapter;
import com.pine.base.recycle_view.bean.BaseListAdapterItemProperty;

import androidx.databinding.DataBindingUtil;

public class ApMusicListAdapter extends BaseNoPaginationListAdapter<ApSheetMusic> {
    @Override
    public BaseListViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new SheetMusicViewHolder(parent.getContext(), LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ap_item_sheet_music, parent, false));
    }

    public class SheetMusicViewHolder extends BaseListViewHolder<ApSheetMusic> {
        private ApItemSheetMusicBinding mBinding;

        public SheetMusicViewHolder(Context context, View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }

        @Override
        public void updateData(ApSheetMusic content, final BaseListAdapterItemProperty propertyEntity, final int position) {
            mBinding.setMusicBean(content);
            // 数据改变时立即刷新数据，解决DataBinding导致的刷新闪烁问题
            mBinding.executePendingBindings();
        }
    }
}
