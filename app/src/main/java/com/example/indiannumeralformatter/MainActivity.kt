@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.indiannumeralformatter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.indiannumeralformatter.ui.theme.IndianNumeralFormatterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IndianNumeralFormatterTheme {
                AppContent()
            }
        }
    }
}

enum class ConverterScreen {
    NUMBER_TO_WORDS,
    WORDS_TO_NUMBER
}

@Composable
fun AppContent() {
    var currentScreen by remember { mutableStateOf(ConverterScreen.NUMBER_TO_WORDS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_title)) },
                actions = {
                    Button(onClick = {
                        currentScreen = when (currentScreen) {
                            ConverterScreen.NUMBER_TO_WORDS -> ConverterScreen.WORDS_TO_NUMBER
                            ConverterScreen.WORDS_TO_NUMBER -> ConverterScreen.NUMBER_TO_WORDS
                        }
                    }) {
                        Text(
                            text = stringResource(
                                if (currentScreen == ConverterScreen.NUMBER_TO_WORDS) R.string.words_to_number_button else R.string.number_to_words_button
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                ConverterScreen.NUMBER_TO_WORDS -> NumberToWordsUI()
                ConverterScreen.WORDS_TO_NUMBER -> WordsToNumberUI()
            }
        }
    }
}

@Composable
fun NumberToWordsUI() {
    var numberInput by remember { mutableStateOf("") }
    var wordsOutput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.number_to_words_title),
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = numberInput,
                onValueChange = { numberInput = it },
                label = { Text(stringResource(R.string.enter_number_hint)) },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = {
                wordsOutput = convertToIndianWords(numberInput)
            }) {
                Text(stringResource(R.string.convert_button_label))
            }

            if (wordsOutput.isNotEmpty()) {
                Text(
                    text = wordsOutput,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun WordsToNumberUI() {
    var wordsInput by remember { mutableStateOf(TextFieldValue()) }
    var numberOutput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.words_to_number_title),
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = wordsInput,
                onValueChange = { wordsInput = it },
                label = { Text(stringResource(R.string.enter_words_hint)) },
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = {
                numberOutput = convertWordsToIndianNumber(wordsInput.text)
            }) {
                Text(stringResource(R.string.convert_button_label))
            }

            if (numberOutput.isNotEmpty())
                Text(
                    text = numberOutput,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
        }
    }
}

fun convertToIndianWords(number: String): String {
    val parts = number.split(".")
    val integerPart = parts[0].toLongOrNull() ?: return "Invalid number"
    val decimalPart = if (parts.size > 1) parts[1] else ""

    fun formatWithIndianCommas(num: Long): String {
        val numStr = num.toString()
        if (numStr.length <= 3) return numStr
        val lastThree = numStr.takeLast(3)
        val other = numStr.dropLast(3)
        val grouped = other.reversed().chunked(2).joinToString(",").reversed()
        return "$grouped,$lastThree"
    }

    val units = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    )

    val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    fun twoDigits(num: Int): String {
        return when {
            num < 20 -> units[num]
            else -> tens[num / 10] + if (num % 10 != 0) " " + units[num % 10] else ""
        }
    }

    fun threeDigits(num: Int): String {
        return if (num >= 100)
            units[num / 100] + " Hundred" + if (num % 100 != 0) " " + twoDigits(num % 100) else ""
        else
            twoDigits(num)
    }

    val partsInteger = arrayOf(
        integerPart % 1000,                         // Hundreds
        (integerPart / 1_000) % 100,                // Thousands
        (integerPart / 1_00_000) % 100,             // Lakhs
        (integerPart / 1_00_00_000) % 100,          // Crores
        (integerPart / 1_00_00_00_000) % 100,       // Arabs
        (integerPart / 1_00_00_00_00_000) % 100     // Kharabs
    )

    val labels = arrayOf("", "Thousand", "Lakh", "Crore", "Arab", "Kharab")

    val wordsOutput = StringBuilder()

    for (index in partsInteger.indices.reversed()) {
        if (partsInteger[index] != 0L) {
            val word =
                if (index == 0) threeDigits(partsInteger[index].toInt()) else twoDigits(partsInteger[index].toInt())
            wordsOutput.append("$word ${labels[index]} ")
        }
    }

    if (decimalPart.isNotEmpty()) {
        wordsOutput.append("Point")
        for (decimalDigit in decimalPart) {
            val digitWord = units[decimalDigit.toString().toInt()]
            wordsOutput.append(" $digitWord")
        }
    }

    val wordsOnly = wordsOutput.toString().trim()
    val formattedInteger = formatWithIndianCommas(integerPart)
    val formattedNumber =
        if (decimalPart.isNotEmpty()) "$formattedInteger.$decimalPart" else formattedInteger

    return "$wordsOnly ($formattedNumber)"
}

fun convertWordsToIndianNumber(words: String): String {
    val wordMap = mapOf(
        "zero" to 0, "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5,
        "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10,
        "eleven" to 11, "twelve" to 12, "thirteen" to 13, "fourteen" to 14, "fifteen" to 15,
        "sixteen" to 16, "seventeen" to 17, "eighteen" to 18, "nineteen" to 19,
        "twenty" to 20, "thirty" to 30, "forty" to 40, "fifty" to 50,
        "sixty" to 60, "seventy" to 70, "eighty" to 80, "ninety" to 90
    )

    val multipliers = mapOf(
        "hundred" to 100,
        "thousand" to 1_000,
        "lakh" to 1_00_000,
        "crore" to 1_00_00_000,
        "arab" to 1_00_00_00_000,
        "kharab" to 1_00_00_00_00_000
    )

    val tokens = words.lowercase().replace("-", " ").split(" ").filter { it.isNotBlank() }

    var total = 0L
    var current = 0L
    var isInDecimalPart = false
    var decimalNumber = ""

    for (token in tokens) {
        when {
            token == "point" -> {
                isInDecimalPart = true
            }

            isInDecimalPart -> {
                val decimalDigit = wordMap[token]
                if (decimalDigit != null) {
                    decimalNumber += decimalDigit
                } else {
                    return "Invalid word in decimal part: $token"
                }
            }

            wordMap.containsKey(token) -> {
                current += wordMap[token]!!.toLong()
            }

            multipliers.containsKey(token) -> {
                val factor = multipliers[token]!!
                if (current == 0L) current = 1
                current *= factor
                total += current
                current = 0
            }

            else -> return "Invalid word: $token"
        }
    }

    total += current
    val formattedNumber =
        if (decimalNumber.isNotEmpty()) "$total.$decimalNumber" else total.toString()

    return formatIndianNumber(formattedNumber)
}

fun formatIndianNumber(originalNumber: String): String {
    val parts = originalNumber.split(".")
    val number = parts[0]
    val decimal = if (parts.size > 1) ".${parts[1]}" else ""

    if (number.length <= 3) return number + decimal

    val lastThree = number.takeLast(3)
    val remaining = number.dropLast(3)
    val formattedNumber = remaining.reversed().chunked(2).joinToString(",").reversed()
    return "$formattedNumber,$lastThree$decimal"
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewAppContent() {
    MaterialTheme {
        AppContent()
    }
}