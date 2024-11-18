package com.example.pixelsmemories;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AlarmReceiver extends BroadcastReceiver {

    public static double calculateAverage(List<Integer> numbers) {
        int sum = 0;
        for (int number : numbers) {
            sum += number;
        }
        return (double) sum / numbers.size();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // Get the notification hour and minute from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("Parametres", Context.MODE_PRIVATE);
        int notificationHour = sharedPreferences.getInt("notification_hour", 20);
        int notificationMinute = sharedPreferences.getInt("notification_minute", 0);

        Calendar now = Calendar.getInstance();
        Calendar scheduledTime = Calendar.getInstance();
        scheduledTime.set(Calendar.HOUR_OF_DAY, notificationHour);
        scheduledTime.set(Calendar.MINUTE, notificationMinute);
        scheduledTime.set(Calendar.SECOND, 0);

        // Max allowed time 2 hours after the scheduled time
        Calendar maxAllowedTime = (Calendar) scheduledTime.clone();
        maxAllowedTime.add(Calendar.HOUR_OF_DAY, 2);

        // If the current time is before the scheduled time or after the max allowed time, return
        if (now.before(scheduledTime) || now.after(maxAllowedTime)) {
            return;
        }

        // To open the app when the notification is clicked
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE);

        PixelsParser jsonParser = new PixelsParser();
        List<Pixels.MoodEntry> moodEntries = jsonParser.parsePixelsFile(context, "backup.json");

        // Clear all notifications before sending new ones
        Notification.clearAllNotifications(context);

        if (moodEntries != null) {

            int memories_number = 0;

            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;

            int sliderMinPixelValue = sharedPreferences.getInt("min_pixels_value", 0);
            int sliderMaxMemories = sharedPreferences.getInt("max_memories_value", 0);
            float minPixelsValue = 1 + (sliderMinPixelValue / 10f);

            // Iterate through all the years from 2017 to the current year
            for (int pastYear = 2017; pastYear < calendar.get(Calendar.YEAR); pastYear++) {
                if ((sliderMaxMemories != 10) && (memories_number >= sliderMaxMemories)) {break;}
                String dateLastYear = pastYear + "-" + month + "-" + day;

                for (Pixels.MoodEntry moodEntry : moodEntries) {
                    if (Objects.equals(moodEntry.getDate(), dateLastYear)) {

                        double moodAvg = calculateAverage(moodEntry.getScores());
                        double moodValue = Math.round(moodAvg * 10.0) / 10.0;
                        List<String> moodList = moodEntry.getScores()
                                .stream()
                                .map(String::valueOf)
                                .collect(Collectors.toList());
                        String moodStrings = String.join(",", moodList);

                        if (moodValue >= minPixelsValue) {
                            memories_number++;
                            Notification.sendNotification(context, pastYear + " - Mood : " + moodStrings, moodEntry.getNotes());
                        }
                        break;
                    }
                }
            }
            if (memories_number > 0) {
                Notification.sendSummaryNotification(context, memories_number);
            }
        }
    }
}
