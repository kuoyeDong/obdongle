package com.example.obdongle.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.obdongle.ControlLampAct;
import com.example.obdongle.R;
import com.example.obdongle.adapter.DeviceAdapter;
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
 * 设备页面
 * Created by adolf_dong on 2018/4/26.
 */

public class DeviceFragment extends BaseFragment implements Respond {
    private List<ObNode> obNodes;

    private DeviceAdapter deviceAdapter;
    private ListView listView;
    private TextView serTv;
    private SpliceTrans spliceTrans;
    private static DeviceFragment deviceFragment;

    public static DeviceFragment instance() {
        synchronized (DeviceFragment.class) {
            if (deviceFragment == null) {
                deviceFragment = new DeviceFragment();
            }
        }
        return deviceFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.device_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final TextView tv = (TextView) view.findViewById(R.id.device_top).findViewById(R.id.mid_tv);
        tv.setText("设备");
        obNodes = DataPool.getInstance().getObNodes();
        listView = (ListView) view.findViewById(R.id.device_lv);
        DataPool.getInstance().regist(this);

        serTv = (TextView) view.findViewById(R.id.ser_tv);

        serTv.setText(Transformation.byteArryToHexString(DataPool.getInstance().getOboxSer()));
        deviceAdapter = new DeviceAdapter(getActivity(), obNodes);
        listView.setAdapter(deviceAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*可控设备跳转到控制页面*/
                ObNode obNode = (ObNode) deviceAdapter.getItem(position);
                int type = obNode.getParentType();
                int childType = obNode.getType();
                if (type == OBConstant.NodeType.IS_LAMP && childType == OBConstant.NodeType.IS_WARM_LAMP) {
                    DataPool.getInstance().setObNode(obNode);
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), ControlLampAct.class);
                    startActivity(intent);
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                /*长按删除节点*/
                showSimpleDialog(TWO, "删除节点", "是否删除节点？", "确认", "取消", new SimpleDialogLSN() {
                    @Override
                    public void pOnClick() {
                        spliceTrans = SpliceTrans.getInstance();
                        if (spliceTrans == null) {
                            showToat("蓝牙未连接");
                            return;
                        }
                        ObNode obNode = (ObNode) deviceAdapter.getItem(position);
                        spliceTrans.setValueAndSend(MakeSendData.deleteNode(obNode));
                        showProgressDialog("稍后", "正在删除节点", false);
                    }

                    @Override
                    public void nOnClick() {

                    }
                });
                return true;
            }
        });
    }

    @Override
    public void onReceive(Message message) {
        disMissProgressDialog();
        switch (message.what) {
            case OBConstant.ReplyType.GET_OBOX_MSG_BACK:
                byte[] serNum = ParseUtil.parseObox(message);
                serTv.setText(Transformation.byteArryToHexString(serNum));
                DataPool.getInstance().setOboxSer(serNum);
                ShareSerializableUtil.getInstance().setIsFirst(false);
                ShareSerializableUtil.getInstance().storageData(DataPool.getInstance());
                break;
            case OBConstant.ReplyType.EDIT_NODE_OR_GROUP_SUC:
                ParseUtil.onDeleteNode(message, obNodes);
                ShareSerializableUtil.getInstance().storageData(DataPool.getInstance());
                deviceAdapter.notifyDataSetChanged();
                break;
            case OBConstant.ReplyType.NOT_REPLY:
                showToat("超时");
                break;
        }
    }

    /**
     * 刷新页面
     */
    public void notifyDeviceChange() {
        if (deviceAdapter != null) {
            deviceAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 请求obox序列号
     */
    public void reqSerNum() {
        spliceTrans = SpliceTrans.getInstance();
        if (spliceTrans == null) {
            showToat("蓝牙未就绪");
            return;
        }
        spliceTrans.setValueAndSend(MakeSendData.reqOboxMsg());
        showProgressDialog("稍等", "首次运行需获取obox序列号", false);
    }
}
