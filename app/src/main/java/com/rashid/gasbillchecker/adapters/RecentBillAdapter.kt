package com.rashid.gasbillchecker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rashid.gasbillchecker.databinding.RecentBillItemBinding
import com.rashid.gasbillchecker.model.BillModel

class RecentBillAdapter(
    private val recentList: List<BillModel>,
    private val delete : (billModel : BillModel) -> Unit
) : RecyclerView.Adapter<RecentBillAdapter.ViewHolder>(){
    inner class ViewHolder(val binding : RecentBillItemBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RecentBillItemBinding.inflate(LayoutInflater.from(parent.context)
            ,parent, false))
    }

    override fun getItemCount() = recentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val index = recentList[position]

        holder.binding.nameTv.text = "Name : "+index.consumerName

        holder.binding.numberTv.text = "Consumer No: "+index.consumerNumber

        holder.binding.dueDateTv.text = "Due Date : "+ index.dueDate

        holder.binding.payableTv.text= "Rs."+ index.payableAmount

        holder.binding.payableAfterDueDateTv.text = "Rs." +index.payableWithLateFee

        holder.binding.deleteIv.setOnClickListener {
            delete.invoke(index)
        }
    }
}