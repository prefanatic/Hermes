package edu.uri.egr.hermessample;

import android.app.Application;

import com.google.android.gms.wearable.Wearable;

import edu.uri.egr.hermes.Hermes;

/**
 * Created by cody on 8/20/15.
 */
public class SampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Hermes.Config config = new Hermes.Config()
                .addApi(Wearable.API);

        Hermes.init(this, config);
    }
}
