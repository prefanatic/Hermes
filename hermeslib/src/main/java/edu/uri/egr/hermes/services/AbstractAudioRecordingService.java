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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import java.io.IOException;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermes.exceptions.HermesException;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;
import timber.log.Timber;

public abstract class AbstractAudioRecordingService extends Service {
    public static final String ACTION_START = "Start";
    public static final String ACTION_STOP = "Stop";
    public static final String SUBJECT_STATE = "audio.recording.state";

    public static final int STATE_IDLE = 0;
    public static final int STATE_RECORDING = 1;
    public static final int STATE_PROCESSING = 2;
    private int STATE = 0;

    private Hermes hermes = Hermes.get();
    private Subject<Integer, Integer> mStateSubject;
    private PowerManager.WakeLock mWakeLock;
    private volatile AudioRecord mAudioRecord;
    private Thread mRecordingThread;

    private volatile int bufferSize;
    private int sampleRate = 44100;
    private int channels = AudioFormat.CHANNEL_IN_MONO;
    private int encoding = AudioFormat.ENCODING_PCM_16BIT;
    private long lastStateUpdate = 0;

    protected static void start(Context context, Class c) {
        Intent intent = new Intent(context, c);
        intent.setAction(ACTION_START);

        context.startService(intent);
    }

    protected static void stop(Context context, Class c) {
        Intent intent = new Intent(context, c);
        intent.setAction(ACTION_STOP);

        context.startService(intent);
    }

    public final void onCreate() {
        // Create our state subject for people to listen in on.
        mStateSubject = hermes.getDispatchWrapper().getSubject(SUBJECT_STATE);

        // Grab objects for wakelock management.
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HermesAudioRecordingLock");

        onRecordingCreate();

        updateState(STATE_IDLE);
    }

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Timber.d("Action: %s", intent.getAction());
            switch (intent.getAction()) {
                case ACTION_START:
                    startRecording();
                    break;
                case ACTION_STOP:
                    stopRecording();
                    break;
            }
        }

        // TODO: 8/25/2015 Set this up so that we can gracefully recontinue recording if something happens.
        return START_NOT_STICKY;
    }

    @Override
    public final void onDestroy() {
        //mStateSubject.onCompleted();
    }

    public static Observable<Integer> getStateObservable() {
        return Hermes.get().getDispatchWrapper().getObservable(SUBJECT_STATE);
    }

    private void updateState(int state) {
        // Debug length of time between state changes.  I'm curious if there is any time spent in STATE_IDLE.
        Timber.d("Switching state from %s to %s - %d ms", STATE, state, System.currentTimeMillis() - lastStateUpdate);
        lastStateUpdate = System.currentTimeMillis();

        STATE = state;
        mStateSubject.onNext(state);
    }

    public abstract boolean autoStartRecording();

    protected final void startRecordThread() {
        mRecordingThread = new Thread(this::receiveRecording);
        mRecordingThread.setPriority(Thread.MAX_PRIORITY);
        mRecordingThread.start();

        updateState(STATE_RECORDING);
    }

    private void startRecording() {
        updateState(STATE_PROCESSING);

        if (!mWakeLock.isHeld())
            mWakeLock.acquire();

        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channels, encoding);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channels, encoding, bufferSize);

        onRecordingPrepare();

        if (autoStartRecording())
            startRecordThread();
    }

    private void receiveRecording() {
        mAudioRecord.startRecording();

        int bytesRead;
        byte[] buffer = new byte[bufferSize];
        while (!Thread.currentThread().isInterrupted()) {
            bytesRead = mAudioRecord.read(buffer, 0, bufferSize);
            if (bytesRead == AudioRecord.ERROR) {
                Timber.e("Error while recording: %s", bytesRead);
                hermes.onException(new HermesException("Unknown error while recording audio.  Failure code: " + bytesRead));
                break;
            }

            try {
                onAudioBufferReceived(buffer);
            } catch (IOException e) {
                Timber.e("IOException pushing onAudioBufferReceived: %s", e.getMessage());
                hermes.onException(e);
                break;
            }
        }

        updateState(STATE_PROCESSING);

        mAudioRecord.stop();
        mAudioRecord.release();
        mAudioRecord = null;

        // Swap to the UI thread to send the stopRecording();
        new Handler(Looper.getMainLooper()).post(this::stopRecordingMainThread);
    }

    private void stopRecording() {
        if (mRecordingThread != null && mRecordingThread.isAlive())
            mRecordingThread.interrupt();
    }

    private void stopRecordingMainThread() {
        try {
            onRecordingEnd();
        } catch (IOException e) {
            Timber.e("IOException pushing onRecordingEnd: %s", e.getMessage());
            hermes.onException(e);
        }

        if (mWakeLock.isHeld())
            mWakeLock.release();

        updateState(STATE_IDLE);
        stopSelf();
    }

    public final void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public abstract void onRecordingCreate();

    public abstract void onRecordingPrepare();

    public abstract void onRecordingEnd() throws IOException;

    public abstract void onAudioBufferReceived(byte[] buffer) throws IOException;

    @Nullable
    @Override
    public final IBinder onBind(Intent intent) {
        return null;
    }
}
