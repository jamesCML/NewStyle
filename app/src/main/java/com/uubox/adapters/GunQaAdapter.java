package com.uubox.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.uubox.padtool.R;
import com.uubox.tools.SimpleUtil;

import java.util.List;

public class GunQaAdapter extends BaseAdapter {
    private Context mContext;
    private List<QAItem> mData;

    public GunQaAdapter(Context context, List<QAItem> data) {
        mContext = context;
        mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
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
            view = LayoutInflater.from(mContext).inflate(R.layout.gungaitem, null);
            holder.mQuesiton = view.findViewById(R.id.gunqaitem_question);
            holder.mOrder = view.findViewById(R.id.gunqaitem_order);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        holder.mQuesiton.setText(mData.get(position).mQueston);
        holder.mOrder.setText((position + 1) + "");
        return view;
    }


    class Holder {
        TextView mQuesiton;
        TextView mOrder;
    }

    public static class QAItem {
        public String mQueston;
        public String mAnswer;
    }
}
