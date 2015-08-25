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

import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.ChannelApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermes.events.ChannelEvent;
import edu.uri.egr.hermes.events.InputClosedEvent;
import edu.uri.egr.hermes.events.OutputClosedEvent;
import edu.uri.egr.hermes.exceptions.HermesException;
import edu.uri.egr.hermes.exceptions.RxGoogleApiException;
import edu.uri.egr.hermes.exceptions.RxGoogleApiStatusException;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class RxWearableWrapper {
    private final Hermes hermes;
    private final List<Node> mConnectedNodes = new ArrayList<>();

    public RxWearableWrapper(Hermes hermes) {
        this.hermes = hermes;

        // Subscribe to the connection and disconnection node things.
        getPeerConnected().subscribe(mConnectedNodes::add);
        getPeerDisconnected().subscribe(mConnectedNodes::remove);
    }

    /*
        -- MessageAPI Send Wrapper
     */
    public Observable<Integer> sendMessage(String nodeId, String path, byte[] payload) {
        return Observable.defer(() -> {
            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(hermes.getGoogleClientUnsafe(), nodeId, path, payload).await();
            if (result.getStatus().isSuccess())
                return Observable.just(result.getRequestId());

            Timber.e("SendMessageResult is not successful: %s", result.getStatus().getStatusMessage());
            throw OnErrorThrowable.from(new RxGoogleApiStatusException(result.getStatus()));
        });
    }

    public Observable<Integer> sendMessage(String nodeId, String path) {
        return sendMessage(nodeId, path, null);
    }

    /*
        -- ChannelAPI Wrapper
     */
    public Observable<Channel> openChannel(String nodeId, String path) {
        return Observable.defer(() -> {
            ChannelApi.OpenChannelResult result = Wearable.ChannelApi.openChannel(hermes.getGoogleClientUnsafe(), nodeId, path).await();
            if (result.getStatus().isSuccess())
                return Observable.just(result.getChannel());

            Timber.e("OpenChannelResult is not successful: %s", result.getStatus().getStatusMessage());
            throw OnErrorThrowable.from(new RxGoogleApiStatusException(result.getStatus()));
        });
    }

    public Observable<InputStream> getInputStream(Channel channel) {
        return Observable.defer(() -> {
            Channel.GetInputStreamResult result = channel.getInputStream(hermes.getGoogleClientUnsafe()).await();
            if (result.getStatus().isSuccess())
                return Observable.just(result.getInputStream());

            Timber.e("GetInputStreamResult is not successful: %s", result.getStatus().getStatusMessage());
            throw OnErrorThrowable.from(new RxGoogleApiStatusException(result.getStatus()));
        });
    }

    public Observable<InputStream> openInputStream(String nodeId, String path) {
        return openChannel(nodeId, path)
                .flatMap(this::getInputStream);
    }

    public Observable<OutputStream> getOutputStream(Channel channel) {
        return Observable.defer(() -> {
            Channel.GetOutputStreamResult result = channel.getOutputStream(hermes.getGoogleClientUnsafe()).await();
            if (result.getStatus().isSuccess())
                return Observable.just(result.getOutputStream());

            Timber.e("GetOutputStreamResult is not successful: %s", result.getStatus().getStatusMessage());
            throw OnErrorThrowable.from(new RxGoogleApiStatusException(result.getStatus()));
        });
    }

    public Observable<OutputStream> openOutputStream(String nodeId, String path) {
        return openChannel(nodeId, path)
                .flatMap(this::getOutputStream);
    }

    /*
        -- Convenience Functions
     */
    public Observable<MessageEvent> getMessageEvent() {
        return hermes.getDispatchWrapper().getObservable(RxDispatchWrapper.SUBJECT_MESSAGE_RECEIVED);
    }

    public Observable<ChannelEvent> getChannelOpened() {
        return hermes.getDispatchWrapper().getObservable(RxDispatchWrapper.SUBJECT_CHANNEL_OPENED);
    }

    public Observable<ChannelEvent> getChannelClosed() {
        return hermes.getDispatchWrapper().getObservable(RxDispatchWrapper.SUBJECT_CHANNEL_CLOSED);
    }

    public Observable<InputClosedEvent> getInputClosed() {
        return hermes.getDispatchWrapper().getObservable(RxDispatchWrapper.SUBJECT_INPUT_CLOSED);
    }

    public Observable<OutputClosedEvent> getOutputClosed() {
        return hermes.getDispatchWrapper().getObservable(RxDispatchWrapper.SUBJECT_OUTPUT_CLOSED);
    }

    public Observable<Node> getPeerConnected() {
        return hermes.getDispatchWrapper().getObservable(RxDispatchWrapper.SUBJECT_PEER_CONNECTED);
    }

    public Observable<Node> getPeerDisconnected() {
        return hermes.getDispatchWrapper().getObservable(RxDispatchWrapper.SUBJECT_PEER_DISCONNECTED);
    }

    public Observable<Node> getNodes() {
        if (!mConnectedNodes.isEmpty())
            return Observable.just(mConnectedNodes)
                    .flatMap(Observable::from);

        return Observable.defer(() -> {
            NodeApi.GetConnectedNodesResult result = Wearable.NodeApi.getConnectedNodes(hermes.getGoogleClientUnsafe()).await();
            if (result.getStatus().isSuccess())
                return Observable.just(result.getNodes())
                        .flatMap(Observable::from)
                        .filter(Node::isNearby)
                        .doOnNext(mConnectedNodes::add);

            throw OnErrorThrowable.from(new HermesException("Node result is null from NodeApi."));
        });
    }
}
