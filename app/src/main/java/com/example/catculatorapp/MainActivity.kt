package com.example.catculatorapp

import android.media.MediaPlayer // <--- BAGONG IMPORT
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.catculatorapp.databinding.ActivityMainBinding
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var currentInput = ""
    private var previousValue = 0.0
    private var currentOperator = ""
    private var isNewInput = true
    private var lockedExpression = ""

    private var mediaPlayer: MediaPlayer? = null // <--- BAGONG VARIABLE PARA SA TUNOG

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ang meow sound
        // SIGURADUHIN NA MAY MEOW.MP3 KA SA RES/RAW FOLDER!
        mediaPlayer = MediaPlayer.create(this, R.raw.doorbellcat)

        setupButtonClicks()
    }

    // IMPORTANTE: I-release ang media player para hindi mag-leak ng memory
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun playMeow() { // <--- BAGONG FUNCTION PARA PATUGTUGIN ANG MEOW
        try {
            val mp = mediaPlayer ?: return
            if (mp.isPlaying) {
                mp.seekTo(0) // Kung mabilis magpindot, restart agad yung tunog
            }
            mp.start()
        } catch (e: Exception) {
            // Huwag mag-crash kung may problema sa tunog
            e.printStackTrace()
        }
    }

    private fun setupButtonClicks() {
        // --- Mga Number Buttons ---
        binding.btn0.setOnClickListener {
            playMeow() // <--- TUNOG DITO
            onNumberClick("0")
        }
        binding.btn1.setOnClickListener {
            playMeow()
            onNumberClick("1")
        }
        binding.btn2.setOnClickListener {
            playMeow()
            onNumberClick("2")
        }
        binding.btn3.setOnClickListener {
            playMeow()
            onNumberClick("3")
        }
        binding.btn4.setOnClickListener {
            playMeow()
            onNumberClick("4")
        }
        binding.btn5.setOnClickListener {
            playMeow()
            onNumberClick("5")
        }
        binding.btn6.setOnClickListener {
            playMeow()
            onNumberClick("6")
        }
        binding.btn7.setOnClickListener {
            playMeow()
            onNumberClick("7")
        }
        binding.btn8.setOnClickListener {
            playMeow()
            onNumberClick("8")
        }
        binding.btn9.setOnClickListener {
            playMeow()
            onNumberClick("9")
        }
        binding.btnDot.setOnClickListener {
            playMeow()
            onDotClick()
        }

        // --- Mga Operator Buttons ---
        binding.btnDivide.setOnClickListener {
            playMeow()
            onOperatorClick("÷")
        }
        binding.btnMultiply.setOnClickListener {
            playMeow()
            onOperatorClick("×")
        }
        binding.btnSubtract.setOnClickListener {
            playMeow()
            onOperatorClick("−")
        }
        binding.btnAdd.setOnClickListener {
            playMeow()
            onOperatorClick("+")
        }

        // --- Mga Action Buttons ---
        binding.btnClear.setOnClickListener {
            playMeow()
            onClearClick()
        }
        binding.btnBackspace.setOnClickListener {
            playMeow()
            onBackspaceClick()
        }
        binding.btnPercent.setOnClickListener {
            playMeow()
            onPercentClick()
        }
        binding.btnEquals.setOnClickListener {
            playMeow()
            onEqualsClick()
        }
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
        // CASE 1: May number na tina-type pa (hal. 1000). Normal na backspace.
        if (currentInput.isNotEmpty()) {
            currentInput = currentInput.dropLast(1)
            isNewInput = currentInput.isEmpty()
            refreshDisplays()
            return
        }

        // CASE 2: Nasa dulo ng operator (hal. "100+"). Burahin yung "+".
        if (lockedExpression.isNotEmpty() && currentOperator.isNotEmpty()) {
            val savedNumber = previousValue // I-save muna yung number (yung 100)

            // Burahin yung operator at i-reset yung state
            lockedExpression = ""
            currentOperator = ""
            previousValue = 0.0

            // Ibalik yung unang number (100) para ma-edit mo ulit kung gusto mo
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
            binding.tvExpression.text = "Error"
            binding.tvDisplay.text = ""
            currentInput = ""
            currentOperator = ""
            lockedExpression = ""
            isNewInput = true
            return
        }

        // i-freeze natin yung buong expression sa taas, at yung result sa baba
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