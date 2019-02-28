package com.example.obdongle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.obdongle.base.BaseAct;
import com.example.obdongle.bean.ObNode;
import com.example.obdongle.constant.OBConstant;
import com.example.obdongle.data.DataPool;
import com.example.obdongle.net.Respond;
import com.example.obdongle.net.SpliceTrans;
import com.example.obdongle.util.MakeSendData;
import com.example.obdongle.util.ParseUtil;
import com.example.obdongle.util.ShareSerializableUtil;
import com.example.obdongle.widget.ColorLampSettingPanel;

import java.util.Arrays;
import java.util.List;

import static java.lang.Math.abs;

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

    /**
     * 步进减少亮度
     */
    public static final int CUT = 100;
    /**
     * 步进增加亮度
     */
    public static final int PLUS = 101;

    /**
     * 步进开关，打开时隐藏滑条
     */
    private Switch gradientSwitch;
    /**
     * 步进亮度加减
     */
    private Switch plusCutSwitch;

    /**
     * 步进加减文字，打开时可见
     */
    private TextView plusCutTv;

    /**
     * 两个开关，设置场景行为的时候控制可见不可见
     */
    private LinearLayout swithLl;
    /**
     * 滑动条的Rl
     */
    private RelativeLayout seekRl;

    /**
     * 是否给客户演示的
     */
    public static final boolean IS_DEV = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_lamp_act);
        View topView = findViewById(R.id.control_top);
        obNode = DataPool.getInstance().getObNode();
        gradientSwitch = (Switch) findViewById(R.id.gradient_switch);
        plusCutSwitch = (Switch) findViewById(R.id.plus_cut_switch);
        plusCutTv = (TextView) findViewById(R.id.plus_cut_tv);
        swithLl = (LinearLayout) findViewById(R.id.switch_ll);
        seekRl = (RelativeLayout) findViewById(R.id.seek_rl);
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
            action = getIntent().getByteArrayExtra(IS_ACTION);
            if (IS_DEV) {
                swithLl.setVisibility(View.VISIBLE);
            }
            initLampView(action, OBConstant.NodeType.IS_WARM_LAMP);
            gradientSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (buttonView.isPressed()) {
                        action[0] = (byte) (isChecked ? CUT : 0);
                        initLampView(action, OBConstant.NodeType.IS_WARM_LAMP);
                    }
                }
            });
            plusCutSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (buttonView.isPressed()) {
                        action[0] = (byte) (isChecked ? PLUS : CUT);
                        initLampView(action, OBConstant.NodeType.IS_WARM_LAMP);
                    }
                }
            });
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
            initLampView(obNode);
            topMidTv.setText(obNode.getNodeId());
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

    /**
     * 初始化行为设置页面
     *
     * @param action 行为
     * @param type   设备类型，目前写定为双色灯
     */
    private void initLampView(byte[] action, int type) {
        int light = action[0] & 0xff;
        if (light == CUT) {
            seekRl.setVisibility(View.INVISIBLE);
            plusCutTv.setVisibility(View.VISIBLE);
            plusCutSwitch.setVisibility(View.VISIBLE);
            gradientSwitch.setChecked(true);
            plusCutSwitch.setChecked(false);
        } else if (light == PLUS) {
            seekRl.setVisibility(View.INVISIBLE);
            plusCutTv.setVisibility(View.VISIBLE);
            plusCutSwitch.setVisibility(View.VISIBLE);
            gradientSwitch.setChecked(true);
            plusCutSwitch.setChecked(true);
        } else {
            gradientSwitch.setChecked(false);
            seekRl.setVisibility(View.VISIBLE);
            plusCutTv.setVisibility(View.INVISIBLE);
            plusCutSwitch.setVisibility(View.INVISIBLE);
            ObNode obNode = new ObNode();
            obNode.setState(action);
            obNode.setType((byte) type);
            initLampView(obNode);
        }
    }

    private void initLampView(ObNode obNode) {
        int light = obNode.getState()[0] & 0xff;
        String protvshow;
        int type = obNode.getType() & 0xff;
        int progress = 0;
        if (light == 0) {
            protvshow = "0%";
        } else {
            int seekBarNum = light;
            switch (type) {
                case OBConstant.NodeType.IS_COLOR_LAMP:
                    if (seekBarNum > 100) {
                        seekBarNum = 100;
                    }
                    progress = seekBarNum;
                    break;
                case OBConstant.NodeType.IS_WARM_LAMP:
                    if (seekBarNum == 255) {
                        progress = 100;
                    } else if (seekBarNum <= 128) {
                        progress = 0;
                    } else if (seekBarNum > 128 && seekBarNum < 255) {
                        progress = (seekBarNum - 128) * 100 / 126;
                    }
                    break;
                default:
                    progress = (seekBarNum - 128) * 100 / 126;
                    break;
            }
            protvshow = progress + "%";
        }
        lightTv.setText(protvshow);
        lightSb.setProgress(progress);
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
        spliceTrans.setValueAndSendShort(MakeSendData.setNodeState(obNode, status), true);
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
                ShareSerializableUtil.getInstance().storageData(DataPool.getInstance());
                break;
            case OBConstant.ReplyType.SET_STATUS_FAL:
                showToat("控制失败");
                break;
            case OBConstant.ReplyType.NOT_REPLY:
            case OBConstant.ReplyType.WRONG_TIME_OUT:
                showToat("超时");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        DataPool.getInstance().unRegist(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (isAction) {
            Intent intent = new Intent();
            intent.putExtra("action", action);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            finish();
        }
    }

    private byte[] action;
}
