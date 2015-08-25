package edu.uri.egr.hermes.services;

import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

import edu.uri.egr.hermes.events.ChannelEvent;
import edu.uri.egr.hermes.events.InputClosedEvent;
import edu.uri.egr.hermes.events.OutputClosedEvent;
import edu.uri.egr.hermes.wrappers.RxDispatchWrapper;
import edu.uri.egr.hermes.wrappers.RxWearableWrapper;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
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
public class RxWearableDispatcherService extends WearableListenerService {
    public RxDispatchWrapper dispatchWrapper = RxDispatchWrapper.get();

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Timber.d("Data events changed.");
        dispatch(RxDispatchWrapper.SUBJECT_DATA_CHANGED, dataEvents);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Timber.d("Message received from %s: %s (%s)", messageEvent.getSourceNodeId(), messageEvent.getPath(), Thread.currentThread().getName());
        dispatch(RxDispatchWrapper.SUBJECT_MESSAGE_RECEIVED, messageEvent);
    }

    @Override
    public void onPeerConnected(Node peer) {
        Timber.d("Adding node: %s (%s)", peer.getDisplayName(), peer.getId());
        dispatch(RxDispatchWrapper.SUBJECT_PEER_CONNECTED, peer);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        Timber.d("Removing node: %s (%s)", peer.getDisplayName(), peer.getId());
        dispatch(RxDispatchWrapper.SUBJECT_PEER_DISCONNECTED, peer);
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
        dispatch(RxDispatchWrapper.SUBJECT_CHANNEL_OPENED, new ChannelEvent(channel));
    }

    @Override
    public void onChannelClosed(Channel channel, int closeReason, int appSpecificErrorCode) {
        Timber.d("Channel closing from %s: %s", channel.getNodeId(), channel.getPath());
        dispatch(RxDispatchWrapper.SUBJECT_CHANNEL_OPENED, new ChannelEvent(channel, closeReason, appSpecificErrorCode));
    }

    @Override
    public void onInputClosed(Channel channel, int closeReason, int appSpecificErrorCode) {
        Timber.d("Channel InputStream closing from %s (%s) with reason %s (%s)", channel.getNodeId(), channel.getPath(), closeReason, appSpecificErrorCode);
        dispatch(RxDispatchWrapper.SUBJECT_INPUT_CLOSED, new InputClosedEvent(channel, closeReason, appSpecificErrorCode));
    }

    @Override
    public void onOutputClosed(Channel channel, int closeReason, int appSpecificErrorCode) {
        Timber.d("Channel OutputStream closing from %s (%s) with reason %s (%s)", channel.getNodeId(), channel.getPath(), closeReason, appSpecificErrorCode);
        dispatch(RxDispatchWrapper.SUBJECT_OUTPUT_CLOSED, new OutputClosedEvent(channel, closeReason, appSpecificErrorCode));
    }

    private void dispatch(int subject, Object o) {
        dispatchWrapper.getSubject(subject)
                .onNext(o);
    }
}
