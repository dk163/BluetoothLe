package com.sharpnow.bluetoothle;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.TimerTask;

/**
 * Created by Administrator on 2016/8/16 0016.
 */
public class TimerBT extends TimerTask {
    private BluetoothLeClass mBLE;
    private BluetoothGattCharacteristic gattCharacteristic;

    public TimerBT(BluetoothLeClass mBLE , BluetoothGattCharacteristic gattCharacteristic) {
        this.mBLE = mBLE;
        this.gattCharacteristic = gattCharacteristic;
    }

    @Override
    public void run() {
        mBLE.readCharacteristic(gattCharacteristic);
    }
}
