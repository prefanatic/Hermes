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

package edu.uri.egr.hermesble.evaluator;

import android.bluetooth.BluetoothGattCharacteristic;

import edu.uri.egr.hermesble.event.BleCharacteristicEvent;
import rx.Observable;

/**
 * Created by cody on 9/26/15.
 */
public abstract class ByteValueEvaluator<T> {
    public Observable<T> handle(BleCharacteristicEvent event) {
        return handle(event.characteristic);
    }

    public Observable<T> handle(BluetoothGattCharacteristic characteristic) {
        return handle(characteristic.getValue());
    }

    public Observable<T> handle(byte[] value) {
        return Observable.just(evaluate(value));
    }

    public abstract T evaluate(byte[] value);
}
