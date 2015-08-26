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

package edu.uri.egr.hermessample;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.wearable.Channel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermes.events.ChannelEvent;
import edu.uri.egr.hermes.manipulators.FileLog;
import edu.uri.egr.hermes.services.WaveProcessorService;
import edu.uri.egr.hermes.wrappers.RxWearableWrapper;
import rx.Observable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class AudioReceiverService extends IntentService {

    // We want to hold our FileLog here.
    // Because it is accessed between methods, we cant create it locally in one, and use it elsewhere.
    // Variable creation is scoped - so if we put it "outside" everything, everything inside can see it.
    // However, if we put it "inside" something, nothing outside can see it.
    private FileLog log;

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

        // Create a log file, where we can store some information.
        log = new FileLog("example-log.csv");

        // We need to set the log headers, so Hermes knows what we're talking about.
        // If you decide to change the headers later, the log file is deleted and recreated.
        log.setHeaders("Date", "Time Started", "Time Ended", "File Name", "File Size");

        // You can log a specific column, or the whole line at a time.
        // Right now, because we have start and end times, we only want to log specific columns now.
        log.writeSpecific(0, log.date()); // In the date column, write the date.
        log.writeSpecific(1, log.time()); // In the time column, write the time.

        // Create the temporary file used to store our audio.
        File outputFile = Hermes.get().getFileWrapper().create("example-audio");

        // Buffer the InputStream.  This allows us to keep reading consistently, even when there may be some slowdowns on the device.
        BufferedInputStream inputStream = new BufferedInputStream(stream);

        int bytesRead; // Variable to hold how many bytes we've read.
        byte[] buffer = new byte[1024]; // Buffer to hold the audio data.
        long startTime = System.currentTimeMillis(); // The UNIX EPOCH staring time of this method.
        long nextCheck = startTime + 1000; // Check to see how many bytes we received in 1 second.
        int totalBytesReceived = 0; // Hold our bytes received.
        int bytesPerSecond; // Hold our bytes per second.
        int i; // Create a variable to hold any loops we do.

        // Put the following in a try catch.
        // This will allow us to handle any errors given by inputStream.read gracefully.
        try {

            // Create an OutputStream for our outputFile.
            // We put this in the try catch statement to catch any FileNotFound exceptions.
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            // We want to repeat the following forever, until we get a -1 value.
            // The -1 signifies that there are no more bytes to be read, which means our wearable has stopped sending data.
            while ((bytesRead = inputStream.read(buffer)) != -1) {

                // For fun, lets calculate how many bytes per second we receive from the wearable.
                // This value varies depending on distance between the two nodes.
                // Add in how many bytes we read into the bytesReceived variable.
                totalBytesReceived += bytesRead;

                // If we've hit our one second check.
                if (System.currentTimeMillis() > nextCheck) {

                    // Move in our bytesReceived into bytesPerSecond.
                    // Because we're checking every second, no additional math needs to occur.
                    bytesPerSecond = totalBytesReceived;

                    // Clean bytesReceived
                    totalBytesReceived = 0;

                    // Increment our nextCheck one second.
                    nextCheck = System.currentTimeMillis() + 1000;

                    // Output how many kB/s we have!
                    Timber.d("Received %d kB/s", bytesPerSecond / 1024);
                }

                // Now that the fun is over, we should save our buffer to a temporary file.
                // Normally, you would be able to call outputStream.write(buffer)
                // However, because this data is coming from the network (the wearable), sometimes we'll receive less that what our buffer actually holds.
                // To handle this, we loop through each byte we receive, and write that individually.
                for (i = 0; i < bytesRead; i++)
                    outputStream.write(buffer[i]);

            }

            // We now are out of our while loop, meaning we've hit the end of available bytes from the InputStream.
            // To gracefully end, we need to tell the InputStream and OutputStream to close.
            inputStream.close();
            outputStream.close();

            // Log down the time that we stopped receiving audio.
            log.writeSpecific(2, log.time());

            // So now we have raw audio in a temporary file, it isn't very useful.
            // Lets push this raw file over to WaveProcessorService to convert it to a Wave file.
            // First, we need to create the output file.
            File outputWaveFile = Hermes.get().getFileWrapper().createExternal("sample-processed.wav");

            // Secondly, we get the Observable WaveProcessorService provides.  This will let us listen to it's results.
            Observable<File> resultObservable = WaveProcessorService.getResultObservable();
            resultObservable.subscribe(this::waveProcessed, this::waveProcessorError);

            // Finally, tell WaveProcessorService that we're ready to process this file.
            // We also have to tell it what sample rate we receive the audio at.  In this sample, it is 44100 kHz.
            WaveProcessorService.process(this, outputFile, outputWaveFile, 44100);

        } catch (IOException e) {
            Timber.e("Error while reading audio data: %s", e.getMessage());
        }
    }

    private void handleError(Throwable e) {
        // This is called specifically when opening the input stream fails.
        // Could be caused by the wearable leaving range of the phone during the channel opening.
        Timber.e("Failed to open InputStream: %s", e.getMessage());
    }

    private void waveProcessed(File file) {
        // Yay!  The file has been processed, and is located on the external storage.
        // Lets make this file visible when you plug in to the computer.
        // This step needs to occur, otherwise, you won't be able to see it.
        Hermes.get().getFileWrapper().makeVisible(file);

        // Log our final pieces of data.
        log.writeSpecific(3, file.getName());
        log.writeSpecific(4, file.length());

        // Because we're using writeSpecific, Hermes will wait for us to manually "finish" the log file.
        // We do that by running log.flush()
        log.flush();

        // If instead of writing specifics, we wanted to write it all at once, we can do the following:
        // log.write("Column 1", "Column 2", "Column 3", ..., "Column 10");
        // However, if you cannot write all your columns at once, Hermes will complain about it, and your next write will overwrite your previous line!

        Timber.d("Finished processing file: %s", file.getPath());
    }

    private void waveProcessorError(Throwable e) {
        // This is called when the WaveProcessorService encounters an error while processing.
        Timber.e("Failed to process WAVE: %s", e.getMessage());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Check to see if we 'actually' get the intent.
        // This service is started through Android implicitly.  This should never be null.
        // However, if someone calls this improperly, it can be null, and can shut down everything.
        // We also don't need to check to see if this intent is really for us, because that is done in the manifest.
        if (intent != null) {

            // Steal the channel in question.
            ChannelEvent event = intent.getParcelableExtra(Hermes.EXTRA_OBJECT);

            // Android is calling us because there is a channel opened to our device.
            // Follow into a cleaner method to do magic.
            handleChannelOpened(event.channel);

        }
    }
}
