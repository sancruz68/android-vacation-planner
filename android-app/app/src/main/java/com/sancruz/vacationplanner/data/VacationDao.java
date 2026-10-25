package com.sancruz.vacationplanner.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface VacationDao {

    @Insert
    long insert(Vacation vacation);

    @Update
    int update(Vacation vacation);

    @Delete
    int delete(Vacation vacation);

    @Query("SELECT * FROM vacations ORDER BY start_date ASC, id DESC")
    List<Vacation> getAllVacations();

    @Query("SELECT * FROM vacations WHERE id = :id LIMIT 1")
    Vacation getVacationById(long id);
}
