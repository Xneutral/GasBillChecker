package com.rashid.gasbillchecker.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "bill_table")
data class BillModel(
    @PrimaryKey(autoGenerate = true)
    val  id : Int? = 0,
    val consumerName : String,
    val consumerNumber : String,
    val payableAmount : String,
    val dueDate : String,
    val payableWithLateFee : String
)
