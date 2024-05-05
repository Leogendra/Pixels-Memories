package com.example.yearsofpixels;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private static final int PICK_JSON_FILE_REQUEST_CODE = 1;
    private Button saveButton;
    private TextView minPixelValueTextView, maxMemoriesTextView;
    private TimePicker timeNotification;
    private SeekBar minPixelValueSlider, maxMemoriesSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        timeNotification = findViewById(R.id.timeNotification);
        minPixelValueTextView = findViewById(R.id.minPixelValueTextView);
        maxMemoriesTextView = findViewById(R.id.maxMemoriesTextView);
        minPixelValueSlider = findViewById(R.id.minPixelValueSlider);
        maxMemoriesSlider = findViewById(R.id.maxMemoriesSlider);

        loadSettings();

        minPixelValueSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateMinPixelsSlider(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        maxMemoriesSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateNumberPixelsSlider(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        saveButton = findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(v -> {
            saveSettings();
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }


    public String getPixelsLastEntryDate() {

        PixelsParser jsonParser = new PixelsParser();
        List<Pixels.MoodEntry> moodEntries = jsonParser.parsePixelsFile(this,"backup.json");

        if (moodEntries != null) {

            Date mostRecentDate = null;
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            try {
                mostRecentDate = dateFormat.parse("1900-01-01");
            } catch (ParseException e) {
                e.printStackTrace();
            }

            for (Pixels.MoodEntry moodEntry : moodEntries) {
                try {
                    Date currentDate = dateFormat.parse(moodEntry.getDate());
                    if (currentDate.after(mostRecentDate)) {
                        mostRecentDate = currentDate;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String formattedDate = sdf.format(mostRecentDate);
            return formattedDate + " (" + moodEntries.size() + " pixels)";
        }
        else {
            return getString(R.string.never);
        }
    }


    private void updatePixelFile(Uri fileUri) {

        String jsonContent = readJsonFile(fileUri);

        try {
            File outputFile = new File(getFilesDir(), "backup.json");
            FileWriter writer = new FileWriter(outputFile);
            writer.write(jsonContent);
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        readTextFromFile();
    }


    public void selectJsonFile(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_file)), PICK_JSON_FILE_REQUEST_CODE);
    }

    private String readJsonFile(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            inputStream.close();

            return stringBuilder.toString();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void readTextFromFile() {
        String mostRecentDate = getPixelsLastEntryDate();

        TextView fileNameTextView = findViewById(R.id.importButtonTextView);
        fileNameTextView.setText(getString(R.string.last_backup) + mostRecentDate);
    }


    private void updateMinPixelsSlider(int progress) {
        float sliderValue = 1 + (progress / 10f);
        minPixelValueTextView.setText(getString(R.string.minimal_value) + sliderValue);
    }

    private void updateNumberPixelsSlider(int progress) {
        if (progress < 10) {
            maxMemoriesTextView.setText(getString(R.string.maximal_number) + progress);
        }
        else {
            maxMemoriesTextView.setText(getString(R.string.maximal_number) + "∞");
        }
    }


    private void saveSettings() {
        int hour = timeNotification.getHour();
        int minute = timeNotification.getMinute();

        int sliderMinValue = minPixelValueSlider.getProgress();
        int sliderMaxValue = minPixelValueSlider.getProgress();

        // Enregistrer les nouvelles valeurs des paramètres dans SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("Parametres", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("notification_hour", hour);
        editor.putInt("notification_minute", minute);
        editor.putInt("min_pixels_value", sliderMinValue);
        editor.putInt("max_memories_value", sliderMaxValue);
        editor.apply();
    }


    private void loadSettings() {
        readTextFromFile();

        SharedPreferences sharedPreferences = getSharedPreferences("Parametres", Context.MODE_PRIVATE);
        int notificationHour = sharedPreferences.getInt("notification_hour", 20);
        int notificationMinute = sharedPreferences.getInt("notification_minute", 0);
        int sliderMinValue = sharedPreferences.getInt("min_pixels_value", 0);
        int sliderNumberValue = sharedPreferences.getInt("min_pixels_value", 0);

        minPixelValueSlider.setProgress(sliderMinValue);
        updateMinPixelsSlider(sliderMinValue);

        maxMemoriesSlider.setProgress(sliderNumberValue);
        updateNumberPixelsSlider(sliderNumberValue);

        timeNotification.setHour(notificationHour);
        timeNotification.setMinute(notificationMinute);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_JSON_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri fileUri = data.getData();
                updatePixelFile(fileUri);
            }
        }
    }
}