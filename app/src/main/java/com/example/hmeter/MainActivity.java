package com.example.hmeter;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private EditText serviceNumberEditText, meterReadingEditText;
    private Button submitButton, showDetailsButton;
    private DatabaseHelper dbHelper;
    private Controller configTable;
    String serviceNumber, meterReading;
    // ROOM DB
    // private InvoiceRepository invoiceRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ROOM DB
        // invoiceRepository = new InvoiceRepository(getApplicationContext());

        dbHelper = new DatabaseHelper(this);

        configTable = new Controller();

        serviceNumberEditText = findViewById(R.id.serviceNumberEditText);
        meterReadingEditText = findViewById(R.id.meterReadingEditText);
        submitButton = findViewById(R.id.submitButton);
        showDetailsButton = findViewById(R.id.detailsButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetails(false);
            }
        });

        showDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetails(true);
            }
        });
    }

    private void saveData(String serviceNumber, String meterReading) {
        if (meterValidation(serviceNumber, Integer.parseInt(meterReading))) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            db.execSQL("INSERT INTO readings (service_number, meter_reading) VALUES (?, ?)", new String[]{serviceNumber, meterReading});

            // ROOM DB
            // invoiceRepository.insertInvoice(serviceNumber, meterReading);

            Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show();
        }

        meterReadingEditText.setText("");
    }

    public void showDetails(Boolean forDetails) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        StringBuilder details = new StringBuilder();

        serviceNumber = serviceNumberEditText.getText().toString().trim();
        meterReading = meterReadingEditText.getText().toString().trim();

        if (forDetails) {
            /*
            // ROOM DB

            for (Invoice invoice : invoiceRepository.getInvoices()) {
                System.out.println(invoice.getServiceNumber() + " " + invoice.getMeter() + " ");
            }
             */

            if(!inputUIController(serviceNumber, meterReading, false)) {
                return;
            }

            Cursor cursor = db.rawQuery("SELECT * FROM readings WHERE service_number = ? ORDER BY _id DESC LIMIT 3",
                    new String[]{serviceNumber});

            int idIndex = cursor.getColumnIndexOrThrow("_id");
            int serviceNumberIndex = cursor.getColumnIndexOrThrow("service_number");
            int meterReadingIndex = cursor.getColumnIndexOrThrow("meter_reading");

            if (cursor.moveToFirst()) {
                do {
                    String _id = cursor.getString(idIndex);
                    String serviceNumber = cursor.getString(serviceNumberIndex);
                    String meterReading = cursor.getString(meterReadingIndex);

                    double previousRecord = configTable.getPreviousReading(dbHelper, serviceNumber,_id);
                    double currentRecord = Double.parseDouble(meterReading);

                    details.append("ID : ").append(_id).append("\n");
                    details.append("Service Number: ").append(serviceNumber).append("\n");
                    details.append("Meter Reading: ").append(meterReading).append("\n");
                    details.append("Previous Reading: ").append(previousRecord).append("\n");

                    double consumption = currentRecord - previousRecord;
                    double cost = configTable.calculateCost(consumption);

                    Log.i("PREVIOUS VALUE", String.valueOf(previousRecord));
                    Log.i("CURRENT VALUE", String.valueOf(currentRecord));
                    Log.i("DIFF VALUE", String.valueOf(previousRecord));
                    Log.i("RESULT VALUE", String.valueOf(cost));

                    details.append("Consumption: ").append(consumption).append("\n");
                    details.append("Cost: ").append(cost + " $").append("\n\n");
                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();

            showDialog(details.toString(), forDetails);
        } else {
            if(!inputUIController(serviceNumber, meterReading, true)) {
                return;
            } else {
                details.append("Service Number: ").append(serviceNumber).append("\n");
                details.append("Meter Reading: ").append(meterReading).append("\n");
            }

            showDialog(details.toString(), forDetails);
        }
    }

    public Boolean meterValidation(String serviceNumber, int meterReadingIndex) {
        double topRecord = 0;

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT meter_reading FROM readings WHERE service_number = ? ORDER BY meter_reading DESC LIMIT 1",
                new String[]{serviceNumber});

        if (cursor.moveToFirst()) {
            int meterIndex = cursor.getColumnIndexOrThrow("meter_reading");

            topRecord = cursor.getDouble(meterIndex);
        }

        double currentRecord = Double.parseDouble(meterReading);

        if (meterReadingIndex < 0) {
            Toast.makeText(this, "Read value cannot be less than 0", Toast.LENGTH_LONG).show();

            return false;
        } else if (topRecord > currentRecord) {
            Toast.makeText(this, "The value read cannot be smaller than the previous value.", Toast.LENGTH_LONG).show();

            return false;
        }

        return true;
    }

    private void showDialog(String results, Boolean forDetails) {
        MainActivity.this.runOnUiThread(() -> {
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.bottom_sheet_word_search);

            TextView resultTextInDialog = dialog.findViewById(R.id.result_of_dialog);

            Button saveButton = dialog.findViewById(R.id.save_button);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveData(serviceNumber, meterReading);
                    dialog.dismiss();

                    showDetails(true);
                }
            });

            if (forDetails) {
                saveButton.setVisibility(View.GONE);
            } else {
                saveButton.setVisibility(View.VISIBLE);
            }

            if (results.isEmpty()) {
                resultTextInDialog.setText("There is no result..");
                saveButton.setVisibility(View.GONE);
            } else {
                resultTextInDialog.setText(results);
            }

            dialog.show();
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.getWindow().setGravity(Gravity.BOTTOM);
        });
    }

    public Boolean inputUIController(String serviceNumber, String meterReading, Boolean forSave) {
        if (serviceNumber.isEmpty() && meterReading.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show();
            return false;
        } else if (serviceNumber.isEmpty()) {
            Toast.makeText(this, "Please fill Service Number area field", Toast.LENGTH_LONG).show();
            return false;
        } else if (meterReading.isEmpty() && forSave) {
            Toast.makeText(this, "Please fill Meter Number area field", Toast.LENGTH_LONG).show();
            return false;
        } else if (serviceNumber.length() != 10) {
            Toast.makeText(this, "Service Number area size should be 10", Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }
}
