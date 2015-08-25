package edu.uri.egr.hermessample;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.wearable.Channel;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermes.events.ChannelEvent;
import edu.uri.egr.hermes.wrappers.RxDispatchWrapper;
import edu.uri.egr.hermes.wrappers.RxWearableWrapper;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class AudioReceiverService extends IntentService {

    public AudioReceiverService() {
        super("AudioReceiverService");
    }

    private void handleChannelOpened(Channel channel) {
        // Call out to RxWearableWrapper to pull out the InputStream from the channel.
        Hermes hermes = Hermes.get(); // Request a Hermes instance.
        RxWearableWrapper wrapper = hermes.getWearableWrapper(); // Get Hermes' Rx WearableAPI wrapper.

        wrapper.getInputStream(channel) // Get the InputStream Observable.
                .subscribeOn(Schedulers.io()) // Request that we perform the work of getting the InputStream on the IO thread.
                .observeOn(Schedulers.io()) // And then keep it on the IO thread.
                .subscribe(this::handleInputStream, this::handleError); // Pass off receiving the InputStream and Errors to different methods.

        /* You can also chain methods - the above can look like the following:

        Hermes.get().getWearableWrapper().getInputStream(channel)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe(this::handleInputStream, this::handleError);

            And it does the same thing, without creating the two Hermes and RxWearableWrapper variables.
         */
    }

    private void handleInputStream(InputStream stream) {
        // We have the InputStream!  Because we're on the IO thread, we can do all the work we want here.
        // Otherwise, we would halt the UI thread, and prevent users from doing anything on their phone.

        // Buffer the InputStream.  This allows us to keep reading consistently, even when there may be some slowdowns on the device.
        BufferedInputStream inputStream = new BufferedInputStream(stream);

        int bytesRead; // Variable to hold how many bytes we've read.
        byte[] buffer = new byte[1024]; // Buffer to hold the audio data.
        long startTime = System.currentTimeMillis(); // The UNIX EPOCH staring time of this method.
        long nextCheck = startTime + 1000; // Check to see how many bytes we received in 1 second.
        int bytesReceived = 0;
        int bytesPerSecond = 0;

        // Put the following in a try catch.
        // This will allow us to handle any errors given by inputStream.read gracefully.
        try {

            // We want to repeat the following forever, until we get a -1 value.
            // The -1 signifies that there are no more bytes to be read, which means our wearable has stopped sending data.
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                // TODO: 8/25/2015 Audio saving example.

                // For fun, lets calculate how many bytes per second we receive from the wearable.
                // This value varies depending on distance between the two nodes.
                // Add in how many bytes we read into the bytesReceived variable.
                bytesReceived += bytesRead;

                // If we've hit our one second check.
                if (System.currentTimeMillis() > nextCheck) {

                    // Move in our bytesReceived into bytesPerSecond.
                    // Because we're checking every second, no additional math needs to occur.
                    bytesPerSecond = bytesReceived;

                    // Clean bytesReceived
                    bytesReceived = 0;

                    // Increment our nextCheck one second.
                    nextCheck = System.currentTimeMillis() + 1000;

                    // Output how many kB/s we have!
                    Timber.d("Received %d kB/s", bytesPerSecond / 1024);
                }

            }
        } catch (IOException e) {
            Timber.e("Error while reading audio data: %s", e.getMessage());
        }
    }

    private void handleError(Throwable e) {
        // This is called specifically when opening the input stream fails.
        // Could be caused by the wearable leaving range of the phone during the channel opening.
        Timber.e("Failed to open InputStream: %s", e.getMessage());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Check to see if we 'actually' get the intent.
        // This service is started through Android implicitly.  This should never be null.
        // However, if someone calls this improperly, it can be null, and can shut down everything.
        if (intent != null) {

            // Does the intent contain the action we want to listen to?
            if (intent.getAction().equals(Hermes.ACTION_WEARABLE_DISPATCH)) {

                // Does the intent contain the dispatch type we want?
                String subject = intent.getStringExtra(Hermes.EXTRA_SUBJECT);
                if (subject != null && subject.equals(RxDispatchWrapper.SUBJECT_CHANNEL_OPENED)) {

                    // Steal the channel in question.
                    ChannelEvent event = intent.getParcelableExtra(Hermes.EXTRA_OBJECT); // TODO: 8/25/2015 Where can I put this string????

                    // Android is calling us because there is a channel opened to our device.
                    // Follow into a cleaner method to do magic.
                    handleChannelOpened(event.channel);
                }
            }
        }
    }
}
