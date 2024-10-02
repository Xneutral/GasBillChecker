package com.rashid.gasbillchecker.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rashid.gasbillchecker.model.BillModel
import kotlinx.coroutines.flow.Flow


@Dao
interface BillDao  {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(billModel: BillModel)

    @Delete
    suspend fun deleteBill(billModel: BillModel)

    @Query("SELECT * FROM bill_table")
    fun getAllBills() : Flow<List<BillModel>>



}