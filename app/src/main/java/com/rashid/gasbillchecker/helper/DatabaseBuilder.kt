package com.rashid.gasbillchecker.helper

import android.content.Context
import androidx.room.Room
import com.rashid.gasbillchecker.roomdb.BillDatabase

object DatabaseBuilder {


    @Volatile
    private var INSTANCE: BillDatabase? = null

    fun getInstance(context: Context): BillDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                BillDatabase::class.java,
                "billing_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }

}