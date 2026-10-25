package com.sancruz.vacationplanner;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ReminderWorker extends Worker {

    public static final String KEY_REMINDER_TYPE = "reminder_type";
    public static final String KEY_ITEM_NAME = "item_name";
    public static final String KEY_VACATION_ID = "vacation_id";

    public static final String TYPE_VACATION = "vacation";
    public static final String TYPE_EXCURSION = "excursion";

    private static final String CHANNEL_ID = "vacation_reminders";

    public ReminderWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParameters
    ) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {

        Context context = getApplicationContext();

        String reminderType = getInputData().getString(
                KEY_REMINDER_TYPE
        );

        String itemName = getInputData().getString(
                KEY_ITEM_NAME
        );

        long vacationId = getInputData().getLong(
                KEY_VACATION_ID,
                -1L
        );

        if (itemName == null || itemName.trim().isEmpty()
                || vacationId < 1
                || (!TYPE_VACATION.equals(reminderType)
                && !TYPE_EXCURSION.equals(reminderType))) {

            return Result.failure();
        }

        // Android 13+ requires notification permission.
        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {

            return Result.success();
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(
                        Context.NOTIFICATION_SERVICE
                );

        if (notificationManager == null) {
            return Result.retry();
        }

        // Create the notification channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Vacation Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            channel.setDescription(
                    "Reminders for vacations and excursions"
            );

            notificationManager.createNotificationChannel(
                    channel
            );
        }

        String title;
        String message;

        if (TYPE_EXCURSION.equals(reminderType)) {

            title = "Excursion Reminder";
            message = itemName + " is scheduled for today.";

        } else {

            title = "Vacation Reminder";
            message = itemName + " starts today.";
        }

        // Open the selected vacation when tapped.
        Intent intent = new Intent(
                context,
                VacationDetailActivity.class
        );

        intent.putExtra(
                VacationDetailActivity.EXTRA_VACATION_ID,
                vacationId
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification =
                new NotificationCompat.Builder(
                        context,
                        CHANNEL_ID
                )
                        .setSmallIcon(
                                android.R.drawable.ic_dialog_info
                        )
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(
                                NotificationCompat.PRIORITY_DEFAULT
                        )
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();

        notificationManager.notify(
                getId().hashCode(),
                notification
        );

        return Result.success();
    }
}
