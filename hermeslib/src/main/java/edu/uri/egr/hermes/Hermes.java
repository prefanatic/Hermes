package edu.uri.egr.hermes;

import android.content.Context;
import android.os.Handler;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.uri.egr.hermes.exceptions.HermesException;
import edu.uri.egr.hermes.exceptions.RxGoogleApiException;
import edu.uri.egr.hermes.wrappers.RxDispatchWrapper;
import edu.uri.egr.hermes.wrappers.RxWearableWrapper;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.OnErrorThrowable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 This file is part of Hermes.
 Developed by Cody Goldberg - 8/24/2015

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
public class Hermes {
    public static final String ACTION_WEARABLE_DISPATCH = "hermes.intent.action.WEARABLE_DISPATCH";
    public static final String EXTRA_SUBJECT = "hermes.intent.extra.SUBJECT";
    public static final String EXTRA_OBJECT = "hermes.intent.extra.OBJECT";

    private static Hermes mInstance;

    private Context context;
    private volatile GoogleApiClient mGoogleApiClient;

    // Children Classes
    private static RxDispatchWrapper mDispatchWrapper;
    private static RxWearableWrapper mWearableWrapper;

    private Hermes(Context context) {
        this.context = context;

        // Plant Timber regardless of being debug or not.
        Timber.plant(new Timber.DebugTree());

        // Connect to Google.
        getGoogleClient()
                .subscribe(client -> {
                    // The client has connected!
                }, e -> {
                    // Something happened :(
                    RxGoogleApiException exception = ((RxGoogleApiException) e);
                    // TODO: What do we do here?
                    // When an Activity/Service requests the client, they'll subscribe to this error.
                    // They can just resolve it there.
                });
    }

    public static void init(Context context) {
        if (mInstance != null)
            throw new HermesException("There is already and instance of Hermes.");

        mInstance = new Hermes(context);
        mDispatchWrapper = RxDispatchWrapper.get();
        mWearableWrapper = new RxWearableWrapper(mInstance);
    }

    public static Hermes get() {
        if (mInstance != null)
            return mInstance;
        throw new HermesException("Hermes has not been initialized yet.");
    }

    /*
        -- Children Wrapper Get Methods
     */
    public RxDispatchWrapper getDispatchWrapper() {
        return mDispatchWrapper;
    }

    public RxWearableWrapper getWearableWrapper() {
        return mWearableWrapper;
    }

    /*
        -- Observer Request Methods
     */
    public <T> Observable<T> getWearableObservable(String subject) {
        return mDispatchWrapper.getObservable(subject);
    }

    public Observable<Node> getWearableNodes() {
        return mWearableWrapper.getNodes();
    }

    /*
        -- Google API Client Methods
     */

    public boolean isClientSafe() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    public GoogleApiClient getGoogleClientUnsafe() {
        return mGoogleApiClient;
    }

    public Observable<GoogleApiClient> getGoogleClient() {
        return Observable.defer(() -> {
            long startTime = System.currentTimeMillis();

            if (context == null)
                throw OnErrorThrowable.from(new HermesException("Hermes has not been initialized yet."));

            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(context)
                        .addApi(Wearable.API)
                        .build();
            }

            if (mGoogleApiClient.isConnected())
                return Observable.just(mGoogleApiClient);

            ConnectionResult result = mGoogleApiClient.blockingConnect();
            if (result.isSuccess()) {
                Timber.d("Connected to Google (%d ms).", System.currentTimeMillis() - startTime);
                return Observable.just(mGoogleApiClient);
            }

            Timber.e("GoogleApiClient error: %d", result.getErrorCode());
            throw OnErrorThrowable.from(new RxGoogleApiException(result));

        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
