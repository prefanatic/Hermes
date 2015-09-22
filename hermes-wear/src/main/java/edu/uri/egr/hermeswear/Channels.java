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

import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.ChannelApi;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.io.OutputStream;

import edu.uri.egr.hermes.exceptions.RxGoogleApiStatusException;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import timber.log.Timber;

/**
 * Created by cody on 9/22/15.
 */
public class Channels {
    protected Channels() {

    }

    public Observable<Channel> openChannel(String nodeId, String path) {
        return Observable.defer(() -> {
            ChannelApi.OpenChannelResult result = Wearable.ChannelApi.openChannel(HermesWearable.getClientBlocking(), nodeId, path).await();
            if (result.getStatus().isSuccess())
                return Observable.just(result.getChannel());

            Timber.e("OpenChannelResult is not successful: %s", result.getStatus().getStatusMessage());
            throw OnErrorThrowable.from(new RxGoogleApiStatusException(result.getStatus()));
        });
    }

    public Observable<InputStream> getInputStream(Channel channel) {
        return Observable.defer(() -> {
            Channel.GetInputStreamResult result = channel.getInputStream(HermesWearable.getClientBlocking()).await();
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
            Channel.GetOutputStreamResult result = channel.getOutputStream(HermesWearable.getClientBlocking()).await();
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
}
