package com.sharpnow.bluetoothle;

import android.app.Activity;
import android.app.SharedElementCallback;
import android.bluetooth.BluetoothProfile;
import android.net.Uri;
import android.os.Bundle;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.sharpnow.bluetoothle.BluetoothLeClass.OnDataAvailableListener;
import com.sharpnow.bluetoothle.BluetoothLeClass.OnServiceDiscoverListener;


/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends ListActivity {
    private final static String TAG = DeviceScanActivity.class.getSimpleName();
    //private final static String UUID_KEY_DATA = "0000fff6-0000-1000-8000-00805f9b34fb";
    private final static String UUID_KEY_DATA = "0000888d-0000-1000-8000-00805f9b34fb";
    private final static UUID UUID_BUZZER_CHARACTER = UUID
            .fromString(UUID_KEY_DATA);
    private final static String SERVICE_UUID = "00008888-0000-1000-8000-00805f9b34fb";
    private final static UUID UUID_BUZZER_SERVICE = UUID
            .fromString(SERVICE_UUID);
    //20ms
    Timer timer;
    TimerBT timerBT;

    private LeDeviceListAdapter mLeDeviceListAdapter;
    /**
     * 搜索BLE终端
     */
    private BluetoothAdapter mBluetoothAdapter;
    /**
     * 读写BLE终端
     */
    private BluetoothLeClass mBLE;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private BluetoothDevice mdevice;
    private BluetoothGattCharacteristic Buzzer_Characteristic = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setTitle(R.string.title_devices);
        mHandler = new Handler();

        init();//初始化BLE相关

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"stop");
        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.i(TAG,"onPause");
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
        if(timerBT != null){
            timerBT.cancel();//canel timerTask
        }
        mBLE.disconnect();
    }

    @Override
    protected void onStop() {
        Log.i(TAG,"stop");
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "DeviceScan Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.sharpnow.bluetoothle/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        if(timerBT != null){
            timerBT.cancel();//canel timerTask
        }
        mBLE.close();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        boolean bCon = false;
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        mdevice = device;
        if (device == null) {
            Log.e(TAG,"onListItemClick---device = null");
            return;
        }
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        //文件名,函数名,行号
        Log.d(TAG, "[ file name: " + Thread.currentThread().getStackTrace()[2].getFileName() + ",function name: "
                + Thread.currentThread().getStackTrace()[2].getMethodName() + ",line: "
                + Thread.currentThread().getStackTrace()[2].getLineNumber() + "]");
        Log.i(TAG, "-----onListItemClick-------");
        try {
            int nState = device.getBondState();
            switch (nState) {
                case BluetoothDevice.BOND_BONDED:   //the remote device is bonded (paired)
                    Log.i(TAG, "BLE already bonded");
                    break;
                case BluetoothDevice.BOND_NONE:
                    bCon = device.createBond();   //与设备配对
                    Log.i(TAG, "createBond bCon = " + bCon);
                    break;
                case BluetoothDevice.BOND_BONDING:  //Indicates bonding (pairing) is in progress with the remote device
                    Log.i(TAG, "BLE is bonding");
                    break;
                default:
                    break;
            }

            bCon = mBLE.connect(device.getAddress());
            Log.i(TAG, "mBLE.connect = " + bCon);
            //bCon = connectBtInputDevice(device);
            //bCon = mBluetoothAdapter.getProfileProxy(this, connect, getInputDeviceHiddenConstant());
            //Log.i(TAG, "connectBtInputDevice = " + bCon);

            //打开系统蓝牙界面
            //startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            //Log.i(TAG, "start Bluetooth activity");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getInputDeviceHiddenConstant() {
        Class<BluetoothProfile> clazz = BluetoothProfile.class;
        for (Field f : clazz.getFields()) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isPublic(mod)
                    && Modifier.isFinal(mod)) {
                try {
                    if (f.getName().equals("INPUT_DEVICE")) {
                        return f.getInt(null);
                    }
                } catch (Exception e) {
                }
            }
        }
        return -1;
    }

    /**
     *查看BluetoothInputDevice源码，connect(BluetoothDevice device)该方法可以连接HID设备，但是查看BluetoothInputDevice这个类
     * 是隐藏类，无法直接使用，必须先通过BluetoothProfile.ServiceListener回调得到BluetoothInputDevice，然后再反射connect方法连接
     *
     */
    private BluetoothProfile mProxy;
    private BluetoothProfile.ServiceListener connect = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            //BluetoothProfile proxy这个已经是BluetoothInputDevice类型了
            try {
                mProxy = proxy;
                Log.d(TAG,"onServiceConnected--line 242");
                if (profile == getInputDeviceHiddenConstant()) {
                    if (mdevice != null) {
                        //得到BluetoothInputDevice然后反射connect连接设备
                        Method method = proxy.getClass().getMethod("connect",
                                new Class[] { BluetoothDevice.class });
                        method.invoke(proxy, mdevice);
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            Log.d(TAG,"onServiceDisconnected");
            //BluetoothProfile proxy这个已经是BluetoothInputDevice类型了
            try {
                Log.d(TAG,"onServiceConnected--line 244");
                if (profile == getInputDeviceHiddenConstant()) {
                    if (mdevice != null) {
                        //得到BluetoothInputDevice然后反射connect连接设备
                        Method method = mProxy.getClass().getMethod("disconnect",
                                new Class[] { BluetoothDevice.class });
                        method.invoke(mProxy, mdevice);
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    @Override
    public void setEnterSharedElementCallback(SharedElementCallback callback) {
        super.setEnterSharedElementCallback(callback);
    }

    @SuppressWarnings("deprecation")
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            boolean flag = false;
            flag = mBluetoothAdapter.startLeScan(mLeScanCallback);
            Log.i(TAG, "[scanLeDevice]-----startLesacn flag=" + flag);
            //文件名,函数名,行号
            Log.d(TAG, "[ file name: " + Thread.currentThread().getStackTrace()[2].getFileName() + ",function name: " + Thread.currentThread().getStackTrace()[2].getMethodName() + ",line: "
                    + Thread.currentThread().getStackTrace()[2].getLineNumber() + "]");
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 与设备解除配对
     *
     */
    public boolean removeBond(Class btClass,BluetoothDevice btDevice) throws Exception {
        Method removeBondMethod = btClass.getDeclaredMethod("removeBond");//getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    public BluetoothProfile.ServiceListener l = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.d(TAG,"onServiceConnected");
        }
        @Override
        public void onServiceDisconnected(int profile) {
            Log.d(TAG,"onServiceDisconnected");
        }
    };

    public boolean connectBtInputDevice(BluetoothDevice btDevice) throws Exception {
        ReflectCase r = new ReflectCase("android.bluetooth.BluetoothInputDevice");

        Class[] clazzs = { Context.class, BluetoothProfile.ServiceListener.class  };
        Object[] objs = { getApplicationContext(), l  };
        Object obj2 = r.initObject(r.getPackageName(), clazzs, objs);

        Class[] methodClazzs = { BluetoothDevice.class };
        Object[] methodValues = { btDevice };
        Boolean returnValue = (Boolean)r.initMethod(obj2, "connect", methodClazzs, methodValues);
        return returnValue;
    }

    /**
     * 打印函数
     */
    /*
    static public void printLog(){
        //文件名+行号
        Log.d(TAG, "[ file name: "+Thread.currentThread().getStackTrace()[2].getFileName()+",function name: "+Thread.currentThread().getStackTrace()[2].getMethodName()+",line: "
                +Thread.currentThread().getStackTrace()[2].getLineNumber()+"]");
    }
    */

    /**
     * 搜索到BLE终端服务的事件
     */
    private OnServiceDiscoverListener mOnServiceDiscover = new OnServiceDiscoverListener() {

        @Override
        public void onServiceDiscover(BluetoothGatt gatt) {
            try {
                //打印所有service信息
                displayGattServices(mBLE.getSupportedGattServices());
                //read assign service UUID
                BluetoothGattService service = mBLE.getService(UUID_BUZZER_SERVICE);
                if (service != null) {
                    Buzzer_Characteristic = service.getCharacteristic(UUID_BUZZER_CHARACTER);
                    if (((Buzzer_Characteristic.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE))) != 0){
                        writeorReadCharact(Buzzer_Characteristic);
                    }else{
                        Log.i(TAG,"Buzzer_Characteristic don't be writed");
                    }
                } else {
                    Log.i(TAG, "BluetoothGattService is null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    /**
     * 收到BLE终端数据交互的事件
     */
    private OnDataAvailableListener mOnDataAvailable = new OnDataAvailableListener() {

        /**
         * BLE终端数据被读的事件
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS)
                Log.e(TAG, "---onCharacteristicRead--- " + gatt.getDevice().getName()
                        + " read "
                        + characteristic.getUuid().toString()
                        + " -> "
                        + Utils.bytesToHexString(characteristic.getValue()));
        }

        /**
         * 收到BLE终端写入数据回调
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic) {
            Log.e(TAG, "onCharacteristicWrite " + gatt.getDevice().getName()
                    + " write "
                    + characteristic.getUuid().toString()
                    + " -> "
                    + new String(characteristic.getValue()));
        }
    };

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);//add device info to listview
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    /**
     * display bt service information
     * */
    private void displayGattServices(List<BluetoothGattService> gattServices) throws Exception{
        if (gattServices == null) {
            Log.e(TAG, "gattServices = null");
            return;
        }

        Set pairedDevices = mBluetoothAdapter.getBondedDevices();
        for(Object bt : pairedDevices)
            Log.i(TAG,"配对的 bt name: "+((BluetoothDevice)bt).getName());
        //文件名,函数名,行号
        Log.d(TAG, "[ file name: " + Thread.currentThread().getStackTrace()[2].getFileName() + ",function name: "
                + Thread.currentThread().getStackTrace()[2].getMethodName() + ",line: "
                + Thread.currentThread().getStackTrace()[2].getLineNumber() + "]");

        for (BluetoothGattService gattService : gattServices) {
            //-----Service的字段信息-----//
            Log.e(TAG,"-----Service的字段信息-----");
            //Log.e(TAG,"-->test handle:" + getHandle(BluetoothGattService.class, gattService));
            int type = gattService.getType();
            Log.e(TAG, "-->service type:" + Utils.getServiceType(type));
            Log.e(TAG, "-->includedServices size:" + gattService.getIncludedServices().size());
            Log.e(TAG, "-->service uuid:" + gattService.getUuid());

            //-----Characteristics的字段信息-----//
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                Log.e(TAG,"-----Characteristics的字段信息-----");
                Log.e(TAG, "---->char InstanceID: "+gattCharacteristic.getInstanceId());

                Log.e(TAG, "---->char uuid:" + gattCharacteristic.getUuid());

                int permission = gattCharacteristic.getPermissions();
                Log.e(TAG, "---->char permission:" + Utils.getCharPermission(permission));

                int property = gattCharacteristic.getProperties();
                Log.e(TAG, "---->char property:" + Utils.getCharPropertie(property));

                //-----Descriptors的字段信息-----//
                List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                    Log.e(TAG,"-----Descriptors的字段信息-----");
                    Log.e(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
                    int descPermission = gattDescriptor.getPermissions();
                    Log.e(TAG, "-------->desc permission:" + Utils.getDescPermission(descPermission));

                    byte[] desData = gattDescriptor.getValue();
                    if (desData != null && desData.length > 0) {
                        Log.e(TAG, "-------->desc value:" + new String(desData));
                    }
                }
            }
        }
    }

    /**
     *init bluetooth
     */
    private void init(){
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.e(TAG, "BLE is not supported on the device");
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter. get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Log.i(TAG, "open Bluetooth device");
            //开启蓝牙
            //mBluetoothAdapter.enable();
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);   //弹出框打开蓝牙
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }


        mBLE = new BluetoothLeClass(this);//init 基类
        if (!mBLE.initialize()) {
            Log.e(TAG, "Unable to initialize Bluetooth");
            finish();
        }
        //发现BLE终端的Service时回调
        mBLE.setOnServiceDiscoverListener(mOnServiceDiscover);
        //收到BLE终端数据交互的事件
        mBLE.setOnDataAvailableListener(mOnDataAvailable);
    }

    /**
    *read or write
    */
    private BluetoothGattCharacteristic gattCharacteristic;
    private void writeorReadCharact (BluetoothGattCharacteristic Buzzer_Characteristic) {
        if (Buzzer_Characteristic == null)
            return;
        gattCharacteristic = Buzzer_Characteristic;
        if (Buzzer_Characteristic.getInstanceId() == 0) {
            //文件名,函数名,行号
            Log.d(TAG, "[ file name: " + Thread.currentThread().getStackTrace()[2].getFileName() + ",function name: " + Thread.currentThread().getStackTrace()[2].getMethodName() + ",line: "
                    + Thread.currentThread().getStackTrace()[2].getLineNumber() + "]");
            Log.i(TAG, "--------enter read ----");
            //read 20ms
            timer = new Timer();
            timerBT = new TimerBT(mBLE,gattCharacteristic);
            timer.schedule(timerBT,0,10);//20ms
            Log.i(TAG, "--------timerBT read start ----");
        }
    }
}