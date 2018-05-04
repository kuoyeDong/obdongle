package com.example.obdongle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.obdongle.base.BaseAct;
import com.example.obdongle.bean.ObNode;
import com.example.obdongle.constant.OBConstant;
import com.example.obdongle.data.DataPool;
import com.example.obdongle.net.Respond;
import com.example.obdongle.net.SpliceTrans;
import com.example.obdongle.util.MakeSendData;
import com.example.obdongle.util.ParseUtil;
import com.example.obdongle.widget.ColorLampSettingPanel;

import java.util.Arrays;
import java.util.List;

/**
 * 灯控制页面||设置情景行为页面
 * Created by adolf_dong on 2018/4/28.
 */

@SuppressWarnings("deprecation")
public class ControlLampAct extends BaseAct implements Respond {

    private ImageView topLeftImg;
    private ImageView topRightImg;
    private TextView topMidTv;
    /**
     * 是否设置场景action
     */
    private boolean isAction;
    /**
     * 传递是否设置action键
     */
    public static final String IS_ACTION = "isaction";
    private ColorLampSettingPanel lampSettingPanel;
    private SeekBar lightSb;
    private TextView lightTv;
    private List<ObNode> obNodes = DataPool.getInstance().getObNodes();
    private ObNode obNode;
    private SpliceTrans spliceTrans;
    /**
     * 控制间隔时间
     */
    private static int CONTROL_TIME = 1000;
    /**
     * 场景设置间隔时间
     */
    private static int SET_ACTION_TIME = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_lamp_act);
        View topView = findViewById(R.id.control_top);
        obNode = DataPool.getInstance().getObNode();

        topLeftImg = (ImageView) topView.findViewById(R.id.left_img);
        topRightImg = (ImageView) topView.findViewById(R.id.right_img);
        topMidTv = (TextView) topView.findViewById(R.id.mid_tv);
        lampSettingPanel = (ColorLampSettingPanel) findViewById(R.id.custom_ctrl_panel);
        lightSb = (SeekBar) findViewById(R.id.custom_ctrl_seekbar);
        lightTv = (TextView) findViewById(R.id.percent);
        isAction = getIntent().hasExtra(IS_ACTION);
        /*设置场景:文字显示区别，不注册网络监听，控制盘间隔时间变短，右上角不可见*/
        if (isAction) {
            topMidTv.setText("场景行为设置");
            lampSettingPanel.setView(obNode.getType(), 0, 0, 0, 0, SET_ACTION_TIME);
            action = new byte[5];
            topLeftImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("action", action);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        } else {
            topMidTv.setText("控制");
            topRightImg.setImageDrawable(getResources().getDrawable(R.drawable.show_scene));
            topRightImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent editSceneIntent = new Intent();
                    editSceneIntent.setClass(ControlLampAct.this, EditSceneAct.class);
                    startActivity(editSceneIntent);
                }
            });
            lampSettingPanel.setView(obNode.getType(), 0, 0, 0, 0, CONTROL_TIME);
            DataPool.getInstance().regist(this);
            topLeftImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        topLeftImg.setImageDrawable(getResources().getDrawable(R.drawable.back));
        lampSettingPanel.setOnColorChangeLsn(new ColorLampSettingPanel.OnColorChangeLsn() {
            @Override
            public void onWarmChange(int cool) {
                if (isAction) {
                    action[1] = (byte) cool;
                    changeAction();
                } else {
                    status = Arrays.copyOf(obNode.getState(), obNode.getState().length);
                    status[1] = (byte) cool;
                    setLampStatusOnLocal();
                }
            }

            @Override
            public void onColorChange(int red, int green, int blue) {
                if (isAction) {
                    /*暂时不支持*/
                    changeAction();
                } else {
                    status = Arrays.copyOf(obNode.getState(), obNode.getState().length);
                    status[3] = (byte) red;
                    status[4] = (byte) green;
                    status[5] = (byte) blue;
                    setLampStatusOnLocal();
                }
            }

            @Override
            public void onUp() {

            }
        });
        lightSb.setOnSeekBarChangeListener(new SeekBarChangeLsn());
        lightSb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        setLight(lightSb.getProgress());
                        break;
                }
                return false;
            }
        });
    }

    private void changeAction() {

    }


    /**
     * 滑动进度条控制事件，包括控制和场景action设置
     *
     * @param progress seekbar进度
     */
    private void setLight(int progress) {
        byte state;
        switch (obNode.getType()) {
            case OBConstant.NodeType.IS_COLOR_LAMP:
                state = (byte) progress;
                break;
            default:
                if (progress == 0) {
                    state = 0;
                } else {
                    state = (byte) ((progress * 126f / 100) + 128);
                }
                break;
        }
        if (isAction) {
            action[0] = state;
            changeAction();
        } else {
            status = Arrays.copyOf(obNode.getState(), obNode.getState().length);
            status[0] = state;
            setLampStatusOnLocal();
        }
    }

    private byte[] status;

    private void setLampStatusOnLocal() {
        spliceTrans = SpliceTrans.getInstance();
        if (spliceTrans == null) {
            showToat("蓝牙未连接");
            return;
        }
        spliceTrans.setValueAndSend(MakeSendData.setNodeState(obNode, status));
        showProgressDialog("稍后", "正在操作", false);
    }

    private class SeekBarChangeLsn implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser)
                return;
            String text = Integer.toString(progress) + "%";
            lightTv.setText(text);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    @Override
    public void onReceive(Message message) {
        disMissProgressDialog();
        switch (message.what) {
            case OBConstant.ReplyType.SET_STATUS_SUC:
                ParseUtil.onSetStatusRec(message, obNodes);
                break;
            case OBConstant.ReplyType.SET_STATUS_FAL:
                showToat("控制失败");
                break;
            case OBConstant.ReplyType.NOT_REPLY:
                showToat("超时");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataPool.getInstance().unRegist(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        intent.putExtra("action", action);
        setResult(RESULT_OK, intent);
        finish();
    }

    private byte[] action;
}
