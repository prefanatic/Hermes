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

package edu.uri.egr.hermesble.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermes.events.BleCharacteristicEvent;
import edu.uri.egr.hermes.events.BleConnectionEvent;
import edu.uri.egr.hermes.events.BleDescriptorEvent;
import edu.uri.egr.hermes.events.BleServiceEvent;
import edu.uri.egr.hermesble.constant.BLEDispatch;
import edu.uri.egr.hermesble.factory.BluetoothGattCallbackFactory;
import rx.Observable;
import rx.subjects.Subject;
import timber.log.Timber;

public class BluetoothLeService extends Service {
    public static final String EXTRA_DEVICE = "bluetooth.le.device";

    public static Observable<BleConnectionEvent> connect(Context context, BluetoothDevice device) {
        Intent intent = new Intent(context, BluetoothLeService.class);
        intent.putExtra(EXTRA_DEVICE, device);

        context.startService(intent);

        return Hermes.Dispatch.getObservable(BLEDispatch.connectionState(device));
    }

    public BluetoothLeService() {
    }

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            BluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
            BluetoothGattCallback callback = BluetoothGattCallbackFactory.create(device);

            device.connectGatt(this, false, callback);
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
