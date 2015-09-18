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

/**
 * Created by cody on 9/10/15.
 */
public class BleConnectionEvent extends BleEvent {
    public int type;

    public BleConnectionEvent(int type, BluetoothGatt gatt) {
        super(BleEvent.CONNECTION_STATE, gatt);
        this.type = type;
    }
}
