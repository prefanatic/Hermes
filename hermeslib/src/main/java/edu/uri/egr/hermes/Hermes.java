package edu.uri.egr.hermes;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import edu.uri.egr.hermes.exceptions.HermesException;
import edu.uri.egr.hermes.exceptions.RxGoogleApiException;
import edu.uri.egr.hermes.wrappers.RxDispatchWrapper;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.OnErrorThrowable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Hermes
 *
 */
public class Hermes {
    private static Hermes mInstance;

    private Context context;
    private volatile GoogleApiClient mGoogleApiClient;

    // Children Classes
    private static RxDispatchWrapper mDispatchWrapper;

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
    }

    public static Hermes get() {
        if (mInstance != null)
            return mInstance;
        throw new HermesException("Hermes has not been initialized yet.");
    }

    /*
        -- Observer Request Methods
     */
    public <T> Observable<T> getWearableObservable(int subject) {
        return mDispatchWrapper.getObserver(subject);
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
