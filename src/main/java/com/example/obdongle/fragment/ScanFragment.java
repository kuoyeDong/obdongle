package com.example.obdongle.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
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
import com.example.obdongle.util.ParseUtil;

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
        spliceTrans.setValueAndSend(MakeSendData.rfCmd(3, 30, oboxSer, new byte[32]));
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
        spliceTrans.setValueAndSend(MakeSendData.rfCmd(2, 30, oboxSer, new byte[32]));
        waittip();

    }

    private void waittip() {
        showToat("请等待" + search_time + "秒");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                disMissProgressDialog();
            }
        }, search_time);
    }

    private Handler handler = new Handler();
    @Override
    public void onReceive(Message message) {
        switch (message.what) {
            case OBConstant.ReplyType.ON_GET_NEW_NODE:
                ObNode obNode = ParseUtil.parseNewNode(message, dataPool.getObNodes());
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
}
