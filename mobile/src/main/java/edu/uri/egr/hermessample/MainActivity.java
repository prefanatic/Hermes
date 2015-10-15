package edu.uri.egr.hermessample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import edu.uri.egr.hermessample.adapter.Sample;
import edu.uri.egr.hermessample.adapter.SampleAdapter;
import edu.uri.egr.hermessample.samples.HeartRateActivity;
import edu.uri.egr.hermessample.samples.RxSensorActivity;
import edu.uri.egr.hermessample.samples.UartActivity;
import edu.uri.egr.hermesui.activity.HermesActivity;

public class MainActivity extends HermesActivity {
    @Bind(R.id.recycler) RecyclerView mRecycler;
    @Bind(R.id.toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar(mToolbar);

        SampleAdapter adapter = new SampleAdapter();
        adapter.addSample(new Sample(HeartRateActivity.class, "Heart Rate Activity", "Receive a heart rate through a bluetooth device."));
        adapter.addSample(new Sample(UartActivity.class, "UART Activity", "Receive data through a UART channel from a bluetooth device."));
        adapter.addSample(new Sample(RxSensorActivity.class, "Sensors Activity", "Receive sensor data using Reactive style programming."));

        mRecycler.setAdapter(adapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
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
