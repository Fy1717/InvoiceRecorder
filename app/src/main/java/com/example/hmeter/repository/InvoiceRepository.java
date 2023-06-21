/*package com.example.hmeter.repository;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;

import com.example.hmeter.db.InvoiceDatabase;
import com.example.hmeter.model.Invoice;

import java.util.List;

public class InvoiceRepository {
    private String DB_NAME = "db_invoice";

    private InvoiceDatabase invoiceDatabase;
    public InvoiceRepository(Context context) {
        invoiceDatabase = Room.databaseBuilder(context, InvoiceDatabase.class, DB_NAME).build();
    }

    public void insertInvoice(String serviceNumber,
                           String meter) {

        Invoice invoice = new Invoice();
        invoice.setServiceNumber(serviceNumber);
        invoice.setMeter(meter);

        insertInvoice(invoice);
    }

    public void insertInvoice(final Invoice invoice) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                invoiceDatabase.daoAccess().insertInvoice(invoice);

                return null;
            }
        }.execute();
    }


    public LiveData<Invoice> getInvoice(int id) {
        return invoiceDatabase.daoAccess().getInvoice(id);
    }

    public List<Invoice> getInvoices() {
        return invoiceDatabase.daoAccess().fetchAllInvoices().getValue();
    }
}
*/