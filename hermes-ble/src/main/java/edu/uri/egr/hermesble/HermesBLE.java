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

package edu.uri.egr.hermesble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermesble.attributes.BLStandardAttributes;
import edu.uri.egr.hermesble.constant.BLEDispatch;
import edu.uri.egr.hermesble.event.BleCharacteristicEvent;
import edu.uri.egr.hermesble.event.BleConnectionEvent;
import edu.uri.egr.hermesble.event.BleServiceEvent;
import edu.uri.egr.hermesble.service.BluetoothLeService;
import rx.Observable;
import rx.functions.Action0;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by cody on 9/22/15.
 */
public class HermesBLE {
    private static final Hermes hermes = Hermes.get();
    private static final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private HermesBLE() {

    }

    /**
     * Locates all BLE devices.
     * @param scanPeriod Length in seconds to scan for.
     * @return Observable of BluetoothDevice found.
     */
    public static Observable<BluetoothDevice> findDevices(int scanPeriod) {
        PublishSubject<BluetoothDevice> subject = PublishSubject.create();
        List<BluetoothDevice> deviceList = new ArrayList<>();
        BluetoothAdapter.LeScanCallback callback = (device, rssi, data) -> subject.onNext(device);

        mBluetoothAdapter.startLeScan(callback);

        Observable.just(null)
                .delay(scanPeriod, TimeUnit.SECONDS)
                .doOnCompleted(subject::onCompleted)
                .subscribe();

        return subject.asObservable()
                .doOnCompleted(() -> mBluetoothAdapter.stopLeScan(callback))
                .filter(device -> !deviceList.contains(device))
                .doOnNext(deviceList::add);
    }

    /**
     * Connect to a specific BluetoothDevice.
     * @param device A BluetoothDevice to connect to.
     * @return Observable of BleConnectionEvent.
     */
    public static Observable<BleConnectionEvent> connect(BluetoothDevice device) {
        // TODO: 9/23/2015 Throw Observable error if we cannot connect.
        return BluetoothLeService.connect(hermes.getContext(), device);
    }

    /**
     * Listen to a specific notification under a Bluetooth service.
     * @param gatt BluetoothGatt object that resides with the BluetoothDevice.
     * @param serviceUuid String of Bluetooth service used.
     * @param characteristicUuid String of Bluetooth characteristic to listen to.
     * @return Observable of BleCharacteristicEvent.
     */
    public static Observable<BleCharacteristicEvent> listen(BluetoothGatt gatt, String serviceUuid, String characteristicUuid) {
        Hermes.Dispatch.getObservable(BLEDispatch.services(gatt.getDevice()), BleServiceEvent.class)
                .doOnNext(event -> Timber.d("Service discovered: %s", event.service.getUuid().toString()))
                .doOnNext(event -> {
                    Timber.d("Characteristics: ");
                    for (BluetoothGattCharacteristic characteristic : event.service.getCharacteristics())
                        Timber.d(characteristic.getUuid().toString());
                })
                .subscribe(event -> {
                    if (event.service.getUuid().toString().equals(serviceUuid)) {
                        // Get a hold of the characteristic.
                        BluetoothGattCharacteristic characteristic =
                                event.service.getCharacteristic(UUID.fromString(characteristicUuid));

                        // We want to read from it.
                        event.gatt.setCharacteristicNotification(characteristic, true);

                        // And receive events from it.
                        BluetoothGattDescriptor config = characteristic.getDescriptor(
                                UUID.fromString(BLStandardAttributes.DESC_CLIENT_CHARACTERISTIC_CONFIG));
                        config.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                        event.gatt.writeDescriptor(config);
                    }
                });

        Timber.d("Are we discovering?");

        gatt.discoverServices();

        return Hermes.Dispatch.getObservable(BLEDispatch.characteristic(gatt.getDevice()));
    }

    /**
     * Connects and then proceeds to listen to a notification.
     * @param device BluetoothDevice to connect to.
     * @param serviceUuid Bluetooth service.
     * @param characteristicUuid Bluetooth characteristic.
     * @return Observable of BleCharacteristicEvent
     */
    public static Observable<BleCharacteristicEvent> connectAndListen(BluetoothDevice device, String serviceUuid, String characteristicUuid) {
        return connect(device)
                .filter(event -> event.type == BluetoothProfile.STATE_CONNECTED)
                .doOnNext(event -> Timber.d("Connected."))
                .flatMap(event -> listen(event.gatt, serviceUuid, characteristicUuid));
    }

    public static void close(BluetoothGatt gatt) {
        if (gatt == null)
            return;

        // Clean Rx
        Hermes.Dispatch.clean(BLEDispatch.characteristic(gatt.getDevice()));
        Hermes.Dispatch.clean(BLEDispatch.connectionState(gatt.getDevice()));
        Hermes.Dispatch.clean(BLEDispatch.descriptor(gatt.getDevice()));
        Hermes.Dispatch.clean(BLEDispatch.services(gatt.getDevice()));

        gatt.disconnect();
        Timber.d("Disconnecting.");
    }
}
