\# Android Vacation Planner



A native Android application I built using Java to organize vacations, manage excursions, and keep track of important travel dates. The app uses Room Database for local storage and WorkManager to schedule reminders.



\## Features



\- \*\*Vacation Management:\*\* Create, view, edit, and delete vacations with destinations and travel dates.

\- \*\*Excursion Planning:\*\* Add, edit, and delete excursions associated with a vacation.

\- \*\*Date Validation:\*\* Prevent invalid vacation date ranges and excursions scheduled outside their vacation dates.

\- \*\*Local Database:\*\* Store vacation and excursion information on the device using Room Database.

\- \*\*Notifications:\*\* Schedule reminders for upcoming vacations and excursions.

\- \*\*Data Management:\*\* Confirm deletions and automatically remove associated excursions when a vacation is deleted.



\## Technologies Used



\- Java

\- Android SDK

\- XML Layouts

\- Room Database (SQLite)

\- Android WorkManager

\- Gradle

\- JUnit and AndroidX Test



\## Screenshots



| Home Screen | Vacation Details | Add Vacation |

|---|---|---|

| !\[Home Screen](android-app/screenshots/vacation-home.png) | !\[Vacation Details](android-app/screenshots/vacation-details.png) | !\[Add Vacation](android-app/screenshots/add-vacation.png) |



\## Automated Testing



I implemented five Android instrumented tests covering database operations and relationships:



1\. Save and retrieve a vacation.

2\. Update an existing vacation.

3\. Save and retrieve an excursion.

4\. Update and delete an excursion.

5\. Verify that deleting a vacation also deletes its associated excursions.



All five tests passed on a Pixel 7 emulator running Android API 35.



!\[Automated Test Results](android-app/screenshots/automated-tests.png)



\## Running the Application



1\. Clone

