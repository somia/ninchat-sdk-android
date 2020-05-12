package com.ninchat.sdk.adapters.holders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.models.NinchatMessage;
import com.ninchat.sdk.models.NinchatOption;

import java.util.List;

public class NinchatMultiChoiceViewholder extends RecyclerView.ViewHolder {
    protected final Callback callback;

    public NinchatMultiChoiceViewholder(@NonNull View itemView, final Callback callback) {
        super(itemView);
        this.callback = callback;
    }

    public void bind(final NinchatMessage message, final int position, final boolean sendAction) {
        final TextView button = (TextView) itemView;
        final List<NinchatOption> options = message.getOptions();
        final NinchatOption option = options.get(position);
        button.setText(option.getLabel());
        button.setOnClickListener(v -> {
            this.callback.onClickListener(message, position);
            if (sendAction) {
                try {
                    option.toggle();
                    NinchatSessionManager.getInstance().sendUIAction(option.toJSON());
                    option.toggle();
                } catch (final Exception e) {
                    // Ignore
                }
            } else {
                this.callback.onClickListener(message, position);
            }
        });
    }

    public interface Callback {
        void onClickListener(final NinchatMessage message, final int position);
    }
}
