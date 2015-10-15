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

package edu.uri.egr.hermessample.samples;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import butterknife.Bind;
import butterknife.OnClick;
import edu.uri.egr.hermesble.HermesBLE;
import edu.uri.egr.hermesble.attributes.RBLGattAttributes;
import edu.uri.egr.hermesble.event.BleConnectionEvent;
import edu.uri.egr.hermesble.ui.BLESelectionDialog;
import edu.uri.egr.hermessample.R;
import edu.uri.egr.hermesui.activity.HermesActivity;
import rx.Subscription;
import timber.log.Timber;

public class UartActivity extends HermesActivity {
    @Bind(R.id.toolbar) Toolbar mToolbar;

    private BluetoothGatt mGatt;
    private Subscription mSubscription;

    private boolean trigger = false;

    @OnClick(R.id.test)
    public void test() {
        onConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uart);
        setSupportActionBar(mToolbar);

        BLESelectionDialog dialog = new BLESelectionDialog();
        mSubscription = dialog.getObservable()
                .doOnCompleted(this::finish)
                .flatMap(HermesBLE::connect)
                .filter(event -> event.type == BluetoothProfile.STATE_CONNECTED)
                .doOnNext(event -> mGatt = event.gatt)
                .flatMap(event -> HermesBLE.listen(event.gatt, RBLGattAttributes.BLE_SHIELD_SERVICE, RBLGattAttributes.BLE_SHIELD_RX))
                .subscribe();

        dialog.show(getFragmentManager(), "devicePicker");
    }


    private void onConnected() {
        byte[] data = new byte[] {(byte) 0x01, (byte) 0x00, (byte) 0x00};
        if (trigger) {
            data[1] = 0x01;
        }
        trigger = !trigger;

        HermesBLE.write(mGatt, RBLGattAttributes.BLE_SHIELD_SERVICE, RBLGattAttributes.BLE_SHIELD_TX, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        HermesBLE.close(mGatt); // Have Hermes handle closing out our bluetooth connection for us.
        mSubscription.unsubscribe();
    }
}
