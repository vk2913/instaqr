package com.tapshare.qr

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.WindowManager
import android.widget.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class MainActivity : Activity() {
    private lateinit var qr: ImageView
    private lateinit var label: TextView
    private lateinit var user: EditText

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.attributes = window.attributes.apply { screenBrightness = 1f }
        val d = resources.displayMetrics.density
        val pad = (20 * d).toInt()
        val prefs = getSharedPreferences("instaqr", MODE_PRIVATE)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(pad, pad * 2, pad, pad)
        }
        label = TextView(this).apply { textSize = 24f; gravity = Gravity.CENTER; setTextColor(Color.BLACK) }
        root.addView(label)
        root.addView(TextView(this).apply {
            text = "Scan with your phone camera to open my Instagram"
            textSize = 15f; gravity = Gravity.CENTER; setTextColor(Color.DKGRAY)
            setPadding(0, (4 * d).toInt(), 0, (12 * d).toInt())
        })

        val w = resources.displayMetrics.widthPixels - pad * 2
        qr = ImageView(this)
        root.addView(qr, LinearLayout.LayoutParams(w, w))

        user = EditText(this).apply {
            hint = "Instagram username"
            setText(prefs.getString("user", "vshalkamat"))
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            isSingleLine = true
        }
        val saveBtn = Button(this).apply {
            text = "Save"
            setOnClickListener {
                val u = clean(user.text.toString())
                if (u.isEmpty()) { Toast.makeText(this@MainActivity, "Enter your username", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                prefs.edit().putString("user", u).apply()
                user.setText(u)
                render(u)
            }
        }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(user, LinearLayout.LayoutParams(0, -2, 2f))
        row.addView(saveBtn, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(row)

        val shareBtn = Button(this).apply {
            text = "Share link instead"
            setOnClickListener {
                val u = clean(user.text.toString())
                if (u.isEmpty()) return@setOnClickListener
                val i = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, url(u)) }
                startActivity(Intent.createChooser(i, "Share your profile"))
            }
        }
        root.addView(shareBtn)

        setContentView(ScrollView(this).apply { setBackgroundColor(Color.WHITE); addView(root) })
        render(clean(prefs.getString("user", "vshalkamat") ?: "vshalkamat"))
    }

    private fun url(u: String) = "https://www.instagram.com/$u"

    private fun clean(raw: String): String {
        var u = raw.trim().removePrefix("@")
        u = u.substringBefore("?").substringBefore("#").trimEnd('/')
        if (u.contains("instagram.com/")) u = u.substringAfter("instagram.com/").substringBefore("/")
        return u.removePrefix("@").filter { it.isLetterOrDigit() || it == '.' || it == '_' }
    }

    private fun render(u: String) {
        label.text = "@$u"
        qr.setImageBitmap(makeQr(url(u), 800))
    }

    private fun makeQr(text: String, size: Int): Bitmap {
        val m = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) for (y in 0 until size) bmp.setPixel(x, y, if (m.get(x, y)) Color.BLACK else Color.WHITE)
        return bmp
    }
}
