package com.pine.media.audio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.pine.media.audio.R;
import com.pine.media.audio.databinding.ApItemMultiMusicSelectBinding;
import com.pine.media.audio.db.entity.ApMusic;
import com.pine.template.base.recycle_view.BaseListViewHolder;
import com.pine.template.base.recycle_view.adapter.BaseNoPaginationListAdapter;
import com.pine.template.base.recycle_view.bean.BaseListAdapterItemProperty;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;

public class ApMultiMusicSelectAdapter extends BaseNoPaginationListAdapter<ApMusic> {
    private HashSet<ApMusic> mSelectSet = new HashSet<>();

    public int getSelectMusicCount() {
        return mSelectSet.size();
    }

    public ArrayList<ApMusic> getSelectMusicList() {
        ArrayList<ApMusic> list = new ArrayList<>();
        Iterator<ApMusic> iterator = mSelectSet.iterator();
        while (iterator.hasNext()) {
            ApMusic music = iterator.next();
            music.setUpdateTimeStamp(Calendar.getInstance().getTimeInMillis());
            music.setCreateTimeStamp(Calendar.getInstance().getTimeInMillis());
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

    public class SheetMusicViewHolder extends BaseListViewHolder<ApMusic> {
        private ApItemMultiMusicSelectBinding mBinding;

        public SheetMusicViewHolder(Context context, View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }

        @Override
        public void updateData(final ApMusic content, final BaseListAdapterItemProperty propertyEntity, final int position) {
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
            // 数据改变时立即刷新数据，解决DataBinding导致的刷新闪烁问题
            mBinding.executePendingBindings();
        }
    }
}
