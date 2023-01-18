package dev.ojhdt.connection

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp

import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message


class FcmController {
    init {
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.getApplicationDefault())
            .build()

        FirebaseApp.initializeApp(options)
    }

    fun sendMessage(registrationToken: String, type: String, json: String){
        val message = Message.builder()
            .putData("type", type)
            .putData("dto", json)
            .setToken(registrationToken)
            .build()
        val response = FirebaseMessaging.getInstance().send(message)
        println("Successfully sent message: $response")
    }
}