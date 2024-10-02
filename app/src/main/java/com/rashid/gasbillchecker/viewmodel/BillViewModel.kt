package com.rashid.gasbillchecker.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rashid.gasbillchecker.helper.DatabaseBuilder
import com.rashid.gasbillchecker.model.BillModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class BillViewModel: ViewModel() {

    private val _billModelFlow = MutableStateFlow<List<BillModel>>(emptyList())
    val billModelFlow : StateFlow<List<BillModel>> get() = _billModelFlow


    fun insertBill(context : Context, billModel: BillModel){
        viewModelScope.launch {
            val db = DatabaseBuilder.getInstance(context)
            val billdao = DatabaseBuilder.getInstance(context).billDao()
            Log.d("DB_TEST", "Database path: ${db.openHelper.writableDatabase.path}")
            billdao.insertBill(billModel)
        }
    }

    fun deleteBill(context : Context, billModel: BillModel){
        viewModelScope.launch {
            val billdao = DatabaseBuilder.getInstance(context).billDao()
            billdao.deleteBill(billModel)
        }
    }

    fun getAllBills(context: Context) {
        viewModelScope.launch {
            val billdao = DatabaseBuilder.getInstance(context).billDao()
            billdao.getAllBills().collect{
                _billModelFlow.value = it
            }
        }
    }

}