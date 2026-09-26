package in.bolpay.app

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var prefs: PrefsHelper
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        prefs = PrefsHelper(this)
        tts = TextToSpeech(this) { }

        val shopEt = findViewById<EditText>(R.id.etShopName)
        val langSpinner = findViewById<Spinner>(R.id.spinnerLang)
        val speedSeek = findViewById<SeekBar>(R.id.seekSpeed)
        val speedLabel = findViewById<TextView>(R.id.tvSpeedLabel)
        val btnSave = findViewById<Button>(R.id.btnSave)

        shopEt.setText(prefs.getShopName())
        val langs = arrayOf("हिंदी (Hindi)", "English")
        langSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, langs)
        langSpinner.setSelection(if (prefs.getLanguage() == "hi") 0 else 1)
        speedSeek.progress = ((prefs.getSpeed() - 0.5f) * 20).toInt()
        speedLabel.text = "Speed: ${prefs.getSpeed()}x"

        speedSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val s = 0.5f + progress / 20f
                speedLabel.text = "Speed: ${String.format("%.2f", s)}x"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        btnSave.setOnClickListener {
            prefs.setShopName(shopEt.text.toString().trim())
            prefs.setLanguage(if (langSpinner.selectedItemPosition == 0) "hi" else "en")
            prefs.setSpeed(0.5f + speedSeek.progress / 20f)
            Toast.makeText(this, "✅ सेव हो गया", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }
}
