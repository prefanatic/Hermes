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

package edu.uri.egr.hermes.wrappers;

import android.support.v4.util.SimpleArrayMap;

import com.google.android.gms.wearable.MessageEvent;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermes.exceptions.HermesException;
import edu.uri.egr.hermes.services.AbstractAudioRecordingService;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;
import timber.log.Timber;

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
        createBehaviorSubject(AbstractAudioRecordingService.SUBJECT_STATE, AbstractAudioRecordingService.STATE_IDLE); // ????
    }

    public static RxDispatchWrapper get() {
        if (instance == null)
            instance = new RxDispatchWrapper();

        return instance;
    }

    public <T> Subject<T, T> createSubject(String key) {
        return (Subject<T, T>) subjectMap.put(key, new SerializedSubject(PublishSubject.create()));
    }

    public <T> Subject<T, T> createBehaviorSubject(String key, T defaultValue) {
        return (Subject<T, T>) subjectMap.put(key, new SerializedSubject<>(BehaviorSubject.create(defaultValue)));
    }

    public <T> Subject<T, T> getSubject(String key) {
        if (!subjectMap.containsKey(key))
            return createSubject(key);

        return (Subject<T, T>) subjectMap.get(key);
    }

    public <T> Observable<T> getObservable(String key) {
        if (!subjectMap.containsKey(key))
            createSubject(key);

        Observable<T> observable = (Observable<T>) subjectMap.get((key));

        return observable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public <T> Observable<T> createReceiver(String key) {
        return null;
    }
}
