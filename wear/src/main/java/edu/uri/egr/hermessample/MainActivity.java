package edu.uri.egr.hermessample;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.Button;

import edu.uri.egr.hermes.Hermes;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class MainActivity extends Activity {

    private Button mTestButton;
    private int serviceState = 0;
    private final Hermes hermes = Hermes.get();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(this::onStubInflated);
    }

    private void onStubInflated(WatchViewStub stub) {
        mTestButton = (Button) stub.findViewById(R.id.button);
        mTestButton.setOnClickListener(v -> {
            // Send a message to our main node.
            /*
            hermes.getWearableNodes()
                    .map(Node::getId)
                    .flatMap(node -> hermes.getWearableWrapper().sendMessage(node, "test_path"))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.newThread())
                    .subscribe(requestId -> {
                        Timber.d("Completed with request id %d", requestId);
                    }, e -> {
                        Timber.e("Failed to send message: %s", e.getMessage());
                    }, () -> Timber.d("Completed message!"));
                    */

            /*
            hermes.getWearableNodes()
                    .map(Node::getId)
                    .flatMap(node -> hermes.getWearableWrapper().openChannel(node, "test_channel"))
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(channel -> Timber.d("Channel opened: %s",
                            channel.getNodeId()), e -> Timber.e("Failed to open channel: %s", e.getMessage()));
                            */
            Timber.d("State: %s", serviceState);

            if (serviceState == 0) {
                Timber.d("Starting");
                AudioStreamingService.start(this);
            } else if (serviceState == 1) {
                Timber.d("Stopping");
                AudioStreamingService.stop(this);
            }
        });

        // Observe AudioStreamingService state
        AudioStreamingService.getStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(state -> {
                    serviceState = state;
                    Timber.d("State update: %s", state);

                    switch (state) {
                        case 0:
                            mTestButton.setText("Record");
                            break;
                        case 1:
                            mTestButton.setText("Stop");
                            break;
                        case 2:
                            mTestButton.setText("Processing");
                            break;
                    }
                });
    }
}
