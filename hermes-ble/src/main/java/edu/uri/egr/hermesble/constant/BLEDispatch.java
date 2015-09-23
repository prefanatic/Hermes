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

package edu.uri.egr.hermesble.constant;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

/**
 * Created by cody on 9/23/15.
 */
public class BLEDispatch {
    private static final String SUBJECT_CONNECTION = "bluetooth.le.connection.";
    private static final String SUBJECT_SERVICES = "bluetooth.le.services.";
    private static final String SUBJECT_CHARACTERISTIC = "bluetooth.le.characteristic.";
    private static final String SUBJECT_DESCRIPTOR = "bluetooth.le.descriptor.";

    public static String connectionState(BluetoothDevice device) {
        return SUBJECT_CONNECTION + device.getAddress();
    }


    public static String services(BluetoothDevice device) {
        return SUBJECT_SERVICES + device.getAddress();
    }

    public static String characteristic(BluetoothDevice device) {
        return SUBJECT_CHARACTERISTIC + device.getAddress();
    }

    public static String descriptor(BluetoothDevice device) {
        return SUBJECT_DESCRIPTOR + device.getAddress();
    }

}
