package edu.uri.egr.hermes.events;

import com.google.android.gms.wearable.Channel;

/**
 * This file is part of Hermes.
 * Developed by Cody Goldberg - 8/24/2015
 * <p>
 * Hermes is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Hermes is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Hermes.  If not, see <http://www.gnu.org/licenses/>.
 */
public class InputClosedEvent extends WearableReasonEvent {
    public InputClosedEvent(Channel channel) {
        super(channel);
    }

    public InputClosedEvent(Channel channel, int closeReason, int appSpecificErrorCode) {
        super(channel, closeReason, appSpecificErrorCode);
    }
}
