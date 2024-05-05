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

        // Pour rediriger la notif
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE);

        PixelsParser jsonParser = new PixelsParser();
        List<Pixels.MoodEntry> moodEntries = jsonParser.parsePixelsFile(context, "backup.json");

        Notification.clearAllNotifications(context);

        if (moodEntries != null) {

            int memories_number = 0;

            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;

            SharedPreferences sharedPreferences = context.getSharedPreferences("Parametres", Context.MODE_PRIVATE);
            int sliderMinPixelValue = sharedPreferences.getInt("min_pixels_value", 0);
            int sliderMaxMemories = sharedPreferences.getInt("max_memories_value", 0);
            float minPixelsValue = 1 + (sliderMinPixelValue / 10f);

            // Parcourir toutes les années précédentes
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
