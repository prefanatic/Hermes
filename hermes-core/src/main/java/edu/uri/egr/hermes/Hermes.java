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

package edu.uri.egr.hermes;

import android.content.Context;
import android.os.Environment;
import android.support.v4.BuildConfig;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

import edu.uri.egr.hermes.callbacks.HermesExceptionCallback;
import edu.uri.egr.hermes.exceptions.HermesException;
import edu.uri.egr.hermes.exceptions.RxGoogleApiException;
import rx.Observable;
import rx.subjects.ReplaySubject;
import rx.subjects.SerializedSubject;
import timber.log.Timber;

public class Hermes {
    public static final String ACTION_WEARABLE_DISPATCH = "hermes.intent.action.WEARABLE_DISPATCH";
    public static final String EXTRA_SUBJECT = "hermes.intent.extra.SUBJECT";
    public static final String EXTRA_OBJECT = "hermes.intent.extra.OBJECT";

    private volatile static Hermes mInstance;

    private Context context;
    private Config config;
    private volatile GoogleApiClient mGoogleApiClient;
    private SerializedSubject<GoogleApiClient, GoogleApiClient> mGoogleApiSubject = new SerializedSubject<>(ReplaySubject.create());
    private java.io.File mRootFolder;

    // Children Classes
    public static final File File = new File();
    public static final Dispatch Dispatch = new Dispatch();
    public static final Sensor Sensor = new Sensor();
    public static final Bus Bus = new Bus();

    private Hermes(Context context, Config config) {
        this.context = context;
        this.config = config;

        // Plant timber if we request debug.
        if (config.debug)
            Timber.plant(new Timber.DebugTree());

        // Connect to Google.
        createGoogleClient();

        mRootFolder = config.baseFolder;
        if (mRootFolder == null)
            mRootFolder = Environment.getExternalStorageDirectory();

        mRootFolder.mkdirs();

        Timber.i("Hermes (%s) - Cody Goldberg [WBL]", BuildConfig.VERSION_CODE);
    }

    public static void init(Context context, Config config) {
        if (mInstance != null)
            throw new HermesException("There is already and instance of Hermes.");

        mInstance = new Hermes(context, config);
    }

    public static Hermes get() {
        if (mInstance != null)
            return mInstance;
        throw new HermesException("Hermes has not been initialized yet.");
    }

    public Config getConfig() {
        return config;
    }

    public Context getContext() {
        return context;
    }

    public java.io.File getRootFolder() {
        return mRootFolder;
    }

    public void setRootFolder(java.io.File folder) {
        mRootFolder = folder;

        mRootFolder.mkdirs();
    }

    public void exception(Exception e) {
        if (config.exceptionCallback != null)
            config.exceptionCallback.onException(e);
        Timber.e(e.getMessage());
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

    public Observable<GoogleApiClient> getGoogleClientObservable() {
        return mGoogleApiSubject.asObservable();
    }

    private void createGoogleClient() {
        new Thread(() -> {
            long startTime = System.currentTimeMillis();

            if (config.apis.size() == 0) {
                mGoogleApiSubject.onError(new HermesException("No API requested for GoogleApiClient."));
                return;
            } else if (context == null) {
                mGoogleApiSubject.onError(new HermesException("Hermes has not been initialized yet."));
                return;
            }

            if (mGoogleApiClient == null) {
                GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context);
                for (int i = 0; i < config.apis.size(); i++) {
                    builder.addApi(config.apis.get(i));
                }

                mGoogleApiClient = builder.build();
            } else if (mGoogleApiClient.isConnected()) {
                mGoogleApiSubject.onNext(mGoogleApiClient);
                mGoogleApiSubject.onCompleted();

                return;
            }

            ConnectionResult result = mGoogleApiClient.blockingConnect();
            if (result.isSuccess()) {
                Timber.d("Connected to Google (%d ms).", System.currentTimeMillis() - startTime);
                mGoogleApiSubject.onNext(mGoogleApiClient);
                mGoogleApiSubject.onCompleted();

                return;
            }

            Timber.e("GoogleApiClient error: %d", result.getErrorCode());
            mGoogleApiSubject.onError(new RxGoogleApiException(result));
        }).start();
    }

    public static final class Config {
        protected List<Api> apis = new ArrayList<>();
        protected java.io.File baseFolder;
        protected HermesExceptionCallback exceptionCallback;
        protected boolean debug;

        public Config addApi(Api api) {
            apis.add(api);
            return this;
        }

        public Config setBaseFolder(java.io.File folder) {
            baseFolder = folder;
            return this;
        }

        public Config setExceptionCallback(HermesExceptionCallback callback) {
            exceptionCallback = callback;
            return this;
        }

        public Config enableDebug(boolean b) {
            debug = b;
            return this;
        }
    }
}
