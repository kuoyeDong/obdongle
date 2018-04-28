package com.example.obdongle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.obdongle.base.BaseAct;
import com.example.obdongle.data.DataPool;
import com.example.obdongle.fragment.DeviceFragment;
import com.example.obdongle.fragment.ScanFragment;
import com.example.obdongle.net.BluetoothLeService;
import com.example.obdongle.net.SampleGattAttributes;
import com.example.obdongle.net.SpliceTrans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("deprecation")
public class MainActivity extends BaseAct {

    private ViewPager viewPager;
    private List<Fragment> fragments = new ArrayList<>();
    private LinearLayout btLeft;
    private LinearLayout btRight;
    private ImageView btLeftImg;
    private ImageView btRightImg;
    private TextView btLeftTv;
    private TextView btRightTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = (ViewPager) findViewById(R.id.vp);
        btLeft = (LinearLayout) findViewById(R.id.bt_left);
        btRight = (LinearLayout) findViewById(R.id.bt_right);
        btLeftImg = (ImageView) findViewById(R.id.bt_left_img);
        btLeftTv = (TextView) findViewById(R.id.bt_left_tv);
        btRightImg = (ImageView) findViewById(R.id.bt_right_img);
        btRightTv = (TextView) findViewById(R.id.bt_right_tv);
        btLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeShowView(true);
            }
        });
        btRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeShowView(false);
            }
        });
        fragments.add(DeviceFragment.instance());
        fragments.add(ScanFragment.instance());
        FragmentPagerAdapter fpa = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        };
        viewPager.setAdapter(fpa);
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "不支持ble", Toast.LENGTH_SHORT).show();
            finish();
        }
        scanBle(true);
        findViewById(R.id.left_img);
        registerReceiver(bleupdatebr, makeGattUpdateIntentFilter());
    }

    /**
     * 改变显示
     *
     * @param isDevice 显示设备页面为true，扫描页面false
     */
    private void changeShowView(boolean isDevice) {
        viewPager.setCurrentItem(isDevice ? 0 : 1);
        btLeftImg.setImageDrawable(getResources().getDrawable(isDevice ? R.drawable.device_press : R.drawable.device_noraml));
        btLeftTv.setTextColor(getResources().getColor(isDevice ? R.color.blue : R.color.gray));
        btRightImg.setImageDrawable(getResources().getDrawable(isDevice ? R.drawable.scan_normal : R.drawable.scan_press));
        btRightTv.setTextColor(getResources().getColor(isDevice ? R.color.gray : R.color.blue));
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bleupdatebr);
        if (mConnected) {
            mBluetoothLeService.disconnect();
            unbindService(mServiceConnection);
        }
        mBluetoothLeService = null;
    }

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 6000;

    /**
     * 打开蓝牙搜索或者停止搜索
     *
     * @param scan 搜索
     */
    private void scanBle(boolean scan) {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }
        if (scan) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        disMissProgressDialog();
                        showDeviece(bluetoothDevices);
                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                showProgressDialog("稍后", "扫描蓝牙", false);
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    }

    private List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private String mDeviceAddress;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        scanBle(true);
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 扫描到设备的回调
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (!bluetoothDevices.contains(device)) {
                        bluetoothDevices.add(device);
                    }
                }
            };

    /**
     * 连接选择的蓝牙
     *
     * @param bluetoothDevices 蓝牙设备列表
     */
    private void showDeviece(final List<BluetoothDevice> bluetoothDevices) {
        String[] items = new String[bluetoothDevices.size()];
        for (int i = 0; i < bluetoothDevices.size(); i++) {
            BluetoothDevice bluetoothDevice = bluetoothDevices.get(i);
            String name = bluetoothDevice.getName() == null ? "unknow" : bluetoothDevice.getName();
            items[i] = name;
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("选择蓝牙");
        alert.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDeviceAddress = bluetoothDevices.get(which).getAddress();
                Intent gattServiceIntent = new Intent(MainActivity.this, BluetoothLeService.class);
                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            }
        });
        alert.show();
//        showToat(getString(R.string.not_find_goal_bluetooth));
    }

    /**
     * 蓝牙连接传输服务
     */
    private BluetoothLeService mBluetoothLeService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver bleupdatebr = new BroadcastReceiver() {
        @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                onConnect();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                onDisConnect();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                onDiscoverServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                spliceTrans.onRecieve();
            }
        }
    };
    private BluetoothGattCharacteristic characteristic;
    private SpliceTrans spliceTrans;

    /**
     * 发现所有服务,连接最后一个组的第一个服务,一键匹配模式发送指令让红外转发器进入读取红外码模式，否则直接获取码库
     *
     * @param supportedGattServices 服务集合
     */
    private void onDiscoverServices(List<BluetoothGattService> supportedGattServices) {
        if (supportedGattServices == null) return;
        String uuid;
        String unknownServiceString = "Unknown service";
        String unknownCharaString = "Unknown characteristic";
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<>();
        ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
        /*获取可用服务*/
        for (BluetoothGattService gattService : supportedGattServices) {
            HashMap<String, String> currentServiceData = new HashMap<>();
            uuid = gattService.getUuid().toString();
            String LIST_NAME = "NAME";
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            String LIST_UUID = "UUID";
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);
            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<>();
            /*获取可用BluetoothGattCharacteristic*/
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        characteristic = mGattCharacteristics.get(mGattCharacteristics.size() - 1).get(0);
        spliceTrans = new SpliceTrans(characteristic, mBluetoothLeService, DataPool.getInstance().getHandler());
        mBluetoothLeService.setCharacteristicNotification(
                characteristic, true);
    }

    /**
     * 蓝牙连接的时候
     */
    private void onConnect() {
        mConnected = true;
        showToat("蓝牙已经连接");
    }

    /**
     * 蓝牙连接状态
     */
    private boolean mConnected;

    /**
     * 蓝牙断开的时候，传输过程进行重新连接，其余直接退出
     */
    private void onDisConnect() {
        mConnected = false;
        mBluetoothLeService.disconnect();
        unbindService(mServiceConnection);
        showToat("蓝牙已经断开并尝试重新扫描");
        reConnectBle();
    }

    /**
     * 蓝牙重新连接
     */
    private void reConnectBle() {
        bluetoothDevices.clear();
        scanBle(true);
        handler.sendEmptyMessageDelayed(RECONNECT_BLE, RECBLE_TIME);
    }

    /**
     * 没回复what
     */
    private final static int NOT_REPLY = 10000;
    /**
     * 超时等待时间
     */
    private final static int DELAY_TIME = 2000;
    /**
     * ble重连状态等待时间
     */
    private final static int RECBLE_TIME = 10000;

    /**
     * 等待下载的时间很长且未知，自动走进度条
     */
    private final static int AUTO_PROGRESS = 10002;


    /**
     * 重新连接ble
     */
    private static final int RECONNECT_BLE = 100;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NOT_REPLY:

                    break;
                case RECONNECT_BLE:
                    if (!mConnected) {
                        Toast.makeText(MainActivity.this, "重连失败，程序退出", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    break;
                case AUTO_PROGRESS:

                    break;
            }
        }
    };
}
