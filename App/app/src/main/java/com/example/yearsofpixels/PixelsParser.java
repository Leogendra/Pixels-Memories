package com.example.yearsofpixels;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class PixelsParser {

    public List<Pixels.MoodEntry> parsePixelsFile(Context context, String fileName) {
        Gson gson = new Gson();
        Type moodEntryListType = new TypeToken<List<Pixels.MoodEntry>>() {}.getType();
        List<Pixels.MoodEntry> moodEntries = null;

        try {
            File file = new File(context.getFilesDir(), fileName); // Chemin absolu vers le fichier
            BufferedReader br = new BufferedReader(new FileReader(file));
            moodEntries = gson.fromJson(br, moodEntryListType);
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return moodEntries;
    }
}
