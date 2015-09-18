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
import android.bluetooth.BluetoothGattService;

/**
 * Created by cody on 9/10/15.
 */
public class BleEvent {
    public static final int CONNECTION_STATE = 0;
    public static final int SERVICE_DISCOVERY = 1;
    public static final int CHARACTERISTIC = 2;
    public static final int DESCRIPTOR = 3;

    public int event;
    public BluetoothGatt gatt;

    public BleEvent(int event, BluetoothGatt gatt) {
        this.event = event;
        this.gatt = gatt;
    }
}
