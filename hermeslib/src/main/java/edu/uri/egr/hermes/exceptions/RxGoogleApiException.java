package edu.uri.egr.hermes.exceptions;

import com.google.android.gms.common.ConnectionResult;

/**
 * Created by cody on 8/20/15.
 */
public class RxGoogleApiException extends RuntimeException {
    public ConnectionResult result;

    public RxGoogleApiException(ConnectionResult result) {
        this.result = result;
    }

    @Override
    public String getMessage() {
        return "Api client failed with code: " + result.getErrorCode();
    }
}
