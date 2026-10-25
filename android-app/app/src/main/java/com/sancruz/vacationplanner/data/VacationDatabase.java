package com.sancruz.vacationplanner.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {
                Vacation.class,
                Excursion.class
        },
        version = 2,
        exportSchema = false
)
public abstract class VacationDatabase extends RoomDatabase {

    private static volatile VacationDatabase INSTANCE;

    public abstract VacationDao vacationDao();

    public abstract ExcursionDao excursionDao();

    // Upgrade the database from version 1 to version 2.
    // Existing vacations are preserved.
    private static final Migration MIGRATION_1_2 =
            new Migration(1, 2) {

                @Override
                public void migrate(
                        @NonNull SupportSQLiteDatabase database
                ) {

                    // Create the excursions table.
                    database.execSQL(
                            "CREATE TABLE IF NOT EXISTS `excursions` (" +
                                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                    "`vacation_id` INTEGER NOT NULL, " +
                                    "`name` TEXT NOT NULL, " +
                                    "`location` TEXT NOT NULL, " +
                                    "`excursion_date` TEXT NOT NULL, " +
                                    "FOREIGN KEY(`vacation_id`) " +
                                    "REFERENCES `vacations`(`id`) " +
                                    "ON UPDATE NO ACTION ON DELETE CASCADE" +
                                    ")"
                    );

                    // Create an index for finding excursions
                    // belonging to a specific vacation.
                    database.execSQL(
                            "CREATE INDEX IF NOT EXISTS " +
                                    "`index_excursions_vacation_id` " +
                                    "ON `excursions` (`vacation_id`)"
                    );
                }
            };

    public static VacationDatabase getInstance(Context context) {

        if (INSTANCE == null) {

            synchronized (VacationDatabase.class) {

                if (INSTANCE == null) {

                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    VacationDatabase.class,
                                    "vacation_planner.db"
                            )
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }

        return INSTANCE;
    }
}
