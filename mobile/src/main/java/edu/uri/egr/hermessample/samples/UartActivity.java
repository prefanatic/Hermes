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

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermesble.HermesBLE;
import edu.uri.egr.hermesble.ui.BLESelectionDialog;
import edu.uri.egr.hermessample.R;
import timber.log.Timber;

public class UartActivity extends AppCompatActivity {

    public static final String SERVICE_UART = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String CHAR_TX = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String CHAR_RX = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        BLESelectionDialog dialog = new BLESelectionDialog();
        dialog.getObservable()
                .flatMap(device -> HermesBLE.connectAndListen(device, SERVICE_UART, CHAR_RX))
                .subscribe(event -> {
                    Timber.d("Received %d bytes", event.characteristic.getValue().length);
                });

        dialog.show(getFragmentManager(), "devicePicker");
    }


}
