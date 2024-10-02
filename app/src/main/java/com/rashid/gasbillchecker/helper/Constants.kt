package com.rashid.gasbillchecker.helper

object Constants {

    val AccountIdKey = "user_account_id"

    val Company_key = "company_key"

    val SNGPL_KEY = "SNGPL"
    val SSGPL_KEY = "SSGPL"

    val accountIdMapKey = "Account ID"
    val NameMapKey = "Name"
    val AmountAfterMapKey = "Amount after due date"
    val DueDateMapKey = "Due Date"
    val billingMonthMapKey = "Billing Month"
    val payableMapKey = "Payable within due date"

    val delimiter = "####"
    val SNGPL_Base_Url = "https://www.sngpl.com.pk/viewbill?proc=viewbill&contype=NewCon&mdids=85&consumer=$delimiter&proc=viewbill"

}