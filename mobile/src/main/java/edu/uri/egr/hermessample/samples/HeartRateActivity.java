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
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import java.nio.ByteBuffer;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import edu.uri.egr.hermesble.HermesBLE;
import edu.uri.egr.hermesble.attributes.BLStandardAttributes;
import edu.uri.egr.hermesble.ui.BLESelectionDialog;
import edu.uri.egr.hermessample.R;
import rx.Subscription;
import timber.log.Timber;

public class HeartRateActivity extends AppCompatActivity {
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.heart) ImageView mHeart;
    @Bind(R.id.heart_rate) TextView mRate;

    @BindColor(R.color.red) int COLOR_HEART;

    private Subscription mDeviceSubscription;
    private BluetoothGatt mGatt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heartrate);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        BLESelectionDialog dialog = new BLESelectionDialog();
        mDeviceSubscription = dialog.getObservable()
                .flatMap(device -> HermesBLE.connectAndListen(device, BLStandardAttributes.SERVICE_HEART_RATE, BLStandardAttributes.CHAR_HEART_RATE_MEASUREMENT))
                .doOnCompleted(() -> Timber.d("Im unsubscribe."))
                .subscribe(event -> {
                    mGatt = event.gatt;

                    // Steal the byte[] value from the characteristic.
                    byte[] val = event.characteristic.getValue();

                    int rate = ByteBuffer.wrap(val).getChar();
                    mRate.setText(String.valueOf(rate));
                });

        dialog.show(getFragmentManager(), "dialog");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        HermesBLE.close(mGatt);
        mDeviceSubscription.unsubscribe();

        finish();
    }
}
