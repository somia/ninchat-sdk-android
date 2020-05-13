package com.ninchat.sdk.adapters.holders;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.models.NinchatMessage;
import com.ninchat.sdk.models.NinchatOption;

import org.json.JSONException;

import java.util.List;

public class NinchatMultiChoiceViewholder extends RecyclerView.ViewHolder {
    public NinchatMultiChoiceViewholder(@NonNull View itemView) {
        super(itemView);
    }

    public void bind(final NinchatMessage message, final int position, final boolean sendAction, final Callback callback) {
        final TextView button = this.getButtonItem();
        final List<NinchatOption> options = getNinchatOptions(message);
        final NinchatOption option = getNinchatOption(options, position);
        button.setText(option.getLabel());
        button.setOnClickListener(v -> {
            if (sendAction) {
                try {
                    option.toggle();
                    sendUIAction(option);
                } catch (final Exception e) {
                    // Ignore
                } finally {
                    option.toggle();
                }
            } else {
                callback.onClickListener(message, position);
            }
        });
    }

    @VisibleForTesting
    protected void sendUIAction(final NinchatOption ninchatOption) throws JSONException {
        NinchatSessionManager.getInstance().sendUIAction(ninchatOption.toJSON());
    }

    @VisibleForTesting
    protected TextView getButtonItem() {
        return (TextView) itemView;
    }

    @VisibleForTesting
    protected List<NinchatOption> getNinchatOptions(final NinchatMessage message) {
        return message.getOptions();
    }

    @VisibleForTesting
    protected NinchatOption getNinchatOption(final List<NinchatOption> options, final int at) {
        return options.get(at);
    }

    public interface Callback {
        void onClickListener(final NinchatMessage message, final int position);
    }
}
