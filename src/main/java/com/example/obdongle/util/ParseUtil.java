package com.example.obdongle.util;

import android.os.Message;
import android.util.Log;

import com.example.obdongle.bean.ObNode;
import com.example.obdongle.constant.OBConstant;

import java.util.Arrays;
import java.util.List;

/**
 * 解析操作返回数据
 * Created by adolf_dong on 2018/4/24.
 */

public class ParseUtil {


    public static byte[] getBytes(Message msg) {
        return msg.getData().getByteArray("obkeydata");
    }

    public static int[] index = new int[65];

    static {
        for (int i = 0; i < index.length; i++) {
            index[i] = i - 1;
        }
    }

    /**
     * 设置节点状态返回
     *
     * @param message 传入message
     * @param obNodes 节点容器
     */
    public static void onSetStatusRec(Message message, List<ObNode> obNodes) {
        byte[] bytes = getBytes(message);
        if (bytes == null) {
            return;
        }
        if (obNodes == null) {
            return;
        }
        byte[] status;
        byte[] rfAddr = Arrays.copyOfRange(bytes, 8, 13);
        int addr = bytes[14] & 0xff;
        ObNode obNode = null;
        for (ObNode cacheobNode :
                obNodes) {
            if (Arrays.equals(cacheobNode.getRfAddr(), rfAddr) && addr == cacheobNode.getAddr()) {
                obNode = cacheobNode;
            }
        }
        if (obNode == null) {
            return;
        }
        if (obNode.getParentType() == OBConstant.NodeType.AEROCRAFT_4WING) {
            status = Arrays.copyOfRange(bytes, 15, 15 + 10);
        } else {
            status = Arrays.copyOfRange(bytes, 15, 15 + 7);
        }
        switch (message.what) {
            case OBConstant.ReplyType.SET_STATUS_SUC:
                obNode.setState(status);
                break;
        }
    }

    /**
     * 此方法只处理收到新节点的情况，其他2003返回在msg.what处理
     * 设备类型，设备子类型，设备id，序列号， 完整地址
     *
     * @param msg 数据
     * @return 本机已经有缓存数据，返回false,否则返回true
     */
    public static ObNode parseNewNode(Message msg, List<ObNode> obNodes) {
        byte[] bytes = getBytes(msg);
        byte parentType = (byte) MathUtil.byteIndexValid(bytes[index[9]], 0, 7);
        byte type = bytes[index[10]];
        byte[] sernums = Arrays.copyOfRange(bytes, index[11], index[11] + 5);
        byte[] rfAddr = Arrays.copyOfRange(bytes, index[11] + 5, index[11] + 5 + 5);
        byte groupAddr = bytes[index[11] + 5 + 5];
        byte addr = bytes[index[11] + 5 + 5 + 1];
        String name = null;
        switch (parentType) {
            case OBConstant.NodeType.IS_LAMP:
                name = "Lamp";
                break;
            case OBConstant.NodeType.IS_SENSOR:
                switch (type) {
                    case OBConstant.NodeType.FLOOD:
                        name = "Water";
                        break;
                    case OBConstant.NodeType.RADAR:
                    case OBConstant.NodeType.XIBING_RADAR:
                        name = "Radar";
                        break;
                    case OBConstant.NodeType.DC_RED_SENSOR:
                    case OBConstant.NodeType.RED_SENSOR:
                        name = "Infrared";
                        break;
                    case OBConstant.NodeType.DOOR_WINDOW_MAGNET:
                        name = "Magnetic";
                        break;
                    case OBConstant.NodeType.LIGHT_SENSOR:
                        name = "Light sensor";
                        break;
                }
                break;
            default:
                name = "unknow";
                break;
        }
        byte[] id = (name + addr).getBytes();
        Log.d(TAG, "parseNewNode: name = " + new String(id));
        ObNode newObNode = new ObNode(parentType, type, id, sernums,
                rfAddr, groupAddr, addr);
        if (parentType == 1) {
            newObNode.setState(new byte[]{20, 0, 0, 0, 0, 0, 1});
        }
        // FIXME: 2017/10/25 新入网的节点可能已经存在
        // rf地址没变化，覆盖参数；rf地址有变化，从原先的obox节点列表删除，添加到新入网时所在obox节点列表
        for (ObNode obNode : obNodes) {
            if (Arrays.equals(obNode.getSerNum(), newObNode.getSerNum())) {
                obNodes.remove(obNode);
                obNodes.add(newObNode);
                return newObNode;
            }
        }
        obNodes.add(newObNode);
        return newObNode;
    }

    private static final String TAG = "ParseUtil";

    /**
     * 新增分组，删除或者重命名组或者节点的处理
     *
     * @param message 参数
     * @param obNodes 装载节点的集合
     */
    public static void onDeleteNode(Message message, List<ObNode> obNodes) {
        switch (message.what) {
            case OBConstant.ReplyType.EDIT_NODE_OR_GROUP_SUC:
                byte[] datas = ParseUtil.getBytes(message);
                switch (MathUtil.byteIndexValid(datas[index[9]], 0, 2)) {
                    /*删除*/
                    case 0:
                        /*5 rf地址 1组地址 1节点地址*/
                        byte[] rfaddr = Arrays.copyOfRange(datas, index[9 + 1], index[9 + 1 + 5]);
                        int addr = datas[index[9 + 1 + 5 + 1]] & 0xff;
                        for (int i = 0; i < obNodes.size(); i++) {
                            ObNode obNode = obNodes.get(i);
                            if (Arrays.equals(rfaddr, obNode.getRfAddr()) && addr == obNode.getAddr()) {
                                obNodes.remove(obNode);
                                break;
                            }
                        }
                        break;
                }
                break;
            case OBConstant.ReplyType.EDIT_NODE_OR_GROUP_FAL:

                break;
        }
    }

    /**
     * 解析obox,对obox各项参数进行设置,序列号，版本号
     *
     * @param msg msg
     */
    public static byte[] parseObox(Message msg) {
        byte[] bytes = getBytes(msg);
        byte[] serNum = Arrays.copyOfRange(bytes, index[11], index[11] + 5);
        byte[] version = Arrays.copyOfRange(bytes, index[11] + 5, index[11] + 5 + 8);
        return serNum;
    }
}
