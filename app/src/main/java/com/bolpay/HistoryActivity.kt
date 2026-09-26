package com.bolpay.app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class HistoryActivity : AppCompatActivity() {
    private lateinit var prefs: PrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        prefs = PrefsHelper(this)

        val payments = prefs.getPayments()
        val today = Calendar.getInstance()
        var todaySum = 0.0
        var allSum = 0.0

        payments.forEach { p ->
            val amt = p.amount.toDoubleOrNull() ?: 0.0
            allSum += amt
            val cal = Calendar.getInstance().apply { timeInMillis = p.time }
            if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) todaySum += amt
        }

        findViewById<TextView>(R.id.todayTotal).text = "₹${fmt(todaySum)}"
        findViewById<TextView>(R.id.allTotal).text = "₹${fmt(allSum)}"

        val rv = findViewById<RecyclerView>(R.id.rvHistory)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = PaymentAdapter(payments)

        findViewById<Button>(R.id.btnClear).setOnClickListener {
            prefs.clearPayments()
            recreate()
        }
    }

    private fun fmt(d: Double): String =
        if (d % 1.0 == 0.0) d.toInt().toString() else String.format("%.2f", d)
}
