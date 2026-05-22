package com.bac.homelink.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bac.homelink.R

object NotificationHelper {
    const val CHANNEL_ID = "homelink_channel"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "HomeLink Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    fun sendMatchingListingNotification(ctx: Context, title: String, location: String, price: Int) {
        try {
            NotificationManagerCompat.from(ctx).notify(
                System.currentTimeMillis().toInt(),
                NotificationCompat.Builder(ctx, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("New Match Found")
                    .setContentText("\"$title\" in $location - BWP $price/month")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()
            )
        } catch (_: SecurityException) {
        }
    }

    fun sendReservationConfirmationNotification(ctx: Context, ref: String, title: String) {
        try {
            NotificationManagerCompat.from(ctx).notify(
                System.currentTimeMillis().toInt(),
                NotificationCompat.Builder(ctx, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Reservation Confirmed")
                    .setContentText("Ref: $ref - $title")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()
            )
        } catch (_: SecurityException) {
        }
    }
}
