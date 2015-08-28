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

package edu.uri.egr.hermes.wrappers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.util.SimpleArrayMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uri.egr.hermes.Hermes;
import rx.Observable;
import rx.Subscriber;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class ScheduleWrapper {
    public static final String EXTRA_ALARM_ID = "alarm.id";

    private final Hermes hermes;
    private final AlarmManager alarmManager;
    private final SimpleArrayMap<Integer, String> activeAlarms = new SimpleArrayMap<>();

    public ScheduleWrapper(Hermes hermes) {
        this.hermes = hermes;
        this.alarmManager = (AlarmManager) hermes.getContext().getSystemService(Context.ALARM_SERVICE);
    }

    /*
    public int scheduleExact(Date date, String action) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, date.getTime());
    }

    private void generatePendingIntent(String action) {
        Intent intent = new Intent(action);
        intent.setPackage(hermes.getContext().getPackageName());
    }

    private int generateAlarmId() {

    }
    */

}
