package com.example.obdongle;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.obdongle.adapter.SensorSceneAdapter;
import com.example.obdongle.base.BaseAct;
import com.example.obdongle.bean.NodeScene;
import com.example.obdongle.bean.ObNode;
import com.example.obdongle.constant.OBConstant;
import com.example.obdongle.data.DataPool;
import com.example.obdongle.databinding.EditSceneOfSensorActBinding;
import com.example.obdongle.net.Respond;
import com.example.obdongle.net.SpliceTrans;
import com.example.obdongle.util.MakeSceneShowStr;
import com.example.obdongle.util.MakeSendData;
import com.example.obdongle.util.ShareSerializableUtil;

import java.util.ArrayList;
import java.util.List;

import static com.example.obdongle.ControlLampAct.IS_ACTION;

/**
 * 传感器设置场景页面
 * 先选定condition再选择任意数量的节点进行action设置
 * 相同action和相同的condition整合为同一个场景显示
 * <p>
 * Created by adolf_dong on 2018/5/9.
 */

public class EditSceneOfSensorAct extends BaseAct implements Respond {
    /**
     * 条件节点
     */
    private ObNode cdtNode;

    /**
     * 行为节点容器
     */
    private List<ObNode> actionNodes = new ArrayList<>();

    /**
     * 总容器
     */
    private List<ObNode> obNodes;

    /**
     * 当前设置的行为
     */
    private byte[] action = new byte[5];
    /**
     * 缓存的临时index，每个action节点的index是独立的
     */
    private int cachIndex;
    /**
     * 页面元素
     */
    private EditSceneOfSensorActBinding binding;

    /**
     * 传感器场景显示适配器
     */
    private SensorSceneAdapter sensorSceneAdapter;

    /**
     * 传输引用
     */
    private ObNode actionObNode;

    /**
     * 是否删除
     */
    private boolean isRemove;

    /**
     * 批量操作时的交互index
     */
    private int index;


    /**
     * 是否修改现有节点action
     */
    private boolean isChange;

    /**
     * 是否有状态
     */
    private boolean isHave = false;


    /**
     * 操作的引用
     */
    private NodeScene cacheNodeScene;

    /**
     * 修改现有情景行为请求码
     */
    public static final int CHANGE_ACTION = 100;
    /**
     * 添加行为请求码
     */
    public static final int ADD_ACTION = 101;


