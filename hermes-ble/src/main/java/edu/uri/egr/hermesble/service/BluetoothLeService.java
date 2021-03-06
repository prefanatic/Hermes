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
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermesble.constant.BLEDispatch;
import edu.uri.egr.hermesble.event.BleConnectionEvent;
import edu.uri.egr.hermesble.factory.BluetoothGattCallbackFactory;
import rx.Observable;
import timber.log.Timber;

public class BluetoothLeService extends Service {
    public static final String EXTRA_DEVICE = "bluetooth.le.device";
    public static final Map<String, BluetoothGatt> mActiveGattServers = new HashMap<>();

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

            BluetoothGatt gatt = device.connectGatt(this, false, callback);

            mActiveGattServers.put(device.getAddress(), gatt);
            Hermes.Dispatch.getObservable(BLEDispatch.connectionState(device))
                    .cast(BleConnectionEvent.class)
                    .doOnCompleted(() -> {
                        gatt.close();

                        mActiveGattServers.remove(device.getAddress());
                        if (mActiveGattServers.size() == 0) {
                            Timber.d("Reached end of life.");
                            stopSelf();
                        }})
                        .subscribe();
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
