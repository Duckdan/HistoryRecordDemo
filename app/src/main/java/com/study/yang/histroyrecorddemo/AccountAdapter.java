package com.study.yang.histroyrecorddemo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.List;

public class AccountAdapter extends BaseAdapter {
    private Context context;
    private List<HotspotUserBean> hubs;

    public AccountAdapter(Context context, List<HotspotUserBean> hubs) {
        this.context = context;
        this.hubs = hubs;
    }

    @Override
    public int getCount() {
        return hubs == null ? 0 : hubs.size();
    }

    @Override
    public Object getItem(int position) {
        return hubs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.login_record_item, null);
        }
        final HotspotUserBean hub = hubs.get(position);
        TextView textTitle = (TextView) convertView.findViewById(R.id.text_title);
        convertView.findViewById(R.id.icon_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hubs.remove(hub);
                ((MainActivity) context).deleteData(hub);
                notifyDataSetChanged();
            }
        });

        textTitle.setText(hub.getAccount());
        return convertView;
    }
}
