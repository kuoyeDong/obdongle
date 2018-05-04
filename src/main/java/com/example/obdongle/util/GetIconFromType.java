package com.example.obdongle.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.obdongle.R;
import com.example.obdongle.bean.ObNode;
import com.example.obdongle.constant.OBConstant;


/**
 * 由于根据类型选择icon的使用地方很多，造成代码冗余，并且难于维护，
 * 使用此工具类解决此问题
 * 随着项目进行，此类出现大量冗余代码，需抽取四个方法，
 * 根据单节点类型和子类型获取图片
 * 根据单节点类型和子类型获取关闭时候的图片
 * 根据组节点类型和子类型获取图片
 * 根据组节点类型和子类型获取关闭时候的图片
 * Created by adolf_dong on 2016/11/2.
 */
@SuppressWarnings("deprecation")
public class GetIconFromType {

    /**
     * 获取本地节点的icon
     */
    public int getDrawResIdForType(ObNode node) {
        int type = node.getParentType();
        int childType = node.getType();
        return getSingleIcon(type, childType);
    }

    /**
     * 获取单节点
     *
     * @param type      类型
     * @param childType 子类型
     * @return 素材id
     */
    private int getSingleIcon(int type, int childType) {
        int resId = R.drawable.unkown_room;
        switch (type) {
            case OBConstant.NodeType.IS_LAMP:
                switch (childType) {
                    case OBConstant.NodeType.IS_WARM_LAMP:
                        resId = R.drawable.led_home;
                        break;
                }
                break;

            case OBConstant.NodeType.IS_SENSOR:
                switch (childType) {
                    case OBConstant.NodeType.RADAR:
                    case OBConstant.NodeType.XIBING_RADAR:
                        resId = R.drawable.radar_have;
                        break;
                    case OBConstant.NodeType.FLOOD:
                        resId = R.drawable.water_have;
                        break;
                    case OBConstant.NodeType.RED_SENSOR:
                    case OBConstant.NodeType.DC_RED_SENSOR:
                        resId = R.drawable.body_have;
                        break;
                }
                break;
            default:
                resId = R.drawable.unkown_room;
                break;
        }
        return resId;
    }

}
