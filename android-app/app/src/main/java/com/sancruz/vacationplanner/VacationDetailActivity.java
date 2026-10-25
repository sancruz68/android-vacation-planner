package com.sancruz.vacationplanner;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sancruz.vacationplanner.data.Excursion;
import com.sancruz.vacationplanner.data.Vacation;
import com.sancruz.vacationplanner.data.VacationDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VacationDetailActivity extends AppCompatActivity {

    public static final String EXTRA_VACATION_ID = "vacationId";

    private TextView textVacationName;
    private TextView textDestination;
    private TextView textStartDate;
    private TextView textEndDate;
    private TextView emptyExcursions;

    private LinearLayout excursionsContainer;

    private Button buttonEditVacation;
    private Button buttonDeleteVacation;
    private Button buttonAddExcursion;

    private long vacationId = -1L;
    private boolean deleting = false;

    private final ExecutorService databaseExecutor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vacation_detail);

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

        textVacationName = findViewById(R.id.textVacationName);
        textDestination = findViewById(R.id.textDestination);
        textStartDate = findViewById(R.id.textStartDate);
        textEndDate = findViewById(R.id.textEndDate);

        excursionsContainer = findViewById(
                R.id.excursionsContainer
        );

        emptyExcursions = findViewById(
                R.id.emptyExcursions
        );

        buttonEditVacation = findViewById(
                R.id.buttonEditVacation
        );

        buttonDeleteVacation = findViewById(
                R.id.buttonDeleteVacation
        );

        buttonAddExcursion = findViewById(
                R.id.buttonAddExcursion
        );

        Button buttonBack = findViewById(
                R.id.buttonBack
        );

        vacationId = getIntent().getLongExtra(
                EXTRA_VACATION_ID,
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

        // Edit the selected vacation.
        buttonEditVacation.setOnClickListener(view -> {

            if (deleting) {
                return;
            }

            Intent intent = new Intent(
                    VacationDetailActivity.this,
                    AddVacationActivity.class
            );

            intent.putExtra(
                    AddVacationActivity.EXTRA_EDIT_VACATION_ID,
                    vacationId
            );

            startActivity(intent);
        });

        // Delete the vacation after confirmation.
        buttonDeleteVacation.setOnClickListener(
                view -> confirmDeletion()
        );

        // Add an excursion to this vacation.
        buttonAddExcursion.setOnClickListener(view -> {

            if (deleting) {
                return;
            }

            Intent intent = new Intent(
                    VacationDetailActivity.this,
                    AddExcursionActivity.class
            );

            intent.putExtra(
                    AddExcursionActivity.EXTRA_VACATION_ID,
                    vacationId
            );

            startActivity(intent);
        });

        // Return to the vacation list.
        buttonBack.setOnClickListener(
                view -> finish()
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (vacationId > 0 && !deleting) {
            loadVacationDetails();
        }
    }

    private void loadVacationDetails() {

        buttonEditVacation.setEnabled(false);
        buttonDeleteVacation.setEnabled(false);
        buttonAddExcursion.setEnabled(false);

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

                List<Excursion> excursions = null;

                if (vacation != null) {

                    excursions = database.excursionDao()
                            .getExcursionsForVacation(vacationId);
                }

                final List<Excursion> savedExcursions = excursions;

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed() || deleting) {
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

                    textVacationName.setText(
                            vacation.getName()
                    );

                    textDestination.setText(
                            vacation.getDestination()
                    );

                    textStartDate.setText(
                            vacation.getStartDate()
                    );

                    textEndDate.setText(
                            vacation.getEndDate()
                    );

                    displayExcursions(savedExcursions);

                    buttonEditVacation.setEnabled(true);
                    buttonDeleteVacation.setEnabled(true);
                    buttonAddExcursion.setEnabled(true);
                });

            } catch (Exception exception) {

                Log.e(
                        "VacationPlanner",
                        "Failed to load vacation details",
                        exception
                );

                runOnUiThread(() -> {

                    if (!isFinishing() && !isDestroyed()) {

                        Toast.makeText(
                                this,
                                "Unable to load vacation details",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }
        });
    }

    private void displayExcursions(List<Excursion> excursions) {

        // Remove old excursion cards while keeping
        // the empty-state TextView.
        int childCount = excursionsContainer.getChildCount();

        if (childCount > 1) {

            excursionsContainer.removeViews(
                    1,
                    childCount - 1
            );
        }

        if (excursions == null || excursions.isEmpty()) {

            emptyExcursions.setVisibility(View.VISIBLE);
            return;
        }

        emptyExcursions.setVisibility(View.GONE);

        for (Excursion excursion : excursions) {

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);

            card.setPadding(
                    dp(20),
                    dp(18),
                    dp(20),
                    dp(18)
            );

            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.WHITE);
            background.setCornerRadius(dp(12));

            card.setBackground(background);
            card.setElevation(dp(2));

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            cardParams.bottomMargin = dp(16);

            // Excursion name.
            TextView nameView = new TextView(this);

            nameView.setText(excursion.getName());
            nameView.setTextSize(19);

            nameView.setTextColor(
                    Color.parseColor("#193F43")
            );

            nameView.setTypeface(
                    null,
                    Typeface.BOLD
            );

            card.addView(nameView);

            // Excursion location.
            TextView locationView = new TextView(this);

            locationView.setText(
                    excursion.getLocation()
            );

            locationView.setTextSize(15);

            locationView.setTextColor(
                    Color.parseColor("#526E69")
            );

            LinearLayout.LayoutParams locationParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            locationParams.topMargin = dp(8);

            card.addView(
                    locationView,
                    locationParams
            );

            // Excursion date.
            TextView dateView = new TextView(this);

            dateView.setText(
                    excursion.getExcursionDate()
            );

            dateView.setTextSize(14);

            dateView.setTextColor(
                    Color.parseColor("#657875")
            );

            LinearLayout.LayoutParams dateParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            dateParams.topMargin = dp(12);

            card.addView(
                    dateView,
                    dateParams
            );

            // Open the excursion editing form.
            card.setClickable(true);
            card.setFocusable(true);

            card.setContentDescription(
                    "Edit excursion: " + excursion.getName()
            );

            card.setOnClickListener(view -> {

                if (deleting) {
                    return;
                }

                Intent intent = new Intent(
                        VacationDetailActivity.this,
                        AddExcursionActivity.class
                );

                intent.putExtra(
                        AddExcursionActivity.EXTRA_VACATION_ID,
                        vacationId
                );

                intent.putExtra(
                        AddExcursionActivity.EXTRA_EDIT_EXCURSION_ID,
                        excursion.getId()
                );

                startActivity(intent);
            });

            excursionsContainer.addView(
                    card,
                    cardParams
            );
        }
    }

    private void confirmDeletion() {

        if (deleting) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Vacation?")
                .setMessage(
                        "Are you sure you want to delete this vacation? "
                                + "Its excursions will also be deleted. "
                                + "This action cannot be undone."
                )
                .setNegativeButton(
                        "Cancel",
                        (dialog, which) -> dialog.dismiss()
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> deleteVacation()
                )
                .show();
    }

    private void deleteVacation() {

        if (deleting) {
            return;
        }

        deleting = true;

        buttonDeleteVacation.setEnabled(false);
        buttonEditVacation.setEnabled(false);
        buttonAddExcursion.setEnabled(false);

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

                if (vacation == null) {

                    throw new IllegalStateException(
                            "Vacation not found"
                    );
                }

                // Delete the vacation from Room Database.
                // Related excursions are removed through
                // the database's foreign-key cascade.
                int deletedRows =
                        database.vacationDao().delete(vacation);

                if (deletedRows != 1) {

                    throw new IllegalStateException(
                            "Vacation could not be deleted"
                    );
                }

                // Cancel the vacation reminder and all
                // excursion reminders belonging to it.
                try {

                    ReminderScheduler.cancelVacation(
                            getApplicationContext(),
                            vacationId
                    );

                } catch (Exception schedulingException) {

                    // Database deletion succeeded.
                    // Log reminder cancellation failures
                    // separately from database failures.
                    Log.e(
                            "VacationPlanner",
                            "Unable to cancel vacation reminders",
                            schedulingException
                    );
                }

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    Toast.makeText(
                            this,
                            "Vacation deleted successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    setResult(RESULT_OK);
                    finish();
                });

            } catch (Exception exception) {

                Log.e(
                        "VacationPlanner",
                        "Failed to delete vacation",
                        exception
                );

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    deleting = false;

                    buttonDeleteVacation.setEnabled(true);
                    buttonEditVacation.setEnabled(true);
                    buttonAddExcursion.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Unable to delete vacation",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private int dp(int value) {

        return (int) (
                value
                        * getResources().getDisplayMetrics().density
                        + 0.5f
        );
    }

    @Override
    protected void onDestroy() {

        databaseExecutor.shutdown();
        super.onDestroy();
    }
}
