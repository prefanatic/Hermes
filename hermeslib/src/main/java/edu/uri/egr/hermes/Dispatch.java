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

package edu.uri.egr.hermes;

import android.support.v4.util.SimpleArrayMap;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermes.services.AbstractAudioRecordingService;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class Dispatch {
    private final SimpleArrayMap<String, Subject<?, ?>> subjectMap = new SimpleArrayMap();

    protected Dispatch() {
        // Create subjects for all our dispatch events.
        // TODO: There must be a better way!
        createBehaviorSubject(AbstractAudioRecordingService.SUBJECT_STATE, AbstractAudioRecordingService.STATE_IDLE); // ????
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
