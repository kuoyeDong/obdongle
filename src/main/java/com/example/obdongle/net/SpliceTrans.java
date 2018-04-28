package com.example.obdongle.net;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.obdongle.constant.OBConstant;
import com.example.obdongle.util.MathUtil;
import com.example.obdongle.util.Transformation;

import java.util.Arrays;

/**
 * 分包传输类
 * 从原68字节去掉前8字节，后3字节，缩减为有效的57字节 ，3包20字节传输完成，每包携带19字节有效数据
 * 间隔30ms发送一个包，判定是否繁忙状态
 * Created by adolf_dong on 2018/4/24.
 */

public class SpliceTrans {

    private static final String TAG = "SpliceTrans";

    private static SpliceTrans spliceTrans;

    private BluetoothGattCharacteristic characteristic;
    private BluetoothLeService mBluetoothLeService;
    private Handler mHandler;

    public static SpliceTrans getInstance() {
        return spliceTrans;
    }

    public SpliceTrans(BluetoothGattCharacteristic characteristic, BluetoothLeService mBluetoothLeService, Handler mHandler) {
        this.characteristic = characteristic;
        this.mBluetoothLeService = mBluetoothLeService;
        this.mHandler = mHandler;
        spliceTrans = this;
    }

    /**
     * 记录上一条数据
     */
    private byte[] beforeData;

    /**
     * 为了使用解析类，补充到64字节
     */
    private byte[] plusReciveData = new byte[64];
    /**
     * 是否繁忙
     */
    public boolean isBusy;
    /**
     * 间隔时间，单位毫秒
     */
    private static final int TIME = 30;

    private byte[] payloadFragment = new byte[20];

    /**
     * 字节数组下标
     */
    private static int[] index = new int[65];

    static {
        for (int i = 0; i < index.length; i++) {
            index[i] = i - 1;
        }
    }

