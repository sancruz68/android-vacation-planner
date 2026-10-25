package com.sancruz.vacationplanner.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "excursions",
        foreignKeys = @ForeignKey(
                entity = Vacation.class,
                parentColumns = "id",
                childColumns = "vacation_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index("vacation_id")
        }
)
public class Excursion {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "vacation_id")
    private long vacationId;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @NonNull
    @ColumnInfo(name = "location")
    private String location;

    @NonNull
    @ColumnInfo(name = "excursion_date")
    private String excursionDate;

    public Excursion(
            long vacationId,
            @NonNull String name,
            @NonNull String location,
            @NonNull String excursionDate
    ) {
        this.vacationId = vacationId;
        this.name = name;
        this.location = location;
        this.excursionDate = excursionDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVacationId() {
        return vacationId;
    }

    public void setVacationId(long vacationId) {
        this.vacationId = vacationId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getLocation() {
        return location;
    }

    public void setLocation(@NonNull String location) {
        this.location = location;
    }

    @NonNull
    public String getExcursionDate() {
        return excursionDate;
    }

    public void setExcursionDate(@NonNull String excursionDate) {
        this.excursionDate = excursionDate;
    }
}
