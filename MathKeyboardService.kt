package com.mohammedrabie.mathkeyboard

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MathKeyboardService : InputMethodService() {

    private data class Key(val label: String, val value: String = label)

    private val pages = linkedMapOf(
        "عربي" to listOf(
            "ض ص ث ق ف غ ع ه خ ح ج د",
            "ش س ي ب ل ا ت ن م ك ط",
            "ئ ء ؤ ر لا ى ة و ز ظ"
        ),
        "English" to listOf(
            "q w e r t y u i o p",
            "a s d f g h j k l",
            "z x c v b n m"
        ),
        "أرقام" to listOf(
            "7 8 9 + − × ÷",
            "4 5 6 = ( ) ⌫",
            "1 2 3 0 . , ↵"
        ),
        "رموز" to listOf(
            "± ≠ ≈ ≤ ≥ ∞ ∝",
            "π θ Δ Σ ∑ ∫ ∂",
            "∈ ∉ ⊂ ⊆ ∩ ∪ ∅",
            "⇒ ⇔ → ← ↑ ↓ ∴"
        ),
        "أسس" to listOf(
            "x² x³ x⁴ xⁿ x⁻¹ x⁻²",
            "² ³ ⁿ ⁻¹ ⁻² ⁺",
            "أس↑ مؤشر↓",
            "^{} _{}"
        ),
        "كسور" to listOf(
            "½ ⅓ ¼ ⅔ ¾ ⅛",
            "كسر",
            "\\frac{}{}"
        ),
        "جذور" to listOf(
            "√ ∛ ∜",
            "جذر2 جذر3 جذرn",
            "\\sqrt{} \\sqrt[3]{} \\sqrt[n]{}"
        ),
        "هندسة" to listOf(
            "∠ ° △ □ ◇ ⊥ ∥",
            "sin cos tan",
            "α β γ δ λ ω",
            "مساحة محيط"
        )
    )

    private var currentPage = "أرقام"
    private lateinit var keyArea: LinearLayout
    private lateinit var rootLayout: LinearLayout

    override fun onCreateInputView(): View {
        rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(5), dp(5), dp(5), dp(5))
            setBackgroundColor(Color.rgb(232, 239, 248))
        }

        rootLayout.addView(TextView(this).apply {
            text = "مستر محمد ربيع • رياضيات بلا حدود"
            textSize = 15f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(23, 54, 93))
            setPadding(4, dp(5), 4, dp(7))
        })

        val tabScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
        }
        val tabs = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        pages.keys.forEach { page ->
            tabs.addView(makeButton(page, 12f, 44) {
                currentPage = page
                showKeys()
            }, LinearLayout.LayoutParams(dp(78), dp(42)).apply {
                setMargins(dp(2), 0, dp(2), dp(4))
            })
        }
        tabScroll.addView(tabs)
        rootLayout.addView(tabScroll)

        keyArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        rootLayout.addView(keyArea)

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        bottom.addView(makeButton("مسافة", 14f, 46) {
            currentInputConnection?.commitText(" ", 1)
        }, weightedParams())
        bottom.addView(makeButton("مسح", 14f, 46) {
            currentInputConnection?.deleteSurroundingText(1, 0)
        }, weightedParams())
        bottom.addView(makeButton("محرر المعادلات", 12f, 46) {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                Toast.makeText(this, "تعذر فتح محرر المعادلات", Toast.LENGTH_SHORT).show()
            }
        }, weightedParams())
        bottom.addView(makeButton("↵", 18f, 46) {
            currentInputConnection?.commitText("\n", 1)
        }, weightedParams())
        rootLayout.addView(bottom)

        showKeys()
        return rootLayout
    }

    private fun showKeys() {
        if (!::keyArea.isInitialized) return
        keyArea.removeAllViews()

        val rows = pages[currentPage].orEmpty()
        rows.forEach { rowText ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            val labels = rowText.split(" ")
            labels.forEach { label ->
                if (label.isNotBlank()) {
                    val value = valueFor(label)
                    row.addView(makeButton(label, keyTextSize(label), 48) {
                        insert(value)
                    }, weightedParams())
                }
            }
            keyArea.addView(row)
        }
    }

    private fun valueFor(label: String): String = when (label) {
        "أس↑" -> "^{ }"
        "مؤشر↓" -> "_{ }"
        "كسر" -> "\\frac{ }{ }"
        "بسط" -> "\\frac{ }{ }"
        "مقام" -> "\\frac{ }{ }"
        "جذر2" -> "\\sqrt{ }"
        "جذر3" -> "\\sqrt[3]{ }"
        "مساحة" -> "A="
        "محيط" -> "P="
        "مسح" -> ""
        "↵" -> "\n"
        else -> label
    }

    private fun insert(value: String) {
        val connection: InputConnection = currentInputConnection ?: return
        when (value) {
            "⌫" -> connection.deleteSurroundingText(1, 0)
            "↵", "\n" -> connection.commitText("\n", 1)
            "x²" -> connection.commitText("x^{2}", 1)
            "x³" -> connection.commitText("x^{3}", 1)
            "x⁴" -> connection.commitText("x^{4}", 1)
            "xⁿ" -> connection.commitText("x^{n}", 1)
            "x⁻¹" -> connection.commitText("x^{-1}", 1)
            "x⁻²" -> connection.commitText("x^{-2}", 1)
            "أس↑" -> connection.commitText("^{ }", 1)
            "مؤشر↓" -> connection.commitText("_{ }", 1)
            "كسر", "بسط", "مقام" -> connection.commitText("\\frac{ }{ }", 1)
            "جذر2" -> connection.commitText("\\sqrt{ }", 1)
            "جذر3" -> connection.commitText("\\sqrt[3]{ }", 1)
            "مسح" -> connection.deleteSurroundingText(1, 0)
            else -> connection.commitText(value, 1)
        }
    }

    private fun makeButton(
        label: String,
        textSize: Float,
        heightDp: Int,
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            text = label
            this.textSize = textSize
            setTextColor(Color.rgb(20, 43, 70))
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false
            minHeight = 0
            minimumHeight = 0
            minWidth = 0
            minimumWidth = 0
            gravity = Gravity.CENTER
            setPadding(dp(2), 0, dp(2), 0)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(7).toFloat()
                setStroke(dp(1), Color.rgb(180, 197, 218))
            }
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(heightDp)
            ).apply {
                setMargins(dp(2), dp(2), dp(2), dp(2))
            }
        }
    }

    private fun weightedParams() = LinearLayout.LayoutParams(
        0,
        dp(48),
        1f
    ).apply {
        setMargins(dp(2), dp(2), dp(2), dp(2))
    }

    private fun keyTextSize(label: String): Float =
        if (label.length > 5) 11f else 17f

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
