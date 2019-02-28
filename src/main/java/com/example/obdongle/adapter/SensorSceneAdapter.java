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
import com.example.obdongle.util.MakeSceneShowStr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 传感器场景显示适配器
 * Created by adolf_dong on 2018/5/11.
 */
@SuppressWarnings("deprecation")
public class SensorSceneAdapter extends BaseAdapter {

    /**
     * 节点总集合
     */
    private List<ObNode> obNodes;

    /**
     * 条件节点
     */
    private ObNode cdtNode;


    /**
     * 相同condition相同action视为同一组
     */
    private List<NodeScene> nodeScenes = new ArrayList<>();

    /**
     * 比对条件，符合同条件即为可显示
     */
    private byte[] condition;

    private Context context;

    public SensorSceneAdapter(Context context, ObNode cdtNode, List<ObNode> obNodes, boolean isHave) {
        this.context = context;
        this.cdtNode = cdtNode;
        this.obNodes = obNodes;
        condition = isHave ? new byte[]{1, 0} : new byte[2];
        makeShowNodeScenes(obNodes);
    }

    private void makeShowNodeScenes(List<ObNode> obNodes) {
        nodeScenes.clear();
        for (int i = 0; i < obNodes.size(); i++) {
            ObNode obNode = obNodes.get(i);
            List<NodeScene> nodeScenes = obNode.getSceneList();
            for (int j = 0; j < nodeScenes.size(); j++) {
                NodeScene nodeScene = nodeScenes.get(j);
                if (Arrays.equals(nodeScene.getCondition(), condition)
                        && nodeScene.getConditionAddr() == cdtNode.getAddr()) {
                    this.nodeScenes.add(nodeScene);
                }
            }
        }
    }

    @Override
    public void notifyDataSetChanged() {
        makeShowNodeScenes(obNodes);
        super.notifyDataSetChanged();
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
        NodeScene nodeScene = nodeScenes.get(position);
        ObNode obNode = nodeScene.findActionNode();
        String showStr = MakeSceneShowStr.makeShowScene(nodeScene, obNode, obNodes);
        viewHolder.textView.setText(showStr);
        return convertView;
    }

    public class ViewHolder {
        ImageView imageView;
        TextView textView;
    }

    public List<NodeScene> getNodeScenes(){
        return nodeScenes;
    }
}
