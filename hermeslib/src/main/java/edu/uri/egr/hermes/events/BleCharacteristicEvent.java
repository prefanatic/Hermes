/*
 * Copyright 2015 Cody Goldberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uri.egr.hermes.events;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by cody on 9/11/15.
 */
public class BleCharacteristicEvent extends BleEvent {
    public static final int READ = 0;
    public static final int WRITE = 1;
    public static final int CHANGED = 2;

    public BluetoothGattCharacteristic characteristic;
    public int type;

    public BleCharacteristicEvent(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int type) {
        super(BleEvent.CHARACTERISTIC, gatt);
        this.characteristic = characteristic;
        this.type = type;
    }
}
