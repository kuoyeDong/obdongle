package com.example.obdongle.data;

import android.os.Handler;
import android.os.Message;

import com.example.obdongle.bean.ObNode;
import com.example.obdongle.net.Respond;

import java.util.ArrayList;
import java.util.List;

/**
 * 放置数据
 * Created by adolf_dong on 2018/4/25.
 */

public class DataPool {
    /**
     * 当前操作的节点
     */
    private ObNode obNode;

    public List<ObNode> getObNodes() {
        return obNodes;
    }

    public void setObNodes(List<ObNode> obNodes) {
        this.obNodes = obNodes;
    }

    private List<ObNode> obNodes = new ArrayList<>();

    public byte[] getOboxSer() {
        return oboxSer;
    }

    public void setOboxSer(byte[] oboxSer) {
        this.oboxSer = oboxSer;
    }

    /**
     * 第一次扫描的obox序列号
     */
    private byte[] oboxSer =new byte[]{0x66,0x77, (byte) 0x88, (byte) 0x99, (byte) 0xff};
    private List<Respond> responds = new ArrayList<>();
    private static DataPool dataPool;

    public static DataPool getInstance() {
        synchronized (DataPool.class) {
            if (dataPool == null) {
                dataPool = new DataPool();
            }
            return dataPool;
        }
    }

    public Handler getHandler() {
        return handler;
    }

    @SuppressWarnings("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            for (Respond respond : responds) {
                respond.onReceive(msg);
            }
        }
    };

    /**
     * 给实现HandlerListner接口的页面设置监听
     *
     * @param respond HandlerListner接口的引用
     */
    public void regist(Respond respond) {
        if (!responds.contains(respond)) {
            responds.add(respond);
        }
    }

    /**
     * 给实现HandlerListner接口的页面移除监听
     *
     * @param respond HandlerListner接口的引用
     */
    public void unRegist(Respond respond) {
        if (responds.contains(respond)) {
            responds.remove(respond);
        }
    }

    public void setObNode(ObNode obNode) {
        this.obNode = obNode;
    }

    public ObNode getObNode() {
        return obNode;
    }
}
