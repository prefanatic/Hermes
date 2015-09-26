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

package edu.uri.egr.hermessample;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import edu.uri.egr.hermessample.adapter.Sample;

/**
 * Created by cody on 9/25/15.
 */
public class SampleAdapter extends RecyclerView.Adapter<SampleAdapter.ViewHolder> implements View.OnClickListener {
    private final List<Sample> samples = new ArrayList<>();
    private RecyclerView mRecycler;

    public void addSample(Sample sample) {
        samples.add(sample);
        notifyItemInserted(samples.size());
    }

    @Override
    public void onClick(View v) {
        if (mRecycler == null)
            return;

        int pos = mRecycler.getChildAdapterPosition(v);
        Sample sample = samples.get(pos);

        Intent intent = new Intent(v.getContext(), sample.activity);
        v.getContext().startActivity(intent);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mRecycler = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mRecycler = null;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_sample, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Sample sample = samples.get(position);

        holder.title.setText(sample.name);
        holder.description.setText(sample.description);
    }

    @Override
    public int getItemCount() {
        return samples.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.sample_title) TextView title;
        @Bind(R.id.sample_description) TextView description;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(SampleAdapter.this);
        }
    }
}
