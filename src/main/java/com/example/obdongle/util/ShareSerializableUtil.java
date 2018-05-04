package com.example.obdongle.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.example.obdongle.bean.ObNode;
import com.example.obdongle.data.DataPool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * 使用共享首选项存储序列化的数据
 * Created by adolf_dong on 2018/5/3.
 */

public class ShareSerializableUtil {

    private static ShareSerializableUtil shareSerializableUtil;

    private SharedPreferences sharedPreferences;

    public static ShareSerializableUtil getInstance() {
        synchronized (ShareSerializableUtil.class) {
            return shareSerializableUtil;
        }
    }

    public ShareSerializableUtil(Context context) {
        sharedPreferences = context.getSharedPreferences("data", MODE_PRIVATE);
        shareSerializableUtil = this;
    }

    /**
     * obox序列号key
     */
    private static final String OBOX_SER = "obox_ser";
    /**
     * 节点集合key
     */
    private static final String OBNODES = "obnodes";

    /**
     * 序列化存储节点数据
     *
     * @param dataPool 要被存储的dataPool对象
     */
    public void storageData(DataPool dataPool) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        List<ObNode> nodesList = dataPool.getObNodes();
        ObNode[] nodesArray = new ObNode[nodesList.size()];
        nodesList.toArray(nodesArray);
        editor.putString(OBOX_SER, Transformation.byteArryToHexString(dataPool.getOboxSer()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(nodesArray);
            String obNodesStr = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            editor.putString(OBNODES, obNodesStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.apply();
    }


    /**
     * 反序列化获取数据实例化对象
     *
     * @param dataPool 要被设置的datapool对象
     */
    public void instatnceData(DataPool dataPool) {
        byte[] oboxSerBytes = Transformation.hexString2Bytes(sharedPreferences.getString(OBOX_SER, "0000000000"));
        dataPool.setOboxSer(oboxSerBytes);
        String obNodesStr = sharedPreferences.getString(OBNODES, "null");
        byte[] obNodesArray = Base64.decode(obNodesStr, Base64.DEFAULT);
        ByteArrayInputStream bais = new ByteArrayInputStream(obNodesArray);
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            ObNode[] obNodes = (ObNode[]) ois.readObject();

            List<ObNode> obNodeList = new ArrayList<>();
            Collections.addAll(obNodeList, obNodes);
            dataPool.setObNodes(obNodeList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final String IS_FIRST = "is_first";

    /**
     * 获取是否第一次运行
     *
     * @return 是则为true
     */
    public boolean isFirst() {
        return sharedPreferences.getBoolean(IS_FIRST, true);
    }

    /**
     * 设置是否第一次运行
     */
    public void setIsFirst(boolean isFirst) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_FIRST, isFirst);
        editor.apply();
    }
}
