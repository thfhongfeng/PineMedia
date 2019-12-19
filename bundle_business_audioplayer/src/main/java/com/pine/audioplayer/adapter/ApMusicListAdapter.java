package com.pine.audioplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.pine.audioplayer.R;
import com.pine.audioplayer.databinding.ApItemSheetMusicBinding;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.base.recycle_view.BaseListViewHolder;
import com.pine.base.recycle_view.adapter.BasePaginationListAdapter;
import com.pine.base.recycle_view.bean.BaseListAdapterItemProperty;

public class ApMusicListAdapter extends BasePaginationListAdapter<ApSheetMusic> {
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
        }
    }
}
