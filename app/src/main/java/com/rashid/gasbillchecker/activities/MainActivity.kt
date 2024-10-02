package com.rashid.gasbillchecker.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.rashid.gasbillchecker.R
import com.rashid.gasbillchecker.adapters.RecentBillAdapter
import com.rashid.gasbillchecker.databinding.ActivityMainBinding
import com.rashid.gasbillchecker.helper.Constants
import com.rashid.gasbillchecker.helper.DatabaseBuilder
import com.rashid.gasbillchecker.model.BillModel
import com.rashid.gasbillchecker.viewmodel.BillViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var is_SNGPL_Selected = true

    private lateinit var billVM : BillViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        billVM = ViewModelProvider(this)[BillViewModel::class.java]
        DatabaseBuilder.getInstance(this)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.hide()

        setupUI()
        clickListener()
        handleBackPress()
    }

    private fun setupUI() {
        billVM.getAllBills(this)
        lifecycleScope.launch {
            billVM.billModelFlow.collect {
                setupRV(it)
            }
        }

    }

    private fun setupRV(recentList : List<BillModel>) {
        if (recentList.isEmpty()){
            binding.emptyRecentBillsTv.visibility = View.VISIBLE
            binding.recentBillsRv.visibility = View.GONE
        }else{
            binding.recentBillsRv.visibility = View.VISIBLE
            binding.emptyRecentBillsTv.visibility = View.GONE
            binding.recentBillsRv.layoutManager = LinearLayoutManager(this)
            binding.recentBillsRv.adapter = RecentBillAdapter(recentList){it ->
                billVM.deleteBill(this,it)
            }
        }

    }

    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }



    private fun clickListener() {
        binding.searchBill.setOnClickListener {
            if (is_SNGPL_Selected) {
                searchSNGPLBill()
            } else {
                // handle ssgpl case
            }
        }

        binding.sngpl.setOnClickListener {
            is_SNGPL_Selected = true


            binding.sngpl.setBackgroundResource(R.drawable.custom_bg)
            binding.sngpl.setTextColor(ContextCompat.getColor(this,R.color.white))

            binding.ssgpl.background = null
            binding.ssgpl.setTextColor(ContextCompat.getColor(this,R.color.black))
        }

        binding.ssgpl.setOnClickListener {
            is_SNGPL_Selected = false

            binding.ssgpl.setBackgroundResource(R.drawable.custom_bgd)
            binding.ssgpl.setTextColor(ContextCompat.getColor(this,R.color.white))

            binding.sngpl.background = null
            binding.sngpl.setTextColor(ContextCompat.getColor(this,R.color.black))
        }

    }

    private fun searchSNGPLBill() {
        val accountId = binding.userAccountId.editText?.text.toString()
        if (accountId.isEmpty() || accountId.isBlank()){
            Toast.makeText(this, "Invalid Consumer Number", Toast.LENGTH_SHORT).show()
            return
        }
        Intent(this@MainActivity, ViewBillActivity::class.java).apply {
            putExtra(Constants.Company_key, Constants.SNGPL_KEY)
            putExtra(Constants.AccountIdKey, accountId)
            startActivity(this)
        }
    }


    override fun onResume() {
        super.onResume()
        setupUI()
    }
}