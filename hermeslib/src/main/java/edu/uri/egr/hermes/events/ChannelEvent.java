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

package edu.uri.egr.hermes.events;

import android.os.Parcel;

import com.google.android.gms.wearable.Channel;

public class ChannelEvent extends WearableReasonEvent {
    public ChannelEvent(Channel channel) {
        super(channel);
    }

    public ChannelEvent(Channel channel, int closeReason, int appSpecificErrorCode) {
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

    protected ChannelEvent(Parcel in) {
        super(in);
    }

    public static final Creator<ChannelEvent> CREATOR = new Creator<ChannelEvent>() {
        public ChannelEvent createFromParcel(Parcel source) {
            return new ChannelEvent(source);
        }

        public ChannelEvent[] newArray(int size) {
            return new ChannelEvent[size];
        }
    };
}