package edu.uri.egr.hermes.wrappers;

import android.support.v4.util.SimpleArrayMap;

import com.google.android.gms.wearable.MessageEvent;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermes.exceptions.HermesException;
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
    public static final int SUBJECT_DATA_CHANGED = 1;
    public static final int SUBJECT_MESSAGE_RECEIVED = 2;
    public static final int SUBJECT_PEER_CONNECTED = 3;
    public static final int SUBJECT_PEER_DISCONNECTED = 4;
    public static final int SUBJECT_NODES_CONNECTED = 5;
    public static final int SUBJECT_CHANNEL_OPENED = 6;
    public static final int SUBJECT_CHANNEL_CLOSED = 7;
    public static final int SUBJECT_INPUT_CLOSED = 8;
    public static final int SUBJECT_OUTPUT_CLOSED = 9;

    private final Hermes hermes = Hermes.get();
    private final SimpleArrayMap<Integer, Subject<?, ?>> subjectMap = new SimpleArrayMap();

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
    }

    public static RxDispatchWrapper get() {
        if (instance == null)
            instance = new RxDispatchWrapper();

        return instance;
    }

    public <T> Subject<T, T> createSubject(int key) {
        return (Subject<T, T>) subjectMap.put(key, PublishSubject.create());
    }

    public <T> Subject<T, T> getSubject(int key) {
        if (!subjectMap.containsKey(key))
            return createSubject(key);

        return (Subject<T, T>) subjectMap.get(key);
    }

    public <T> Observable<T> getObservable(int key) {
        if (!subjectMap.containsKey(key))
            throw new HermesException("No observable found under key " + key);

        Observable<T> observable = (Observable<T>) subjectMap.get((key));

        return observable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
