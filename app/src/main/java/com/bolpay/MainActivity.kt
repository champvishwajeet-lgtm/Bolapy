package com.bolpay.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: PrefsHelper
    private lateinit var statusTv: TextView
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = PrefsHelper(this)
        statusTv = findViewById(R.id.statusTv)
        tts = TextToSpeech(this) { }

        findViewById<Button>(R.id.btnPermission).setOnClickListener {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }
        findViewById<Button>(R.id.btnTest).setOnClickListener {
            val msg = if (prefs.getLanguage() == "hi") "एक सौ रुपये प्राप्त हुए"
                      else "One hundred rupees received"
            tts?.language = if (prefs.getLanguage() == "hi") Locale("hi","IN") else Locale.US
            tts?.setSpeechRate(prefs.getSpeed())
            tts?.speak(msg, TextToSpeech.QUEUE_FLUSH, null, "test")
        }
        findViewById<Button>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val granted = flat != null && flat.contains(packageName)
        statusTv.text = if (granted) "✅ Notification Access चालू है\nBolpay काम कर रहा है"
                        else "❌ Notification Access बंद है\nनीचे वाला बटन दबाएं"
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }
}
