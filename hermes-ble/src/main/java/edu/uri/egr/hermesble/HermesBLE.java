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
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermes.attributes.RBLGattAttributes;
import edu.uri.egr.hermes.events.BleConnectionEvent;
import edu.uri.egr.hermes.events.BleEvent;
import edu.uri.egr.hermes.events.BleServiceEvent;
import edu.uri.egr.hermes.services.BluetoothLeService;
import rx.Observable;
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

    public static Observable<BluetoothDevice> findDevices(int scanPeriod) {
        PublishSubject<BluetoothDevice> subject = PublishSubject.create();
        List<BluetoothDevice> deviceList = new ArrayList<>();
        BluetoothAdapter.LeScanCallback callback = (device, rssi, data) -> subject.onNext(device);

        mBluetoothAdapter.startLeScan(callback);

        Observable.just(null)
                .delay(scanPeriod, TimeUnit.SECONDS)
                .doOnCompleted(() -> {
                    mBluetoothAdapter.stopLeScan(callback);
                    subject.onCompleted();
                })
                .subscribe();

        return subject.asObservable()
                .filter(device -> !deviceList.contains(device))
                .doOnNext(deviceList::add);
    }

    public Observable<BleConnectionEvent> connect(BluetoothDevice device) {
        return BluetoothLeService.connect(hermes.getContext(), device);
    }

    public Observable<BleEvent> connectAndListen(BluetoothDevice device, String serviceUuid, String characteristicUuid) {
        Observable<BleConnectionEvent> observable = connect(device);
        Observable<BleServiceEvent> serviceObservable = hermes.getDispatchWrapper().getObservable(BluetoothLeService.SUBJECT_SERVICES);
        PublishSubject<BleEvent> subject = PublishSubject.create();

        serviceObservable.subscribe(event -> {
            Timber.d("Discovered: %s", event.service.getUuid());

            if (event.service.getUuid().toString().equals(serviceUuid)) {
                BluetoothGattCharacteristic characteristic =
                        event.service.getCharacteristic(UUID.fromString(characteristicUuid));

                event.gatt.setCharacteristicNotification(characteristic, true);
                event.gatt.readCharacteristic(characteristic);

                // FIXME: 9/10/15 This isn't correct!!!!!!
                BluetoothGattDescriptor config = characteristic.getDescriptor(
                        UUID.fromString(RBLGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                config.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                event.gatt.writeDescriptor(config);

                Timber.d("Subscribed to characteristic.");
            }
        });

        observable.subscribe(event -> {
            switch (event.type) {
                case BluetoothProfile.STATE_CONNECTED:
                    Timber.d("Connected!");

                    event.gatt.discoverServices();

                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Timber.d("Disconnected :(");
                    break;
            }
        });

        return null;
    }
}
