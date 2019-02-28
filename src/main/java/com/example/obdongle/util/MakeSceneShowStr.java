package com.example.obdongle.util;

import com.example.obdongle.bean.NodeScene;
import com.example.obdongle.bean.ObNode;
import com.example.obdongle.constant.OBConstant;

import java.util.Arrays;
import java.util.List;

/**
 * 解析原始场景数据为可视化显示
 * 原始语句为：7字节行为节点完整地址+1字节编号+1字节类型+1字节传感器地址+2字节传感器状态+5字节行为
 * Created by adolf_dong on 2018/5/10.
 */

public class MakeSceneShowStr {
    /**
     * 获取显示action表达语句
     *
     * @param nodeScene 场景
     * @return 非工程表达语句
     */
    public static String makeShowActionStr(NodeScene nodeScene, ObNode obNode) {
        byte[] action = nodeScene.getAction();
        int pType = obNode.getParentType() & 0xff;
        int type = obNode.getType() & 0xff;
        switch (pType) {
            case OBConstant.NodeType.IS_LAMP:
                switch (type) {
                    case OBConstant.NodeType.IS_WARM_LAMP:
                        int light = action[0] & 0xff;
                        switch (light) {
                            case 100:
                                return obNode.getNodeId() + "亮度值" + "降低" + "色温比" + (action[1] & 0xff);
                            case 101:
                                return obNode.getNodeId() + "亮度值" + "增加" + "色温比" + (action[1] & 0xff);
                            default:
                                return obNode.getNodeId() + "亮度值" + (action[0] & 0xff) + "色温比" + (action[1] & 0xff);
                        }
                }
                break;
        }
        return null;
    }

    /**
     * 获取显示语句
     *
     * @param nodeScene 原始语句
     * @param obNode    行为节点
     * @param obNodes   根据地址在此集合查找目标传感器
     * @return 显示信息
     */
    public static String makeShowScene(NodeScene nodeScene, ObNode obNode, List<ObNode> obNodes) {
        ObNode cdtNode = null;
        for (int i = 0; i < obNodes.size(); i++) {
            ObNode obNode1 = obNodes.get(i);
            if (obNode1.getAddr() == nodeScene.getConditionAddr() && obNode1.getParentType() != OBConstant.NodeType.IS_LAMP) {
                cdtNode = obNode1;
                break;
            }
        }
        if (cdtNode == null) {
            return "传感器已不在系统内，场景已失效";
        }
        return "当" + cdtNode.getNodeId() + showConditionWithBool(cdtNode, !Arrays.equals(nodeScene.getCondition(), new byte[2])) + "时:"
                + makeShowActionStr(nodeScene, obNode);
    }

    /**
     * 显示条件
     *
     * @param nodeScene 原始数据
     * @param obNodes   根据地址在此集合查找目标传感器
     * @return 显示信息
     */
    public static String makeShowCondition(NodeScene nodeScene, List<ObNode> obNodes) {
        ObNode cdtNode = null;
        for (int i = 0; i < obNodes.size(); i++) {
            ObNode obNode1 = obNodes.get(i);
            if (obNode1.getAddr() == nodeScene.getConditionAddr() && obNode1.getParentType() != OBConstant.NodeType.IS_LAMP) {
                cdtNode = obNode1;
                break;
            }
        }
        if (cdtNode == null) {
            return "传感器已不在系统内，场景已失效";
        }
        return "当" + cdtNode.getNodeId() + showConditionWithBool(cdtNode, !Arrays.equals(nodeScene.getCondition(), new byte[2]));
    }

    /**
     * 显示不同类型传感器有无状态的对应条件
     *
     * @param obNode 条件节点
     * @param isHave 是否有状态
     * @return 显示数据
     */
    public static String showConditionWithBool(ObNode obNode, boolean isHave) {
        int pType = obNode.getParentType() & 0xff;
        int type = obNode.getType() & 0xff;
        switch (pType) {
            case OBConstant.NodeType.IS_SENSOR:
                switch (type) {
                    case OBConstant.NodeType.FLOOD:
                        return isHave ? "有水" : "无水";
                    case OBConstant.NodeType.RADAR:
                    case OBConstant.NodeType.XIBING_RADAR:
                        return isHave ? "有人" : "无人";
                    case OBConstant.NodeType.DC_RED_SENSOR:
                    case OBConstant.NodeType.RED_SENSOR:
                        return isHave ? "有人" : "无人";
                    case OBConstant.NodeType.DOOR_WINDOW_MAGNET:
                        return isHave ? "开启" : "关闭";
                    case OBConstant.NodeType.LIGHT_SENSOR:
                        return isHave ? "光线强" : "光线弱";
                }
                break;
        }
        return "未知";
    }
}
