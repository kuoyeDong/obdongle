package com.example.obdongle.util;

import android.util.Log;

import com.example.obdongle.bean.ObNode;

import java.util.List;

/**
 * 产生发送指令
 * Created by adolf_dong on 2018/4/25.
 */

public class MakeSendData {
    public static int[] index = new int[65];

    static {
        for (int i = 0; i < index.length; i++) {
            index[i] = i - 1;
        }
    }

    private static final String TAG = "MakeSendData";

    /**
     * 设置任何设备的状态，主要的状态设置在设置页面中设定
     *
     * @param obNode obnode设备
     * @param status 状态
     */
    public static byte[] setNodeState(ObNode obNode, byte[] status) {
        byte[] cmd = new byte[64];
        cmd[index[5]] = (byte) 0x81;
        System.arraycopy(obNode.getCplAddr(), 0, cmd, index[8], obNode.getCplAddr().length);
        cmd[index[8 + 5]] = 0;
        System.arraycopy(status, 0, cmd, index[15], status.length);
        cmd[61] = 0x55;
        Log.d(TAG, "setNodeState: 设置状态");
        return cmd;
    }


    /**
     * 开始或者停止扫描节点
     *
     * @param mode       1.开启扫描，0.关闭扫描，2.通知所有节点可以重新入网,3节点主动入网
     * @param time       扫描时间 30s-60s
     * @param oboxSer    obox序列号
     * @param indexBytes 节点下标数组。0为已经有节点 ，1为空
     * @return
     */
    public static byte[] rfCmd(int mode, int time, byte[] oboxSer, byte[] indexBytes) {
        byte[] cmd = new byte[64];
        cmd[index[6]] = 0x03;
        cmd[index[8]] = (byte) mode;
        cmd[index[12]] = (byte) time;
        cmd[index[12] + 1 + 5 + 1 + 1 + 1 + 1 + 1] = (byte) 0x06;
        System.arraycopy(oboxSer, 0, cmd, index[12] + 1 + 5 + 1 + 1 + 1 + 1 + 1 + 1, oboxSer.length);
        System.arraycopy(indexBytes, 0, cmd, index[12] + 1 + 5 + 1 + 1 + 1 + 1 + 1 + 1 + 5, indexBytes.length);
        cmd[index[62]] = 0x55;
        Log.d(TAG, " 扫描设备" + mode);
        return cmd;
    }

    /**
     * 设置节点的情景,此情景由节点存储
     *
     * @param sceneIndex    行为节点的情景下标
     * @param actionNode    行为节点
     * @param conditionNode 条件节点
     *                      完整地址 7， 编号1 类型1（0x0a为一字节等于），传感器地址1，传感器比较状态2，行为5
     */
    public static byte[] setNodeScene(int sceneIndex, ObNode actionNode, ObNode conditionNode,boolean isOpen,byte[] action) {
        byte[] cmd = new byte[64];
        cmd[index[5]] = (byte) 0x80;
        cmd[index[6]] = (byte) 0x21;
        System.arraycopy(actionNode.getCplAddr(), 0, cmd, index[8], 7);
        cmd[index[8] + 7] = (byte) sceneIndex;
        cmd[index[8] + 7 + 1] = 0x0a;
        cmd[index[8] + 7 + 1 + 1] = conditionNode.getAddr();
        cmd[index[8] + 7 + 1 + 1 + 1 + 1] = (byte) (isOpen ? 1 : 0);
        System.arraycopy(action,0,cmd,cmd[index[8] + 7 + 1 + 1 + 1 + 1+1],action.length);
        return cmd;
    }


    /**
     * 新增（只对组）、删除、重命名设置节点或者组名称
     *
     * @param obNode 单节点操作传，否则为null
     */
    public static byte[] deleteNode(ObNode obNode) {
        byte[] cmd = new byte[64];
        cmd[index[5]] = (byte) 0x80;
        cmd[index[6]] = (byte) 0x04;
        cmd[index[8]] = (byte) 0;
        System.arraycopy(obNode.getRfAddr(), 0, cmd, index[9], obNode.getRfAddr().length);
        cmd[index[9] + obNode.getRfAddr().length] = 0;
        cmd[index[9] + obNode.getRfAddr().length + 1] = obNode.getAddr();
        cmd[61] = 0x55;
        Log.d(TAG, "deleteNode:删除节点");
        return cmd;
    }

    /**
     * 获取obox的信息
     */
    public static byte[] reqOboxMsg() {
        byte[] cmd = new byte[64];
        cmd[4] = (byte) 0x80;
        cmd[5] = 0x13;
        cmd[7] = 0x03;
        cmd[8] = 0x0a;
        cmd[9] = 0x01;
        cmd[61] = 0x55;
        Log.d(TAG, "reqOboxMsg: 获取obox信息");
        return cmd;
    }
}
