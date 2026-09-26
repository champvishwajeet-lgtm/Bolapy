package com.bolpay.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class PaymentAdapter(private val list: List<PaymentModel>) :
    RecyclerView.Adapter<PaymentAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val amount: TextView = v.findViewById(R.id.tvAmount)
        val source: TextView = v.findViewById(R.id.tvSource)
        val time: TextView = v.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_payment, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = list[position]
        holder.amount.text = "₹${p.amount}"
        holder.source.text = p.source
        holder.time.text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(p.time))
    }

    override fun getItemCount() = list.size
}
