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

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;

public class Bus {
    private final SerializedSubject<Seat, Seat> bus;
    private final SerializedSubject<Seat, Seat> stickyBus;

    protected Bus() {
        bus = new SerializedSubject<>(PublishSubject.create());
        stickyBus = new SerializedSubject<>(BehaviorSubject.create());
    }

    public void push(String subject, Object payload) {
        bus.onNext(new Seat(subject, payload));
    }

    public void pushSticky(String subject, Object payload) {
        stickyBus.onNext(new Seat(subject, payload));
    }

    public <T> Observable<T> observe(String subject, Class<T> cla) {
        return bus.asObservable()
                .filter(seat -> seat.subject.endsWith(subject))
                .map(seat -> seat.payload)
                .cast(cla);
    }

    public <T> Observable<T> observeSticky(String subject, Class<T> cla) {
        return stickyBus.asObservable()
                .filter(seat -> seat.subject.endsWith(subject))
                .map(seat -> seat.payload)
                .cast(cla);
    }

    public class Seat {
        public final String subject;
        public final Object payload;

        public Seat(String subject, Object payload) {
            this.payload = payload;
            this.subject = subject;
        }
    }

}
