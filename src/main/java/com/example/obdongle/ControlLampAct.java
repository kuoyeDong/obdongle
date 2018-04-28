package com.example.obdongle;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.obdongle.base.BaseAct;
import com.example.obdongle.net.Respond;
import com.example.obdongle.widget.ColorLampSettingPanel;

/**
 * 灯控制页面
 * Created by adolf_dong on 2018/4/28.
 */

public class ControlLampAct extends BaseAct implements Respond {

    private ImageView topLeftImg;
    private ImageView topRightImg;
    private TextView topMidTv;

    private ColorLampSettingPanel lampSettingPanel;
    private SeekBar lightSb;
    private TextView lightTv;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_lamp_act);
        topLeftImg = (ImageView) findViewById(R.id.control_top).findViewById(R.id.left_img);


        



    }

    @Override
    public void onReceive(Message message) {

    }

}
