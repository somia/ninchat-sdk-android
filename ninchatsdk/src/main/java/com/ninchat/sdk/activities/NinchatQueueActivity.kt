package com.ninchat.sdk.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.helper.session.NinchatSessionManagerHelper.Companion.getQueueStatus
import com.ninchat.sdk.helper.session.NinchatSessionManagerHelper.Companion.mayBeJoinQueue
import com.ninchat.sdk.networkdispatchers.NinchatDeleteUser
import com.ninchat.sdk.utils.misc.Broadcast
import com.ninchat.sdk.utils.misc.Misc.Companion.toRichText
import com.ninchat.sdk.utils.misc.Parameter
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler
import kotlinx.coroutines.launch

// todo (pallab) write instrumentation test and replace base activity with base activity2
class NinchatQueueActivity : NinchatBaseActivity2() {
    private var queueId: String? = null
    override val layoutRes: Int
        get() = R.layout.activity_ninchat_queue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If the app is killed in the background sessionManager is not initialized the SDK must
        // be exited and the NinchatSession needs to be initialized again
        if (NinchatSessionManager.getInstance() == null) {
            setResult(RESULT_CANCELED, null)
            finish()
            overridePendingTransition(0, 0)
            return
        }
        // update the queue id
        intent?.getStringExtra(NinchatQueueActivity.QUEUE_ID)?.let {
            queueId = it
        }
        // show rorate animation
        findViewById<View>(R.id.ninchat_queue_activity_progress)?.run {
            animation = RotateAnimation(0f, 359f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
                interpolator = LinearInterpolator()
                repeatCount = Animation.INFINITE
                duration = 3000
            }
        }
        // update view
        updateQueueStatus(queueId);
        // subscribe broadcast receiver
        LocalBroadcastManager.getInstance(this).run {
            registerReceiver(channelJoinedBroadcastReceiver, IntentFilter(Broadcast.CHANNEL_JOINED))
            registerReceiver(channelUpdatedBroadcastReceiver, IntentFilter(Broadcast.QUEUE_UPDATED))
        }
        mayBeJoinQueue(queueId ?: "")
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).run {
            unregisterReceiver(channelJoinedBroadcastReceiver)
            unregisterReceiver(channelUpdatedBroadcastReceiver)
        }
    }

    private val channelJoinedBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Broadcast.CHANNEL_JOINED) {
                val currentIntent = Intent(applicationContext, NinchatChatActivity::class.java).run {
                    val chatClose = extras?.getBoolean(Parameter.CHAT_IS_CLOSED) ?: false
                    putExtra(Parameter.CHAT_IS_CLOSED, chatClose)
                }
                startActivityForResult(currentIntent, NinchatChatActivity.REQUEST_CODE)
            }
        }
    }

    private val channelUpdatedBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Broadcast.QUEUE_UPDATED) {
                updateQueueStatus(queueId)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == NinchatChatActivity.REQUEST_CODE) {
            // check data is null or not. Can through exception
            data?.getStringExtra(Parameter.QUEUE_ID).let { currentQueueId ->
                if (currentQueueId.isNullOrEmpty()) {
                    setResult(RESULT_OK, data)
                    finish()
                } else {
                    queueId = currentQueueId
                    updateQueueStatus(currentQueueId)
                }
            }
        }
    }

    private fun updateQueueStatus(queueId: String?) {
        findViewById<TextView>(R.id.ninchat_queue_activity_queue_status)?.let { status ->
            findViewById<TextView>(R.id.ninchat_queue_activity_queue_message)?.let { message ->
                findViewById<Button>(R.id.ninchat_queue_activity_close_button)?.let { close ->
                    NinchatSessionManager.getInstance()?.let { ninchatSessionManager ->
                        status.text = toRichText(getQueueStatus(queueId), status)
                        message.text = toRichText(ninchatSessionManager.ninchatState?.siteConfig?.getInQueueMessageText(), message)
                        close.text = toRichText(ninchatSessionManager.ninchatState?.siteConfig?.getChatCloseText(), message)
                        val hasChannel: Boolean = ninchatSessionManager.ninchatSessionHolder?.hasChannel()
                                ?: false
                        status.visibility = if (hasChannel) View.INVISIBLE else View.VISIBLE
                        message.visibility = if (hasChannel) View.INVISIBLE else View.VISIBLE
                        close.visibility = if (hasChannel) View.INVISIBLE else View.VISIBLE
                    }
                }
            }
        }
    }

    fun onClose(view: View) {
        NinchatScopeHandler.getIOScope().launch {
            NinchatDeleteUser.execute(
                    currentSession = NinchatSessionManager.getInstance().session
            )
        }
        setResult(RESULT_CANCELED, null)
        finish()
    }

    companion object {
        @JvmField
        val REQUEST_CODE = NinchatQueueActivity::class.java.hashCode() and 0xffff
        const val QUEUE_ID = "queueId"

        @JvmStatic
        fun getLaunchIntent(context: Context?, queueId: String?): Intent {
            return Intent(context, NinchatQueueActivity::class.java).putExtra(QUEUE_ID, queueId)
        }
    }
}