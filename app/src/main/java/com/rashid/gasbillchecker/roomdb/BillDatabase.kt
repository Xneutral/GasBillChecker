package com.rashid.gasbillchecker.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rashid.gasbillchecker.model.BillModel

@Database(entities = [BillModel::class], version = 2, exportSchema = false)
abstract class BillDatabase : RoomDatabase() {

    abstract fun billDao(): BillDao

}