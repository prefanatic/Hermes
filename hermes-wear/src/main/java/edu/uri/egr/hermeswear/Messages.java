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

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import edu.uri.egr.hermes.exceptions.RxGoogleApiStatusException;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by cody on 9/22/15.
 */
public class Messages {
    protected Messages() {
    }

    public Observable<Integer> sendMessage(String nodeId, String path, byte[] payload) {
        return Observable.defer(() -> {
            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(HermesWearable.getClientBlocking(), nodeId, path, payload).await();
            if (result.getStatus().isSuccess())
                return Observable.just(result.getRequestId());

            Timber.e("SendMessageResult is not successful: %s", result.getStatus().getStatusMessage());
            throw OnErrorThrowable.from(new RxGoogleApiStatusException(result.getStatus()));
        });
    }

    public Observable<Integer> sendMessage(String nodeId, String path) {
        return sendMessage(nodeId, path, null);
    }

    public Observable<Integer> sendMessage(String path) {
        return HermesWearable.Node.getNodes()
                .map(Node::getId)
                .doOnNext(id -> Timber.d("Node: %s", id))
                .flatMap(s -> sendMessage(s, path))
                .subscribeOn(Schedulers.io());
    }
}
