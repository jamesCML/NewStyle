package com.uubox.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


import com.uubox.padtool.R;
import com.uubox.tools.AOAConfigTool;

public class OverSizeAdapter extends BaseAdapter {
    private Context mContext;
    private List<AOAConfigTool.Config> mConfigs;

    public OverSizeAdapter(Context context, List<AOAConfigTool.Config> configs) {
        mContext = context;
        mConfigs = configs;
    }

    @Override
    public int getCount() {
        return mConfigs.size();
    }

    @Override
    public Object getItem(int position) {
        return mConfigs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        Holder holder;
        if (view == null) {
            holder = new Holder();
            view = LayoutInflater.from(mContext).inflate(R.layout.oversizeitem, null);
            holder.mContent = view.findViewById(R.id.oversie_content);
            holder.mName = view.findViewById(R.id.oversie_name);
            holder.mSize = view.findViewById(R.id.oversie_size);
            holder.mDeleted = view.findViewById(R.id.oversie_delete);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }
        holder.mDeleted.setVisibility(mConfigs.get(position).getIsDeleted() ? View.VISIBLE : View.GONE);
        holder.mContent.setText("游戏:" + mConfigs.get(position).getmContent());
        holder.mName.setText("配置:" + mConfigs.get(position).getmConfigName());
        holder.mSize.setText("大小:" + mConfigs.get(position).getmSize());
        return view;
    }

    class Holder {
        TextView mContent;
        TextView mName;
        TextView mSize;
        ImageView mDeleted;
    }

}
