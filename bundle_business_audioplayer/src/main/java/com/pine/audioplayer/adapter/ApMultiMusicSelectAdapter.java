package com.pine.audioplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pine.audioplayer.R;
import com.pine.audioplayer.databinding.ApItemMultiMusicSelectBinding;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.base.recycle_view.BaseListViewHolder;
import com.pine.base.recycle_view.adapter.BaseNoPaginationListAdapter;
import com.pine.base.recycle_view.bean.BaseListAdapterItemProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import androidx.databinding.DataBindingUtil;

public class ApMultiMusicSelectAdapter extends BaseNoPaginationListAdapter<ApSheetMusic> {
    private HashSet<ApSheetMusic> mSelectSet = new HashSet<>();

    public int getSelectMusicCount() {
        return mSelectSet.size();
    }

    public List<ApSheetMusic> getSelectMusicList(long sheetId) {
        List<ApSheetMusic> list = new ArrayList<>();
        Iterator<ApSheetMusic> iterator = mSelectSet.iterator();
        while (iterator.hasNext()) {
            ApSheetMusic music = iterator.next();
            music.setId(0);
            music.setSheetId(sheetId);
            list.add(music);
        }
        return list;
    }

    public void setAllSelectMusic(boolean allSelect) {
        if (allSelect) {
            mSelectSet = new HashSet<>(getOriginData());
        } else {
            mSelectSet = new HashSet<>();
        }
        notifyDataSetChangedSafely();
    }

    @Override
    public BaseListViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new SheetMusicViewHolder(parent.getContext(), LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ap_item_multi_music_select, parent, false));
    }

    public class SheetMusicViewHolder extends BaseListViewHolder<ApSheetMusic> {
        private ApItemMultiMusicSelectBinding mBinding;

        public SheetMusicViewHolder(Context context, View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }

        @Override
        public void updateData(final ApSheetMusic content, final BaseListAdapterItemProperty propertyEntity, final int position) {
            mBinding.setMusicBean(content);
            mBinding.selectBtn.setSelected(mSelectSet.contains(content));
            mBinding.selectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setSelected(!v.isSelected());
                    if (v.isSelected()) {
                        mSelectSet.add(content);
                    } else {
                        mSelectSet.remove(content);
                    }
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(v, position, "check", content);
                    }
                }
            });
        }
    }
}
