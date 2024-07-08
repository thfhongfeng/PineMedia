package com.pine.media.audio.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.pine.media.audio.R;
import com.pine.media.audio.databinding.ApItemSheetMusicBinding;
import com.pine.media.audio.db.entity.ApMusic;
import com.pine.template.base.recycle_view.BaseListViewHolder;
import com.pine.template.base.recycle_view.adapter.BaseNoPaginationListAdapter;
import com.pine.template.base.recycle_view.bean.BaseListAdapterItemProperty;

public class ApMusicListAdapter extends BaseNoPaginationListAdapter<ApMusic> {
    private ApMusic mPlayMusic;
    private boolean mIsPlaying;

    public void setPlayMusic(ApMusic playMusic, boolean playing) {
        mPlayMusic = playMusic;
        mIsPlaying = playing;
        notifyDataSetChangedSafely();
    }

    @Override
    public BaseListViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new SheetMusicViewHolder(parent.getContext(), LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ap_item_sheet_music, parent, false));
    }

    public class SheetMusicViewHolder extends BaseListViewHolder<ApMusic> {
        private ApItemSheetMusicBinding mBinding;

        public SheetMusicViewHolder(Context context, View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }

        @Override
        public void updateData(final ApMusic content, final BaseListAdapterItemProperty propertyEntity, final int position) {
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
            if (mPlayMusic != null && mPlayMusic.getSongId() == content.getSongId()) {
                if (mIsPlaying) {
                    mBinding.playStateIv.setImageResource(R.drawable.ap_anim_playing);
                    AnimationDrawable playAnim = (AnimationDrawable) mBinding.playStateIv.getDrawable();
                    playAnim.start();
                } else {
                    mBinding.playStateIv.setImageResource(R.mipmap.ap_ic_playing_1_1);
                }
                mBinding.playStateIv.setVisibility(View.VISIBLE);
            } else {
                mBinding.playStateIv.setVisibility(View.INVISIBLE);
            }

            // 数据改变时立即刷新数据，解决DataBinding导致的刷新闪烁问题
            mBinding.executePendingBindings();
        }
    }
}
