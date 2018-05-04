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

/**
 * 显示场景适配器
 * Created by adolf_dong on 2018/5/2.
 */

@SuppressWarnings("deprecation")
public class SceneAdapter extends BaseAdapter {

    private Context context;

    private ObNode obNode;

    public SceneAdapter(Context context, ObNode obNode) {
        this.context = context;
        this.obNode = obNode;
    }

    @Override
    public int getCount() {
        return obNode.getActionMap().size();
    }

    @Override
    public Object getItem(int position) {
        return null;
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
        viewHolder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.scene_p));
        String sceneStr = obNode.getActionMap().get((position + 1) + "");
        String showStr = makeShowStr(sceneStr);
        viewHolder.textView.setText(showStr);
        return convertView;
    }

    private String makeShowStr(String sceneStr) {
        return "条件为:" + "传感器地址:" + sceneStr.substring(18, 20) +"存储条件:"+
                (sceneStr.substring(20, 24).equals("0000") ? "无状态" : "有状态") + "行为:"
                + sceneStr.substring(24, sceneStr.length());
    }

    public class ViewHolder {
        ImageView imageView;
        TextView textView;
    }
}
