package net.dankito.deepthought.android.androidservice

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.RemoteInput
import net.dankito.deepthought.android.MainActivity
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.service.speech.SpeechToTextConverter
import net.dankito.deepthought.data.EntryPersister
import net.dankito.deepthought.model.Item


class PermanentNotificationService(private val context: Context, private val itemPersister: EntryPersister) {

    companion object {
        const val PermanentNotificationNotificationId = 27388
        const val PermanentNotificationRequestCode = 27388
        const val PermanentNotificationTextInputKey = "deepthought_text_input_key"

        const val TextInputAction = "TextInputAction"
        const val SpeechToTextAction = "Speech-to-TextAction"
    }


    fun showPermanentNotification() {
        showNotification(R.string.permanent_notification_content)
    }

    private fun showNotification(contentResourceId: Int) {
        val builder = NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.app_icon_for_status_bar)
    //                .setContentTitle(context.getString(R.string.permanent_notification_title))
                .setContentText(context.getString(contentResourceId))
                .setOngoing(true)
        //                        .setChannelId(CHANNEL_ID)

        val startMainActivityServiceIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, PermanentNotificationRequestCode, startMainActivityServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(pendingIntent)

        builder.addAction(createCreateItemAction())

        builder.addAction(createCreateItemFromSpeechToTextAction())

        val notification = builder.build()
        notification.flags = Notification.FLAG_NO_CLEAR

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(PermanentNotificationNotificationId, notification)
    }

    private fun createCreateItemAction(): NotificationCompat.Action? {
        val startAndroidServiceIntent = Intent(context, PermanentNotificationAndroidService::class.java)
        startAndroidServiceIntent.action = TextInputAction

        val pendingIntent = PendingIntent.getService(context, PermanentNotificationRequestCode, startAndroidServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val replyLabel = context.getString(R.string.permanent_notification_create_item_label)
        val remoteInput = RemoteInput.Builder(PermanentNotificationTextInputKey).setLabel(replyLabel).build()

        val action = NotificationCompat.Action.Builder(android.R.drawable.ic_menu_add,
                context.getString(R.string.permanent_notification_create_item_action), pendingIntent)
                .addRemoteInput(remoteInput)
                .build()
        return action
    }

    private fun createCreateItemFromSpeechToTextAction(): NotificationCompat.Action? {
        val startAndroidServiceIntent = Intent(context, PermanentNotificationAndroidService::class.java)
        startAndroidServiceIntent.action = SpeechToTextAction

        val pendingIntent = PendingIntent.getService(context, PermanentNotificationRequestCode, startAndroidServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Action.Builder(android.R.drawable.ic_btn_speak_now,
                context.getString(R.string.permanent_notification_create_item_from_speech_to_text_action), pendingIntent)
                .build()
    }


    fun handlesIntent(intent: Intent): Boolean {
        if(intent.action == TextInputAction) {
            return handleNotificationTextInput(intent)
        }
        else if(intent.action == SpeechToTextAction) {
            return startSpeechToText()
        }

        return false
    }


    private fun handleNotificationTextInput(intent: Intent): Boolean {
        RemoteInput.getResultsFromIntent(intent)?.let { remoteInput ->
            val input = remoteInput.getCharSequence(PermanentNotificationTextInputKey)

            itemPersister.saveEntryAsync(Item(input.toString())) { successful ->
                showResultToUser(successful)
            }

            return true
        }

        return true
    }

    private fun showResultToUser(successful: Boolean) {
        if(successful) {
            showNotification(R.string.permanent_notification_reply_successfully_saved_item)
        }
        else {
            showNotification(R.string.permanent_notification_reply_could_not_save_item)
        }
    }


    private fun startSpeechToText(): Boolean {
        val speechToTextConverter = SpeechToTextConverter(context)

        speechToTextConverter.startSpeechToTextConversion { recognizedData ->
            handleSpeechRecognitionResult(recognizedData)
        }

        return true
    }

    private fun handleSpeechRecognitionResult(recognizedData: List<String>) {
        val content = "Speech recognition result:\n" + recognizedData.joinToString("\n") { it } // TODO: create real content

        itemPersister.saveEntryAsync(Item(content)) {

        }
    }

}