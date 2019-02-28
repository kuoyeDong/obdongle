package com.example.obdongle;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.obdongle.adapter.SceneAdapter;
import com.example.obdongle.base.BaseAct;
import com.example.obdongle.bean.NodeScene;
import com.example.obdongle.bean.ObNode;
import com.example.obdongle.constant.OBConstant;
import com.example.obdongle.data.DataPool;
import com.example.obdongle.net.Respond;
import com.example.obdongle.net.SpliceTrans;
import com.example.obdongle.util.GetIconFromType;
import com.example.obdongle.util.MakeSendData;
import com.example.obdongle.util.ShareSerializableUtil;
import com.example.obdongle.widget.ShowNodeSceneDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.obdongle.ControlLampAct.IS_ACTION;

/**
 * 编辑设备场景页面（可控制设备）
 * Created by adolf_dong on 2018/5/2.
 */
@SuppressWarnings("deprecation")
public class EditSceneAct extends BaseAct implements Respond {
    private SceneAdapter sceneAdapter;


    /**
     * action节点
     */
    private ObNode obNode;

    /**
     * 传感器节点
     */
    private ObNode cdtNode;
    /**
     * 场景
     */
    private NodeScene nodeScene;

    /**
     * 行为
     */
    private byte[] action;
    /**
     * 单condition是否有状态
     */
    private boolean isOpen;
    /**
     * key
     */
    private int index;

    private GetIconFromType getIconFromType = new GetIconFromType();
    private List<ObNode> obNodeList;
    /**
     * 编辑场景的action请求码
     */
    public static final int EDIT_ACTION_CODE = 100;

    /**
     * 是否删除
     */
    private boolean isRemove;
    /**
     * 与位
     */
    private int groupIndex;

