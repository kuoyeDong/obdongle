package com.example.obdongle;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.obdongle.base.BaseAct;
import com.example.obdongle.bean.ObNode;
import com.example.obdongle.constant.OBConstant;
import com.example.obdongle.data.DataPool;
import com.example.obdongle.databinding.SetSensorActBinding;
import com.example.obdongle.net.Respond;
import com.example.obdongle.net.SpliceTrans;
import com.example.obdongle.util.MakeSendData;
import com.example.obdongle.util.ParseUtil;
import com.example.obdongle.util.ShareSerializableUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置传感器灵敏度、监测时间周期
 * 灵敏度1-200，超过200按200计，
 * 监测时间周期2byte，高位在左，低位在右，单位秒
 * Created by adolf_dong on 2018/5/10.
 */

public class SetSensorAct extends BaseAct implements Respond {
    private ObNode obNode;

    private SetSensorActBinding setSensorActBinding;

    /**
     * 距离下标
     */
    public static final int DISTANCE_INDEX = 1;

    /**
     * 状态
     */
    private byte[] status;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        obNode = DataPool.getInstance().getObNode();
        setSensorActBinding = DataBindingUtil.setContentView(this, R.layout.set_sensor_act);
        DataPool.getInstance().regist(this);
        TextView midTv = (TextView) setSensorActBinding.setSensorTop.findViewById(R.id.mid_tv);
        midTv.setText(obNode.getNodeId());
        setShowData();
        ImageView topLeftImg = (ImageView) setSensorActBinding.setSensorTop.findViewById(R.id.left_img);
        ImageView topRightImg = (ImageView) setSensorActBinding.setSensorTop.findViewById(R.id.right_img);
        topLeftImg.setImageResource(R.drawable.back);
        topRightImg.setImageResource(R.drawable.show_scene);
        topLeftImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        topRightImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(SetSensorAct.this, EditSceneOfSensorAct.class);
                startActivity(intent);
            }
        });
        setSensorActBinding.timeRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canSetting(obNode)) {
                    showToat("该类设备不能设置");
                    return;
                }
                final String[] timeStrs = new String[]{"3", "5", "10", "15", "20", "30", "60"};
                showItemDialog("选择间隔时间(秒)", timeStrs, new ItemDialogLSN() {
                    @Override
                    public void onItemDialogClick(int which) {
                        status = obNode.getState();
                        status[3] = (byte) Integer.parseInt(timeStrs[which]);
                        setSensorLevel();
                    }
                });
            }
        });
        setSensorActBinding.distanceRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canSetting(obNode)) {
                    showToat("该类设备不能设置");
                    return;
                }
                final List<ShowData> distanceStrs = makeLevelStr(obNode);
                String[] showStrs = new String[distanceStrs.size()];
                for (int i = 0; i < distanceStrs.size(); i++) {
                    showStrs[i] = distanceStrs.get(i).showStr;
                }
                showItemDialog("选择灵敏度", showStrs, new ItemDialogLSN() {
                    @Override
                    public void onItemDialogClick(int which) {
                        status = obNode.getState();
                        status[DISTANCE_INDEX] = (byte) distanceStrs.get(which).val;
                        setSensorLevel();
                    }
                });
            }
        });
    }

    private void setShowData() {
        int distance = obNode.getState()[DISTANCE_INDEX] & 0xff;
        int time = ((obNode.getState()[2] & 0xff) << 8) + (obNode.getState()[3] & 0xff);
        setSensorActBinding.watchTimeTv.setText(time == 0 ? "默认值" : (time + "s"));
        if (distance == 0) {
            setSensorActBinding.watchDistanceTv.setText("默认值");
        } else {
            setSensorActBinding.watchDistanceTv.setText("未知");
            int pType = obNode.getParentType();
            int type = obNode.getType();
            switch (pType) {
                case OBConstant.NodeType.IS_SENSOR:
                    switch (type) {
                        case OBConstant.NodeType.FLOOD:
                        case OBConstant.NodeType.DC_RED_SENSOR:
                        case OBConstant.NodeType.RED_SENSOR:
                        case OBConstant.NodeType.DOOR_WINDOW_MAGNET:
                            switch (distance) {
                                case 1:
                                    setSensorActBinding.watchDistanceTv.setText("强");
                                    break;
                                case 3:
                                    setSensorActBinding.watchDistanceTv.setText("一般");
                                    break;
                                case 5:
                                    setSensorActBinding.watchDistanceTv.setText("弱");
                                    break;
                            }
                            break;
                        case OBConstant.NodeType.RADAR:
                        case OBConstant.NodeType.XIBING_RADAR:
                            switch (distance) {
                                case 70:
                                    setSensorActBinding.watchDistanceTv.setText("非常强");
                                    break;
                                case 85:
                                    setSensorActBinding.watchDistanceTv.setText("强");
                                    break;
                                case 100:
                                    setSensorActBinding.watchDistanceTv.setText("一般");
                                    break;
                                case 125:
                                    setSensorActBinding.watchDistanceTv.setText("弱");
                                    break;
                                case 150:
                                    setSensorActBinding.watchDistanceTv.setText("非常弱");
                                    break;
                            }
                            break;
                    }
                    break;
            }
        }
    }


    @Override
    public void onReceive(Message message) {
        disMissProgressDialog();
        switch (message.what) {
            case OBConstant.ReplyType.SET_STATUS_SUC:
                showToat("控制成功！");
                ParseUtil.onSetStatusRec(message, DataPool.getInstance().getObNodes());
                setShowData();
                ShareSerializableUtil.getInstance().storageData(DataPool.getInstance());
                break;
            case OBConstant.ReplyType.SET_STATUS_FAL:
                showToat("控制失败");
                break;
            case OBConstant.ReplyType.NOT_REPLY:
            case OBConstant.ReplyType.WRONG_TIME_OUT:
                showToat("超时");
                break;
        }
    }

    /**
     * 根据传感器类型设置等级显示String
     *
     * @param obNode 要设置的传感器
     */
    private List<ShowData> makeLevelStr(ObNode obNode) {
        int pType = obNode.getParentType() & 0xff;
        int type = obNode.getType() & 0xff;
        List<ShowData> datas = new ArrayList<>();
        switch (pType) {
            case OBConstant.NodeType.IS_SENSOR:
                switch (type) {
//                    case OBConstant.NodeType.FLOOD:
                    case OBConstant.NodeType.DC_RED_SENSOR:
                    case OBConstant.NodeType.RED_SENSOR:
//                    case OBConstant.NodeType.DOOR_WINDOW_MAGNET:
                        /*1 强 3一般  5 弱*/
                        ShowData showData = new ShowData();
                        showData.showStr = "强";
                        showData.val = 1;
                        ShowData showData1 = new ShowData();
                        showData1.showStr = "一般";
                        showData1.val = 3;
                        ShowData showData2 = new ShowData();
                        showData2.showStr = "弱";
                        showData2.val = 5;
                        datas.add(showData);
                        datas.add(showData1);
                        datas.add(showData2);
                        return datas;
                    case OBConstant.NodeType.RADAR:
                    case OBConstant.NodeType.XIBING_RADAR:
                        /*70非常强，85强，100一般，125弱，150非常弱*/
                        showData = new ShowData();
                        showData.showStr = "非常强";
                        showData.val = 70;
                        showData1 = new ShowData();
                        showData1.showStr = "强";
                        showData1.val = 85;
                        showData2 = new ShowData();
                        showData2.showStr = "一般";
                        showData2.val = 100;
                        ShowData showData3 = new ShowData();
                        showData3.showStr = "弱";
                        showData3.val = 125;
                        ShowData showData4 = new ShowData();
                        showData4.showStr = "非常弱";
                        showData4.val = 150;
                        datas = new ArrayList<>();
                        datas.add(showData);
                        datas.add(showData1);
                        datas.add(showData2);
                        datas.add(showData3);
                        datas.add(showData4);
                        return datas;
                }

                break;
        }
        return datas;
    }

    class ShowData {
        String showStr;
        int val;
    }

    /**
     * 开始设置
     */
    private void setSensorLevel() {
        SpliceTrans spliceTrans = SpliceTrans.getInstance();
        if (spliceTrans == null) {
            showToat("蓝牙未连接");
            return;
        }
        spliceTrans.setValueAndSendShort(MakeSendData.setNodeState(obNode, status),true);
        showProgressDialog("请稍后", "正在设置", false);
    }

    @Override
    protected void onDestroy() {
        DataPool.getInstance().unRegist(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataPool.getInstance().setObNode(obNode);
    }

    /**
     * @param obNode 是否可设置
     * @return 可以返回true
     */
    private boolean canSetting(ObNode obNode) {
        int pType = obNode.getParentType();
        int type = obNode.getType();
        switch (pType) {
            case OBConstant.NodeType.IS_SENSOR:
                switch (type) {
                    case OBConstant.NodeType.DC_RED_SENSOR:
                    case OBConstant.NodeType.RED_SENSOR:
                    case OBConstant.NodeType.RADAR:
                    case OBConstant.NodeType.XIBING_RADAR:
                        return true;
                }
                break;
        }

        return false;
    }
}