    private TextView midTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataPool.getInstance().regist(this);
        binding = DataBindingUtil.setContentView(this, R.layout.edit_scene_of_sensor_act);
        cdtNode = DataPool.getInstance().getObNode();
        obNodes = DataPool.getInstance().getObNodes();
        midTv = (TextView) binding.editSceneOfSensorTop.findViewById(R.id.mid_tv);
        midTv.setText(MakeSceneShowStr.showConditionWithBool(cdtNode,isHave));
        ImageView topLeftImg = (ImageView) binding.editSceneOfSensorTop.findViewById(R.id.left_img);
        topLeftImg.setImageResource(R.drawable.back);
        topLeftImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.editSceneSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isHave = isChecked;
                midTv.setText(MakeSceneShowStr.showConditionWithBool(cdtNode,isHave));
                sensorSceneAdapter = new SensorSceneAdapter(EditSceneOfSensorAct.this, cdtNode, obNodes, isHave);
                binding.sceneLv.setAdapter(sensorSceneAdapter);
            }
        });
        sensorSceneAdapter = new SensorSceneAdapter(this, cdtNode, obNodes, isHave);
        binding.sceneLv.setAdapter(sensorSceneAdapter);
        /*点击修改行为*/
        binding.sceneLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                isChange = true;
                NodeScene nodeScene = (NodeScene) sensorSceneAdapter.getItem(position);
                cachIndex = nodeScene.getIndex();
                groupIndex = nodeScene.getGroupIndex();
                cacheNodeScene = nodeScene;
                actionObNode = nodeScene.findActionNode();
                DataPool.getInstance().setObNode(actionObNode);
                Intent intent = new Intent();
                intent.setClass(EditSceneOfSensorAct.this, ControlLampAct.class);
                intent.putExtra(IS_ACTION, nodeScene.getAction());
                startActivityForResult(intent, CHANGE_ACTION);
            }
        });
        /*长按删除场景*/
        binding.sceneLv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                showSimpleDialog(TWO, null, "删除场景吗？", "确定", "取消", new SimpleDialogLSN() {
                    @Override
                    public void pOnClick() {
                        NodeScene nodeScene = (NodeScene) sensorSceneAdapter.getItem(position);
                        cacheNodeScene = nodeScene;
                        actionObNode = nodeScene.findActionNode();
                        isRemove = true;
                        cachIndex = nodeScene.getIndex();
                        groupIndex = nodeScene.getGroupIndex();
                        editScene(true);
                    }

                    @Override
                    public void nOnClick() {

                    }
                });
                return true;
            }
        });
        binding.addSceneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showSimpleDialog(TWO, "请注意", "程序会自动排除已经存在于本条件下的节点以及已经有10个场景的行为节点，您清楚了吗？", "知道了", "不清楚",
                        new SimpleDialogLSN() {
                            @Override
                            public void pOnClick() {
                                                /*过滤节点，批量选择节点，统一设置行为*/
                                actionNodes.clear();
                                final List<ObNode> caches = new ArrayList<>();
                                List<NodeScene> nodeScenes = sensorSceneAdapter.getNodeScenes();
                                for (int i = 0; i < obNodes.size(); i++) {
                                    ObNode obNode = obNodes.get(i);
                                      /*判断节点是否还有场景位*/
                                    if (obNode.canAddScene() && obNode.getParentType() == OBConstant.NodeType.IS_LAMP) {
                                        /*判断节点是否在场景集合中*/
                                        boolean isContain = false;
                                        for (int j = 0; j < nodeScenes.size(); j++) {
                                            NodeScene nodeScene = nodeScenes.get(j);
                                            ObNode obNode1 = nodeScene.findActionNode();
                                            if (obNode1 == obNode) {
                                                isContain = true;
                                                break;
                                            }
                                        }
                                        if (!isContain) {
                                            caches.add(obNode);
                                        }
                                    }
                                }
                                String[] showStr = new String[caches.size()];
                                for (int i = 0; i < caches.size(); i++) {
                                    ObNode obNode = caches.get(i);
                                    showStr[i] = obNode.getNodeId();
                                }
                                if (caches.size() == 0) {
                                    showToat("没有适用此条件的节点，每个可控类设备最多只能存在于10个场景内，或者您的设备已经有类条件下行为");
                                    return;
                                }
                                AlertDialog.Builder builder = new AlertDialog.Builder(EditSceneOfSensorAct.this);
                                builder.setTitle("选择设备");
                                builder.setMultiChoiceItems(showStr, null, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        if (isChecked) {
                                            actionNodes.add(caches.get(which));
                                        } else {
                                            actionNodes.remove(caches.get(which));
                                        }
                                    }
                                });
                                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (actionNodes.size() == 0) {
                                            showToat("请至少选择一个设备");
                                            return;
                                        }
                                        isChange = false;
                                        DataPool.getInstance().setObNode(actionNodes.get(0));
                                        Intent intent = new Intent();
                                        intent.setClass(EditSceneOfSensorAct.this, ControlLampAct.class);
                                        intent.putExtra(IS_ACTION, new byte[5]);
                                        startActivityForResult(intent, ADD_ACTION);
                                    }
                                });
                                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                                builder.show();
                            }

                            @Override
                            public void nOnClick() {

                            }
                        });

            }
        });
    }


    private void editScene(boolean isRemove) {
        this.isRemove = isRemove;
        SpliceTrans spliceTrans = SpliceTrans.getInstance();
        if (spliceTrans == null) {
            showToat("蓝牙未就绪");
            return;
        }
        if (cdtNode == null && !isRemove) {
            showToat("选择条件");
            return;
        }
        spliceTrans.setValueAndSend(MakeSendData.setNodeScene(groupIndex,cachIndex, actionObNode, cdtNode, isHave, action, isRemove));
        showProgressDialog("请稍后", "正在设置", false);
    }

    private int groupIndex;
    @Override
    public void onReceive(Message message) {
        disMissProgressDialog();
        switch (message.what) {
            case OBConstant.ReplyType.ON_SET_SCENE_SUC:
                /*删除*/
                if (isRemove) {
                    actionObNode.getSceneList().remove(cacheNodeScene);
                } else {
                    /*增加*/
                    if (!isChange) {
                        cacheNodeScene = new NodeScene();
                        actionObNode.getSceneList().add(cacheNodeScene);
                    }
                    cacheNodeScene.setIndex(cachIndex);
                    cacheNodeScene.setGroupIndex(groupIndex);
                    cacheNodeScene.setActionAddr(actionObNode.getCplAddr());
                    cacheNodeScene.setAction(action);
                    cacheNodeScene.setCondition(isHave ? new byte[]{1, 0} : new byte[2]);
                    cacheNodeScene.setConditionAddr(cdtNode.getAddr());
                }
                sensorSceneAdapter.notifyDataSetChanged();
                ShareSerializableUtil.getInstance().storageData(DataPool.getInstance());
                /*增加时查看有无后续节点*/
                if (!isChange) {
                    index++;
                    if (index < actionNodes.size()) {
                        actionObNode = actionNodes.get(index);
                        cachIndex = actionObNode.canUseSceneIndex();
                        groupIndex = cachIndex;
                        editScene(false);
                    }
                }
                break;
            case OBConstant.ReplyType.NOT_REPLY:
            case OBConstant.ReplyType.WRONG_TIME_OUT:
                showToat("超时");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        DataPool.getInstance().unRegist(this);
        super.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            /*修改单个*/
            if (requestCode == CHANGE_ACTION) {
                action = data.getByteArrayExtra("action");
                editScene(false);
            /*添加多个*/
            } else if (requestCode == ADD_ACTION) {
                action = data.getByteArrayExtra("action");
                index = 0;
                actionObNode = actionNodes.get(index);
                cachIndex = actionObNode.canUseSceneIndex();
                groupIndex = cachIndex;
                editScene(false);
            }
        }
    }
}
