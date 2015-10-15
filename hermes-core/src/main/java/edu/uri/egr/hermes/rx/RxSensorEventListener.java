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

package edu.uri.egr.hermes.rx;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import rx.Subscriber;

/**
 * Created by cody on 10/15/15.
 */
public class RxSensorEventListener implements SensorEventListener {
    private final Subscriber<? super SensorEvent> subscriber;

    public RxSensorEventListener(Subscriber<? super SensorEvent> subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        subscriber.onNext(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
