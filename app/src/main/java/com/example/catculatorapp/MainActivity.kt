package com.example.catculatorapp

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.catculatorapp.databinding.ActivityMainBinding
import java.text.NumberFormat
import java.util.Locale
import android.content.pm.ActivityInfo

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var currentInput = ""
    private var previousValue = 0.0
    private var currentOperator = ""
    private var isNewInput = true
    private var lockedExpression = ""

    private var mediaPlayer: MediaPlayer? = null
    private var mediaPlayerClear: MediaPlayer? = null
    private var mediaPlayerBackspace: MediaPlayer? = null

    private var mediaPlayerError: MediaPlayer? = null

    private var mediaPlayerEqualbtn: MediaPlayer? = null
    private var isDarkMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mediaPlayer = MediaPlayer.create(this, R.raw.doorbellcat)
        mediaPlayerClear = MediaPlayer.create(this, R.raw.ac_meow)
        mediaPlayerBackspace = MediaPlayer.create(this, R.raw.fart)
        mediaPlayerError = MediaPlayer.create(this, R.raw.error)
        mediaPlayerEqualbtn = MediaPlayer.create(this, R.raw.bonecrack)


        // BAGO: SharedPreferences - natatandaan nito yung huling
        // ginamit mong mode, kahit i-close mo yung app
        val prefs = getSharedPreferences("calc_prefs", Context.MODE_PRIVATE)
        isDarkMode = prefs.getBoolean("is_dark_mode", true)
        applyTheme()

        binding.btnThemeToggle.isChecked = isDarkMode
        binding.btnThemeToggle.setOnCheckedChangeListener { _, isChecked ->
            isDarkMode = isChecked
            prefs.edit().putBoolean("is_dark_mode", isDarkMode).apply()
            applyTheme()
        }

        setupButtonClicks()
    }

    // BAGO: ito ang naglalagay ng tamang kulay sa lahat ng views
    // depende kung dark o light mode ang current setting
    private fun applyTheme() {
        val numberButtons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9,
            binding.btnDot
        )

        if (isDarkMode) {
            binding.ivBackground.setImageResource(R.drawable.darkmodecat_bg)
            numberButtons.forEach { setButtonAppearance(it, R.drawable.btn_paw_selector, R.color.calc_white) }
            binding.tvExpression.setTextColor(ContextCompat.getColor(this, R.color.calc_white))
            binding.tvDisplay.setTextColor(ContextCompat.getColor(this, R.color.calc_light_gray))
        } else {
            binding.ivBackground.setImageResource(R.drawable.lightmodecat_bg)
            numberButtons.forEach { setButtonAppearance(it, R.drawable.btn_paw_selector_light, R.color.calc_text_dark) }
            binding.tvExpression.setTextColor(ContextCompat.getColor(this, R.color.calc_text_dark))
            binding.tvDisplay.setTextColor(ContextCompat.getColor(this, R.color.calc_text_secondary_light))
        }

        // Sync switch state without triggering listener recursively
        if (binding.btnThemeToggle.isChecked != isDarkMode) {
            binding.btnThemeToggle.isChecked = isDarkMode
        }
    }

    // BAGO: helper para hindi na natin i-uulit ang parehong 2 linya
    // (set background + set text color) sa bawat button paulit-ulit
    private fun setButtonAppearance(view: TextView, backgroundDrawableRes: Int, textColorRes: Int) {
        view.setBackgroundResource(backgroundDrawableRes)
        view.setTextColor(ContextCompat.getColor(this, textColorRes))
    }

    // ============ BUTTON PRESS ANIMATION ============

    // Ito ang gumagawa ng "paw bounce" - paliitin papuntang 95%,
    // tapos pabalikin sa 100%. Tinatawag natin ito sa bawat button click.
    private fun animatePress(view: View) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(80)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(80)
                    .start()
            }
            .start()
    }

    // BAGO: may 3rd parameter na "sound" - default ay mediaPlayer,
    // pero pwede nating i-override kapag kailangan ng ibang tunog
    // (tulad ng AC button)
    private fun onButtonTap(view: View, sound: MediaPlayer? = mediaPlayer, action: () -> Unit) {
        animatePress(view)
        playSound(sound)
        action()
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        mediaPlayerClear?.release()
        mediaPlayerClear = null
        mediaPlayerBackspace?.release()
        mediaPlayerBackspace = null
        mediaPlayerError?.release()
        mediaPlayerError = null
    }

    // BAGO: pinalitan ang dating playMeow() - tumatanggap na ngayon
    // ng "kung anong sound player" ang gagamitin, para reusable
    // ito para sa doorbellcat AT ac_meow
    private fun playSound(player: MediaPlayer?) {
        try {
            val mp = player ?: return
            if (mp.isPlaying) {
                mp.seekTo(0)
            }
            mp.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupButtonClicks() {

        binding.btn0.setOnClickListener { onButtonTap(it) { onNumberClick("0") } }
        binding.btn1.setOnClickListener { onButtonTap(it) { onNumberClick("1") } }
        binding.btn2.setOnClickListener { onButtonTap(it) { onNumberClick("2") } }
        binding.btn3.setOnClickListener { onButtonTap(it) { onNumberClick("3") } }
        binding.btn4.setOnClickListener { onButtonTap(it) { onNumberClick("4") } }
        binding.btn5.setOnClickListener { onButtonTap(it) { onNumberClick("5") } }
        binding.btn6.setOnClickListener { onButtonTap(it) { onNumberClick("6") } }
        binding.btn7.setOnClickListener { onButtonTap(it) { onNumberClick("7") } }
        binding.btn8.setOnClickListener { onButtonTap(it) { onNumberClick("8") } }
        binding.btn9.setOnClickListener { onButtonTap(it) { onNumberClick("9") } }
        binding.btnDot.setOnClickListener { onButtonTap(it) { onDotClick() } }

        binding.btnDivide.setOnClickListener { onButtonTap(it) { onOperatorClick("÷") } }
        binding.btnMultiply.setOnClickListener { onButtonTap(it) { onOperatorClick("×") } }
        binding.btnSubtract.setOnClickListener { onButtonTap(it) { onOperatorClick("−") } }
        binding.btnAdd.setOnClickListener { onButtonTap(it) { onOperatorClick("+") } }

        // BAGO: AC button gumagamit na ngayon ng mediaPlayerClear
        // sa halip na yung default na mediaPlayer
        binding.btnClear.setOnClickListener { onButtonTap(it, mediaPlayerClear) { onClearClick() } }

        binding.btnBackspace.setOnClickListener { onButtonTap(it, mediaPlayerBackspace) { onBackspaceClick() } }
        binding.btnPercent.setOnClickListener { onButtonTap(it) { onPercentClick() } }
        binding.btnEquals.setOnClickListener { onButtonTap(it, sound = null) { onEqualsClick() } }
    }

    private fun onNumberClick(number: String) {
        if (isNewInput) {
            currentInput = number
            isNewInput = false
        } else {
            currentInput += number
        }
        refreshDisplays()
    }

    private fun onDotClick() {
        if (isNewInput) {
            currentInput = "0."
            isNewInput = false
        } else if (!currentInput.contains(".")) {
            currentInput += "."
        }
        refreshDisplays()
    }

    private fun onOperatorClick(operator: String) {
        if (currentInput.isNotEmpty()) {
            previousValue = currentInput.toDouble()
        }
        currentOperator = operator
        lockedExpression = formatResult(previousValue) + operator
        currentInput = ""
        isNewInput = true
        refreshDisplays()
    }

    private fun onClearClick() {
        currentInput = ""
        previousValue = 0.0
        currentOperator = ""
        lockedExpression = ""
        isNewInput = true
        binding.tvExpression.text = "0"
        binding.tvDisplay.text = ""
    }

    private fun onBackspaceClick() {

        if (currentInput.isNotEmpty()) {
            currentInput = currentInput.dropLast(1)
            isNewInput = currentInput.isEmpty()
            refreshDisplays()
            return
        }


        if (lockedExpression.isNotEmpty() && currentOperator.isNotEmpty()) {
            val savedNumber = previousValue


            lockedExpression = ""
            currentOperator = ""
            previousValue = 0.0


            currentInput = formatResult(savedNumber)
            isNewInput = true
            refreshDisplays()
        }
    }

    private fun onPercentClick() {
        if (currentInput.isNotEmpty()) {
            val value = currentInput.toDouble() / 100
            currentInput = formatResult(value)
            refreshDisplays()
        }
    }

    private fun onEqualsClick() {
        if (currentInput.isEmpty() || currentOperator.isEmpty()) return

        val currentValue = currentInput.toDouble()
        val result = calculate(previousValue, currentOperator, currentValue)

        if (result == null) {
            playSound(mediaPlayerError)
            binding.tvExpression.text = "Error"
            binding.tvDisplay.text = ""
            currentInput = ""
            currentOperator = ""
            lockedExpression = ""
            isNewInput = true
            return
        }


        binding.tvExpression.text = lockedExpression + formatResult(currentValue)
        binding.tvDisplay.text = "= " + formatResult(result)

        currentInput = formatResult(result)
        currentOperator = ""
        lockedExpression = ""
        isNewInput = true
    }

    private fun calculate(a: Double, operator: String, b: Double): Double? {
        return when (operator) {
            "+" -> a + b
            "−" -> a - b
            "×" -> a * b
            "÷" -> if (b == 0.0) null else a / b
            else -> null
        }
    }

    private fun formatResult(value: Double): String {
        return NumberFormat.getNumberInstance(Locale.US).format(value)
    }

    private fun refreshDisplays() {
        val displayInput = formatTypedInput(currentInput)

        binding.tvExpression.text =
            if (currentOperator.isEmpty()) displayInput.ifEmpty { "0" }
            else lockedExpression + displayInput

        binding.tvDisplay.text =
            if (currentOperator.isNotEmpty() && currentInput.isNotEmpty()) {
                val second = currentInput.toDoubleOrNull()
                val live = second?.let { calculate(previousValue, currentOperator, it) }
                if (live != null) "= " + formatResult(live) else ""
            } else {
                ""
            }
    }

    private fun formatTypedInput(input: String): String {
        if (input.isEmpty()) return ""
        val parts = input.split(".")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) ".${parts[1]}" else ""

        if (integerPart.isEmpty()) return "0$decimalPart"

        val formattedInteger = try {
            NumberFormat.getNumberInstance(Locale.US).format(integerPart.toLong())
        } catch (e: NumberFormatException) {
            integerPart
        }
        return formattedInteger + decimalPart
    }
}