package com.example.pixelsmemories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.Manifest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    Button previousButton, nextButton, settings;
    TextView dateDisplay;
    LinearLayout pixelsContainer;
    Calendar dateMemories;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = findViewById(R.id.settings);
        pixelsContainer = findViewById(R.id.pixelsContainer);
        dateDisplay = findViewById(R.id.date_display);

        checkNotificationPermission();

        scheduleNotification(this);

        Notification.clearAllNotifications(this);

        dateMemories = Calendar.getInstance();
        readJsonFromFile(dateMemories);

        previousButton = findViewById(R.id.button_previous);
        previousButton.setOnClickListener(v -> {
            dateMemories.add(Calendar.DAY_OF_MONTH, -1);
            readJsonFromFile(dateMemories);
        });

        nextButton = findViewById(R.id.button_next);
        nextButton.setOnClickListener(v -> {
            dateMemories.add(Calendar.DAY_OF_MONTH, 1);
            readJsonFromFile(dateMemories);
        });

        settings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }


    public static double calculateAverage(List<Integer> numbers) {
        int sum = 0;
        for (int number : numbers) {
            sum += number;
        }
        return (double) sum / numbers.size();
    }


    private void readJsonFromFile(Calendar today) {

        // Update date display
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM", Locale.getDefault());
        String formattedDate = capitalizeEachWord(sdf.format(today.getTime()));
        dateDisplay.setText(formattedDate);

        PixelsParser jsonParser = new PixelsParser();
        List<Pixels.MoodEntry> moodEntries = jsonParser.parsePixelsFile(this, "backup.json");

        // Calendar today = Calendar.getInstance();
        int day = today.get(Calendar.DAY_OF_MONTH);
        int month = today.get(Calendar.MONTH) + 1;
        int year;

        List<String[]> datesList = new ArrayList<>();
        List<String[]> moodsList = new ArrayList<>();
        List<String> avgMoodsList = new ArrayList<>();
        List<String> summariesList = new ArrayList<>();

        if (moodEntries != null) {

            // Iterate through all the years from 2017 to the current year
            for (int i = 1; i <= today.get(Calendar.YEAR) - today.getActualMinimum(Calendar.YEAR); i++) {
                year = today.get(Calendar.YEAR) - i;
                String datePastYear = year + "-" + month + "-" + day;

                for (Pixels.MoodEntry moodEntry : moodEntries) {
                    if (Objects.equals(moodEntry.getDate(), datePastYear)) {
                        List<Integer> scores = moodEntry.getScores();
                        double moodAvg = calculateAverage(scores);
                        double moodValue = Math.round(moodAvg * 10.0) / 10.0;

                        datesList.add(new String[] {String.valueOf(day), String.valueOf(month), String.valueOf(year)});
                        moodsList.add(scores.stream().map(String::valueOf).toArray(String[]::new));
                        avgMoodsList.add(String.valueOf(moodAvg));
                        summariesList.add(moodEntry.getNotes());

                        break;
                    }
                }
            }

            if (datesList.isEmpty()) {
                summariesList.add(getString(R.string.no_pixel_today));
            }

        }
        else {
            summariesList.add(getString(R.string.no_pixel_file));
        }

        if (datesList.isEmpty()) {
            datesList.add(new String[]{String.valueOf(day), String.valueOf(month), String.valueOf(today.get(Calendar.YEAR))});
            moodsList.add(new String[]{});
            avgMoodsList.add("5");
        }
        createDynamicLayouts(
                datesList.toArray(new String[datesList.size()][]),
                moodsList.toArray(new String[moodsList.size()][]),
                avgMoodsList.toArray(new String[avgMoodsList.size()]),
                summariesList.toArray(new String[summariesList.size()])
        );
    }


    public void createDynamicLayouts(String[][] dates, String[][] moods, String[] moodAvgs, String[] summaries) {
        pixelsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < dates.length; i++) {
            // Create a layout_pixels.xml layout for each date
            View dynamicItem = inflater.inflate(R.layout.layout_pixels, pixelsContainer, false);

            TextView dateTextView = dynamicItem.findViewById(R.id.dateText);
            LinearLayout moodIconsContainer = dynamicItem.findViewById(R.id.moodIconsContainer);
            TextView summaryTextView = dynamicItem.findViewById(R.id.resumeText);

            int day = Integer.parseInt(dates[i][0]);
            int month = Integer.parseInt(dates[i][1]) - 1; // Month is 0-based
            int year = Integer.parseInt(dates[i][2]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);

            SimpleDateFormat sdf = new SimpleDateFormat("EEEE d MMMM yyyy", Locale.getDefault());
            String formattedDate = capitalizeEachWord(sdf.format(calendar.getTime()));

            for (String mood : moods[i]) {
                double moodValue = Double.parseDouble(mood);
                int moodIndex = (int) Math.round(moodValue);
                String iconName = "pixel_" + moodIndex;
                int iconId = this.getResources().getIdentifier(iconName, "drawable", this.getPackageName());

                if (iconId != 0) {
                    ImageView moodIcon = new ImageView(this);
                    moodIcon.setImageResource(iconId);

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1.0f // Weight of 1 for each icon
                    );
                    moodIcon.setLayoutParams(layoutParams);
                    moodIconsContainer.addView(moodIcon);
                }
            }

            dateTextView.setText(formattedDate);
            summaryTextView.setText(summaries[i]);

            double moodAvg = Double.parseDouble(moodAvgs[i]);
            int moodIndex = (int) Math.round(moodAvg);
            String drawableName = "pixel_entry_border_" + moodIndex;
            int drawableId = this.getResources().getIdentifier(drawableName, "drawable", this.getPackageName());

            // Apply the border drawable to the pixelContainer
            LinearLayout pixelContainer = dynamicItem.findViewById(R.id.pixelContainer);
            pixelContainer.setBackground(ContextCompat.getDrawable(this, drawableId));

            pixelsContainer.addView(dynamicItem);
        }
    }


    public static String capitalizeEachWord(String text) {
        String[] words = text.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                sb.append(word.substring(1).toLowerCase());
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }


    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 14+
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // If the permission is not granted, request it
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
            else {
                scheduleNotification(this);
            }
        }
        else {
            scheduleNotification(this);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scheduleNotification(this);
            }
            else {
                Toast.makeText(this, getString(R.string.refused_notification), Toast.LENGTH_SHORT).show();
            }
        }
    }


    @SuppressLint("ScheduleExactAlarm")
    public void scheduleNotification(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Use the notification hour and minute from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("Parametres", Context.MODE_PRIVATE);
        int notificationHour = sharedPreferences.getInt("notification_hour", 20);
        int notificationMinute = sharedPreferences.getInt("notification_minute", 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, notificationHour);
        calendar.set(Calendar.MINUTE, notificationMinute);
        calendar.set(Calendar.SECOND, 0);

        // If the time has already passed, set it for the next day
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        /*Toast.makeText(this, "Memories set to " + notificationHour+"h"+notificationMinute, Toast.LENGTH_SHORT).show();*/

        // Set the alarm to trigger exactly at this time every day
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}