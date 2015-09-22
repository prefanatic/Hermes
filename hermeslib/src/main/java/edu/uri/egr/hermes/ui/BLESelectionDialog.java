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

package edu.uri.egr.hermes.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermes.R;
import rx.Subscription;

public class BLESelectionDialog extends DialogFragment {
    private Hermes hermes = Hermes.get();
    private BLEDeviceAdapter mAdapter;
    private Subscription mSubscription;
    private ProgressBar mProgressBar;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_bluetooth_selection, null);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressbar);

        mAdapter = new BLEDeviceAdapter();
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(R.string.ble_search_title)
                .setNeutralButton(R.string.ble_search_cancel, (dialog, which) -> dialog.cancel())
                .setPositiveButton(R.string.ble_search_continue, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setView(view);

        subscribeToFinder();

        return builder.create();
    }

    private void error(Throwable e) {

    }

    private void searchCompleted() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void subscribeToFinder() {
        mSubscription = hermes.getBleWrapper().findDevices(10)
                .subscribe(mAdapter::addDevice, this::error, this::searchCompleted);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        mSubscription.unsubscribe();
    }
}
