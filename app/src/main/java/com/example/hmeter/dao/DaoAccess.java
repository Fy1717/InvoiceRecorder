/*package com.example.hmeter.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import androidx.lifecycle.LiveData;

import com.example.hmeter.model.Invoice;

import java.util.List;

@Dao
public interface DaoAccess {
    @Insert
    Long insertInvoice(Invoice invoice);


    @Query("SELECT * FROM Invoice ORDER BY id")
    LiveData<List<Invoice>> fetchAllInvoices();


    @Query("SELECT * FROM Invoice WHERE id =:id")
    LiveData<Invoice> getInvoice(int id);
}
*/
