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
import rx.Observable;
import rx.subjects.Subject;
import timber.log.Timber;

public class BluetoothLeService extends Service {
    public static final String EXTRA_DEVICE = "bluetooth.le.device";
    public static final String SUBJECT_CONNECTION = "bluetooth.le.connection";
    public static final String SUBJECT_SERVICES = "bluetooth.le.services";
    public static final String SUBJECT_CHARACTERISTIC = "bluetooth.le.characteristic";
    public static final String SUBJECT_DESCRIPTOR = "bluetooth.le.descriptor";

    private static Hermes hermes = Hermes.get();

    private BluetoothGattCallback mBluetoothCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Timber.d("Connection state changed - from %d to %d", status, newState);
            hermes.getDispatchWrapper().getSubject(SUBJECT_CONNECTION)
                    .onNext(new BleConnectionEvent(newState, gatt));

            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                hermes.getDispatchWrapper().getSubject(SUBJECT_CONNECTION)
                        .onCompleted();
                stopSelf();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Subject<BleServiceEvent, BleServiceEvent> subject = hermes.getDispatchWrapper().getSubject(SUBJECT_SERVICES);
            for (BluetoothGattService service : gatt.getServices())
                subject.onNext(new BleServiceEvent(gatt, service));
            subject.onCompleted();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic, int status) {
            Timber.d("Characteristic read: %s - %d", characteristic.getUuid().toString(), status);
            hermes.getDispatchWrapper().getSubject(SUBJECT_CHARACTERISTIC)
                    .onNext(new BleCharacteristicEvent(gatt, characteristic, BleCharacteristicEvent.READ));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic, int status) {
            Timber.d("Characteristic wrote: %s - %d", characteristic.getUuid().toString(), status);
            hermes.getDispatchWrapper().getSubject(SUBJECT_CHARACTERISTIC)
                    .onNext(new BleCharacteristicEvent(gatt, characteristic, BleCharacteristicEvent.WRITE));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic) {
            Timber.d("Characteristic changed: %s", characteristic.getUuid().toString());
            hermes.getDispatchWrapper().getSubject(SUBJECT_CHARACTERISTIC)
                    .onNext(new BleCharacteristicEvent(gatt, characteristic, BleCharacteristicEvent.CHANGED));
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                     int status) {
            Timber.d("Descriptor read: %s - %d", descriptor.getUuid(), status);
            hermes.getDispatchWrapper().getSubject(SUBJECT_DESCRIPTOR)
                    .onNext(new BleDescriptorEvent(gatt, descriptor, BleDescriptorEvent.READ));
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            Timber.d("Descriptor wrote: %s - %d", descriptor.getUuid(), status);
            hermes.getDispatchWrapper().getSubject(SUBJECT_DESCRIPTOR)
                    .onNext(new BleDescriptorEvent(gatt, descriptor, BleDescriptorEvent.WRITE));
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            Timber.d("Reliable write completed: %d", status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Timber.d("Read remote RSSI: %d - %d", rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Timber.d("MTU changed: %d - %d", mtu, status);
        }
    };

    public static Observable<BleConnectionEvent> connect(Context context, BluetoothDevice device) {
        hermes.getDispatchWrapper().createSubject(SUBJECT_CONNECTION);

        Intent intent = new Intent(context, BluetoothLeService.class);
        intent.putExtra(EXTRA_DEVICE, device);

        context.startService(intent);

        return hermes.getDispatchWrapper().getObservable(SUBJECT_CONNECTION);
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
            device.connectGatt(this, false, mBluetoothCallback);
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