    /**
     * 修改action和批量删除删除缓存集合,非批量时设置为null即可
     */
    private List<NodeScene> nodeScenes;
    /**
     * 批量操作的记录
     */
    private int operatIndex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_scene_act);
        DataPool.getInstance().regist(this);
        obNodeList = DataPool.getInstance().getObNodes();
        View topView = findViewById(R.id.edit_scene_top);
        ImageView topLeftImg = (ImageView) topView.findViewById(R.id.left_img);
        TextView topMidTv = (TextView) topView.findViewById(R.id.mid_tv);
        //情景列表
        final ListView sceneLv = (ListView) findViewById(R.id.scene_lv);
        //添加场景按钮
        Button addSceneBtn = (Button) findViewById(R.id.add_scene_btn);
        topLeftImg.setImageDrawable(getResources().getDrawable(R.drawable.back));
        topMidTv.setText("场景");
        topLeftImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        obNode = DataPool.getInstance().getObNode();
        sceneAdapter = new SceneAdapter(this, obNode, obNodeList);
        sceneLv.setAdapter(sceneAdapter);
        sceneLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //noinspection unchecked
                List<NodeScene> nodeScenes = (List<NodeScene>) sceneAdapter.getItem(position);
                edit(nodeScenes);
            }
        });
        sceneLv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                showItemDialog(null, new String[]{"删除"}, new ItemDialogLSN() {
                    @Override
                    public void onItemDialogClick(int which) {
                        switch (which) {
                            case 0:
                                //noinspection unchecked
                                nodeScenes = (List<NodeScene>) sceneAdapter.getItem(position);
                                operatIndex = 0;
                                nodeScene = nodeScenes.get(operatIndex);
                                index = nodeScene.getIndex();
                                groupIndex = nodeScene.getGroupIndex();
                                editScene(true);
                                break;
                        }
                    }
                });

                return true;
            }
        });
        addSceneBtn.setText("添加场景");
        addSceneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FIXME: 2018/5/2 判断当前场景数量决定是否创建场景
                nodeScenes = null;
                creat(true);
            }
        });
    }

    /**选择并设置condition，非批量操作，交互缓存集合设置为null
     * @param isCreatFirst 是否创建一个独立的场景
     */
    private void toSetCondition(final boolean isCreatFirst) {
        final List<ObNode> sensorList = new ArrayList<>();
        for (int i = 0; i < obNodeList.size(); i++) {
            ObNode obNode = obNodeList.get(i);
            if (obNode.getParentType() != OBConstant.NodeType.IS_LAMP) {
                sensorList.add(obNode);
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(EditSceneAct.this);
        builder.setTitle("选择一个传感器");
        builder.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return sensorList.size();
            }

            @Override
            public Object getItem(int position) {
                return sensorList.get(position);
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
                    convertView = LayoutInflater.from(EditSceneAct.this).inflate(R.layout.img_tv_item, null);
                    viewHolder = new ViewHolder();
                    viewHolder.imageView = (ImageView) convertView.findViewById(R.id.img);
                    viewHolder.textView = (TextView) convertView.findViewById(R.id.tv);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                ObNode obNode = sensorList.get(position);
                viewHolder.imageView.setImageResource(getIconFromType.getDrawResIdForType(obNode));
                viewHolder.textView.setText(obNode.getNodeId());
                return convertView;
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cdtNode = sensorList.get(which);
                final String[] conditonStr = new String[]{"有状态", "无状态"};
                showItemDialog("选择条件", conditonStr, new ItemDialogLSN() {
                    @Override
                    public void onItemDialogClick(int which) {
                        isOpen = which == 0;
                        if (isCreatFirst) {
                            toSetAction(new byte[5]);
                        } else {
                            editScene(false);
                        }
                    }
                });
            }
        });
        builder.show();
    }

    /**
     * 设置action
     */
    private void toSetAction(byte[] action) {
        Intent intent = new Intent();
        intent.setClass(EditSceneAct.this, ControlLampAct.class);
        intent.putExtra(IS_ACTION, action);
        startActivityForResult(intent, EDIT_ACTION_CODE);
    }


    @Override
    public void onReceive(Message message) {
        disMissProgressDialog();
        switch (message.what) {
            case OBConstant.ReplyType.ON_SET_SCENE_SUC:
                if (isRemove) {
                    obNode.getSceneList().remove(nodeScene);
                } else {
                    if (nodeScene == null) {
                    /*添加*/
                        nodeScene = new NodeScene();
                        obNode.getSceneList().add(nodeScene);
                    }
                    nodeScene.setIndex(index);
                    nodeScene.setGroupIndex(groupIndex);
                    nodeScene.setActionAddr(obNode.getCplAddr());
                    nodeScene.setAction(action);
                    nodeScene.setCondition(isOpen ? new byte[]{1, 0} : new byte[2]);
                    nodeScene.setConditionAddr(cdtNode.getAddr());
                }
                sceneAdapter.notifyDataSetChanged();
                ShareSerializableUtil.getInstance().storageData(DataPool.getInstance());
                /*以上为页面数据处理，以下为交互是否结束判断*/
                operatIndex++;
                if (nodeScenes != null) {
                    if (operatIndex < nodeScenes.size()) {
                        nodeScene = nodeScenes.get(operatIndex);
                        groupIndex = nodeScene.getGroupIndex();
                        index = nodeScene.getIndex();
                        cdtNode = nodeScene.findConditionNode();
                        isOpen = !Arrays.equals(nodeScene.getCondition(), new byte[2]);
                        editScene(isRemove);
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


    /**
     * 开始交互
     *
     * @param isRemove 是否删除场景
     */
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
        /*场景的目标数据*/
        byte[] sceneGoalbytes = MakeSendData.setNodeScene(groupIndex, index, obNode, cdtNode, isOpen, action, isRemove);
        spliceTrans.setValueAndSend(sceneGoalbytes);
        showProgressDialog("请稍后", "正在设置", false);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_ACTION_CODE) {
            if (resultCode == RESULT_OK) {
                action = data.getByteArrayExtra("action");
                editScene(false);
            }
        }
    }

    class ViewHolder {
        ImageView imageView;
        TextView textView;
    }

    /**
     * 新建场景
     */
    private void creat(boolean isCreatFirst) {
        if (obNode.canAddScene()) {
            nodeScene = null;
            index = obNode.canUseSceneIndex();
            groupIndex = index;
            toSetCondition(isCreatFirst);
        } else {
            showToat("最多支持10个场景");
        }
    }


    /**
     * 编辑场景界面
     *
     * @param nodeScenes 编辑的场景
     */
    private void edit(final List<NodeScene> nodeScenes) {
        ShowNodeSceneDialog showNodeSceneDialog = new ShowNodeSceneDialog(this, R.style.Dialog, nodeScenes, obNode) {
            @Override
            protected void onAddCondition(int groupIndex, byte[] action) {
                EditSceneAct.this.nodeScenes = null;
                creat(false);
                /*追加的场景需要改变groupIndex*/
                EditSceneAct.this.groupIndex = groupIndex;
                EditSceneAct.this.action = action;
            }

            @Override
            protected void onEditAction(List<NodeScene> nodeScenes) {
                EditSceneAct.this.nodeScenes = nodeScenes;
                operatIndex = 0;
                nodeScene = nodeScenes.get(operatIndex);
                groupIndex = nodeScene.getGroupIndex();
                index = nodeScene.getIndex();
                cdtNode = nodeScene.findConditionNode();
                isOpen = !Arrays.equals(nodeScene.getCondition(), new byte[2]);
                byte[] action = nodeScenes.get(0).getAction();
                toSetAction(action);
            }

            @Override
            protected void onEditCondition(NodeScene nodeScene) {
                EditSceneAct.this.nodeScene = nodeScene;
                index = nodeScene.getIndex();
                groupIndex = nodeScene.getGroupIndex();
                action = nodeScene.getAction();
                EditSceneAct.this.nodeScenes = null;
                toSetCondition(false);
            }

            @Override
            protected void onRemoveCondition(NodeScene nodeScene) {
                EditSceneAct.this.nodeScene = nodeScene;
                index = nodeScene.getIndex();
                groupIndex = nodeScene.getGroupIndex();
                showItemDialog(null, new String[]{"删除"}, new ItemDialogLSN() {
                    @Override
                    public void onItemDialogClick(int which) {
                        switch (which) {
                            case 0:
                                EditSceneAct.this.nodeScenes = null;
                                editScene(true);
                                break;
                        }
                    }
                });
            }
        };
        showNodeSceneDialog.show();
    }

}
