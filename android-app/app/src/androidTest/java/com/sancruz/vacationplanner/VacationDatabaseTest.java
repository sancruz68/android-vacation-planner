package com.sancruz.vacationplanner;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.sancruz.vacationplanner.data.Excursion;
import com.sancruz.vacationplanner.data.Vacation;
import com.sancruz.vacationplanner.data.VacationDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class VacationDatabaseTest {

    private VacationDatabase database;

    @Before
    public void setUp() {

        Context context = InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext();

        database = Room.inMemoryDatabaseBuilder(
                        context,
                        VacationDatabase.class
                )
                .allowMainThreadQueries()
                .build();
    }

    @After
    public void tearDown() {

        if (database != null) {
            database.close();
        }
    }

    @Test
    public void shouldSaveAndRetrieveVacation() {

        Vacation vacation = new Vacation(
                "Puerto Rico Getaway",
                "San Juan, Puerto Rico",
                "2026-10-26",
                "2026-10-30"
        );

        long id = database.vacationDao().insert(vacation);

        Vacation saved = database.vacationDao()
                .getVacationById(id);

        assertNotNull(saved);
        assertEquals("Puerto Rico Getaway", saved.getName());
        assertEquals("San Juan, Puerto Rico", saved.getDestination());
        assertEquals("2026-10-26", saved.getStartDate());
        assertEquals("2026-10-30", saved.getEndDate());
    }

    @Test
    public void shouldUpdateVacation() {

        Vacation vacation = new Vacation(
                "Original Trip",
                "San Juan",
                "2026-10-26",
                "2026-10-30"
        );

        long id = database.vacationDao().insert(vacation);

        Vacation saved = database.vacationDao()
                .getVacationById(id);

        assertNotNull(saved);

        saved.setName("Updated Trip");
        saved.setDestination("Ponce, Puerto Rico");

        int updated = database.vacationDao().update(saved);

        Vacation result = database.vacationDao()
                .getVacationById(id);

        assertEquals(1, updated);
        assertNotNull(result);
        assertEquals("Updated Trip", result.getName());
        assertEquals("Ponce, Puerto Rico", result.getDestination());
    }

    @Test
    public void shouldSaveAndRetrieveExcursion() {

        Vacation vacation = new Vacation(
                "Puerto Rico Getaway",
                "San Juan",
                "2026-10-26",
                "2026-10-30"
        );

        long vacationId = database.vacationDao()
                .insert(vacation);

        Excursion excursion = new Excursion(
                vacationId,
                "Old San Juan Walking Tour",
                "Old San Juan",
                "2026-10-28"
        );

        long excursionId = database.excursionDao()
                .insert(excursion);

        Excursion saved = database.excursionDao()
                .getExcursionById(excursionId);

        assertNotNull(saved);
        assertEquals(vacationId, saved.getVacationId());
        assertEquals(
                "Old San Juan Walking Tour",
                saved.getName()
        );
        assertEquals("2026-10-28", saved.getExcursionDate());
    }

    @Test
    public void shouldUpdateAndDeleteExcursion() {

        Vacation vacation = new Vacation(
                "Test Trip",
                "Puerto Rico",
                "2026-10-26",
                "2026-10-30"
        );

        long vacationId = database.vacationDao()
                .insert(vacation);

        Excursion excursion = new Excursion(
                vacationId,
                "Walking Tour",
                "San Juan",
                "2026-10-28"
        );

        long excursionId = database.excursionDao()
                .insert(excursion);

        Excursion saved = database.excursionDao()
                .getExcursionById(excursionId);

        assertNotNull(saved);

        saved.setName("Historic Walking Tour");

        assertEquals(
                1,
                database.excursionDao().update(saved)
        );

        Excursion updated = database.excursionDao()
                .getExcursionById(excursionId);

        assertNotNull(updated);
        assertEquals(
                "Historic Walking Tour",
                updated.getName()
        );

        assertEquals(
                1,
                database.excursionDao().delete(updated)
        );

        assertNull(
                database.excursionDao()
                        .getExcursionById(excursionId)
        );
    }

    @Test
    public void shouldDeleteExcursionsWhenVacationDeleted() {

        Vacation vacation = new Vacation(
                "Test Vacation",
                "Puerto Rico",
                "2026-10-26",
                "2026-10-30"
        );

        long vacationId = database.vacationDao()
                .insert(vacation);

        Excursion excursion = new Excursion(
                vacationId,
                "Snorkeling",
                "Fajardo",
                "2026-10-28"
        );

        database.excursionDao().insert(excursion);

        Vacation saved = database.vacationDao()
                .getVacationById(vacationId);

        assertNotNull(saved);

        assertEquals(
                1,
                database.vacationDao().delete(saved)
        );

        assertNull(
                database.vacationDao()
                        .getVacationById(vacationId)
        );

        assertTrue(
                database.excursionDao()
                        .getExcursionsForVacation(vacationId)
                        .isEmpty()
        );
    }
}
