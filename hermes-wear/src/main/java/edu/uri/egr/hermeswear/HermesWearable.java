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

package edu.uri.egr.hermeswear;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermeswear.event.ChannelEvent;
import edu.uri.egr.hermeswear.event.InputClosedEvent;
import edu.uri.egr.hermeswear.event.OutputClosedEvent;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by cody on 9/22/15.
 */
public class HermesWearable {
    public static final String SUBJECT_DATA_CHANGED = "data.changed";
    public static final String SUBJECT_MESSAGE_RECEIVED = "message.received";
    public static final String SUBJECT_PEER_CONNECTED = "peer.connected";
    public static final String SUBJECT_PEER_DISCONNECTED = "peer.disconnected";
    public static final String SUBJECT_NODES_CONNECTED = "nodes.connected";
    public static final String SUBJECT_CHANNEL_OPENED = "channel.opened";
    public static final String SUBJECT_CHANNEL_CLOSED = "channel.closed";
    public static final String SUBJECT_INPUT_CLOSED = "input.closed";
    public static final String SUBJECT_OUTPUT_CLOSED = "output.closed";

    public static final Messages Message = new Messages();
    public static final Nodes Node = new Nodes();
    public static final Channels Channel = new Channels();

    private static final Hermes hermes = Hermes.get();

    private HermesWearable() {
    }

    public static Observable<MessageEvent> getMessageEvent() {
        return Hermes.Dispatch.getObservable(SUBJECT_MESSAGE_RECEIVED);
    }

    public static Observable<ChannelEvent> getChannelOpened() {
        return Hermes.Dispatch.getObservable(SUBJECT_CHANNEL_OPENED);
    }

    public static Observable<ChannelEvent> getChannelClosed() {
        return Hermes.Dispatch.getObservable(SUBJECT_CHANNEL_CLOSED);
    }

    public static Observable<InputClosedEvent> getInputClosed() {
        return Hermes.Dispatch.getObservable(SUBJECT_INPUT_CLOSED);
    }

    public static Observable<OutputClosedEvent> getOutputClosed() {
        return Hermes.Dispatch.getObservable(SUBJECT_OUTPUT_CLOSED);
    }

    public static Observable<Node> getPeerConnected() {
        return Hermes.Dispatch.getObservable(SUBJECT_PEER_CONNECTED)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .cast(Node.class);
    }

    public static Observable<Node> getPeerDisconnected() {
        return Hermes.Dispatch.getObservable(SUBJECT_PEER_DISCONNECTED)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .cast(Node.class);
    }

    static GoogleApiClient getClientBlocking() {
        return Hermes.get().getGoogleClientObservable()
                .toBlocking()
                .last();
    }

}
