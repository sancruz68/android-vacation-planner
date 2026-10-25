package com.sancruz.vacationplanner.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vacations")
public class Vacation {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @NonNull
    @ColumnInfo(name = "destination")
    private String destination;

    @NonNull
    @ColumnInfo(name = "start_date")
    private String startDate;

    @NonNull
    @ColumnInfo(name = "end_date")
    private String endDate;

    public Vacation(
            @NonNull String name,
            @NonNull String destination,
            @NonNull String startDate,
            @NonNull String endDate) {

        this.name = name;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getDestination() {
        return destination;
    }

    public void setDestination(@NonNull String destination) {
        this.destination = destination;
    }

    @NonNull
    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(@NonNull String startDate) {
        this.startDate = startDate;
    }

    @NonNull
    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(@NonNull String endDate) {
        this.endDate = endDate;
    }
}
