package org.example;
import java.time.Instant;
import java.util.Random;
import com.google.gson.Gson;
import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class FitnessDataGenerator {

    private static final Random random = new Random();
    private static final double LATITUDE_MIN = 55.5;
    private static final double LATITUDE_MAX = 55.9;
    private static final double LONGITUDE_MIN = 37.4;
    private static final double LONGITUDE_MAX = 37.8;
    private static final String[] activityTypes = {"sedentary", "walking", "running", "cycling", "sleeping"};
    private static final Map<String, PreviousValues> previousValues = new HashMap<>();

    private static final List<String> userIds = generateUserIds(10000); // Список user_id



    public static class FitnessData {
        public String timestamp;
        public String user_id;
        public int heart_rate;
        public double latitude;
        public double longitude;
        public int steps;
        public int battery_level;
        public String activity_type;

        public FitnessData(String timestamp, String user_id, int heart_rate, double latitude, double longitude, int steps, int battery_level, String activity_type) {
            this.timestamp = timestamp;
            this.user_id = user_id;
            this.heart_rate = heart_rate;
            this.latitude = latitude;
            this.longitude = longitude;
            this.steps = steps;
            this.battery_level = battery_level;
            this.activity_type = activity_type;
        }

        @Override
        public String toString() {
            return new Gson().toJson(this);
        }
    }
    private static class PreviousValues {
        public double latitude;
        public double longitude;
        public int heartRate;
        public int steps;
        public int batteryLevel;
    }

    private static List<String> generateUserIds(int count) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String userId = "user_" + i;
            ids.add(userId);
            previousValues.put(userId, new PreviousValues());
        }
        return ids;
    }

    public static FitnessData generateFitnessData(String userId) {
        PreviousValues prev = previousValues.get(userId);
        if (prev == null) {
            prev = new PreviousValues();
            previousValues.put(userId, prev);
        }

        String timestamp = Instant.now().toString();
        int heartRate = adjustValue(prev.heartRate, random.nextInt(11) - 5, 60, 180); // +/- 5, min 60, max 180
        double latitude = adjustValue(prev.latitude, (random.nextDouble() - 0.5) * 0.001, LATITUDE_MIN, LATITUDE_MAX); // +/- 0.0005
        double longitude = adjustValue(prev.longitude, (random.nextDouble() - 0.5) * 0.001, LONGITUDE_MIN, LONGITUDE_MAX); // +/- 0.0005
        int steps = adjustValue(prev.steps + random.nextInt(101), 0, Integer.MAX_VALUE, Integer.MAX_VALUE); // Increase steps
        int batteryLevel = adjustValue(prev.batteryLevel - random.nextInt(3), 0, 100, 100); // Decrease battery

        String activityType = activityTypes[random.nextInt(activityTypes.length)];
        FitnessData data = new FitnessData(timestamp, userId, heartRate, latitude, longitude, steps, batteryLevel, activityType);
        prev.heartRate = heartRate;
        prev.latitude = latitude;
        prev.longitude = longitude;
        prev.steps = steps;
        prev.batteryLevel = batteryLevel;

        return data;
    }
    private static int adjustValue(int value, int change, int min, int max) {
        int newValue = value + change;
        return Math.max(min, Math.min(max, newValue));
    }

    private static double adjustValue(double value, double change, double min, double max) {
        double newValue = value + change;
        return Math.max(min, Math.min(max, newValue));
    }


    public static void main(String[] args) {
        Gson gson = new Gson();
        try (PrintWriter out = new PrintWriter("fitness_data.jsonl")) {
            for (String userId : userIds) {
                PreviousValues initialValues = new PreviousValues();
                initialValues.latitude = LATITUDE_MIN + (LATITUDE_MAX - LATITUDE_MIN) * random.nextDouble();
                initialValues.longitude = LONGITUDE_MIN + (LONGITUDE_MAX - LONGITUDE_MIN) * random.nextDouble();
                initialValues.heartRate = random.nextInt(71) + 70; // 70-140 bpm
                initialValues.steps = 0;
                initialValues.batteryLevel = 100;
                previousValues.put(userId, initialValues);
            }

            while (true) {
                for (String userId : userIds) {
                    FitnessData data = generateFitnessData(userId);
                    String jsonOutput = gson.toJson(data);
                    out.println(jsonOutput);
                    System.out.println(jsonOutput);
                }
                Thread.sleep(1000);
            }
        } catch (FileNotFoundException | InterruptedException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}