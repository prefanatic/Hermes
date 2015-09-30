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
import edu.uri.egr.hermesble.evaluator.HeartRateEvaluator;
import edu.uri.egr.hermesble.ui.BLESelectionDialog;
import edu.uri.egr.hermessample.R;
import edu.uri.egr.hermesui.activity.HermesActivity;
import rx.Subscription;
import timber.log.Timber;

public class HeartRateActivity extends HermesActivity {

    // We need to bind our views so we can access them.
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.heart) ImageView mHeart;
    @Bind(R.id.heart_rate) TextView mRate;

    @BindColor(R.color.red) int COLOR_HEART;

    // Create Activity global variables for things we need across different methods.
    private Subscription mDeviceSubscription;
    private BluetoothGatt mGatt;

    /*
    This is called when the Activity is created.  In Android, this will be when the activity is started fresh
    for the first time, or when the screen is rotated.  Pressing the back button will cause the view to be
    destroyed, but pressing home, and then using multitasking to get back, will not (most of the time)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Call super, we must do this because it handles a lot of nice things for us.
        setContentView(R.layout.activity_heartrate); // Set our content view for what we want to show on screen.
        setSupportActionBar(mToolbar); // Set our toolbar to the one we designated in the layout.

        // Create a new device selection dialog.
        BLESelectionDialog dialog = new BLESelectionDialog();

        // Now, we need to subscribe to it.  This might look like black magic, but just follow the comments.
        mDeviceSubscription = dialog.getObservable() // Get the Observable from the device dialog.

                // We then want to "map" this observable to a different one.
                // HermesBLE.connectAndListen outputs a different observable.  So, once we get to this point,
                // we're passing our device we select in the dialog, on to HermesBLE for the connecting.
                .flatMap(device -> HermesBLE.connectAndListen(device,
                        BLStandardAttributes.SERVICE_HEART_RATE,
                        BLStandardAttributes.CHAR_HEART_RATE_MEASUREMENT))

                // After the above runs, we'll be connected.  So, the first "event" we get will be a success.
                // Lets take out the BluetoothGatt from this event and save it.  We'll need it to clean up later.
                .doOnNext(event -> mGatt = event.gatt)

                // We also should change the color of our heart if we get a solid connection!
                .doOnNext(event -> mHeart.setColorFilter(COLOR_HEART))

                // Now that we're connected, we'll be getting events every time what we've subscribed to
                // changes.  To keep things clean, we've supplied a HeartRateEvaluator that handles the
                // byte conversion for you.  Just map it in the same way you mapped connectAndListen.
                .flatMap(event -> new HeartRateEvaluator().handle(event))

                // Finally, we're at a point where we're getting something we can use.
                // .subscribe tells the Observable to finally startup, and that after we've marched
                // through the above, we'll get a rate.  We can use this rate to set our TextView mRate.
                .subscribe(rate -> {
                    mRate.setText(String.valueOf(rate));
                });

        // We also need to make sure our dialog can be seen.  If this isn't run, then nothing shows up!.
        dialog.show(getFragmentManager(), "dialog");
    }

    /*
    onDestroy is ran every time the activity is destroyed.  This is normally the last we see of the Activity.
    Because of this, we don't want our bluetooth subscriptions to continue to run.
    We NEED to tell HermesBLE to clean up our mess.  Otherwise, good luck connecting again!
     */
    @Override
    protected void onDestroy() {
        super.onDestroy(); // Call super, because things.

        HermesBLE.close(mGatt); // Have Hermes handle closing out our bluetooth connection for us.
        mDeviceSubscription.unsubscribe(); // And unsubscribe from the dialog we created.

        // Finally, just incase we're not really closing, make sure we do - by running finish.
        finish();
    }
}
