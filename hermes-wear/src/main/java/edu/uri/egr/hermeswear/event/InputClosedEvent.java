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

package edu.uri.egr.hermeswear.event;

import android.os.Parcel;

import com.google.android.gms.wearable.Channel;

public class InputClosedEvent extends WearableReasonEvent {
    public InputClosedEvent(Channel channel) {
        super(channel);
    }

    public InputClosedEvent(Channel channel, int closeReason, int appSpecificErrorCode) {
        super(channel, closeReason, appSpecificErrorCode);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected InputClosedEvent(Parcel in) {
        super(in);
    }

    public static final Creator<InputClosedEvent> CREATOR = new Creator<InputClosedEvent>() {
        public InputClosedEvent createFromParcel(Parcel source) {
            return new InputClosedEvent(source);
        }

        public InputClosedEvent[] newArray(int size) {
            return new InputClosedEvent[size];
        }
    };
}
