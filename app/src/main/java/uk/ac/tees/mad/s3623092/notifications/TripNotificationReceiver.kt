package uk.ac.tees.mad.s3623092.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import uk.ac.tees.mad.s3623092.R

class TripNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        createNotificationChannel(context)

        val tripId =
            intent.getLongExtra(
                TripNotificationScheduler.EXTRA_TRIP_ID,
                0L
            )

        val title =
            intent.getStringExtra(
                TripNotificationScheduler.EXTRA_NOTIFICATION_TITLE
            ) ?: "TravelPack reminder"

        val message =
            intent.getStringExtra(
                TripNotificationScheduler.EXTRA_NOTIFICATION_MESSAGE
            ) ?: "You have an upcoming trip."

        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
                )
                .setPriority(
                    NotificationCompat.PRIORITY_DEFAULT
                )
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat
            .from(context)
            .notify(
                tripId.hashCode(),
                notification
            )
    }

    private fun createNotificationChannel(
        context: Context
    ) {
        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Trip reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description =
                    "Notifications for upcoming trips."
            }

            val manager =
                context.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID =
            "trip_reminders"
    }
}