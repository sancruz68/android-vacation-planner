package com.sancruz.vacationplanner.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ExcursionDao {

    @Insert
    long insert(Excursion excursion);

    @Update
    int update(Excursion excursion);

    @Delete
    int delete(Excursion excursion);

    @Query(
            "SELECT * FROM excursions " +
                    "WHERE vacation_id = :vacationId " +
                    "ORDER BY excursion_date ASC, id ASC"
    )
    List<Excursion> getExcursionsForVacation(long vacationId);

    @Query(
            "SELECT * FROM excursions " +
                    "WHERE id = :excursionId LIMIT 1"
    )
    Excursion getExcursionById(long excursionId);
}
