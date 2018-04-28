package com.example.obdongle.fragment;

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

import java.util.List;

/**设备页面
 * Created by adolf_dong on 2018/4/26.
 */

public class DeviceFragment extends BaseFragment implements Respond{
    private List<ObNode> obNodes;

    private DeviceAdapter deviceAdapter;
    private ListView listView;
    private Button getSerBtn;
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
        TextView tv = (TextView) view.findViewById(R.id.device_top).findViewById(R.id.mid_tv);
        tv.setText("设备");
        obNodes = DataPool.getInstance().getObNodes();
        listView = (ListView) view.findViewById(R.id.device_lv);
        DataPool.getInstance().regist(this);
        getSerBtn = (Button) view.findViewById(R.id.get_ser_btn);
        getSerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spliceTrans = SpliceTrans.getInstance();
                if (spliceTrans == null) {
                    showToat("蓝牙未连接");
                    return;
                }
                spliceTrans.setValueAndSend(MakeSendData.reqOboxMsg());
            }
        });
        serTv = (TextView) view.findViewById(R.id.ser_tv);

        deviceAdapter = new DeviceAdapter(getActivity(), obNodes);
        listView.setAdapter(deviceAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*可控设备跳转到控制页面*/
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                /*长按删除节点*/
                return true;
            }
        });
    }

    @Override
    public void onReceive(Message message) {
        switch (message.what) {
            case OBConstant.ReplyType.GET_OBOX_MSG_BACK:
                serTv.setText(ParseUtil.parseObox(message));
                break;
        }
    }
}
