package uk.ac.tees.mad.s3623092.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TripNotificationScheduler {

    private const val ACTION_ONE_DAY_BEFORE =
        "uk.ac.tees.mad.s3623092.TRIP_ONE_DAY_BEFORE"

    private const val ACTION_TRIP_DAY =
        "uk.ac.tees.mad.s3623092.TRIP_DAY"

    const val EXTRA_TRIP_ID =
        "extra_trip_id"

    const val EXTRA_TRIP_NAME =
        "extra_trip_name"

    const val EXTRA_NOTIFICATION_TITLE =
        "extra_notification_title"

    const val EXTRA_NOTIFICATION_MESSAGE =
        "extra_notification_message"

    private const val DATE_PATTERN = "yyyy-MM-dd"

    fun scheduleTripNotifications(
        context: Context,
        tripId: Long,
        tripName: String,
        startDate: String
    ) {
        val tripDate = parseDate(startDate)
            ?: return

        val oneDayBefore = Calendar.getInstance().apply {
            timeInMillis = tripDate
            add(Calendar.DAY_OF_MONTH, -1)
            setNotificationTime()
        }

        val tripDay = Calendar.getInstance().apply {
            timeInMillis = tripDate
            setNotificationTime()
        }

        scheduleNotification(
            context = context,
            triggerTime = oneDayBefore.timeInMillis,
            requestCode = createRequestCode(
                tripId,
                1
            ),
            action = ACTION_ONE_DAY_BEFORE,
            tripId = tripId,
            tripName = tripName,
            title = "Trip tomorrow",
            message = "$tripName starts tomorrow."
        )

        scheduleNotification(
            context = context,
            triggerTime = tripDay.timeInMillis,
            requestCode = createRequestCode(
                tripId,
                2
            ),
            action = ACTION_TRIP_DAY,
            tripId = tripId,
            tripName = tripName,
            title = "Trip starts today",
            message = "$tripName starts today."
        )
    }

    fun cancelTripNotifications(
        context: Context,
        tripId: Long
    ) {
        cancelNotification(
            context = context,
            requestCode = createRequestCode(
                tripId,
                1
            ),
            action = ACTION_ONE_DAY_BEFORE
        )

        cancelNotification(
            context = context,
            requestCode = createRequestCode(
                tripId,
                2
            ),
            action = ACTION_TRIP_DAY
        )
    }

    private fun scheduleNotification(
        context: Context,
        triggerTime: Long,
        requestCode: Int,
        action: String,
        tripId: Long,
        tripName: String,
        title: String,
        message: String
    ) {
        if (triggerTime <= System.currentTimeMillis()) {
            return
        }

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val pendingIntent =
            createPendingIntent(
                context = context,
                requestCode = requestCode,
                action = action,
                tripId = tripId,
                tripName = tripName,
                title = title,
                message = message
            )

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    private fun cancelNotification(
        context: Context,
        requestCode: Int,
        action: String
    ) {
        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val intent = Intent(
            context,
            TripNotificationReceiver::class.java
        ).apply {
            this.action = action
        }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        alarmManager.cancel(pendingIntent)
    }

    private fun createPendingIntent(
        context: Context,
        requestCode: Int,
        action: String,
        tripId: Long,
        tripName: String,
        title: String,
        message: String
    ): PendingIntent {
        val intent = Intent(
            context,
            TripNotificationReceiver::class.java
        ).apply {
            this.action = action
            putExtra(
                EXTRA_TRIP_ID,
                tripId
            )
            putExtra(
                EXTRA_TRIP_NAME,
                tripName
            )
            putExtra(
                EXTRA_NOTIFICATION_TITLE,
                title
            )
            putExtra(
                EXTRA_NOTIFICATION_MESSAGE,
                message
            )
        }

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun parseDate(
        date: String
    ): Long? {
        return try {
            val formatter =
                SimpleDateFormat(
                    DATE_PATTERN,
                    Locale.UK
                ).apply {
                    isLenient = false
                }

            formatter.parse(date)?.time
        } catch (_: Exception) {
            null
        }
    }

    private fun Calendar.setNotificationTime() {
        set(
            Calendar.HOUR_OF_DAY,
            9
        )
        set(
            Calendar.MINUTE,
            0
        )
        set(
            Calendar.SECOND,
            0
        )
        set(
            Calendar.MILLISECOND,
            0
        )
    }

    private fun createRequestCode(
        tripId: Long,
        notificationType: Int
    ): Int {
        return (
                tripId.hashCode() * 31 +
                        notificationType
                )
    }
}