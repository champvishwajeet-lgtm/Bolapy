package com.bolpay.app

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PrefsHelper(context: Context) {
    private val prefs = context.getSharedPreferences("bolpay_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getShopName(): String = prefs.getString("shop_name", "") ?: ""
    fun setShopName(name: String) = prefs.edit().putString("shop_name", name).apply()
    fun getLanguage(): String = prefs.getString("lang", "hi") ?: "hi"
    fun setLanguage(lang: String) = prefs.edit().putString("lang", lang).apply()
    fun getSpeed(): Float = prefs.getFloat("speed", 0.95f)
    fun setSpeed(speed: Float) = prefs.edit().putFloat("speed", speed).apply()

    fun addPayment(p: PaymentModel) {
        val list = getPayments().toMutableList()
        list.add(0, p)
        val trimmed = if (list.size > 500) list.take(500) else list
        prefs.edit().putString("payments", gson.toJson(trimmed)).apply()
    }

    fun getPayments(): List<PaymentModel> {
        val json = prefs.getString("payments", "[]") ?: "[]"
        val type = object : TypeToken<List<PaymentModel>>() {}.type
        return try { gson.fromJson(json, type) } catch (e: Exception) { emptyList() }
    }

    fun clearPayments() = prefs.edit().remove("payments").apply()
}
