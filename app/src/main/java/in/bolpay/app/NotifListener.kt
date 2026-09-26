package in.bolpay.app

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class NotifListener : NotificationListenerService(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private lateinit var prefs: PrefsHelper

    private val upiApps = setOf(
        "com.paytm.android", "net.one97.paytm", "com.phonepe.app",
        "com.google.android.apps.nbu.paisa.user", "in.org.npci.upiapp",
        "com.mobikwik_new", "com.freecharge.android",
        "com.amazon.mShop.android.shopping", "com.dreamplug.androidapp"
    )

    override fun onCreate() {
        super.onCreate()
        prefs = PrefsHelper(this)
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val lang = if (prefs.getLanguage() == "hi") Locale("hi", "IN") else Locale.US
            tts?.language = lang
            tts?.setSpeechRate(prefs.getSpeed())
            tts?.setPitch(1.0f)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val pkg = sbn.packageName ?: return
        if (pkg !in upiApps) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
        val fullText = "$title $text $bigText".trim()

        Log.d("Bolpay", "$pkg : $fullText")
        if (!isCreditNotification(fullText)) return
        val amount = extractAmount(fullText) ?: return
        saveAndAnnounce(amount, pkg)
    }

    private fun isCreditNotification(text: String): Boolean {
        val t = text.lowercase()
        val credit = listOf("received", "credited", "you got", "you have got",
            "प्राप्त", "जमा", "आया", "मिले", "payment received", "got rs", "added to")
        val debit = listOf("sent", "paid", "debited", "withdrawn", "transfer to",
            "भेजा", "दिया", "काटा", "spent", "purchase", "failed")
        return credit.any { t.contains(it) } && debit.none { t.contains(it) }
    }

    private fun extractAmount(text: String): String? {
        val patterns = listOf(
            Regex("₹\\s*([\\d,]+(?:\\.\\d{1,2})?)"),
            Regex("Rs\\.?\\s*([\\d,]+(?:\\.\\d{1,2})?)", RegexOption.IGNORE_CASE),
            Regex("INR\\s*([\\d,]+(?:\\.\\d{1,2})?)", RegexOption.IGNORE_CASE),
            Regex("([\\d,]+(?:\\.\\d{1,2})?)\\s*(?:rupees|rupaye|rs\\b)", RegexOption.IGNORE_CASE),
            Regex("received.*?([\\d,]+(?:\\.\\d{1,2})?)", RegexOption.IGNORE_CASE),
            Regex("([\\d,]+)\\s*(?:प्राप्त|रुपये)")
        )
        for (p in patterns) {
            val m = p.find(text) ?: continue
            val raw = m.groupValues[1].replace(",", "").trim()
            val num = raw.toDoubleOrNull() ?: continue
            if (num > 0 && num < 1000000) {
                return if (num % 1.0 == 0.0) num.toInt().toString()
                       else String.format("%.2f", num)
            }
        }
        return null
    }

    private fun saveAndAnnounce(amount: String, pkg: String) {
        val appName = when (pkg) {
            "com.paytm.android", "net.one97.paytm" -> "Paytm"
            "com.phonepe.app" -> "PhonePe"
            "com.google.android.apps.nbu.paisa.user" -> "GPay"
            "in.org.npci.upiapp" -> "BHIM"
            else -> "UPI"
        }
        prefs.addPayment(PaymentModel(amount, appName, System.currentTimeMillis()))
        val shopName = prefs.getShopName()
        val msg = if (prefs.getLanguage() == "hi") {
            if (shopName.isNotEmpty()) "$shopName में $amount रुपये प्राप्त हुए"
            else "$amount रुपये प्राप्त हुए"
        } else {
            if (shopName.isNotEmpty()) "$amount rupees received at $shopName"
            else "$amount rupees received"
        }
        tts?.speak(msg, TextToSpeech.QUEUE_FLUSH, null, "b_${System.currentTimeMillis()}")
    }

    override fun onDestroy() {
        tts?.stop(); tts?.shutdown()
        super.onDestroy()
    }
}
