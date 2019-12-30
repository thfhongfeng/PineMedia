package com.pine.audioplayer.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pine.audioplayer.R;
import com.pine.audioplayer.databinding.ApItemSheetMusicBinding;
import com.pine.audioplayer.db.entity.ApSheetMusic;
import com.pine.base.recycle_view.BaseListViewHolder;
import com.pine.base.recycle_view.adapter.BaseNoPaginationListAdapter;
import com.pine.base.recycle_view.bean.BaseListAdapterItemProperty;
import com.pine.player.bean.PineMediaPlayerBean;

import java.util.ArrayList;
import java.util.List;

import androidx.databinding.DataBindingUtil;

public class ApMusicListAdapter extends BaseNoPaginationListAdapter<ApSheetMusic> {
    private List<PineMediaPlayerBean> mMediaList = new ArrayList<>();

    public List<PineMediaPlayerBean> getMediaList() {
        return mMediaList;
    }

    @Override
    protected void onDataSet() {
        super.onDataSet();
        mMediaList = new ArrayList<>();
        if (mOriginData != null) {
            for (ApSheetMusic music : mOriginData) {
                PineMediaPlayerBean bean = new PineMediaPlayerBean(music.getSongId() + "",
                        music.getName(), Uri.parse(music.getFilePath()),
                        PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null,
                        null, null);
                bean.setMediaDesc(music.getAuthor() + " - " + music.getAlbum());
                mMediaList.add(bean);
            }
        }
    }

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
        public void updateData(final ApSheetMusic content, final BaseListAdapterItemProperty propertyEntity, final int position) {
            mBinding.setMusicBean(content);
            mBinding.menuBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(v, position, "menu", content);
                    }
                }
            });
            mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(v, position, "", content);
                    }
                }
            });
            // 数据改变时立即刷新数据，解决DataBinding导致的刷新闪烁问题
            mBinding.executePendingBindings();
        }
    }
}
