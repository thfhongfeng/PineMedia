package com.pine.audioplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.pine.audioplayer.R;
import com.pine.audioplayer.databinding.ApItemMusicSheetBinding;
import com.pine.audioplayer.db.entity.ApMusicSheet;
import com.pine.base.recycle_view.BaseListViewHolder;
import com.pine.base.recycle_view.adapter.BaseNoPaginationListAdapter;
import com.pine.base.recycle_view.bean.BaseListAdapterItemProperty;

public class ApMusicSheetAdapter extends BaseNoPaginationListAdapter<ApMusicSheet> {
    @Override
    public BaseListViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new MusicSheetViewHolder(parent.getContext(), LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ap_item_music_sheet, parent, false));
    }

    public class MusicSheetViewHolder extends BaseListViewHolder<ApMusicSheet> {
        private ApItemMusicSheetBinding mBinding;

        public MusicSheetViewHolder(Context context, View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }

        @Override
        public void updateData(ApMusicSheet content, final BaseListAdapterItemProperty propertyEntity, final int position) {
            mBinding.setSheetBean(content);
            mBinding.goDetailBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
    }
}
