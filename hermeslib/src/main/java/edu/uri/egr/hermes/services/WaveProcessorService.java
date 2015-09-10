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

package edu.uri.egr.hermes.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermes.exceptions.HermesException;
import edu.uri.egr.hermes.manipulators.WaveHeader;
import rx.Observable;
import rx.subjects.Subject;
import timber.log.Timber;


public class WaveProcessorService extends IntentService {
    public static final String EXTRA_INPUT_URI = "input.uri";
    public static final String EXTRA_OUTPUT_URI = "output.uri";
    public static final String EXTRA_BITS_PER_SAMPLE = "bits.per.sample";
    public static final String EXTRA_ENCODING_FORMAT = "encoding.format";
    public static final String EXTRA_NUM_CHANNELS = "num.channels.";
    public static final String EXTRA_SAMPLE_RATE = "sample.rate";

    public static final String SUBJECT_RESULT = "wave.processor.subject";

    private Subject<File, File> mResultSubject;

    public WaveProcessorService() {
        super("WaveProcessorService");
    }

    public static void process(Context context, Uri inputUri, Uri outputUri, int sampleRate) {
        Intent intent = new Intent(context, WaveProcessorService.class);
        intent.putExtra(EXTRA_INPUT_URI, inputUri);
        intent.putExtra(EXTRA_OUTPUT_URI, outputUri);
        intent.putExtra(EXTRA_SAMPLE_RATE, sampleRate);

        context.startService(intent);
    }

    public static void process(Context context, File inputFile, File outputFile, int sampleRate) {
        Intent intent = new Intent(context, WaveProcessorService.class);
        intent.putExtra(EXTRA_INPUT_URI, Uri.fromFile(inputFile));
        intent.putExtra(EXTRA_OUTPUT_URI, Uri.fromFile(outputFile));
        intent.putExtra(EXTRA_SAMPLE_RATE, sampleRate);

        context.startService(intent);
    }

    public static Observable<File> getResultObservable() {
        return Hermes.get().getDispatchWrapper().getObservable(SUBJECT_RESULT);
    }

    private WaveHeader prepareHeader(short bitsPerSample, short encodingFormat, short channels, int sampleRate) {
        WaveHeader header = new WaveHeader();
        header.setBitsPerSample(bitsPerSample);
        header.setFormat(encodingFormat);
        header.setNumChannels(channels);
        header.setSampleRate(sampleRate);

        return header;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mResultSubject = Hermes.get().getDispatchWrapper().getSubject(SUBJECT_RESULT);
        // TODO: 8/28/2015 Do we need a wakelock here??
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Uri inputUri = intent.getParcelableExtra(EXTRA_INPUT_URI);
            Uri outputUri = intent.getParcelableExtra(EXTRA_OUTPUT_URI);
            short bitsPerSample = intent.getShortExtra(EXTRA_BITS_PER_SAMPLE, (short) 16);
            short encodingFormat = intent.getShortExtra(EXTRA_ENCODING_FORMAT, (short) 1);
            short numChannels = intent.getShortExtra(EXTRA_NUM_CHANNELS, (short) 1);
            int sampleRate = intent.getIntExtra(EXTRA_SAMPLE_RATE, 44100);

            if (inputUri == null || outputUri == null) {
                Timber.e("Missing input or output URI.  Ending.");
                return;
            }

            File inputFile = new File(inputUri.getPath());
            File outputFile = new File(outputUri.getPath());

            if (!inputFile.exists()) {
                Timber.e("Input file does not exist at path: %s", inputFile.getPath());
                return;
            }

            try {
                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
                BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

                // Write the Wave header.
                WaveHeader header = prepareHeader(bitsPerSample, encodingFormat, numChannels, sampleRate);
                header.setNumBytes(inputStream.available());
                header.write(outputStream);

                int bytesRead;
                byte[] buffer = new byte[1024];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer);
                }

                inputStream.close();
                outputStream.close();

                mResultSubject.onNext(outputFile);
                //mResultSubject.onCompleted();

            } catch (FileNotFoundException e) {
                Timber.e("File not found: %s", e.getMessage());
                mResultSubject.onError(e);
            } catch (IOException e) {
                Timber.e("Failed to process file: %s", e.getMessage());
                mResultSubject.onError(e);
            }
        }
    }
}
