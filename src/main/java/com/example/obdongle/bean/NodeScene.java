package com.example.obdongle.bean;

import com.example.obdongle.constant.OBConstant;
import com.example.obdongle.data.DataPool;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * 存储于节点的场景
 * <p>
 * Created by adolf_dong on 2018/5/11.
 */

public class NodeScene implements Serializable {

    /**
     * 1-10
     * 场景序号
     */
    private int index;


    /**
     * 1-10
     * 形成场景与关系的序号，此序号相同则底层的情景底层认为形成与关系
     */
    private int groupIndex;
    /**
     * 7字节action完整地址
     */
    private byte[] actionAddr = new byte[7];

    /**
     * 条件类型，包括长度和比较关系
     */
    private byte conditionType = 0x0a;

    /**
     * 传感器地址
     */
    private byte conditionAddr;

    /**
     * 条件，  有状态0100  无状态0000
     */
    private byte[] condition = new byte[2];


    /**
     * 行为
     */
    private byte[] action = new byte[5];

    public int getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(int groupIndex) {
        this.groupIndex = groupIndex;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public byte[] getActionAddr() {
        return actionAddr;
    }

    public void setActionAddr(byte[] actionAddr) {
        this.actionAddr = actionAddr;
    }

    public byte getConditionType() {
        return conditionType;
    }

    public void setConditionType(byte conditionType) {
        this.conditionType = conditionType;
    }

    public byte getConditionAddr() {
        return conditionAddr;
    }

    public void setConditionAddr(byte conditionAddr) {
        this.conditionAddr = conditionAddr;
    }

    public byte[] getCondition() {
        return condition;
    }

    public void setCondition(byte[] condition) {
        this.condition = condition;
    }

    public byte[] getAction() {
        return action;
    }

    public void setAction(byte[] action) {
        this.action = action;
    }

    /**
     * @return action节点
     */
    public ObNode findActionNode() {
        List<ObNode> obNodeList = DataPool.getInstance().getObNodes();
        for (int i = 0; i < obNodeList.size(); i++) {
            ObNode obNode = obNodeList.get(i);
            if (Arrays.equals(obNode.getCplAddr(), actionAddr)) {
                return obNode;
            }
        }
        return null;
    }

    /**
     * @return condition节点
     */
    public ObNode findConditionNode() {
        List<ObNode> obNodeList = DataPool.getInstance().getObNodes();
        for (int i = 0; i < obNodeList.size(); i++) {
            ObNode obNode = obNodeList.get(i);
            if (obNode.getAddr() == conditionAddr && obNode.getParentType() != OBConstant.NodeType.IS_LAMP) {
                return obNode;
            }
        }
        return null;
    }
}
