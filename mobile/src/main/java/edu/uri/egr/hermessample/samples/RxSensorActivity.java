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

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.Bind;
import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermessample.R;
import edu.uri.egr.hermesui.activity.HermesActivity;
import rx.Subscription;

/**
 * Created by cody on 10/15/15.
 */
public class RxSensorActivity extends HermesActivity {
    @Bind({R.id.sensor_x, R.id.sensor_y, R.id.sensor_z})
    TextView[] mTextViews;

    private Subscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rxsensor);

        // Register our accelerometer as a sensor to listen to.
        mSubscription = Hermes.Sensor.observeSensor(Sensor.TYPE_ACCELEROMETER, SensorManager.SENSOR_DELAY_NORMAL)
                .subscribe(this::onSensorEvent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSubscription.unsubscribe();
    }

    private void onSensorEvent(SensorEvent event) {
        for (int i = 0; i < 3; i++)
            mTextViews[i].setText(String.valueOf(event.values[i]));
    }


}
