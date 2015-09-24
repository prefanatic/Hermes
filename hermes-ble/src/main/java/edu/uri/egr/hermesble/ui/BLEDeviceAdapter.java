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

package edu.uri.egr.hermesble.ui;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import java.util.ArrayList;
import java.util.List;

import edu.uri.egr.hermesble.R;
import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;


public class BLEDeviceAdapter extends RecyclerView.Adapter<BLEDeviceAdapter.ViewHolder> implements View.OnClickListener {
    public final List<BluetoothDevice> devices = new ArrayList<>();
    public ViewHolder mSelected;
    private RecyclerView mRecycler;
    private BehaviorSubject<View> mClickSubject = BehaviorSubject.create();

    public Observable<View> getClickObservable() {
        return mClickSubject.asObservable();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecycler = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecycler = null;
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void addDevice(BluetoothDevice device) {
        devices.add(device);
        notifyItemInserted(devices.size());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);

        holder.deviceName.setText(device.getName());
        holder.deviceId.setText(device.getAddress());

        if (isSelected(holder)) {
            holder.itemView.setBackgroundColor(Color.GRAY);
        } else {
            holder.itemView.setBackground(null);
        }
    }

    private boolean isSelected(ViewHolder holder) {
        return mSelected != null && holder == mSelected;
    }

    @Override
    public void onClick(View v) {
        mSelected = (ViewHolder) mRecycler.getChildViewHolder(v);

        notifyDataSetChanged();
        mClickSubject.onNext(v);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_bluetooth_device, parent, false));
        holder.itemView.setOnClickListener(this);

        return holder;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView deviceName, deviceId;

        public ViewHolder(View itemView) {
            super(itemView);
            deviceName = (TextView) itemView.findViewById(R.id.device_name);
            deviceId = (TextView) itemView.findViewById(R.id.device_id);
        }
    }
}
