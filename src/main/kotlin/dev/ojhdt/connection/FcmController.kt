package dev.ojhdt.connection

import com.wedevol.xmpp.EntryPoint
import com.wedevol.xmpp.bean.CcsInMessage
import com.wedevol.xmpp.bean.CcsOutMessage
import com.wedevol.xmpp.server.CcsClient
import com.wedevol.xmpp.util.MessageMapper
import com.wedevol.xmpp.util.Util
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPException
import org.slf4j.LoggerFactory
import java.io.IOException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.concurrent.CountDownLatch

const val SENDER_ID = ""
const val SERVER_KEY = ""


class FcmController: CcsClient(SENDER_ID, SERVER_KEY, true) {
    private val logger = LoggerFactory.getLogger(FcmController::class.java)
    init{
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

    fun sendMessage(registrationToken: String, type: String, json: String){
        val messageId = Util.getUniqueMessageId();
        val dataPayload = mapOf<String, String>(
            Util.PAYLOAD_ATTRIBUTE_MESSAGE to json
        )
        val message = CcsOutMessage(registrationToken, messageId, dataPayload)
        val jsonRequest = MessageMapper.toJsonString(message)
        sendDownstreamMessage(messageId, jsonRequest)
        try {
            val latch = CountDownLatch(1)
            latch.await()
        } catch (e: InterruptedException) {
            logger.error("An error occurred while latch was waiting. Error: {}", e.message)
        }
    }

    override fun handleUpstreamMessage(inMessage: CcsInMessage?) {
        // The custom 'action' payload attribute defines what the message action is about.

        // The custom 'action' payload attribute defines what the message action is about.
        val actionObj = Optional.ofNullable(inMessage!!.dataPayload[Util.PAYLOAD_ATTRIBUTE_ACTION])
        check(actionObj.isPresent) { "Action must not be null! Options: 'ECHO', 'MESSAGE'" }
        val action = actionObj.get()

        // 1. send ACK to FCM

        // 1. send ACK to FCM
        val ackJsonRequest = MessageMapper.createJsonAck(inMessage!!.from, inMessage!!.messageId)
        sendAck(ackJsonRequest)

        // 2. process and send message
        logger.info("Received message from FCM: {}", inMessage!!.dataPayload)
    }
}