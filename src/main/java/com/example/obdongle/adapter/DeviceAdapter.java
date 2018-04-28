package com.example.obdongle.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.obdongle.R;
import com.example.obdongle.bean.ObNode;

import java.util.List;

/**
 * 节点显示适配器
 * Created by adolf_dong on 2018/4/27.
 */

@SuppressWarnings("deprecation")
public class DeviceAdapter extends BaseAdapter {
    private Context context;

    public DeviceAdapter(Context context, List<ObNode> obNodes) {
        this.context = context;
        this.obNodes = obNodes;
    }

    private List<ObNode> obNodes;

    @Override
    public int getCount() {
        return obNodes.size();
    }

    @Override
    public Object getItem(int position) {
        return obNodes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.img_tv_item, null);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.img);
            viewHolder.textView = (TextView) convertView.findViewById(R.id.tv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            convertView.setTag(viewHolder);
        }
        ObNode obNode = obNodes.get(position);
        /*暂时用默认的图片*/
        viewHolder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.show_scene));
        viewHolder.textView.setText(obNode.getNodeId());
        return convertView;
    }

    public class ViewHolder {
        ImageView imageView;
        TextView textView;
    }
}
