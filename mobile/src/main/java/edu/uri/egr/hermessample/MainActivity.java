package edu.uri.egr.hermessample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.wearable.MessageEvent;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermes.wrappers.RxDispatchWrapper;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    private Subscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Grab Hermes.
        Hermes hermes = Hermes.get();

        // Test receiving messages!
        Observable<MessageEvent> eventObservable = hermes.getWearableObservable(RxDispatchWrapper.SUBJECT_MESSAGE_RECEIVED);
        eventObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> Timber.d("Observable message from %s: %s", event.getSourceNodeId(), event.getPath()));

        // Test Channels!
        hermes.getWearableWrapper().getChannelOpened()
                .subscribe(channelEvent -> {
                    Timber.d("Channel opened: %s", channelEvent.channel.getNodeId());
                });
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
