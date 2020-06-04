package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ninchat.sdk.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class NinchatCheckboxViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatCheckboxViewHolder.class.getSimpleName();

    private final TextView mLabel;
    private final RecyclerView mRecycleView;

    public NinchatCheckboxViewHolder(@NonNull View itemView, final JSONObject item) {
        super(itemView);
        mLabel = (TextView) itemView.findViewById(R.id.checkbox_group_label);
        mRecycleView = (RecyclerView) itemView.findViewById(R.id.ninchat_chat_form_checkbox_rview);
        this.bind(item);
    }


    public void bind(JSONObject item) {
        mLabel.setText(item.optString("label", ""));
        final JSONArray options = item.optJSONArray("options");
        if (options == null) {
            return;
        }
        mRecycleView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        mRecycleView.setAdapter(new NinchatCheckboxAdapter(options));
    }

    public class NinchatCheckboxAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private JSONArray options;

        public NinchatCheckboxAdapter(final JSONArray options) {
            this.options = options;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
            final JSONObject currentItem = this.options.optJSONObject(position);
            return new NinchatSingleCheckboxViewHolder(
                    LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.checkbox, viewGroup, false),
                    currentItem);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            final JSONObject currentItem = this.options.optJSONObject(position);
            if (viewHolder instanceof NinchatSingleCheckboxViewHolder) {
                ((NinchatSingleCheckboxViewHolder) viewHolder).bind(currentItem);
            }
        }

        @Override
        public int getItemCount() {
            return options.length();
        }

        public class NinchatSingleCheckboxViewHolder extends RecyclerView.ViewHolder {
            private final TextView mOptionLabel;

            public NinchatSingleCheckboxViewHolder(@NonNull View itemView, final JSONObject items) {
                super(itemView);
                mOptionLabel = (TextView) itemView.findViewById(R.id.checkbox_single);
                this.bind(items);
            }

            public void bind(final JSONObject items) {
                mOptionLabel.setText(items.optString("label"));
            }
        }
    }
}
