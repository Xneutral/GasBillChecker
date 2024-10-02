package com.rashid.gasbillchecker.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.rashid.gasbillchecker.R
import com.rashid.gasbillchecker.databinding.ActivityViewBillBinding
import com.rashid.gasbillchecker.helper.Constants
import com.rashid.gasbillchecker.model.BillModel
import com.rashid.gasbillchecker.viewmodel.BillViewModel
import kotlinx.coroutines.flow.collect
import org.json.JSONObject


class ViewBillActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewBillBinding

    private lateinit var billVM: BillViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewBillBinding.inflate(layoutInflater)
        billVM = ViewModelProvider(this)[BillViewModel::class.java]
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUI()
        handleBackPress()
    }

    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupUI() {
        try {
            binding.billWebView.settings.javaScriptEnabled = true
            binding.billWebView.addJavascriptInterface(WebAppInterface(this), "Android")

            binding.billWebView.settings.setSupportZoom(true)
            binding.billWebView.settings.builtInZoomControls = true
            binding.billWebView.invokeZoomPicker()

            val accountId = intent.getStringExtra(Constants.AccountIdKey)
            val company = intent.getStringExtra(Constants.Company_key)

            if (company == Constants.SNGPL_KEY) {
                binding.billWebView
                    .loadUrl(Constants.SNGPL_Base_Url.replace(Constants.delimiter, accountId!!))

                loadJavaScriptData()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error while fetching bill", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadJavaScriptData() {


        binding.billWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {


                // Execute the JavaScript to extract the required data
                binding.billWebView.evaluateJavascript(
                    """
            (function() {
                var extractedData = {};
                
                var accountIdRow = Array.from(document.querySelectorAll('tr')).find(row => row.innerText.includes('Account ID'));
                if (accountIdRow) { extractedData.accountId = accountIdRow.querySelector('td:last-child').innerText.trim(); }
                
                // Send the data to Android
                Android.sendData(JSON.stringify(extractedData));
            })();
            """.trimIndent(), null
                )
            }
        }


    }

    fun extractBillingInfo(input: String): Map<String, String> {
        val extractedData = mutableMapOf<String, String>()

        // Regex patterns for the required fields
        val accountIdRegex = Regex("""Account ID\s+صارف نمبر\s+(\d{11})""")
        val billingMonthRegex = Regex("""Billing Month\s+بلنگ کا مہینہ\s+(\w+\s+\d{4})""")
        val payableAmountRegex =
            Regex("""Payable within due date\(Rs\.\)\s+واجب الادارقم\s+([\d,]+)""")
        val dueDateRegex = Regex("""Due Date\s+آخری تاریخ ادائیگی\s+(\d{2}-\d{2}-\d{4})""")
        val amountAfterDueDateRegex =
            Regex("""Amount after due date\(Rs\.\)\s+کل رقم بعدازتاریخ ادائیگی\s+([\d,]+)""")
        val nameRegex = Regex("""Name:\s+نام\s+([A-Za-z\s]+)(?=\s+S/O)""")


        // Extract each field using the corresponding regex
        val accountIdMatch = accountIdRegex.find(input)
        if (accountIdMatch != null) {
            extractedData[Constants.accountIdMapKey] = accountIdMatch.groupValues[1]
        }

        val billingMonthMatch = billingMonthRegex.find(input)
        if (billingMonthMatch != null) {
            extractedData[Constants.billingMonthMapKey] = billingMonthMatch.groupValues[1]
        }

        val payableAmountMatch = payableAmountRegex.find(input)
        if (payableAmountMatch != null) {
            extractedData[Constants.payableMapKey] = payableAmountMatch.groupValues[1]
        }

        val dueDateMatch = dueDateRegex.find(input)
        if (dueDateMatch != null) {
            extractedData[Constants.DueDateMapKey] = dueDateMatch.groupValues[1]
        }

        val amountAfterDueDateMatch = amountAfterDueDateRegex.find(input)
        if (amountAfterDueDateMatch != null) {
            extractedData[Constants.AmountAfterMapKey] = amountAfterDueDateMatch.groupValues[1]
        }

        val nameMatch = nameRegex.find(input)
        if (nameMatch != null) {
            extractedData[Constants.NameMapKey] = nameMatch.groupValues[1]
        }

        return extractedData
    }


    class WebAppInterface(private val context: Context) {

        @JavascriptInterface
        fun sendData(data: String) {
            val jsonData = JSONObject(data)
            val billingInfo = jsonData.optString("accountId", "N/A")

            val billInfo = (context as ViewBillActivity).extractBillingInfo(billingInfo)
            billInfo.forEach { (key, value) ->
                Log.d("ExtractedData", "key $key, value -> $value")
            }

            val billModel = BillModel(
                id = 0,
                consumerName = billInfo[Constants.NameMapKey] ?: return,
                consumerNumber = billInfo[Constants.accountIdMapKey] ?: return,
                dueDate = billInfo[Constants.DueDateMapKey] ?: return,
                payableAmount = billInfo[Constants.payableMapKey] ?: return,
                payableWithLateFee = billInfo[Constants.AmountAfterMapKey] ?: return,
            )

            Log.d("DatabaseInsertion", "Inserting bill: $billModel")
            context.insertBillModel(billModel)
        }


    }

    fun insertBillModel(billModel: BillModel) {
        billVM.insertBill(this, billModel)
    }

}