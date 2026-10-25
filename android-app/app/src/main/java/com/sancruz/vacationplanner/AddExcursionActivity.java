package com.sancruz.vacationplanner;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sancruz.vacationplanner.data.Excursion;
import com.sancruz.vacationplanner.data.ExcursionDao;
import com.sancruz.vacationplanner.data.Vacation;
import com.sancruz.vacationplanner.data.VacationDatabase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddExcursionActivity extends AppCompatActivity {

    public static final String EXTRA_VACATION_ID = "vacationId";
    public static final String EXTRA_EDIT_EXCURSION_ID = "editExcursionId";

    private EditText editExcursionName;
    private EditText editExcursionLocation;
    private TextView textExcursionFormHeading;

    private Button buttonExcursionDate;
    private Button buttonSaveExcursion;
    private Button buttonDeleteExcursion;
    private Button buttonCancelExcursion;

    private LocalDate excursionDate;
    private Vacation selectedVacation;

    private long vacationId = -1L;
    private long editingExcursionId = -1L;

    private boolean busy = false;

    private final ExecutorService databaseExecutor =
            Executors.newSingleThreadExecutor();

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_excursion);

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

        textExcursionFormHeading = findViewById(
                R.id.textExcursionFormHeading
        );

        editExcursionName = findViewById(
                R.id.editExcursionName
        );

        editExcursionLocation = findViewById(
                R.id.editExcursionLocation
        );

        buttonExcursionDate = findViewById(
                R.id.buttonExcursionDate
        );

        buttonSaveExcursion = findViewById(
                R.id.buttonSaveExcursion
        );

        buttonDeleteExcursion = findViewById(
                R.id.buttonDeleteExcursion
        );

        buttonCancelExcursion = findViewById(
                R.id.buttonCancelExcursion
        );

        vacationId = getIntent().getLongExtra(
                EXTRA_VACATION_ID,
                -1L
        );

        editingExcursionId = getIntent().getLongExtra(
                EXTRA_EDIT_EXCURSION_ID,
                -1L
        );

        if (vacationId < 1) {
            Toast.makeText(
                    this,
                    "No vacation selected",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        if (editingExcursionId > 0) {

            textExcursionFormHeading.setText("Edit Excursion");
            buttonSaveExcursion.setText("Save Changes");
            buttonDeleteExcursion.setVisibility(View.VISIBLE);

        } else {

            textExcursionFormHeading.setText("Add an Excursion");
            buttonSaveExcursion.setText("Save Excursion");
            buttonDeleteExcursion.setVisibility(View.GONE);
        }

        buttonSaveExcursion.setEnabled(false);
        buttonDeleteExcursion.setEnabled(false);
        buttonExcursionDate.setEnabled(false);

        buttonExcursionDate.setOnClickListener(
                view -> showDatePicker()
        );

        buttonSaveExcursion.setOnClickListener(
                view -> saveExcursion()
        );

        buttonDeleteExcursion.setOnClickListener(
                view -> confirmDeletion()
        );

        buttonCancelExcursion.setOnClickListener(
                view -> finish()
        );

        loadFormData();
    }

    private void loadFormData() {

        databaseExecutor.execute(() -> {

            try {

                VacationDatabase database =
                        VacationDatabase.getInstance(
                                getApplicationContext()
                        );

                Vacation vacation =
                        database.vacationDao().getVacationById(
                                vacationId
                        );

                Excursion excursion = null;

                if (editingExcursionId > 0) {
                    excursion = database.excursionDao()
                            .getExcursionById(editingExcursionId);
                }

                if (vacation == null) {
                    throw new IllegalStateException(
                            "Vacation not found"
                    );
                }

                if (editingExcursionId > 0 &&
                        (excursion == null ||
                                excursion.getVacationId() != vacationId)) {

                    throw new IllegalStateException(
                            "Excursion not found"
                    );
                }

                final Excursion loadedExcursion = excursion;

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    selectedVacation = vacation;

                    if (loadedExcursion != null) {

                        editExcursionName.setText(
                                loadedExcursion.getName()
                        );

                        editExcursionLocation.setText(
                                loadedExcursion.getLocation()
                        );

                        excursionDate = LocalDate.parse(
                                loadedExcursion.getExcursionDate()
                        );

                        buttonExcursionDate.setText(
                                excursionDate.format(dateFormatter)
                        );
                    }

                    buttonSaveExcursion.setEnabled(true);
                    buttonExcursionDate.setEnabled(true);

                    if (editingExcursionId > 0) {
                        buttonDeleteExcursion.setEnabled(true);
                    }
                });

            } catch (Exception exception) {

                Log.e(
                        "VacationPlanner",
                        "Failed to load excursion",
                        exception
                );

                runOnUiThread(() -> {

                    if (!isFinishing() && !isDestroyed()) {

                        Toast.makeText(
                                this,
                                "Unable to load excursion",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                    }
                });
            }
        });
    }

    private void showDatePicker() {

        if (selectedVacation == null || busy) {
            return;
        }

        LocalDate initialDate;

        if (excursionDate != null) {

            initialDate = excursionDate;

        } else {

            initialDate = LocalDate.parse(
                    selectedVacation.getStartDate()
            );
        }

        DatePickerDialog picker = new DatePickerDialog(
                this,
                (datePicker, year, month, day) -> {

                    excursionDate = LocalDate.of(
                            year,
                            month + 1,
                            day
                    );

                    buttonExcursionDate.setText(
                            excursionDate.format(dateFormatter)
                    );
                },
                initialDate.getYear(),
                initialDate.getMonthValue() - 1,
                initialDate.getDayOfMonth()
        );

        picker.show();
    }

    private void saveExcursion() {

        if (busy || selectedVacation == null) {
            return;
        }

        String excursionName = editExcursionName.getText()
                .toString()
                .trim();

        String location = editExcursionLocation.getText()
                .toString()
                .trim();

        if (excursionName.isEmpty()) {

            editExcursionName.setError(
                    "Please enter an excursion name"
            );

            editExcursionName.requestFocus();
            return;
        }

        if (location.isEmpty()) {

            editExcursionLocation.setError(
                    "Please enter a location"
            );

            editExcursionLocation.requestFocus();
            return;
        }

        if (excursionDate == null) {

            Toast.makeText(
                    this,
                    "Please select an excursion date",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        LocalDate vacationStart = LocalDate.parse(
                selectedVacation.getStartDate()
        );

        LocalDate vacationEnd = LocalDate.parse(
                selectedVacation.getEndDate()
        );

        if (excursionDate.isBefore(vacationStart) ||
                excursionDate.isAfter(vacationEnd)) {

            Toast.makeText(
                    this,
                    "Excursion date must be within the vacation dates",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        final LocalDate dateToSave = excursionDate;

        setBusy(true);

        databaseExecutor.execute(() -> {

            try {

                VacationDatabase database =
                        VacationDatabase.getInstance(
                                getApplicationContext()
                        );

                ExcursionDao dao = database.excursionDao();

                Vacation vacation =
                        database.vacationDao().getVacationById(
                                vacationId
                        );

                if (vacation == null) {
                    throw new IllegalStateException(
                            "Vacation no longer exists"
                    );
                }

                LocalDate currentStart = LocalDate.parse(
                        vacation.getStartDate()
                );

                LocalDate currentEnd = LocalDate.parse(
                        vacation.getEndDate()
                );

                if (dateToSave.isBefore(currentStart) ||
                        dateToSave.isAfter(currentEnd)) {

                    throw new IllegalStateException(
                            "Excursion date is outside vacation dates"
                    );
                }

                Excursion savedExcursion;

                if (editingExcursionId > 0) {

                    // Update an existing excursion.
                    savedExcursion = dao.getExcursionById(
                            editingExcursionId
                    );

                    if (savedExcursion == null ||
                            savedExcursion.getVacationId() != vacationId) {

                        throw new IllegalStateException(
                                "Excursion no longer exists"
                        );
                    }

                    savedExcursion.setName(excursionName);
                    savedExcursion.setLocation(location);
                    savedExcursion.setExcursionDate(
                            dateToSave.toString()
                    );

                    int updated = dao.update(savedExcursion);

                    if (updated != 1) {
                        throw new IllegalStateException(
                                "Excursion update failed"
                        );
                    }

                } else {

                    // Create a new excursion.
                    savedExcursion = new Excursion(
                            vacationId,
                            excursionName,
                            location,
                            dateToSave.toString()
                    );

                    long newExcursionId = dao.insert(
                            savedExcursion
                    );

                    // WorkManager needs the generated ID
                    // to identify this excursion's reminder.
                    savedExcursion.setId(newExcursionId);
                }

                // Schedule or replace the reminder.
                // An error here must not undo a successful
                // database operation.
                try {

                    ReminderScheduler.scheduleExcursion(
                            getApplicationContext(),
                            savedExcursion
                    );

                } catch (Exception schedulingException) {

                    Log.e(
                            "VacationPlanner",
                            "Unable to schedule excursion reminder",
                            schedulingException
                    );
                }

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    Toast.makeText(
                            this,
                            editingExcursionId > 0
                                    ? "Excursion updated successfully"
                                    : "Excursion saved successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    setResult(RESULT_OK);
                    finish();
                });

            } catch (Exception exception) {

                Log.e(
                        "VacationPlanner",
                        "Failed to save excursion",
                        exception
                );

                runOnUiThread(() -> {

                    if (!isFinishing() && !isDestroyed()) {

                        setBusy(false);

                        Toast.makeText(
                                this,
                                "Unable to save excursion",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }
        });
    }

    private void confirmDeletion() {

        if (busy || editingExcursionId < 1) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Excursion?")
                .setMessage(
                        "Are you sure you want to delete this excursion? "
                                + "This action cannot be undone."
                )
                .setNegativeButton(
                        "Cancel",
                        (dialog, which) -> dialog.dismiss()
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> deleteExcursion()
                )
                .show();
    }

    private void deleteExcursion() {

        if (busy || editingExcursionId < 1) {
            return;
        }

        setBusy(true);

        databaseExecutor.execute(() -> {

            try {

                VacationDatabase database =
                        VacationDatabase.getInstance(
                                getApplicationContext()
                        );

                ExcursionDao dao = database.excursionDao();

                Excursion excursion = dao.getExcursionById(
                        editingExcursionId
                );

                if (excursion == null ||
                        excursion.getVacationId() != vacationId) {

                    throw new IllegalStateException(
                            "Excursion not found"
                    );
                }

                int deletedRows = dao.delete(excursion);

                if (deletedRows != 1) {
                    throw new IllegalStateException(
                            "Excursion could not be deleted"
                    );
                }

                // Cancel the deleted excursion's reminder.
                try {

                    ReminderScheduler.cancelExcursion(
                            getApplicationContext(),
                            editingExcursionId
                    );

                } catch (Exception schedulingException) {

                    Log.e(
                            "VacationPlanner",
                            "Unable to cancel excursion reminder",
                            schedulingException
                    );
                }

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    Toast.makeText(
                            this,
                            "Excursion deleted successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    setResult(RESULT_OK);
                    finish();
                });

            } catch (Exception exception) {

                Log.e(
                        "VacationPlanner",
                        "Failed to delete excursion",
                        exception
                );

                runOnUiThread(() -> {

                    if (!isFinishing() && !isDestroyed()) {

                        setBusy(false);

                        Toast.makeText(
                                this,
                                "Unable to delete excursion",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }
        });
    }

    private void setBusy(boolean isBusy) {

        busy = isBusy;

        boolean ready = !isBusy && selectedVacation != null;

        buttonSaveExcursion.setEnabled(ready);
        buttonCancelExcursion.setEnabled(!isBusy);
        buttonExcursionDate.setEnabled(ready);

        if (editingExcursionId > 0) {
            buttonDeleteExcursion.setEnabled(ready);
        }
    }

    @Override
    protected void onDestroy() {
        databaseExecutor.shutdown();
        super.onDestroy();
    }
}
