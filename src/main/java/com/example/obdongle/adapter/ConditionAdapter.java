package com.example.obdongle.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.obdongle.R;
import com.example.obdongle.bean.NodeScene;
import com.example.obdongle.bean.ObNode;
import com.example.obdongle.data.DataPool;
import com.example.obdongle.util.GetIconFromType;
import com.example.obdongle.util.MakeSceneShowStr;

import java.util.List;

/**
 * 编辑场景条件时使用的适配器
 * Created by adolf_dong on 2018/6/27.
 */
public class ConditionAdapter extends BaseAdapter {

    private GetIconFromType getIconFromType;
    /**
     * 被编辑的场景集合
     */
    private List<NodeScene> nodeScenes;

    private Context context;

    public ConditionAdapter(Context context, List<NodeScene> nodeScenes) {
        this.context = context;
        this.nodeScenes = nodeScenes;
        getIconFromType = new GetIconFromType();
    }

    @Override
    public int getCount() {
        return nodeScenes.size();
    }

    @Override
    public Object getItem(int position) {
        return nodeScenes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VH vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.img_tv_item, null);
            vh = new VH();
            vh.iconImg = (ImageView) convertView.findViewById(R.id.img);
            vh.conditionShowTv = (TextView) convertView.findViewById(R.id.tv);
            convertView.setTag(vh);
        } else {
            vh = (VH) convertView.getTag();
        }
        NodeScene nodeScene = nodeScenes.get(position);
        ObNode cdtNode = nodeScene.findConditionNode();
        vh.iconImg.setImageResource(getIconFromType.getDrawResIdForType(cdtNode));
        vh.conditionShowTv.setText(MakeSceneShowStr.makeShowCondition(nodeScene, DataPool.getInstance().getObNodes()));
        return convertView;
    }

    class VH {
        /**
         * 条件icon
         */
        ImageView iconImg;
        /**
         * 条件显示状态
         */
        TextView conditionShowTv;
    }
}
