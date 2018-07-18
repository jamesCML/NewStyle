package com.uubox.tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.uubox.padtool.R;

public class BigKeyConfigAd extends BaseAdapter {
    private List<BigKeyConfigItemObj> mData;
    private Context mContext;
    private IBigKeyConfigClick mIBigKeyConfigClick;

    public BigKeyConfigAd(Context context, List<BigKeyConfigItemObj> data) {
        mContext = context;
        mData = data;
    }

    public void setmIBigKeyConfigClick(IBigKeyConfigClick iBigKeyConfigClick) {
        mIBigKeyConfigClick = iBigKeyConfigClick;
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
    public View getView(final int position, View convertView, ViewGroup group) {
        Holder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.bigkeyconfigitem, null);
            TextView tv = convertView.findViewById(R.id.bigkeyconfigitem_tv);
            ImageView img = convertView.findViewById(R.id.bigkeyconfigitem_img);
            ListView listview = convertView.findViewById(R.id.bigkeyconfigitem_list);


            holder = new Holder();
            holder.img = img;
            holder.tv = tv;
            holder.listView = listview;
            convertView.setTag(holder);

        } else {
            holder = (Holder) convertView.getTag();
        }

        holder.tv.setText(mData.get(position).mTv);
        holder.img.setImageResource(mData.get(position).mImgState ? R.mipmap.down_arrow : R.mipmap.right_arrow);

        if (mData.get(position).mImgState) {
            holder.img.setImageResource(R.mipmap.down_arrow);
            holder.listView.setVisibility(View.VISIBLE);
            final IniAdapter iniAdapter = new IniAdapter(mContext, mData.get(position).mSubData);
            holder.listView.setAdapter(iniAdapter);
            holder.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position2, long id) {
                    SimpleUtil.log("subItem:" + mData.get(position).mSubData.get(position2).name + "  size:" + mData.get(position).mSubData.size());
                    mIBigKeyConfigClick.onItemClick(position, position2, iniAdapter);
                }
            });
            holder.listView.measure(0, 0);
            LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) holder.listView.getLayoutParams();
            param.height = holder.listView.getMeasuredHeight() * mData.get(position).mSubData.size();
            holder.listView.setLayoutParams(param);
        } else {
            holder.img.setImageResource(R.mipmap.right_arrow);
            holder.listView.setVisibility(View.GONE);
        }


        return convertView;
    }

    class Holder {
        TextView tv;
        ImageView img;
        ListView listView;
    }

    public static class BigKeyConfigItemObj {
        public String mTv;
        //false:down true:right
        public boolean mImgState;
        public List<IniAdapter.IniObj> mSubData = new ArrayList<>();
    }

    public interface IBigKeyConfigClick {
        void onItemClick(int mainPosition, int subPosition, IniAdapter adapter);
    }
}
