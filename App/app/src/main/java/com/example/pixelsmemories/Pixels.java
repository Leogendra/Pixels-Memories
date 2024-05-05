package com.example.pixelsmemories;

import java.util.List;

public class Pixels {
    public static class MoodEntry {
        private String date;
        private String type;
        private List<Integer> scores;
        private String notes;
        private List<Tag> tags;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Integer> getScores() {
            return scores;
        }

        public void setScores(List<Integer> scores) {
            this.scores = scores;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public List<Tag> getTags() {
            return tags;
        }

        public void setTags(List<Tag> tags) {
            this.tags = tags;
        }
    }

    public static class Tag {
        private String type;

        private List<String> entries;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<String> getEntries() {
            return entries;
        }

        public void setEntries(List<String> entries) {
            this.entries = entries;
        }
    }
}
