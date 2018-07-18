package com.uubox.tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import com.uubox.padtool.R;


/**
 * Created by CG_Dawson on 2018/1/22.
 */

public class IniAdapter extends BaseAdapter {
    private List<IniObj> data;
    private Context context;
    private LayoutInflater layoutInflater;

    public IniAdapter(Context contex_, List<IniObj> data_) {
        this.context = contex_;
        layoutInflater = LayoutInflater.from(context);
        data = data_;
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            holder = new Holder();
            convertView = layoutInflater.inflate(R.layout.iniitem, null);
            holder.name = convertView.findViewById(R.id.ininame);
            holder.state = convertView.findViewById(R.id.inistate);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        holder.name.setText(data.get(position).name);
        if (data.get(position).name.endsWith("[官方]")) {
            holder.name.setTextColor(context.getResources().getColor(R.color.truered));
        } else {
            holder.name.setTextColor(context.getResources().getColor(R.color.mk_white));

        }

        if (data.get(position).state != null) {
            holder.state.setVisibility(View.VISIBLE);
            holder.state.setTextColor(context.getResources().getColor(R.color.color_union_button));
            holder.state.setText(data.get(position).state);
        } else {
            holder.state.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    class Holder {

        TextView name;
        TextView state;

    }

    public static class IniObj {
        public String name;
        public String state;
        public String whole;

        @Override
        public boolean equals(Object obj) {
            IniObj objRight = (IniObj) obj;
            return this.name.equals(objRight.name);
        }
    }
}
