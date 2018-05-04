package com.example.obdongle.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.obdongle.R;
import com.example.obdongle.base.BaseFragment;
import com.example.obdongle.bean.ObNode;
import com.example.obdongle.constant.OBConstant;
import com.example.obdongle.data.DataPool;
import com.example.obdongle.net.Respond;
import com.example.obdongle.net.SpliceTrans;
import com.example.obdongle.util.MakeSendData;
import com.example.obdongle.util.MathUtil;
import com.example.obdongle.util.ParseUtil;
import com.example.obdongle.util.ShareSerializableUtil;

import java.util.List;

/**
 * 扫描页面
 * Created by adolf_dong on 2018/4/26.
 */

public class ScanFragment extends BaseFragment implements Respond {

    private SpliceTrans spliceTrans;
    private byte[] oboxSer;
    private DataPool dataPool;
    private static ScanFragment scanFragment;

    public static ScanFragment instance() {
        synchronized (ScanFragment.class) {
            if (scanFragment == null) {
                scanFragment = new ScanFragment();
            }
        }
        return scanFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.scan_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dataPool = DataPool.getInstance();
        dataPool.regist(this);
        oboxSer = dataPool.getOboxSer();
        TextView topMidTv = (TextView) view.findViewById(R.id.scan_top).findViewById(R.id.mid_tv);
        topMidTv.setText("扫描设备");

        Button scanAcBtn = (Button) view.findViewById(R.id.scan_ac_btn);
        Button scanDcBtn = (Button) view.findViewById(R.id.scan_dc_btn);
        scanAcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanAcDevice();
            }
        });
        scanDcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanDcDevice();
            }
        });
    }

    /**
     * 扫描dc设备
     */
    private void scanDcDevice() {
        spliceTrans = SpliceTrans.getInstance();
        if (spliceTrans == null) {
            showToat("蓝牙未连接");
            return;
        }
        spliceTrans.setValueAndSend(MakeSendData.rfCmd(3, 30, oboxSer, makeScanIndexBytes(dataPool.getObNodes())));
        waittip();
    }

    /**
     * 扫描时间
     */
    private static final int search_time = 30;

    /**
     * 扫描ac设备
     */
    private void scanAcDevice() {
        spliceTrans = SpliceTrans.getInstance();
        if (spliceTrans == null) {
            showToat("蓝牙未连接");
            return;
        }
        spliceTrans.setValueAndSend(MakeSendData.rfCmd(2, 30, oboxSer, makeScanIndexBytes(dataPool.getObNodes())));
        waittip();

    }

    private void waittip() {
        showToat("请等待" + search_time + "秒");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                disMissProgressDialog();
                Fragment fragment = getActivity().getSupportFragmentManager().getFragments().get(0);
                if (fragment instanceof DeviceFragment) {
                    DeviceFragment deviceFragment = (DeviceFragment) fragment;
                    deviceFragment.notifyDeviceChange();
                }else {
                    Fragment fragment1 = getActivity().getSupportFragmentManager().getFragments().get(1);
                    DeviceFragment deviceFragment = (DeviceFragment) fragment1;
                    deviceFragment.notifyDeviceChange();
                }
            }
        }, search_time * 1000);
    }

    private Handler handler = new Handler();

    @Override
    public void onReceive(Message message) {
        switch (message.what) {
            case OBConstant.ReplyType.ON_GET_NEW_NODE:
                ObNode obNode = ParseUtil.parseNewNode(message, dataPool.getObNodes());
                ShareSerializableUtil.getInstance().storageData(dataPool);
                setDialogMessage("扫描到设备" + obNode.getNodeId());
                break;
            case OBConstant.ReplyType.FORCE_SEARCH_SUC:
                showProgressDialog("请稍后", "正在扫描", false);
                break;
            case OBConstant.ReplyType.ACTIVE_SEARCH_SUC:
                showProgressDialog("请稍后", "请戳孔", false);
                break;
        }
    }

    /**计算扫描时的index数组，先设置占用的为1再取反
     * @param obNodes 现有节点集合
     * @return 目标index数组，bit为0表述位置已经被占用
     */
    private byte[] makeScanIndexBytes(List<ObNode> obNodes) {
        byte[] bytes = new byte[32];
        for (int i = 0; i < obNodes.size(); i++) {
            ObNode obNode = obNodes.get(i);
            int addr = obNode.getAddr() & 0xff;
            int index = addr / 8;
            int sup = addr % 8;
            bytes[index] |= (1 << sup);
        }
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) ~bytes[i];
        }
        return bytes;
    }
}
