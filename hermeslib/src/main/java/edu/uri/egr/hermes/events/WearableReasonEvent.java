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
import android.os.Parcelable;

import com.google.android.gms.wearable.Channel;

public class WearableReasonEvent implements Parcelable {
    public Channel channel;
    public int closeReason;
    public int appSpecificErrorCode;

    public WearableReasonEvent(Channel channel) {
        this(channel, 0, 0);
    }

    public WearableReasonEvent(Channel channel, int closeReason, int appSpecificErrorCode) {
        this.channel = channel;
        this.closeReason = closeReason;
        this.appSpecificErrorCode = appSpecificErrorCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.channel, 0);
        dest.writeInt(this.closeReason);
        dest.writeInt(this.appSpecificErrorCode);
    }

    protected WearableReasonEvent(Parcel in) {
        this.channel = in.readParcelable(Channel.class.getClassLoader());
        this.closeReason = in.readInt();
        this.appSpecificErrorCode = in.readInt();
    }

}
