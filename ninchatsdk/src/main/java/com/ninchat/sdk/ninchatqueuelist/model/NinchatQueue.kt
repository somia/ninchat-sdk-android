package com.ninchat.sdk.ninchatqueuelist.model

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 28/08/2018.
 */
class NinchatQueue(val id: String, val name: String?) {
    var position = Long.MAX_VALUE
    var isClosed = false
}