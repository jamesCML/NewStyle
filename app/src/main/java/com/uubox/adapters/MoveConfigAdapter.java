package com.uubox.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.uubox.padtool.R;
import com.uubox.tools.AOAConfigTool;
import com.uubox.tools.SimpleUtil;

import java.util.List;

public class MoveConfigAdapter extends BaseAdapter {
    private Context mContext;
    private List<AOAConfigTool.Config> mConfigs;

    public MoveConfigAdapter(Context context, List<AOAConfigTool.Config> configs) {
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
            holder.mCuruse = view.findViewById(R.id.moveconfig_item_curuse);
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
        holder.mContent.setText(mContext.getString(R.string.initab_game) + ":" + SimpleUtil.zhChange(mContext, mConfigs.get(position).getmBelongGame()));
        if (!SimpleUtil.isOfficialConfig(mConfigs.get(position).getmConfigName())) {
            holder.mName.setText(mContext.getString(R.string.ini_config) + ":" + SimpleUtil.zhChange(mContext, mConfigs.get(position).getmConfigName()));
        } else {
            holder.mName.setText(Html.fromHtml(mContext.getString(R.string.ini_config) + ":<font color='#ff5959'>" + SimpleUtil.zhChange(mContext, mConfigs.get(position).getmConfigName()) + "</font>"));
        }
        holder.mSize.setText("编号:" + mConfigs.get(position).getmConfigid());
        if (mConfigs.get(position).getIsDeleted()) {
            holder.mRightArrow.setVisibility(View.VISIBLE);
            holder.mLeftArrow.setVisibility(View.GONE);
            holder.mFastkey.setVisibility(View.GONE);
            holder.mOriPar.setVisibility(View.GONE);
        } else {
            holder.mRightArrow.setVisibility(View.GONE);
            holder.mLeftArrow.setVisibility(View.VISIBLE);
            holder.mFastkey.setVisibility(View.VISIBLE);
            holder.mFastkey.setText(mContext.getString(R.string.movead_fastkey) + (position + 1));
            holder.mOriPar.setVisibility(View.VISIBLE);
        }
        holder.mCuruse.setVisibility(mConfigs.get(position).getIsUsed() ? View.VISIBLE : View.GONE);
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
        TextView mCuruse;
        TextView mFastkey;
        ImageView mLeftArrow;
        ImageView mRightArrow;
        ImageView mShangArrow;
        ImageView mXiaArrow;
        FrameLayout mOriPar;
    }

}
