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

package edu.uri.egr.hermesble.factory;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermes.events.BleCharacteristicEvent;
import edu.uri.egr.hermes.events.BleConnectionEvent;
import edu.uri.egr.hermes.events.BleDescriptorEvent;
import edu.uri.egr.hermes.events.BleServiceEvent;
import edu.uri.egr.hermesble.constant.BLEDispatch;
import rx.subjects.Subject;
import timber.log.Timber;

/**
 * Created by cody on 9/23/15.
 */
public class BluetoothGattCallbackFactory {
    public static BluetoothGattCallback create(BluetoothDevice device) {
        // Construct our callback strings.
        final String SUBJECT_CONNECTION = BLEDispatch.connectionState(device);
        final String SUBJECT_SERVICES = BLEDispatch.services(device);
        final String SUBJECT_CHARACTERISTIC = BLEDispatch.characteristic(device);
        final String SUBJECT_DESCRIPTOR = BLEDispatch.descriptor(device);

        // Construct our callback with unique ID's used for Observables.
        final BluetoothGattCallback callback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Timber.d("Connection state changed - from %d to %d", status, newState);
                Hermes.Dispatch.getSubject(SUBJECT_CONNECTION)
                        .onNext(new BleConnectionEvent(newState, gatt));

                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Hermes.Dispatch.getSubject(SUBJECT_CONNECTION)
                            .onCompleted();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                Subject<BleServiceEvent, BleServiceEvent> subject = Hermes.Dispatch.getSubject(SUBJECT_SERVICES);
                for (BluetoothGattService service : gatt.getServices())
                    subject.onNext(new BleServiceEvent(gatt, service));

                // This should only be observed once, and then NO MORE.
                subject.onCompleted();
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic
                    characteristic, int status) {
                Timber.d("Characteristic read: %s - %d", characteristic.getUuid().toString(), status);
                Hermes.Dispatch.getSubject(SUBJECT_CHARACTERISTIC)
                        .onNext(new BleCharacteristicEvent(gatt, characteristic, BleCharacteristicEvent.READ));
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic
                    characteristic, int status) {
                Timber.d("Characteristic wrote: %s - %d", characteristic.getUuid().toString(), status);
                Hermes.Dispatch.getSubject(SUBJECT_CHARACTERISTIC)
                        .onNext(new BleCharacteristicEvent(gatt, characteristic, BleCharacteristicEvent.WRITE));
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic
                    characteristic) {
                Timber.d("Characteristic changed: %s", characteristic.getUuid().toString());
                Hermes.Dispatch.getSubject(SUBJECT_CHARACTERISTIC)
                        .onNext(new BleCharacteristicEvent(gatt, characteristic, BleCharacteristicEvent.CHANGED));
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                         int status) {
                Timber.d("Descriptor read: %s - %d", descriptor.getUuid(), status);
                Hermes.Dispatch.getSubject(SUBJECT_DESCRIPTOR)
                        .onNext(new BleDescriptorEvent(gatt, descriptor, BleDescriptorEvent.READ));
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                          int status) {
                Timber.d("Descriptor wrote: %s - %d", descriptor.getUuid(), status);
                Hermes.Dispatch.getSubject(SUBJECT_DESCRIPTOR)
                        .onNext(new BleDescriptorEvent(gatt, descriptor, BleDescriptorEvent.WRITE));
            }
        };

        return callback;
    }
}
