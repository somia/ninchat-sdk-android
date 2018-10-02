package com.ninchat.sdk.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ninchat.sdk.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 22/08/2018.
 */
public final class NinchatMessageAdapter extends RecyclerView.Adapter<NinchatMessageAdapter.NinchatMessageViewHolder> {

    public class NinchatMessageViewHolder extends RecyclerView.ViewHolder {

        public NinchatMessageViewHolder(View itemView) {
            super(itemView);
        }

        void bind(final Pair<String, Boolean> data) {
            itemView.findViewById(R.id.start_margin).setVisibility(data.second ? View.VISIBLE : View.GONE);
            itemView.findViewById(R.id.end_margin).setVisibility(data.second ? View.GONE : View.VISIBLE);
            if (data.second) {
                itemView.findViewById(R.id.message_container).setBackgroundResource(R.color.ninchat_activity_bottom_matter_background);
            }
            final TextView message = itemView.findViewById(R.id.message_content);
            message.setText(data.first);
        }
    }

    private List<Pair<String, Boolean>> data;

    public NinchatMessageAdapter() {
        this.data = new ArrayList<>();
    }

    public void add(final String data, final boolean isRemoteMessage) {
        this.data.add(new Pair<>(data, isRemoteMessage));
        notifyItemInserted(this.data.size()-1);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NonNull
    @Override
    public NinchatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NinchatMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NinchatMessageViewHolder holder, int position) {
        holder.bind(data.get(position));
    }
}
