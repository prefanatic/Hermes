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

package edu.uri.egr.hermes;

import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

import edu.uri.egr.hermes.exceptions.HermesException;
import edu.uri.egr.hermes.rx.RxSensorEventListener;
import edu.uri.egr.hermes.rx.RxUnregisterSensorListener;
import rx.Observable;
import rx.subscriptions.Subscriptions;

/**
 * RxSensor wrapper to observe the SensorManager
 */
public class Sensor {
    protected Sensor() {

    }

    /**
     * Observes a sensor given by sensorType, receiving data in a sampling period given by samplingPeriodUs.
     *
     * @param sensorType       Integer type of sensor requested.
     * @param samplingPeriodUs Sampling period in microseconds.
     * @return Observable of SensorEvent.
     */
    public Observable<SensorEvent> observeSensor(int sensorType, int samplingPeriodUs) {
        return observeSensor(sensorType, samplingPeriodUs, 0);
    }

    /**
     * Observes a sensor given by sensorType, receiving data in a sampling period given by samplingPeriodUs, with a maximum retrieval by maximumReportLatencyUs.
     *
     * @param sensorType             Integer type of sensor requested.
     * @param samplingPeriodUs       Sampling period in microseconds.
     * @param maximumReportLatencyUs Maximum retrieval rate in microseconds.
     * @return Observable of SensorEvent.
     */
    public Observable<SensorEvent> observeSensor(int sensorType, int samplingPeriodUs, int maximumReportLatencyUs) {
        SensorManager manager = (SensorManager) Hermes.get().getContext().getSystemService(Context.SENSOR_SERVICE);

        return Observable.create(subscriber -> {
            android.hardware.Sensor sensor = manager.getDefaultSensor(sensorType);
            if (sensor == null) {
                subscriber.onError(new HermesException("Supplied sensor (" + sensorType + ") does not exist."));
                return;
            }

            final RxSensorEventListener listener = new RxSensorEventListener(subscriber);
            boolean success = registerListener(manager, listener, sensor, samplingPeriodUs, maximumReportLatencyUs);
            if (!success) {
                subscriber.onError(new HermesException("Unable to initialize sensor " + sensor.getName()));
                return;
            }

            subscriber.add(Subscriptions.create(new RxUnregisterSensorListener(manager, listener, sensor)));
        });
    }

    private boolean registerListener(SensorManager manager, SensorEventListener listener, android.hardware.Sensor sensor, int samplingPeriodUs, int maximumReportLatencyUs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return manager.registerListener(listener, sensor, samplingPeriodUs, maximumReportLatencyUs);
        else
            return manager.registerListener(listener, sensor, samplingPeriodUs);
    }

}
