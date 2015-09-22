package edu.uri.egr.hermessample;

import android.content.Context;

import com.google.android.gms.wearable.Node;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermes.services.AbstractAudioRecordingService;
import edu.uri.egr.hermeswear.HermesWearable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class AudioStreamingService extends AbstractAudioRecordingService {
    private Hermes hermes;
    private BufferedOutputStream outputStream;

    public static void start(Context context) {
        start(context, AudioStreamingService.class);
    }

    public static void stop(Context context) {
        stop(context, AudioStreamingService.class);
    }

    public AudioStreamingService() {
    }

    @Override
    public void onRecordingCreate() {
        hermes = Hermes.get();
    }

    @Override
    public boolean autoStartRecording() {
        return false; // We don't want to auto start the recording.  We need to open a channel before audio starts coming in.
    }

    @Override
    public void onRecordingPrepare() {
        // Open an output stream to our phone.
        // TODO: 8/25/2015 Better detection of phone node, instead of this loop.
        HermesWearable.Node.getNodes()
                .map(Node::getId)
                .flatMap(node -> HermesWearable.Channel.openOutputStream(node, "audio_stream"))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::outputStreamOpened, this::error, this::completed);
    }

    private void outputStreamOpened(OutputStream stream) {
        outputStream = new BufferedOutputStream(stream);
    }

    private void error(Throwable e) {
        Timber.e("Error while retrieving output stream: %s", e.getMessage());
    }

    private void completed() {
        startRecordThread();
    }

    @Override
    public void onRecordingEnd() throws IOException {
        outputStream.close();
    }

    @Override
    public void onAudioBufferReceived(byte[] buffer) throws IOException {
        outputStream.write(buffer);
    }
}
