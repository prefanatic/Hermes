package edu.uri.egr.hermes;

import android.app.Application;

/**
 * Created by cody on 8/20/15.
 */
public class HermesApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Hermes.init(this);
    }
}
