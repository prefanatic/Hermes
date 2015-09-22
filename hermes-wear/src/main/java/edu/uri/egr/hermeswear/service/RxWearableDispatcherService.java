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

package edu.uri.egr.hermeswear.service;

import android.content.Intent;
import android.os.Parcelable;

import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermeswear.event.ChannelEvent;
import edu.uri.egr.hermeswear.event.InputClosedEvent;
import edu.uri.egr.hermeswear.event.OutputClosedEvent;
import edu.uri.egr.hermes.Dispatch;
import edu.uri.egr.hermeswear.HermesWearable;
import timber.log.Timber;

public class RxWearableDispatcherService extends WearableListenerService {
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Timber.d("Data events changed.");
        dispatch(HermesWearable.SUBJECT_DATA_CHANGED, dataEvents);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Timber.d("Message received from %s: %s (%s)", messageEvent.getSourceNodeId(), messageEvent.getPath(), Thread.currentThread().getName());
        dispatch(HermesWearable.SUBJECT_MESSAGE_RECEIVED, messageEvent);
    }

    @Override
    public void onPeerConnected(Node peer) {
        Timber.d("Adding node: %s (%s)", peer.getDisplayName(), peer.getId());
        dispatch(HermesWearable.SUBJECT_PEER_CONNECTED, peer);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        Timber.d("Removing node: %s (%s)", peer.getDisplayName(), peer.getId());
        dispatch(HermesWearable.SUBJECT_PEER_DISCONNECTED, peer);
    }

    @Override
    public void onConnectedNodes(List<Node> connectedNodes) {
        Timber.d("Nodes connected: %s", connectedNodes);
        // TODO: 8/24/2015 Do we need this?
    }

    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        // TODO: 8/24/2015 Do we need this?
    }

    @Override
    public void onChannelOpened(Channel channel) {
        Timber.d("Channel opening from %s: %s", channel.getNodeId(), channel.getPath());
        dispatch(HermesWearable.SUBJECT_CHANNEL_OPENED, new ChannelEvent(channel));
    }

    @Override
    public void onChannelClosed(Channel channel, int closeReason, int appSpecificErrorCode) {
        Timber.d("Channel closing from %s: %s", channel.getNodeId(), channel.getPath());
        dispatch(HermesWearable.SUBJECT_CHANNEL_OPENED, new ChannelEvent(channel, closeReason, appSpecificErrorCode));
    }

    @Override
    public void onInputClosed(Channel channel, int closeReason, int appSpecificErrorCode) {
        Timber.d("Channel InputStream closing from %s (%s) with reason %s (%s)", channel.getNodeId(), channel.getPath(), closeReason, appSpecificErrorCode);
        dispatch(HermesWearable.SUBJECT_INPUT_CLOSED, new InputClosedEvent(channel, closeReason, appSpecificErrorCode));
    }

    @Override
    public void onOutputClosed(Channel channel, int closeReason, int appSpecificErrorCode) {
        Timber.d("Channel OutputStream closing from %s (%s) with reason %s (%s)", channel.getNodeId(), channel.getPath(), closeReason, appSpecificErrorCode);
        dispatch(HermesWearable.SUBJECT_OUTPUT_CLOSED, new OutputClosedEvent(channel, closeReason, appSpecificErrorCode));
    }

    private void dispatch(String subject, Object o) {
        Hermes.Dispatch.getSubject(subject)
                .onNext(o);

        // TODO: 8/26/2015 The below doesn't belong here.  Move to RxDispatchWrapper or a new IntentDispatchWrapper
        // Broadcast this as a dispatch event.
        Intent intent = new Intent(Hermes.ACTION_WEARABLE_DISPATCH);
        intent.putExtra(Hermes.EXTRA_SUBJECT, subject);

        if (o instanceof Parcelable)
            intent.putExtra(Hermes.EXTRA_OBJECT, ((Parcelable) o));

        intent.setPackage(getPackageName());
        intent.addCategory(subject);

        startService(intent);
    }
}
