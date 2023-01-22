package dev.ojhdt.connection

import com.wedevol.xmpp.bean.CcsInMessage
import com.wedevol.xmpp.bean.CcsOutMessage
import com.wedevol.xmpp.server.CcsClient
import com.wedevol.xmpp.util.MessageMapper
import com.wedevol.xmpp.util.Util
import dev.ojhdt.data.model.UpstreamMessage
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPException
import org.slf4j.LoggerFactory
import java.io.IOException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.util.*

const val SENDER_ID = ""
const val SERVER_KEY =
    ""


class XmppController(val onUpstreamMessage: (msg: UpstreamMessage) -> Unit) : CcsClient(SENDER_ID, SERVER_KEY, true) {
    private val logger = LoggerFactory.getLogger(XmppController::class.java)
    private val queue = LinkedList<Pair<String, String>>()
    private var sending = false

    init {
        try {
            connect()
        } catch (e: XMPPException) {
            logger.error("Error trying to connect. Error: {}", e.message)
        } catch (e: InterruptedException) {
            logger.error("Error trying to connect. Error: {}", e.message)
        } catch (e: KeyManagementException) {
            logger.error("Error trying to connect. Error: {}", e.message)
        } catch (e: NoSuchAlgorithmException) {
            logger.error("Error trying to connect. Error: {}", e.message)
        } catch (e: SmackException) {
            logger.error("Error trying to connect. Error: {}", e.message)
        } catch (e: IOException) {
            logger.error("Error trying to connect. Error: {}", e.message)
        }
    }

    private fun takeAndSendMessage() {
        if (queue.isNotEmpty()) {
            val msg = queue.poll()
            sending = true
            sendDownstreamMessage(msg.first, msg.second)
        }
    }

    fun sendMessage(registrationToken: String, wsSessionId: String, type: String, json: String) {
        val messageId = Util.getUniqueMessageId();
        val dataPayload = mapOf<String, String>(
            "ws_session_id" to wsSessionId,
            "type" to type,
            "dto" to json
        )
        val message = CcsOutMessage(registrationToken, messageId, dataPayload)
        val jsonRequest = MessageMapper.toJsonString(message)
        logger.info("queue size: ${queue.size}")
        if (!sending) {
            sendDownstreamMessage(messageId, jsonRequest)
            sending = true
        } else {
            queue.add(messageId to jsonRequest)
        }
//        try {
//            val latch = CountDownLatch(1)
//            latch.await()
//        } catch (e: InterruptedException) {
//            logger.error("An error occurred while latch was waiting. Error: {}", e.message)
//        }
    }

    override fun handleUpstreamMessage(inMessage: CcsInMessage?) {
        // 1. send ACK to FCM
        val ackJsonRequest = MessageMapper.createJsonAck(inMessage!!.from, inMessage!!.messageId)
        sendAck(ackJsonRequest)

        // 2. process and send message
        logger.info("Received message from FCM: {}", inMessage!!.dataPayload)
        logger.info("session_id: {}", inMessage!!.dataPayload["session_id"])
        logger.info("message: {}", inMessage!!.dataPayload["message"])
        onUpstreamMessage(
            UpstreamMessage(
                session_id = inMessage!!.dataPayload.get("session_id")!!,
                message = inMessage!!.dataPayload.get("message")!!,
            )
        )
    }

    override fun removeMessageFromSyncMessages(messageId: String?) {
        sending = false
        takeAndSendMessage()
        super.removeMessageFromSyncMessages(messageId)
    }
}