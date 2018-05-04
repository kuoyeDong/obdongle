package com.example.obdongle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.obdongle.adapter.SceneAdapter;
import com.example.obdongle.base.BaseAct;
import com.example.obdongle.bean.ObNode;
import com.example.obdongle.constant.OBConstant;
import com.example.obdongle.data.DataPool;
import com.example.obdongle.net.Respond;
import com.example.obdongle.net.SpliceTrans;
import com.example.obdongle.util.MakeSendData;
import com.example.obdongle.util.MathUtil;
import com.example.obdongle.util.ShareSerializableUtil;
import com.example.obdongle.util.Transformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.obdongle.ControlLampAct.IS_ACTION;

/**
 * 编辑设备场景页面
 * Created by adolf_dong on 2018/5/2.
 */
@SuppressWarnings("deprecation")
public class EditSceneAct extends BaseAct implements Respond {
    private SceneAdapter sceneAdapter;

    private LinearLayout editPanel;

    private Button editActionBtn;
    private Button editConditionBtn;
    private Button sureBtn;
    private Button cacelBtn;

    /**
     * action节点
     */
    private ObNode obNode;

    /**
     * 传感器节点
     */
    private ObNode cdtNode;
    /**
     * 场景字符串
     */
    private String sceneStr;

    private SpliceTrans spliceTrans;
    /**
     * 行为
     */
    private byte[] action;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_scene_act);
        DataPool.getInstance().regist(this);
        View topView = findViewById(R.id.edit_scene_top);
        ImageView topLeftImg = (ImageView) topView.findViewById(R.id.left_img);
        TextView topMidTv = (TextView) topView.findViewById(R.id.mid_tv);
        //情景列表
        ListView sceneLv = (ListView) findViewById(R.id.scene_lv);
        //添加场景按钮
        Button addSceneBtn = (Button) findViewById(R.id.add_scene_btn);
        topLeftImg.setImageDrawable(getResources().getDrawable(R.drawable.back));
        topMidTv.setText("场景");
        editPanel = (LinearLayout) findViewById(R.id.edit_panel);
        editPanel.setVisibility(View.GONE);
        editActionBtn = (Button) editPanel.findViewById(R.id.edit_action);
        editActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(EditSceneAct.this, ControlLampAct.class);
                intent.putExtra(IS_ACTION, true);
                startActivityForResult(intent, 100);
            }
        });
        editConditionBtn = (Button) editPanel.findViewById(R.id.edit_condition);
        editConditionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<ObNode> sensorList = new ArrayList<>();
                List<ObNode> obNodeList = DataPool.getInstance().getObNodes();
                for (int i = 0; i < obNodeList.size(); i++) {
                    ObNode obNode = obNodeList.get(i);
                    if (obNode.getParentType() != OBConstant.NodeType.IS_LAMP) {
                        sensorList.add(obNode);
                    }
                }
                String[] sensorIds = new String[sensorList.size()];
                for (int i = 0; i < sensorList.size(); i++) {
                    sensorIds[i] = sensorList.get(i).getNodeId();
                }
                showItemDialog("选择一个传感器", sensorIds, new ItemDialogLSN() {
                    @Override
                    public void onItemDialogClick(int which) {
                        cdtNode = sensorList.get(which);
                        final String[] conditonStr = new String[]{"有状态", "无状态"};
                        showItemDialog("选择条件", conditonStr, new ItemDialogLSN() {
                            @Override
                            public void onItemDialogClick(int which) {
                                isOpen = which == 0;
                                editConditionBtn.setText(conditonStr[which]);
                            }
                        });
                    }
                });
            }
        });
        sureBtn = (Button) editPanel.findViewById(R.id.sure);
        sureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editScene();
            }
        });
        cacelBtn = (Button) editPanel.findViewById(R.id.cancel);
        cacelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPanel.setVisibility(View.GONE);
            }
        });
        topLeftImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        obNode = DataPool.getInstance().getObNode();
        sceneAdapter = new SceneAdapter(this, obNode);
        sceneLv.setAdapter(sceneAdapter);
        sceneLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editPanel.setVisibility(View.VISIBLE);
                index = position + 1;
                sceneStr = obNode.getActionMap().get("" + index);
                action = Transformation.hexString2Bytes(sceneStr.substring(24, 24 + 10));
                editActionBtn.setText(Transformation.byteArryToHexString(action));
                editConditionBtn.setText(MathUtil.byteArrayIsZero(Transformation.hexString2Bytes(sceneStr.substring(20, 24))) ? "无状态" : "有状态");
            }
        });
        addSceneBtn.setText("添加场景");
        addSceneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FIXME: 2018/5/2 判断当前场景数量决定是否创建场景
                if (sceneAdapter.getCount() < 3) {
                    sceneStr = null;
                    editPanel.setVisibility(View.VISIBLE);
                    editActionBtn.setText("点击修改行为");
                    editConditionBtn.setText("点击修改条件");
                    index = sceneAdapter.getCount() + 1;
                    action = new byte[5];
                } else {
                    showToat("最多支持三个场景");
                }
            }
        });
    }

    @Override
    public void onReceive(Message message) {
        disMissProgressDialog();
        switch (message.what) {
            case OBConstant.ReplyType.ON_SET_SCENE_SUC:
                obNode.getActionMap().put(index + "", sceneStr);
                sceneAdapter.notifyDataSetChanged();
                editPanel.setVisibility(View.GONE);
                ShareSerializableUtil.getInstance().storageData(DataPool.getInstance());
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataPool.getInstance().unRegist(this);
    }

    /**
     * 开始交互
     */
    private void editScene() {
        spliceTrans = SpliceTrans.getInstance();
        if (spliceTrans == null) {
            showToat("蓝牙未就绪");
            return;
        }
        /*场景的目标数据*/
        byte[] sceneGoalbytes = MakeSendData.setNodeScene(index, obNode, cdtNode, isOpen, action);
        sceneStr = Transformation.byteArryToHexString(Arrays.copyOfRange(sceneGoalbytes, 7, 7 + 17));
        spliceTrans.setValueAndSend(sceneGoalbytes);
    }

    /**
     * 单condition是否有状态
     */
    private boolean isOpen;
    /**
     * key
     */
    private int index;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                action = data.getByteArrayExtra("action");
                editActionBtn.setText(Transformation.byteArryToHexString(action));
            }
        }
    }


}
