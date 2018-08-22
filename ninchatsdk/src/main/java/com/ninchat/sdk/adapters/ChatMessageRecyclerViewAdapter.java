package com.ninchat.sdk.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ninchat.sdk.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 22/08/2018.
 */
public class ChatMessageRecyclerViewAdapter extends RecyclerView.Adapter<ChatMessageRecyclerViewAdapter.ChatMessageViewHolder> {

    public class ChatMessageViewHolder extends RecyclerView.ViewHolder {

        public ChatMessageViewHolder(View itemView) {
            super(itemView);
        }

        void bind(final Object data) {
        }
    }

    private List<Object> data;

    public ChatMessageRecyclerViewAdapter() {
        this.data = new ArrayList<>();
    }

    public void add(final Object data) {
        this.data.add(data);
        notifyItemInserted(this.data.size());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        holder.bind(data.get(position));
    }
}
