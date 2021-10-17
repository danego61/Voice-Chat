package com.danego.voicechat.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.core.app.NotificationCompat
import com.danego.voicechat.MainActivity
import com.danego.voicechat.R
import com.danego.voicechat.model.MessageModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

@ExperimentalAnimationApi
class NotificationService : Service() {

    private lateinit var userEmail: String
    private var listener: ListenerRegistration? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val userEmail = intent?.getStringExtra("userEmail")

        if (userEmail == null) {

            stopSelf()

        } else {

            this.userEmail = userEmail
            listenChanges()
        }

        return START_STICKY
    }

    private fun listenChanges() {

        listener?.remove()
        listener = Firebase.firestore
            .collection(userEmail)
            .addSnapshotListener { value, _ ->

                value?.documents?.let { changes ->

                    for (doc in changes) {

                        processDocument(doc)

                    }

                }

            }

    }

    private fun processDocument(document: DocumentSnapshot) {

        var isFound = false

        document.data?.let { data ->

            for (message in data) {

                val gson = Gson()
                val model: MessageModel = gson.fromJson(
                    message.value.toString(),
                    MessageModel::class.java
                )

                if (model.isNotified == false) {

                    isFound = true
                    Firebase.firestore.collection(userEmail).document(document.id)
                        .update(message.key, gson.toJson(model.copy(isNotified = null)))

                }

            }

        }

        if (isFound)
            showNotification(document.id)

    }

    private fun showNotification(email: String) {

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("email", email)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val builder = NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("New Message")
            .setContentText("New Message from $email")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "Voice_Chat_Message_Notification"
            val channel = NotificationChannel(
                channelId,
                "Message Notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
            builder.setChannelId(channelId)
        }

        notificationManager.notify(emailToId(email), builder.build())

    }

    private fun emailToId(email: String): Int {
        var id = 1

        for (mail in email)
            id = (id + mail.code) / id

        return id
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        listener?.remove()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent =
            Intent(applicationContext, NotificationService::class.java).also {
                it.putExtra("userEmail", userEmail)
            }
        startService(restartServiceIntent)
        super.onTaskRemoved(rootIntent)
    }

}