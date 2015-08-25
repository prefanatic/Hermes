package edu.uri.egr.hermes.wrappers;

import android.support.v4.util.SimpleArrayMap;

import com.google.android.gms.wearable.MessageEvent;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermes.exceptions.HermesException;
import edu.uri.egr.hermes.services.AbstractAudioRecordingService;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;
import timber.log.Timber;

/**
 This file is part of Hermes.
 Developed by Cody Goldberg - 8/20/15

 Hermes is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Hermes is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Hermes.  If not, see <http://www.gnu.org/licenses/>.

 */
public class RxDispatchWrapper {
    public static final String SUBJECT_DATA_CHANGED = "data.changed";
    public static final String SUBJECT_MESSAGE_RECEIVED = "message.received";
    public static final String SUBJECT_PEER_CONNECTED = "peer.connected";
    public static final String SUBJECT_PEER_DISCONNECTED = "peer.disconnected";
    public static final String SUBJECT_NODES_CONNECTED = "nodes.connected";
    public static final String SUBJECT_CHANNEL_OPENED = "channel.opened";
    public static final String SUBJECT_CHANNEL_CLOSED = "channel.closed";
    public static final String SUBJECT_INPUT_CLOSED = "input.closed";
    public static final String SUBJECT_OUTPUT_CLOSED = "output.closed";

    private final Hermes hermes = Hermes.get();
    private final SimpleArrayMap<String, Subject<?, ?>> subjectMap = new SimpleArrayMap();

    private static RxDispatchWrapper instance;

    private RxDispatchWrapper() {
        // Create subjects for all our dispatch events.
        // TODO: There must be a better way!
        createSubject(SUBJECT_DATA_CHANGED);
        createSubject(SUBJECT_MESSAGE_RECEIVED);
        createSubject(SUBJECT_PEER_CONNECTED);
        createSubject(SUBJECT_PEER_DISCONNECTED);
        createSubject(SUBJECT_NODES_CONNECTED);
        createSubject(SUBJECT_CHANNEL_OPENED);
        createSubject(SUBJECT_CHANNEL_CLOSED);
        createSubject(SUBJECT_INPUT_CLOSED);
        createSubject(SUBJECT_OUTPUT_CLOSED);
        createSubject(AbstractAudioRecordingService.SUBJECT_STATE); // ????
    }

    public static RxDispatchWrapper get() {
        if (instance == null)
            instance = new RxDispatchWrapper();

        return instance;
    }

    public <T> Subject<T, T> createSubject(String key) {
        return (Subject<T, T>) subjectMap.put(key, new SerializedSubject(PublishSubject.create()));
    }

    public <T> Subject<T, T> getSubject(String key) {
        if (!subjectMap.containsKey(key))
            return createSubject(key);

        return (Subject<T, T>) subjectMap.get(key);
    }

    public <T> Observable<T> getObservable(String key) {
        if (!subjectMap.containsKey(key))
            throw new HermesException("No observable found under key " + key);

        Observable<T> observable = (Observable<T>) subjectMap.get((key));

        return observable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
