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

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.uri.egr.hermes.callbacks.HermesExceptionCallback;
import edu.uri.egr.hermes.exceptions.HermesException;
import edu.uri.egr.hermes.exceptions.RxGoogleApiException;
import edu.uri.egr.hermes.services.RxWearableDispatcherService;
import edu.uri.egr.hermes.wrappers.FileWrapper;
import edu.uri.egr.hermes.wrappers.RxBleWrapper;
import edu.uri.egr.hermes.wrappers.RxDispatchWrapper;
import edu.uri.egr.hermes.wrappers.RxWearableWrapper;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.OnErrorThrowable;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;
import timber.log.Timber;

public class Hermes {
    public static final String ACTION_WEARABLE_DISPATCH = "hermes.intent.action.WEARABLE_DISPATCH";
    public static final String EXTRA_SUBJECT = "hermes.intent.extra.SUBJECT";
    public static final String EXTRA_OBJECT = "hermes.intent.extra.OBJECT";

    private static Hermes mInstance;

    private Context context;
    private Config config;
    private volatile GoogleApiClient mGoogleApiClient;
    private Subject<GoogleApiClient, GoogleApiClient> mGoogleSubject = new SerializedSubject<>(BehaviorSubject.create());
    private File mRootFolder;

    // Children Classes
    private static RxDispatchWrapper mDispatchWrapper;
    private static RxWearableWrapper mWearableWrapper;
    private static FileWrapper mFileWrapper;
    private static RxBleWrapper mBleWrapper;

    private Hermes(Context context, Config config) {
        this.context = context;
        this.config = config;

        // Plant Timber regardless of being debug or not.
        Timber.plant(new Timber.DebugTree());

        // Connect to Google.
        createGoogleClient();

        mRootFolder = config.baseFolder;
        if (mRootFolder == null)
            mRootFolder = Environment.getExternalStorageDirectory();

        mRootFolder.mkdirs();
    }

    public static void init(Context context, Config config) {
        if (mInstance != null)
            throw new HermesException("There is already and instance of Hermes.");

        mInstance = new Hermes(context, config);
        mDispatchWrapper = RxDispatchWrapper.get();
        mWearableWrapper = new RxWearableWrapper(mInstance);
        mFileWrapper = new FileWrapper(mInstance);
        mBleWrapper = new RxBleWrapper(mInstance);
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

    public File getRootFolder() {
        return mRootFolder;
    }

    public void onException(Exception e) {
        if (config.exceptionCallback != null)
            config.exceptionCallback.onException(e);
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

    public FileWrapper getFileWrapper() {
        return mFileWrapper;
    }

    public RxBleWrapper getBleWrapper() {
        return mBleWrapper;
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

    public Observable<GoogleApiClient> getGoogleClientObservable() {
        return mGoogleSubject;
    }

    private void createGoogleClient() {
        new Thread(() -> {
            long startTime = System.currentTimeMillis();

            if (context == null)
                throw OnErrorThrowable.from(new HermesException("Hermes has not been initialized yet."));

            if (mGoogleApiClient == null) {
                GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context);
                for (int i = 0; i < config.apis.size(); i++) {
                    builder.addApi(config.apis.get(i));

                    // FIXME: 9/19/2015 This is hardcoded for WearableAPI.  Will future ones have something like this?
                    if (config.apis.get(i).equals(Wearable.API)) {
                        PackageManager manager = getContext().getPackageManager();
                        ComponentName name = new ComponentName(RxWearableDispatcherService.class.getPackage().toString(), RxWearableDispatcherService.class.getName());

                        manager.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    }
                }

                mGoogleApiClient = builder.build();
            }

            if (mGoogleApiClient.isConnected()) {
                mGoogleSubject.onNext(mGoogleApiClient);
                return;
            }

            ConnectionResult result = mGoogleApiClient.blockingConnect();
            if (result.isSuccess()) {
                Timber.d("Connected to Google (%d ms).", System.currentTimeMillis() - startTime);
                mGoogleSubject.onNext(mGoogleApiClient);
                return;
            }

            Timber.e("GoogleApiClient error: %d", result.getErrorCode());
            mGoogleSubject.onError(new RxGoogleApiException(result));
        }).start();
    }

    public static final class Config {
        List<Api> apis = new ArrayList<>();
        File baseFolder;
        HermesExceptionCallback exceptionCallback;

        public Config setExceptionCallback(HermesExceptionCallback callback) {
            exceptionCallback = callback;
            return this;
        }

        public Config addApi(Api api) {
            apis.add(api);
            return this;
        }

        public Config setBaseFolder(File folder) {
            baseFolder = folder;
            return this;
        }
    }
}
