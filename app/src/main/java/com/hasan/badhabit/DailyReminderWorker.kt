package com.hasan.badhabit

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        try {
            showNotification()
            scheduleNextReminder()
            return Result.success()
        } catch (e: Exception) {
            Log.e("DailyReminderWorker", "Error sending notification", e)
            return Result.failure()
        }
    }

    private fun scheduleNextReminder() {
        val prefs = applicationContext.getSharedPreferences("bad_habit_prefs", Context.MODE_PRIVATE)
        val hour = prefs.getInt("notif_hour", 21)
        val minute = prefs.getInt("notif_minute", 0)

        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        dueDate.set(Calendar.HOUR_OF_DAY, hour)
        dueDate.set(Calendar.MINUTE, minute)
        dueDate.set(Calendar.SECOND, 0)

        if (dueDate.before(currentDate) || dueDate.timeInMillis == currentDate.timeInMillis) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

        val nextWorkRequest = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "DailyReminder",
            ExistingWorkPolicy.REPLACE,
            nextWorkRequest
        )
    }

    private fun showNotification() {
        val channelId = "daily_reminder_channel"
        val notificationId = 1
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Günlük Hatırlatıcı",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alışkanlık takibi için günlük hatırlatma"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // YENİ: Kayıtlı mesajı al
        val prefs = applicationContext.getSharedPreferences("bad_habit_prefs", Context.MODE_PRIVATE)
        val message = prefs.getString("notif_message", NotificationMessages.DEFAULT_MESSAGE)

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("Bugün nasıl geçti?")
            .setContentText(message) // Dinamik mesaj
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(0xFF6200EE.toInt())

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            Log.e("DailyReminderWorker", "Notification permission missing")
        }
    }
}
