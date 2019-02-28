package com.example.obdongle.widget;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import com.example.obdongle.R;
import com.example.obdongle.adapter.ConditionAdapter;
import com.example.obdongle.bean.NodeScene;
import com.example.obdongle.bean.ObNode;
import com.example.obdongle.databinding.ShowNodeSceneDialgBinding;
import com.example.obdongle.util.MakeSceneShowStr;

import java.util.List;

/**
 * 编辑场景时，显示节点与场景整合的对话框
 * Created by adolf_dong on 2018/6/27.
 */

public abstract class ShowNodeSceneDialog extends Dialog {

    /**
     * 被编辑的场景集合
     */
    private List<NodeScene> nodeScenes;
    /**
     * 设备
     */
    private ObNode obNode;

    private ConditionAdapter conditionAdapter;

    protected ShowNodeSceneDialog(Context context, int themeResId, List<NodeScene> nodeScenes, ObNode obNode) {
        super(context,themeResId);
        this.nodeScenes = nodeScenes;
        this.obNode = obNode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ShowNodeSceneDialgBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.show_node_scene_dialg, null, false);
        setContentView(binding.getRoot());
        binding.actionTv.setText(MakeSceneShowStr.makeShowActionStr(nodeScenes.get(0), obNode));
        conditionAdapter = new ConditionAdapter(getContext(), nodeScenes);
        binding.conditionLv.setAdapter(conditionAdapter);
        binding.actionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                onEditAction(nodeScenes);
            }
        });
        binding.conditionLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dismiss();
                NodeScene nodeScene = (NodeScene) conditionAdapter.getItem(position);
                onEditCondition(nodeScene);
            }
        });
        binding.conditionLv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                dismiss();
                NodeScene nodeScene = (NodeScene) conditionAdapter.getItem(position);
                onRemoveCondition(nodeScene);
                return true;
            }
        });
        binding.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                NodeScene nodeScene = nodeScenes.get(0);
                int groupIndex = nodeScene.getGroupIndex();
                byte[] action = nodeScene.getAction();
                onAddCondition(groupIndex, action);
            }
        });
    }

    /**
     * 添加场景
     *
     * @param groupIndex 已经设置好与编号的场景
     * @param action 通用的action
     */
    protected abstract void onAddCondition(int groupIndex, byte[] action);

    /**
     * 编辑场景action
     *
     * @param nodeScenes 如果action发生变化，则对此集合内的场景action编辑
     */
    protected abstract void onEditAction(List<NodeScene> nodeScenes);

    /**
     * 编辑场景单个condition
     *
     * @param nodeScene 被编辑的场景
     */
    protected abstract void onEditCondition(NodeScene nodeScene);

    /**
     * 删除单个场景
     *
     * @param nodeScene 被删除的场景
     */
    protected abstract void onRemoveCondition(NodeScene nodeScene);


}
