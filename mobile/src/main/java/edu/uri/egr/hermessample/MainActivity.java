package edu.uri.egr.hermessample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermes.attributes.RBLGattAttributes;
import rx.Subscription;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    private Subscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(v -> Hermes.get().getBleWrapper().findDevices(5)
                .doOnNext(device -> Timber.d(device.getName() + " -- " + device.getAddress()))
                .filter(device -> device.getAddress().equals("72:3A:DA:81:3C:14"))
                .doOnNext(device -> Hermes.get().getBleWrapper().connectAndListen(device, RBLGattAttributes.BLE_SHIELD_SERVICE, RBLGattAttributes.BLE_SHIELD_RX))
                        //.flatMap(device -> Hermes.get().getBleWrapper().connectAndList(device))
                .subscribe());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
