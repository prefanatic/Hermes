package edu.uri.egr.hermes.services;

import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

import edu.uri.egr.hermes.wrappers.RxDispatchWrapper;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * Created by cody on 8/20/15.
 */
public class RxWearableDispatcherService extends WearableListenerService {
    public static final int SUBJECT_DATA_CHANGED = 1;
    public static final int SUBJECT_MESSAGE_RECEIVED = 2;

    public RxDispatchWrapper dispatchWrapper = RxDispatchWrapper.get();

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        dispatchWrapper.getSubject(SUBJECT_DATA_CHANGED)
                .onNext(dataEvents);

        PublishSubject<DataEventBuffer> subject = dispatchWrapper.getSubject(SUBJECT_DATA_CHANGED);

        subject.onNext(dataEvents);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        dispatchWrapper.getSubject(SUBJECT_MESSAGE_RECEIVED)
                .onNext(messageEvent);
    }

    @Override
    public void onPeerConnected(Node peer) {
    }

    @Override
    public void onPeerDisconnected(Node peer) {
    }

    @Override
    public void onConnectedNodes(List<Node> connectedNodes) {
    }

    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        super.onCapabilityChanged(capabilityInfo);
    }

    @Override
    public void onChannelOpened(Channel channel) {
        super.onChannelOpened(channel);
    }

    @Override
    public void onChannelClosed(Channel channel, int closeReason, int appSpecificErrorCode) {
        super.onChannelClosed(channel, closeReason, appSpecificErrorCode);
    }

    @Override
    public void onInputClosed(Channel channel, int closeReason, int appSpecificErrorCode) {
        super.onInputClosed(channel, closeReason, appSpecificErrorCode);
    }

    @Override
    public void onOutputClosed(Channel channel, int closeReason, int appSpecificErrorCode) {
        super.onOutputClosed(channel, closeReason, appSpecificErrorCode);
    }
}
