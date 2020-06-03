package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ninchat.sdk.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class NinchatMultichoiceViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatMultichoiceViewHolder.class.getSimpleName();

    private final TextView mLabel;
    private final RecyclerView mRecycleView;

    public NinchatMultichoiceViewHolder(@NonNull View itemView, final JSONObject item) {
        super(itemView);
        mLabel = (TextView) itemView.findViewById(R.id.multichoice_label);
        mRecycleView = (RecyclerView) itemView.findViewById(R.id.ninchat_chat_form_multichoice_options);
        this.bind(item);
    }

    public void bind(JSONObject item) {
        mLabel.setText(item.optString("label", ""));
        final JSONArray options = item.optJSONArray("options");
        if (options == null) {
            return;
        }
        mRecycleView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        mRecycleView.setAdapter(new NinchatMultichoiceAdapter(options));
    }

    public class NinchatMultichoiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private JSONArray options;
        public NinchatMultichoiceAdapter(final JSONArray options) {
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
            return new NinchatMultichoiceTextViewHolder(
                    LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.item_chat_multichoice_selected, viewGroup, false),
                    currentItem);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            final JSONObject currentItem = this.options.optJSONObject(position);
            if( viewHolder instanceof NinchatMultichoiceTextViewHolder) {
                ((NinchatMultichoiceTextViewHolder)viewHolder).bind(currentItem);
            }
        }

        @Override
        public int getItemCount() {
            return options.length();
        }

        public class NinchatMultichoiceTextViewHolder extends RecyclerView.ViewHolder {
            private final TextView mOptionLabel;

            public NinchatMultichoiceTextViewHolder(@NonNull View itemView, final JSONObject items) {
                super(itemView);
                mOptionLabel = (TextView) itemView.findViewById(R.id.text_view_options_label);
                this.bind(items);
            }

            public void bind(final JSONObject items) {
                mOptionLabel.setText(items.optString("label"));
            }
        }
    }
}
