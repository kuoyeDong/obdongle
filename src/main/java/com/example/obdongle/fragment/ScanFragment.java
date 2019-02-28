package com.example.obdongle.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import com.example.obdongle.util.ShareSerializableUtil;
import com.example.obdongle.util.Transformation;

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
        ImageView riggtImg = (ImageView) view.findViewById(R.id.scan_top).findViewById(R.id.right_img);
        riggtImg.setImageResource(R.drawable.display_image);
        riggtImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serNumStr = Transformation.byteArryToHexString(DataPool.getInstance().getOboxSer());
                String version = null;
                try {
                    version = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                showSimpleDialog(ONE, null, "设备序列号" + serNumStr + "\n" + "版本号" + version, "确定", null, new SimpleDialogLSN() {
                    @Override
                    public void pOnClick() {
                    }

                    @Override
                    public void nOnClick() {

                    }
                });
            }
        });
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
        spliceTrans.setValueAndSend(MakeSendData.rfCmd(3, search_time, oboxSer, makeScanIndexBytes(dataPool.getObNodes())));
        waittip();
    }

    /**
     * 扫描时间
     */
    private static final int search_time = 90;
    private static final String TAG = "ScanFragment";

    /**
     * 扫描ac设备
     */
    private void scanAcDevice() {
        spliceTrans = SpliceTrans.getInstance();
        byte[] cache = makeScanIndexBytes(dataPool.getObNodes());
        Log.d(TAG, "index: ==" + Transformation.byteArryToHexString(cache));
        if (spliceTrans == null) {
            showToat("蓝牙未连接");
            return;
        }
        spliceTrans.setValueAndSend(MakeSendData.rfCmd(2, search_time, oboxSer, makeScanIndexBytes(dataPool.getObNodes())));
        waittip();

    }

    private void waittip() {
        showToat("请等待" + search_time + "秒");
    }

    @SuppressWarnings("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TIME_OUT:
                    notifyDevice();
                    break;
            }
        }
    };

    private static final int TIME_OUT = 200;

    @Override
    public void onReceive(Message message) {
        switch (message.what) {
            case OBConstant.ReplyType.ON_GET_NEW_NODE:
                long timebefore = System.currentTimeMillis();
                ObNode obNode = ParseUtil.parseNewNode(message, dataPool.getObNodes());
                ShareSerializableUtil.getInstance().storageData(dataPool);
                setDialogMessage("扫描到设备" + obNode.getNodeId());
                long timeAfter = System.currentTimeMillis();
                long time = timeAfter - timebefore;
                Log.d(TAG, "onReceive: use time =" + time);
                break;
            case OBConstant.ReplyType.FORCE_SEARCH_SUC:
                handler.sendEmptyMessageDelayed(TIME_OUT, search_time * 1000);
                showProgressDialogCanClick("请稍后", "正在扫描", false, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        initiativeStop();
                    }
                });
                break;
            case OBConstant.ReplyType.ACTIVE_SEARCH_SUC:
                handler.sendEmptyMessageDelayed(TIME_OUT, search_time * 1000);
                showProgressDialogCanClick("请稍后", "请戳孔", false, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        initiativeStop();
                    }
                });
                break;
            case OBConstant.ReplyType.NOT_REPLY:
            case OBConstant.ReplyType.WRONG_TIME_OUT:
                showToat("超时");
                break;
        }
    }

    /**
     * 主动结束扫描，刷新页面，移除handler消息
     */
    private void initiativeStop() {
        spliceTrans.setValueAndSend(MakeSendData.rfCmd(0, search_time, oboxSer, makeScanIndexBytes(dataPool.getObNodes())));
        handler.removeMessages(TIME_OUT);
        notifyDevice();
    }

    /**
     * 扫描后刷新页面
     */
    private void notifyDevice() {
        disMissProgressDialog();
        Fragment fragment = getActivity().getSupportFragmentManager().getFragments().get(0);
        if (fragment instanceof DeviceFragment) {
            DeviceFragment deviceFragment = (DeviceFragment) fragment;
            deviceFragment.notifyDeviceChange();
        } else {
            Fragment fragment1 = getActivity().getSupportFragmentManager().getFragments().get(1);
            DeviceFragment deviceFragment = (DeviceFragment) fragment1;
            deviceFragment.notifyDeviceChange();
        }
    }

    /**
     * 计算扫描时的index数组，先设置占用的为1再取反
     *
     * @param obNodes 现有节点集合
     * @return 目标index数组，bit为0表述位置已经被占用
     */
    private byte[] makeScanIndexBytes(List<ObNode> obNodes) {
        byte[] bytes = new byte[32];
        for (int i = 0; i < obNodes.size(); i++) {
            ObNode obNode = obNodes.get(i);
            int addr = (obNode.getAddr() & 0xff) - 1;
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
