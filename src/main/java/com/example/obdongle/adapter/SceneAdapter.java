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
import com.example.obdongle.widget.ShowNodeSceneDialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 显示场景适配器
 * Created by adolf_dong on 2018/5/2.
 */

@SuppressWarnings("deprecation")
public class SceneAdapter extends BaseAdapter {

    private Context context;

    /**
     * 行为节点
     */
    private ObNode obNode;

    /**
     * 所有节点集合
     */
    private List<ObNode> obNodes;

    private List<List<NodeScene>> nodeScenesList;

    public SceneAdapter(Context context, ObNode obNode, List<ObNode> obNodes) {
        this.context = context;
        this.obNode = obNode;
        this.obNodes = obNodes;
        nodeScenesList = makeAndScenesList(obNode);
    }

    /**
     * 整合节点内场景的与关系，形成与关系的归纳为一组
     * 遍历出groupIndex的集合，再以此集合为依据对场景进行分类归并
     *
     * @param obNode 目标节点
     * @return 目标列表
     */
    private List<List<NodeScene>> makeAndScenesList(ObNode obNode) {
        List<List<NodeScene>> nodeScenesList = new ArrayList<>();
        List<NodeScene> nodeScenes = obNode.getSceneList();
        Set<Integer> groupIndexCacheSet = new HashSet<>();
        for (int i = 0; i < nodeScenes.size(); i++) {
            NodeScene nodeScene = nodeScenes.get(i);
            int groupIndex = nodeScene.getGroupIndex();
            groupIndexCacheSet.add(groupIndex);
        }
        for (Integer integer : groupIndexCacheSet) {
            int groupIndex = integer.byteValue();
            List<NodeScene> nodeScenes1 = new ArrayList<>();
            for (int i = 0; i < nodeScenes.size(); i++) {
                NodeScene nodeScene = nodeScenes.get(i);
                if (groupIndex == nodeScene.getGroupIndex()) {
                    nodeScenes1.add(nodeScene);
                }
            }
            nodeScenesList.add(nodeScenes1);
        }
        return nodeScenesList;
    }

    @Override
    public void notifyDataSetChanged() {
        nodeScenesList = makeAndScenesList(obNode);
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return nodeScenesList.size();
    }

    @Override
    public Object getItem(int position) {
        return nodeScenesList.get(position);
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
        String showStr = showStr(position);
        viewHolder.textView.setText(showStr);
        return convertView;
    }


    /**
     * 生成显示数据
     *
     * @param position un
     * @return 显示数据
     */
    private String showStr(int position) {
        List<NodeScene> nodeScenes = nodeScenesList.get(position);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodeScenes.size(); i++) {
            NodeScene nodeScene = nodeScenes.get(i);
            if (i != nodeScenes.size() - 1) {
                sb.append(MakeSceneShowStr.makeShowCondition(nodeScene, obNodes));
            } else {
                sb.append(MakeSceneShowStr.makeShowScene(nodeScene, obNode, obNodes));
            }
        }
        return sb.toString();
    }

    public class ViewHolder {
        ImageView imageView;
        TextView textView;
    }

}
