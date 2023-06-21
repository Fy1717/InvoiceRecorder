package com.example.hmeter;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Controller {
    private static final double[] RATE_TABLE = {5.0, 8.0, 10.0};
    public double getPreviousReading(DatabaseHelper dbHelper, String serviceNumber, String rowId) {
        double previousReading = 0.0;

        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            Cursor cursor = db.rawQuery("SELECT meter_reading FROM readings WHERE service_number = ? AND  _id < ? ORDER BY _id DESC",
                    new String[]{serviceNumber, rowId});

            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("meter_reading");
                if (columnIndex != -1) {
                    previousReading = cursor.getDouble(columnIndex);
                }
            }

            cursor.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            previousReading = 0.0;
        }

        return previousReading;
    }

    public double calculateCost(double consumption) {
        double cost = 0.0;

        if (consumption <= 100) {
            cost = consumption * RATE_TABLE[0];
        } else if (consumption > 100 && consumption <= 500) {
            cost = 100 * RATE_TABLE[0] + (consumption - 100) * RATE_TABLE[1];
        } else {
            cost = 100 * RATE_TABLE[0] + (consumption - 100) * RATE_TABLE[1] + (consumption - 400) * RATE_TABLE[2];
        }

        return cost;
    }
}
