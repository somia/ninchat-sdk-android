package com.ninchat.sdk.tasks;

import android.os.AsyncTask;

import com.ninchat.sdk.NinchatSessionManager;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 27/08/2018.
 */
abstract class NinchatBaseTask extends AsyncTask<Void, Void, Exception> {

    @Override
    protected void onPostExecute(final Exception error) {
        if (error != null) {
            NinchatSessionManager.getInstance().sessionError(error);
        }
    }
}
