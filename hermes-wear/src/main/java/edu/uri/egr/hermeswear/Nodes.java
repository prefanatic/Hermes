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

import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

import edu.uri.egr.hermes.exceptions.HermesException;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;

/**
 * Created by cody on 9/22/15.
 */
public class Nodes {
    private final List<Node> mConnectedNodes = new ArrayList<>();

    protected Nodes() {
        HermesWearable.getPeerConnected().subscribe(mConnectedNodes::add);
        HermesWearable.getPeerDisconnected().subscribe(mConnectedNodes::remove);
    }

    public Observable<Node> getNodes() {
        if (!mConnectedNodes.isEmpty())
            return Observable.just(mConnectedNodes)
                    .flatMap(Observable::from);

        return Observable.defer(() -> {
            NodeApi.GetConnectedNodesResult result = Wearable.NodeApi.getConnectedNodes(HermesWearable.getClientBlocking()).await();
            if (result.getStatus().isSuccess())
                return Observable.just(result.getNodes())
                        .flatMap(Observable::from)
                        .filter(Node::isNearby)
                        .doOnNext(mConnectedNodes::add);

            throw OnErrorThrowable.from(new HermesException("Node result is null from NodeApi."));
        });
    }
}
