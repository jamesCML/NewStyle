package com.uubox.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uubox.padtool.R;
import com.uubox.tools.AOADataPack;
import com.uubox.tools.SimpleUtil;

import java.util.List;

public class MoveConfigAdapter extends BaseAdapter {
    private Context mContext;
    private List<AOADataPack.Config> mConfigs;

    public MoveConfigAdapter(Context context, List<AOADataPack.Config> configs) {
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
    public View getView(final int position, View view, ViewGroup parent) {

        Holder holder;
        if (view == null) {
            holder = new Holder();
            view = LayoutInflater.from(mContext).inflate(R.layout.dialog_oversize_item, null);
            holder.mContent = view.findViewById(R.id.moveconfig_item_content);
            holder.mName = view.findViewById(R.id.moveconfig_item_name);
            holder.mSize = view.findViewById(R.id.moveconfig_item_size);
            holder.mFastkey = view.findViewById(R.id.moveconfig_item_fastkey);
            holder.mLeftArrow = view.findViewById(R.id.moveconfig_item_left);
            holder.mRightArrow = view.findViewById(R.id.moveconfig_item_right);
            holder.mShangArrow = view.findViewById(R.id.moveconfig_item_shang);
            holder.mXiaArrow = view.findViewById(R.id.moveconfig_item_xia);
            holder.mOriPar = view.findViewById(R.id.moveconfig_item_shang_par);

            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }
        holder.mContent.setText("目录:" + mConfigs.get(position).getmContent());
        holder.mName.setText("配置:" + mConfigs.get(position).getmConfigName());
        holder.mSize.setText("大小:" + mConfigs.get(position).getmSize());
        if (mConfigs.get(position).getIsDeleted()) {
            holder.mRightArrow.setVisibility(View.VISIBLE);
            holder.mLeftArrow.setVisibility(View.GONE);
            holder.mFastkey.setVisibility(View.GONE);
            holder.mOriPar.setVisibility(View.GONE);
        } else {
            holder.mRightArrow.setVisibility(View.GONE);
            holder.mLeftArrow.setVisibility(View.VISIBLE);
            holder.mFastkey.setVisibility(View.VISIBLE);
            holder.mFastkey.setText("快捷键:Ctrl+" + (position + 1));
            holder.mOriPar.setVisibility(View.VISIBLE);
        }

        holder.mLeftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleUtil.notifyall_(10007, mConfigs.get(position));
            }
        });
        holder.mRightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleUtil.notifyall_(10008, mConfigs.get(position));
            }
        });
        holder.mShangArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleUtil.notifyall_(10009, position);
            }
        });
        holder.mXiaArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleUtil.notifyall_(10010, position);
            }
        });
        return view;
    }

    class Holder {
        TextView mContent;
        TextView mName;
        TextView mSize;
        TextView mFastkey;
        ImageView mLeftArrow;
        ImageView mRightArrow;
        ImageView mShangArrow;
        ImageView mXiaArrow;
        LinearLayout mOriPar;
    }

}
