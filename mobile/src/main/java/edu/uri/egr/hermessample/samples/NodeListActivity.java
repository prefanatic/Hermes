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

package edu.uri.egr.hermessample.samples;

import android.os.Bundle;

import com.google.android.gms.wearable.Node;

import edu.uri.egr.hermesui.activity.HermesActivity;
import edu.uri.egr.hermeswear.HermesWearable;
import timber.log.Timber;

/**
 * Created by cody on 10/16/15.
 */
public class NodeListActivity extends HermesActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HermesWearable.Node.getNodes()
                .subscribe(this::nodeFound, this::nodeError, this::nodeComplete);
    }

    private void nodeFound(Node node) {
        Timber.d("Node found: %s", node.getDisplayName());
    }

    private void nodeError(Throwable e) {
        Timber.e("Node error: %s", e.getMessage());
    }

    private void nodeComplete() {
        Timber.d("Node complete.");
    }
}
