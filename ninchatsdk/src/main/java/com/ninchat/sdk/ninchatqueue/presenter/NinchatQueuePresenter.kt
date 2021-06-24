package com.ninchat.sdk.ninchatqueue.presenter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.activities.NinchatChatActivity
import com.ninchat.sdk.helper.session.NinchatSessionManagerHelper
import com.ninchat.sdk.networkdispatchers.NinchatDeleteUser
import com.ninchat.sdk.networkdispatchers.NinchatDescribeQueue
import com.ninchat.sdk.ninchatqueue.model.NinchatQueueModel
import com.ninchat.sdk.ninchatqueue.view.NinchatQueueActivity
import com.ninchat.sdk.ninchattitlebar.view.NinchatTitlebarView
import com.ninchat.sdk.utils.misc.Broadcast
import com.ninchat.sdk.utils.misc.Misc
import com.ninchat.sdk.utils.misc.Parameter
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler
import kotlinx.android.synthetic.main.activity_ninchat_queue.view.*
import kotlinx.coroutines.launch

interface INinchatQueuePresenter {
    fun onChannelJoined(isClosed: Boolean)
    fun onQueueUpdate()
}

class NinchatQueuePresenter(
    val ninchatQueueModel: NinchatQueueModel,
    val callback: INinchatQueuePresenter?,
    val mContext: Context,
) {

    fun hasSession(): Boolean {
        return NinchatSessionManager.getInstance() != null
    }

    fun updateQueueView(view: View) {
        // update queue text
        view.ninchat_queue_activity_queue_status.text = Misc.toRichText(
            ninchatQueueModel.getQueueStatus(),
            view.ninchat_queue_activity_queue_status
        )
        view.ninchat_queue_activity_queue_message.text = Misc.toRichText(
            ninchatQueueModel.getInQueueMessageText(),
            view.ninchat_queue_activity_queue_message
        )
        view.ninchat_queue_activity_close_button.text = Misc.toRichText(
            ninchatQueueModel.getChatCloseText(),
            view.ninchat_queue_activity_close_button
        )

        // update queue visibility
        view.ninchat_queue_activity_queue_status.visibility =
            if (ninchatQueueModel.hasChannel()) View.INVISIBLE else View.VISIBLE
        view.ninchat_queue_activity_queue_message.visibility =
            if (ninchatQueueModel.hasChannel()) View.INVISIBLE else View.VISIBLE
        view.ninchat_queue_activity_close_button.visibility =
            if (ninchatQueueModel.hasChannel()) View.INVISIBLE else View.VISIBLE
    }

    fun updateQueueId(intent: Intent?) {
        // update queue id
        val queueId = intent?.getStringExtra(NinchatQueueModel.QUEUE_ID)
        if (ninchatQueueModel.queueId == queueId) return
        ninchatQueueModel.queueId = queueId
        if (!ninchatQueueModel.isAlreadyInQueueList()) {
            // if not already in queue then try to describe the queue
            NinchatScopeHandler.getIOScope().launch {
                NinchatDescribeQueue.execute(
                    currentSession = NinchatSessionManager.getInstance()?.session,
                    queueId = queueId
                )
            }
        }
    }

    fun showQueueAnimation(view: ImageView?) {
        // show rotate animation
        view?.run {
            animation = RotateAnimation(
                0f,
                359f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            ).apply {
                interpolator = LinearInterpolator()
                repeatCount = Animation.INFINITE
                duration = 3000
            }
        }
    }

    fun mayBeJoinQueue() {
        // if there is no queue id. then don't try to join the queue
        if (ninchatQueueModel.queueId.isNullOrEmpty()) return
        NinchatSessionManagerHelper.mayBeJoinQueue(ninchatQueueModel.queueId ?: "")
    }

    fun subscribeBroadcaster() {
        // subscribe broadcast receiver
        LocalBroadcastManager.getInstance(mContext).run {
            registerReceiver(channelJoinedBroadcastReceiver, IntentFilter(Broadcast.CHANNEL_JOINED))
            registerReceiver(channelUpdatedBroadcastReceiver, IntentFilter(Broadcast.QUEUE_UPDATED))
        }
    }

    fun unSubscribeBroadcaster() {
        // unsubscribe broadcast receiver
        LocalBroadcastManager.getInstance(mContext).run {
            unregisterReceiver(channelJoinedBroadcastReceiver)
            unregisterReceiver(channelUpdatedBroadcastReceiver)
        }
    }


    fun mayBeDeleteUser() {
        NinchatSessionManager.getInstance()?.let { ninchatSessionManager ->
            NinchatScopeHandler.getIOScope().launch {
                NinchatDeleteUser.execute(
                    currentSession = ninchatSessionManager.session
                )
            }
        }
    }

    private val channelJoinedBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent?.action == Broadcast.CHANNEL_JOINED) {
                val chatClose = intent.extras?.getBoolean(Parameter.CHAT_IS_CLOSED) ?: false
                callback?.onChannelJoined(chatClose)
            }
        }
    }

    private val channelUpdatedBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Broadcast.QUEUE_UPDATED) {
                callback?.onQueueUpdate()
            }
        }
    }

    fun mayBeAttachTitlebar(view: View, callback: () -> Unit) {
        NinchatTitlebarView.showTitlebarForInQueueView(view, callback = callback)
    }

    companion object {
        fun getLaunchIntentForChatActivity(context: Context?, isClosed: Boolean): Intent {
            return Intent(context, NinchatChatActivity::class.java).run {
                putExtra(Parameter.CHAT_IS_CLOSED, isClosed)
            }
        }

        @JvmStatic
        fun getLaunchIntentWithQueueId(context: Context?, queueId: String?): Intent {
            return Intent(context, NinchatQueueActivity::class.java).run {
                putExtra(NinchatQueueModel.QUEUE_ID, queueId)
            }
        }
    }
}

// no questionnaire name -> always show placeholder ( same for old questionnaire -> need fix)