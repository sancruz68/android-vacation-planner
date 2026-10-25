package com.sancruz.vacationplanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sancruz.vacationplanner.data.Excursion;
import com.sancruz.vacationplanner.data.Vacation;
import com.sancruz.vacationplanner.data.VacationDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private LinearLayout vacationsContainer;
    private TextView emptyState;

    private final ExecutorService databaseExecutor =
            Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<String>
            notificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {

                        if (!granted) {
                            Toast.makeText(
                                    this,
                                    "Notifications are disabled. Enable them in app settings.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (view, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        vacationsContainer = findViewById(
                R.id.vacationsContainer
        );

        emptyState = findViewById(
                R.id.emptyState
        );

        Button addVacationButton = findViewById(
                R.id.buttonAddVacation
        );

        addVacationButton.setOnClickListener(view -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    AddVacationActivity.class
            );

            startActivity(intent);
        });

        requestNotificationPermission();
    }

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= 33) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                );
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVacations();
    }

    private void loadVacations() {

        databaseExecutor.execute(() -> {

            try {

                VacationDatabase database =
                        VacationDatabase.getInstance(
                                getApplicationContext()
                        );

                List<Vacation> vacations =
                        database.vacationDao().getAllVacations();

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    displayVacations(vacations);
                });

                // Synchronize reminders for saved trips.
                synchronizeReminders(database, vacations);

            } catch (Exception exception) {

                Log.e(
                        "VacationPlanner",
                        "Failed to load vacations",
                        exception
                );

                runOnUiThread(() -> {

                    if (!isFinishing() && !isDestroyed()) {

                        Toast.makeText(
                                this,
                                "Unable to load vacations",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }
        });
    }

    private void synchronizeReminders(
            VacationDatabase database,
            List<Vacation> vacations
    ) {

        for (Vacation vacation : vacations) {

            try {

                ReminderScheduler.scheduleVacation(
                        getApplicationContext(),
                        vacation
                );

            } catch (Exception exception) {

                Log.e(
                        "VacationPlanner",
                        "Failed to synchronize vacation reminder",
                        exception
                );
            }

            try {

                List<Excursion> excursions =
                        database.excursionDao()
                                .getExcursionsForVacation(
                                        vacation.getId()
                                );

                for (Excursion excursion : excursions) {

                    try {

                        ReminderScheduler.scheduleExcursion(
                                getApplicationContext(),
                                excursion
                        );

                    } catch (Exception exception) {

                        Log.e(
                                "VacationPlanner",
                                "Failed to synchronize excursion reminder",
                                exception
                        );
                    }
                }

            } catch (Exception exception) {

                Log.e(
                        "VacationPlanner",
                        "Failed to load excursions for reminders",
                        exception
                );
            }
        }
    }

    private void displayVacations(List<Vacation> vacations) {

        // Keep the empty-state TextView.
        int existingCards =
                vacationsContainer.getChildCount() - 1;

        if (existingCards > 0) {

            vacationsContainer.removeViews(
                    1,
                    existingCards
            );
        }

        if (vacations.isEmpty()) {

            emptyState.setVisibility(View.VISIBLE);
            return;
        }

        emptyState.setVisibility(View.GONE);

        for (Vacation vacation : vacations) {

            LinearLayout card = new LinearLayout(this);

            card.setOrientation(LinearLayout.VERTICAL);

            card.setPadding(
                    dp(20),
                    dp(18),
                    dp(20),
                    dp(18)
            );

            GradientDrawable background =
                    new GradientDrawable();

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

            // Vacation name.
            TextView nameView = new TextView(this);

            nameView.setText(vacation.getName());
            nameView.setTextSize(20);

            nameView.setTextColor(
                    Color.parseColor("#193F43")
            );

            nameView.setTypeface(
                    null,
                    Typeface.BOLD
            );

            card.addView(nameView);

            // Destination.
            TextView destinationView = new TextView(this);

            destinationView.setText(
                    vacation.getDestination()
            );

            destinationView.setTextSize(15);

            destinationView.setTextColor(
                    Color.parseColor("#526E69")
            );

            LinearLayout.LayoutParams destinationParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            destinationParams.topMargin = dp(8);

            card.addView(
                    destinationView,
                    destinationParams
            );

            // Vacation dates.
            TextView datesView = new TextView(this);

            datesView.setText(
                    vacation.getStartDate()
                            + "  -  "
                            + vacation.getEndDate()
            );

            datesView.setTextSize(14);

            datesView.setTextColor(
                    Color.parseColor("#657875")
            );

            LinearLayout.LayoutParams datesParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            datesParams.topMargin = dp(14);

            card.addView(
                    datesView,
                    datesParams
            );

            // Open vacation details.
            card.setClickable(true);
            card.setFocusable(true);

            card.setContentDescription(
                    "View vacation: " + vacation.getName()
            );

            card.setOnClickListener(view -> {

                Intent intent = new Intent(
                        MainActivity.this,
                        VacationDetailActivity.class
                );

                intent.putExtra(
                        VacationDetailActivity.EXTRA_VACATION_ID,
                        vacation.getId()
                );

                startActivity(intent);
            });

            vacationsContainer.addView(
                    card,
                    cardParams
            );
        }
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
