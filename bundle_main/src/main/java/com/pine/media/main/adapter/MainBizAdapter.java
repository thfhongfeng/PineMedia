package com.pine.media.main.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.pine.media.main.R;
import com.pine.media.main.bean.MainBizItemEntity;
import com.pine.media.main.databinding.MainItemBinding;
import com.pine.media.main.remote.MainRouterClient;
import com.pine.template.base.recycle_view.BaseListViewHolder;
import com.pine.template.base.recycle_view.adapter.BaseNoPaginationListAdapter;
import com.pine.template.base.recycle_view.bean.BaseListAdapterItemProperty;
import com.pine.tool.router.IRouterCallback;

/**
 * Created by tanghongfeng on 2019/1/16
 */

public class MainBizAdapter extends BaseNoPaginationListAdapter {

    @Override
    public BaseListViewHolder getViewHolder(ViewGroup parent, int viewType) {
        BaseListViewHolder viewHolder = new BusinessViewHolder(parent.getContext(), LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_item_biz, parent, false));
        return viewHolder;
    }

    public class BusinessViewHolder extends BaseListViewHolder<MainBizItemEntity> {
        private Context mContext;
        private MainItemBinding mBinding;

        public BusinessViewHolder(Context context, View itemView) {
            super(itemView);
            mContext = context;
            mBinding = DataBindingUtil.bind(itemView);
        }

        @Override
        public void updateData(final MainBizItemEntity content,
                               BaseListAdapterItemProperty propertyEntity, int position) {
            mBinding.setItemData(content);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainRouterClient.goBizHome(mContext, content, null, new IRouterCallback() {
                        @Override
                        public void onSuccess(Bundle responseBundle) {

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
