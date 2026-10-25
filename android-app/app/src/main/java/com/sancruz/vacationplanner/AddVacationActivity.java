package com.sancruz.vacationplanner;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sancruz.vacationplanner.data.Vacation;
import com.sancruz.vacationplanner.data.VacationDao;
import com.sancruz.vacationplanner.data.VacationDatabase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddVacationActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_VACATION_ID =
            "editVacationId";

    private EditText editVacationName;
    private EditText editDestination;

    private TextView textFormHeading;

    private Button buttonStartDate;
    private Button buttonEndDate;
    private Button buttonSaveVacation;
    private Button buttonCancelVacation;

    private LocalDate startDate;
    private LocalDate endDate;

    private long editingVacationId = -1L;
    private boolean saving = false;

    private final ExecutorService databaseExecutor =
            Executors.newSingleThreadExecutor();

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern(
                    "MMM d, yyyy",
                    Locale.US
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_vacation);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (view, insets) -> {

                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            bars.left,
                            bars.top,
                            bars.right,
                            bars.bottom
                    );

                    return insets;
                }
        );

        textFormHeading = findViewById(R.id.textFormHeading);
        editVacationName = findViewById(R.id.editVacationName);
        editDestination = findViewById(R.id.editDestination);

        buttonStartDate = findViewById(R.id.buttonStartDate);
        buttonEndDate = findViewById(R.id.buttonEndDate);
        buttonSaveVacation = findViewById(R.id.buttonSaveVacation);
        buttonCancelVacation = findViewById(R.id.buttonCancelVacation);

        buttonStartDate.setOnClickListener(
                view -> showDatePicker(true)
        );

        buttonEndDate.setOnClickListener(
                view -> showDatePicker(false)
        );

        buttonSaveVacation.setOnClickListener(
                view -> saveVacation()
        );

        buttonCancelVacation.setOnClickListener(
                view -> finish()
        );

        editingVacationId = getIntent().getLongExtra(
                EXTRA_EDIT_VACATION_ID,
                -1L
        );

        if (editingVacationId > 0) {

            textFormHeading.setText("Edit Vacation");

            buttonSaveVacation.setText("Save Changes");
            buttonSaveVacation.setEnabled(false);

            loadVacationForEditing();

        } else {

            textFormHeading.setText("Plan a new vacation");
            buttonSaveVacation.setText("Save Vacation");
        }
    }

    private void loadVacationForEditing() {

        databaseExecutor.execute(() -> {

            try {

                Vacation vacation =
                        VacationDatabase.getInstance(
                                getApplicationContext()
                        ).vacationDao().getVacationById(
                                editingVacationId
                        );

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (vacation == null) {

                        Toast.makeText(
                                this,
                                "Vacation not found",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                        return;
                    }

                    editVacationName.setText(vacation.getName());
                    editDestination.setText(vacation.getDestination());

                    startDate = LocalDate.parse(
                            vacation.getStartDate()
                    );

                    endDate = LocalDate.parse(
                            vacation.getEndDate()
                    );

                    buttonStartDate.setText(
                            startDate.format(dateFormatter)
                    );

                    buttonEndDate.setText(
                            endDate.format(dateFormatter)
                    );

                    buttonSaveVacation.setEnabled(true);
                });

            } catch (Exception exception) {

                Log.e(
                        "VacationPlanner",
                        "Failed to load vacation",
                        exception
                );

                runOnUiThread(() -> {

                    if (!isFinishing() && !isDestroyed()) {

                        Toast.makeText(
                                this,
                                "Unable to load vacation",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                    }
                });
            }
        });
    }

    private void showDatePicker(boolean selectingStartDate) {

        if (saving) {
            return;
        }

        LocalDate initialDate;

        if (selectingStartDate) {

            initialDate = startDate != null
                    ? startDate
                    : LocalDate.now();

        } else {

            initialDate = endDate != null
                    ? endDate
                    : startDate != null
                      ? startDate
                      : LocalDate.now();
        }

        DatePickerDialog picker = new DatePickerDialog(
                this,
                (datePicker, year, month, day) -> {

                    LocalDate selectedDate = LocalDate.of(
                            year,
                            month + 1,
                            day
                    );

                    if (selectingStartDate) {

                        startDate = selectedDate;

                        buttonStartDate.setText(
                                startDate.format(dateFormatter)
                        );

                    } else {

                        endDate = selectedDate;

                        buttonEndDate.setText(
                                endDate.format(dateFormatter)
                        );
                    }
                },
                initialDate.getYear(),
                initialDate.getMonthValue() - 1,
                initialDate.getDayOfMonth()
        );

        picker.show();
    }

    private void saveVacation() {

        if (saving) {
            return;
        }

        String vacationName = editVacationName.getText()
                .toString()
                .trim();

        String destination = editDestination.getText()
                .toString()
                .trim();

        if (vacationName.isEmpty()) {

            editVacationName.setError(
                    "Please enter a vacation name"
            );

            editVacationName.requestFocus();
            return;
        }

        if (destination.isEmpty()) {

            editDestination.setError(
                    "Please enter a destination"
            );

            editDestination.requestFocus();
            return;
        }

        if (startDate == null || endDate == null) {

            Toast.makeText(
                    this,
                    "Please select both vacation dates",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (endDate.isBefore(startDate)) {

            Toast.makeText(
                    this,
                    "End date cannot be before start date",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        final String startDateToSave = startDate.toString();
        final String endDateToSave = endDate.toString();

        saving = true;

        buttonSaveVacation.setEnabled(false);
        buttonCancelVacation.setEnabled(false);
        buttonStartDate.setEnabled(false);
        buttonEndDate.setEnabled(false);

        databaseExecutor.execute(() -> {

            try {

                VacationDao dao =
                        VacationDatabase.getInstance(
                                getApplicationContext()
                        ).vacationDao();

                Vacation savedVacation;

                if (editingVacationId > 0) {

                    // Update an existing vacation.
                    savedVacation = dao.getVacationById(
                            editingVacationId
                    );

                    if (savedVacation == null) {
                        throw new IllegalStateException(
                                "Vacation no longer exists"
                        );
                    }

                    savedVacation.setName(vacationName);
                    savedVacation.setDestination(destination);
                    savedVacation.setStartDate(startDateToSave);
                    savedVacation.setEndDate(endDateToSave);

                    int updated = dao.update(savedVacation);

                    if (updated != 1) {
                        throw new IllegalStateException(
                                "Vacation update failed"
                        );
                    }

                } else {

                    // Create a new vacation.
                    savedVacation = new Vacation(
                            vacationName,
                            destination,
                            startDateToSave,
                            endDateToSave
                    );

                    long newVacationId = dao.insert(
                            savedVacation
                    );

                    // Room returns the generated ID.
                    // The reminder needs this ID to open
                    // the correct vacation when tapped.
                    savedVacation.setId(newVacationId);
                }

                // Schedule or replace the vacation reminder.
                // A reminder is not scheduled for a past date.
                try {

                    ReminderScheduler.scheduleVacation(
                            getApplicationContext(),
                            savedVacation
                    );

                } catch (Exception schedulingException) {

                    // The vacation was saved successfully.
                    // A scheduling error should not be reported
                    // as a database-saving failure.
                    Log.e(
                            "VacationPlanner",
                            "Unable to schedule vacation reminder",
                            schedulingException
                    );
                }

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    Toast.makeText(
                            this,
                            editingVacationId > 0
                                    ? "Vacation updated successfully"
                                    : "Vacation saved successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    setResult(RESULT_OK);
                    finish();
                });

            } catch (Exception exception) {

                Log.e(
                        "VacationPlanner",
                        "Failed to save vacation",
                        exception
                );

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    saving = false;

                    buttonSaveVacation.setEnabled(true);
                    buttonCancelVacation.setEnabled(true);
                    buttonStartDate.setEnabled(true);
                    buttonEndDate.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Unable to save vacation",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        databaseExecutor.shutdown();
        super.onDestroy();
    }
}
