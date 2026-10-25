package com.sancruz.vacationplanner;

import android.content.Context;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.sancruz.vacationplanner.data.Excursion;
import com.sancruz.vacationplanner.data.Vacation;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public class ReminderScheduler {

    private static final String VACATION_PREFIX =
            "vacation_reminder_";

    private static final String EXCURSION_PREFIX =
            "excursion_reminder_";

    private static final String VACATION_TAG_PREFIX =
            "vacation_group_";

    private ReminderScheduler() {
        // Utility class.
    }

    public static void scheduleVacation(
            Context context,
            Vacation vacation
    ) {

        scheduleReminder(
                context,
                VACATION_PREFIX + vacation.getId(),
                vacationTag(vacation.getId()),
                ReminderWorker.TYPE_VACATION,
                vacation.getName(),
                vacation.getId(),
                vacation.getStartDate()
        );
    }

    public static void scheduleExcursion(
            Context context,
            Excursion excursion
    ) {

        scheduleReminder(
                context,
                EXCURSION_PREFIX + excursion.getId(),
                vacationTag(excursion.getVacationId()),
                ReminderWorker.TYPE_EXCURSION,
                excursion.getName(),
                excursion.getVacationId(),
                excursion.getExcursionDate()
        );
    }

    public static void cancelVacation(
            Context context,
            long vacationId
    ) {

        // Cancels the vacation reminder and all
        // excursion reminders belonging to it.
        WorkManager.getInstance(
                context.getApplicationContext()
        ).cancelAllWorkByTag(
                vacationTag(vacationId)
        );
    }

    public static void cancelExcursion(
            Context context,
            long excursionId
    ) {

        WorkManager.getInstance(
                context.getApplicationContext()
        ).cancelUniqueWork(
                EXCURSION_PREFIX + excursionId
        );
    }

    private static String vacationTag(long vacationId) {
        return VACATION_TAG_PREFIX + vacationId;
    }

    private static void scheduleReminder(
            Context context,
            String uniqueWorkName,
            String vacationTag,
            String reminderType,
            String itemName,
            long vacationId,
            String dateString
    ) {

        WorkManager workManager =
                WorkManager.getInstance(
                        context.getApplicationContext()
                );

        LocalDate reminderDate =
                LocalDate.parse(dateString);

        Instant reminderTime = reminderDate
                .atTime(9, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant();

        Instant now = Instant.now();

        // Do not create overdue reminders.
        if (!reminderTime.isAfter(now)) {

            workManager.cancelUniqueWork(
                    uniqueWorkName
            );

            return;
        }

        long delayMillis = Duration.between(
                now,
                reminderTime
        ).toMillis();

        Data reminderData = new Data.Builder()
                .putString(
                        ReminderWorker.KEY_REMINDER_TYPE,
                        reminderType
                )
                .putString(
                        ReminderWorker.KEY_ITEM_NAME,
                        itemName
                )
                .putLong(
                        ReminderWorker.KEY_VACATION_ID,
                        vacationId
                )
                .build();

        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(
                        ReminderWorker.class
                )
                        .setInputData(reminderData)
                        .setInitialDelay(
                                delayMillis,
                                TimeUnit.MILLISECONDS
                        )
                        .addTag(vacationTag)
                        .build();

        // Replace the previous reminder when
        // a vacation or excursion is edited.
        workManager.enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.REPLACE,
                request
        );
    }
}