    /**
     * 设置要发送数据并开始发送，采用原64字节方式，在方法内获取57字节
     *
     * @param payload 要发送的原始64字节数据
     */
    public void setValueAndSend(byte[] payload) {
        if (isBusy || spliceTrans == null) {
            return;
        }
        isBusy = true;
        beforeData = payload;
        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(TIME);
                int index = i + 1;
                payloadFragment[0] = (byte) index;
                System.arraycopy(payload, i * 19 + 4, payloadFragment, 1, 19);
                send(payloadFragment);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "send:all " + Transformation.byteArryToHexString(payload));
        isBusy = false;
    }

    private void send(byte[] payloadFragment) {
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        characteristic.setValue(payloadFragment);
        mBluetoothLeService.writeCharacteristic(characteristic);
        Log.d(TAG, "send:fragment " + Transformation.byteArryToHexString(payloadFragment));
    }

    /**
     * 接收到数据
     */
    public void onRecieve() {
        byte[] recieveBytes = characteristic.getValue();
        int index = recieveBytes[0] & 0x07;
        System.arraycopy(recieveBytes, 1, plusReciveData, (index - 1) * 19 + 4, 19);
        Log.d(TAG, "onRecieve:fragment " + Transformation.byteArryToHexString(recieveBytes));
        if (index == 3) {
            Log.d(TAG, "onRecieve: all" + Transformation.byteArryToHexString(plusReciveData));
            handleRecieve(plusReciveData);
        }
    }

    /**
     * 处理接收数据
     *
     * @param reciveData 补充64字节后的接收数据
     */
    private void handleRecieve(byte[] reciveData) {
        mHandler.removeMessages(OBConstant.ReplyType.NOT_REPLY);
        Message msg = Message.obtain();
        if (reciveData.length == 5) {
            msg.what = OBConstant.ReplyType.ON_SET_MODE;
            mHandler.sendMessage(msg);
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putByteArray(OBConstant.StringKey.KEY, reciveData);
        msg.setData(bundle);

        int cmd = ((reciveData[4] & 0xff) << 8) + (reciveData[5] & 0xff);
        switch (cmd) {
            /*扫描节点处理*/
            case 0x2003:
                onRfCmd(reciveData, msg);
                break;
            /*设置节点状态回复*/
            case 0xa100:
                onSetState(reciveData, msg);
                break;
            /*释放节点返回*/
            case 0xa00a:
                onRealease(reciveData, msg);
                break;
              /*获取obox信息，obox内节点信息，组信息，版本信息*/
            case 0xa013:
                onGetMsg(reciveData, msg);
                break;
            /*出错*/
            case 0x200f:
                onWrong(reciveData, msg);
                break;
            default:
                break;
        }
        mHandler.sendMessage(msg);
    }


    /**
     * 扫描节点处理
     */
    private void onRfCmd(byte[] bf, Message msg) {
        byte[] serNm = Arrays.copyOfRange(bf, index[27], index[27 + 5]);
        boolean isSuc = bf[index[8]] == OBConstant.ReplyType.SUC;
        if (!MathUtil.byteArrayIsZero(serNm)) {
            msg.what = isSuc ? OBConstant.ReplyType.ON_GET_NEW_NODE : OBConstant.ReplyType.SEARCH_NODE_FAL;
        } else {
            byte setType = bf[index[9]];
            switch (setType) {
                case 0:
                    msg.what = isSuc ? OBConstant.ReplyType.STOP_SEARCH_SUC : OBConstant.ReplyType.STOP_SEARCH_FAL;
                    break;
                case 1:
                    msg.what = isSuc ? OBConstant.ReplyType.START_SEARCH_SUC : OBConstant.ReplyType.START_SEARCH_FAL;
                    break;
                case 2:
                    msg.what = isSuc ? OBConstant.ReplyType.FORCE_SEARCH_SUC : OBConstant.ReplyType.FORCE_SEARCH_FAL;
                    break;
                case 3:
                    msg.what = isSuc ? OBConstant.ReplyType.ACTIVE_SEARCH_SUC : OBConstant.ReplyType.ACTIVE_SEARCH_FAL;
                    break;
            }
        }
    }

    private void onSetState(byte[] bf, Message msg) {
        /*byte7是否成功,8-14节点完整地址，接节点状态*/
        if (bf[7] == OBConstant.ReplyType.SUC) {
            msg.what = OBConstant.ReplyType.SET_STATUS_SUC;
        } else if (bf[7] == OBConstant.ReplyType.FAL) {
            msg.what = OBConstant.ReplyType.SET_STATUS_FAL;
        }
    }

    private void onRealease(byte[] bf, Message msg) {
        msg.what = bf[index[8]] == OBConstant.ReplyType.SUC ?
                OBConstant.ReplyType.ON_REALEASE_SUC : OBConstant.ReplyType.ON_REALEASE_FAL;
    }

    /**
     * 获得obox内的版本信息
     */
    private void onGetMsg(byte[] bf, Message msg) {
        switch (bf[7]) {
            case 3:
                switch (bf[8]) {
                    /*节点信息 */
                    case 2:
                        msg.what = OBConstant.ReplyType.GET_SINGLENODE_BACK;
                        break;
                    /*组信息 */
                    case 4:
                        msg.what = OBConstant.ReplyType.GET_GROUP_BACK;
                        break;
                    /*obox信息  序列号 版本号 */

                    case 10:
                        msg.what = OBConstant.ReplyType.GET_OBOX_MSG_BACK;
                        break;
                    case 11:
                        switch (bf[10]) {
                            case 1:
                                msg.what = OBConstant.ReplyType.ON_SET_MODE;
                                break;
                            case 18:
                                msg.what = OBConstant.ReplyType.ON_SET_ROUTE_SSID;
                                break;
                            case 19:
                                msg.what = OBConstant.ReplyType.ON_SET_ROUTE_PWD;
                                break;
                        }
                        break;
                }
        }
    }

    private void onWrong(byte[] bf, Message msg) {
        switch (bf[index[8]]) {
            case 1:
                msg.what = OBConstant.ReplyType.WRONG_CRC;
                break;
            case 2:
                msg.what = OBConstant.ReplyType.WRONG_TIME_OUT;
                break;
            case 3:
                msg.what = OBConstant.ReplyType.WRONG_NOT_SUPPORT;
                break;
            case 4:
                msg.what = OBConstant.ReplyType.WRONG_WRONG_PWD;
                break;
        }
    }
}
