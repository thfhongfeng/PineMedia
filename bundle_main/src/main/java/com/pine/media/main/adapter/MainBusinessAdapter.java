package com.pine.media.main.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.pine.media.base.recycle_view.BaseListViewHolder;
import com.pine.media.base.recycle_view.adapter.BaseNoPaginationListAdapter;
import com.pine.media.base.recycle_view.bean.BaseListAdapterItemProperty;
import com.pine.media.base.track.AppTrackManager;
import com.pine.media.base.track.TrackModuleTag;
import com.pine.media.main.R;
import com.pine.media.main.bean.MainBusinessItemEntity;
import com.pine.media.main.databinding.MainItemBinding;
import com.pine.media.main.remote.MainRouterClient;
import com.pine.tool.router.IRouterCallback;
import com.pine.tool.router.RouterCommandType;

/**
 * Created by tanghongfeng on 2019/1/16
 */

public class MainBusinessAdapter extends BaseNoPaginationListAdapter {

    @Override
    public BaseListViewHolder getViewHolder(ViewGroup parent, int viewType) {
        BaseListViewHolder viewHolder = new BusinessViewHolder(parent.getContext(), LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_item_business, parent, false));
        return viewHolder;
    }

    public class BusinessViewHolder extends BaseListViewHolder<MainBusinessItemEntity> {
        private Context mContext;
        private MainItemBinding mBinding;

        public BusinessViewHolder(Context context, View itemView) {
            super(itemView);
            mContext = context;
            mBinding = DataBindingUtil.bind(itemView);
        }

        @Override
        public void updateData(final MainBusinessItemEntity content,
                               BaseListAdapterItemProperty propertyEntity, int position) {
            mBinding.setItemData(content);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainRouterClient.callCommand(mContext, content.getBundle(), RouterCommandType.TYPE_UI_COMMAND,
                            content.getCommand(), null, new IRouterCallback() {
                                @Override
                                public void onSuccess(Bundle responseBundle) {
                                    AppTrackManager.getInstance().trackButton(mContext, TrackModuleTag.MODULE_DEFAULT,
                                            "MainHomeActivity", "MainHomeActivity",
                                            "HomeGoBundle※" + content.getCommand(), content.getBundle());
                                }

                                @Override
                                public boolean onFail(int failCode, String errorInfo) {
                                    return false;
                                }
                            });
                }
            });
            // 数据改变时立即刷新数据，解决DataBinding导致的刷新闪烁问题
            mBinding.executePendingBindings();
        }
    }
}
