package com.hashinology.cryptoalgorithmsapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputText: EditText = findViewById(R.id.inputText)
        val algorithmSpinner: Spinner = findViewById(R.id.algorithmSpinner)
        val encryptButton: Button = findViewById(R.id.encryptButton)
        val outputText: TextView = findViewById(R.id.outputText)

        val algorithms = arrayOf(
            "Shift Cipher", "Monoalphabetic Cipher", "Affine Cipher",
            "Playfair Cipher", "Vigenere Cipher", "Vernam Cipher", "SHA-1", "DES"
        )

        algorithmSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, algorithms)

        encryptButton.setOnClickListener {
            val text = inputText.text.toString()
            val algorithm = algorithmSpinner.selectedItem.toString()
            val result = when (algorithm) {
                "Shift Cipher" -> shiftCipher(text, 3) // example shift of 3
                "Monoalphabetic Cipher" -> monoalphabeticCipher(text)
                "Affine Cipher" -> affineCipher(text, 5, 8)
                "Playfair Cipher" -> playfairCipher(text, "keyword")
                "Vigenere Cipher" -> vigenereCipher(text, "keyword")
                "Vernam Cipher" -> vernamCipher(text, "keyword")
                "SHA-1" -> sha1Hash(text)
                "DES" -> desEncrypt(text, "secret")
                else -> "Invalid algorithm"
            }
            outputText.text = result
        }
    }

    private fun shiftCipher(text: String, shift: Int): String {
        return text.map {
            when (it) {
                in 'a'..'z' -> 'a' + (it - 'a' + shift) % 26
                in 'A'..'Z' -> 'A' + (it - 'A' + shift) % 26
                else -> it
            }
        }.joinToString("")
    }

    private fun monoalphabeticCipher(text: String): String {
        /*val key = "QWERTYUIOPASDFGHJKLZXCVBNM" // A simple substitution key
        val lookup = ('A'..'Z').zip(key).toMap()

        return text.uppercase().map {
            lookup[it] ?: it
        }.joinToString("")*/
        val alphabet = ('A'..'Z').toList()
        return alphabet.shuffled(Random(System.currentTimeMillis())).joinToString("")
    }

    private fun affineCipher(text: String, a: Int, b: Int): String {
        val m = 26 // length of the alphabet
        return text.map {
            when (it) {
                in 'a'..'z' -> 'a' + ((a * (it - 'a') + b) % m)
                in 'A'..'Z' -> 'A' + ((a * (it - 'A') + b) % m)
                else -> it
            }
        }.joinToString("")
    }


    private fun playfairCipher(text: String, keyword: String): String {
        // Generating the 5x5 key matrix
        val matrix = generatePlayfairMatrix(keyword)
        // Implementing encryption logic based on the matrix
        // Example implementation only for demonstration, actual logic may vary
        return text // Replace with actual Playfair encryption logic
    }

    private fun generatePlayfairMatrix(keyword: String): Array<CharArray> {
        val matrix = Array(5) { CharArray(5) }
        val alphabet = "ABCDEFGHIKLMNOPQRSTUVWXYZ" // Note: 'J' is usually merged with 'I'
        val used = BooleanArray(26) { false }

        var i = 0
        var j = 0
        for (char in keyword.uppercase()) {
            if (!used[char - 'A']) {
                matrix[i][j] = char
                used[char - 'A'] = true
                j++
                if (j == 5) {
                    j = 0
                    i++
                }
            }
        }
        for (char in alphabet) {
            if (!used[char - 'A']) {
                matrix[i][j] = char
                used[char - 'A'] = true
                j++
                if (j == 5) {
                    j = 0
                    i++
                }
            }
        }
        return matrix
    }

    private fun vigenereCipher(text: String, keyword: String): String {
        val key = keyword.uppercase()
        return text.mapIndexed { index, char ->
            if (char.isLetter()) {
                val shift = key[index % key.length] - 'A'
                when (char) {
                    in 'a'..'z' -> 'a' + (char - 'a' + shift) % 26
                    in 'A'..'Z' -> 'A' + (char - 'A' + shift) % 26
                    else -> char
                }
            } else {
                char
            }
        }.joinToString("")
    }


    private fun vernamCipher(text: String, key: String): String {
        return text.mapIndexed { index, char ->
            val keyChar = key[index % key.length]
            char xor keyChar
        }.joinToString("") { "%02x".format(it.toInt()) }
    }

    private infix fun Char.xor(other: Char): Char {
        return this.code.xor(other.code).toChar()
    }


    private fun sha1Hash(text: String): String {
        val md = MessageDigest.getInstance("SHA-1")
        val hash = md.digest(text.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun desEncrypt(text: String, secretKey: String): String {
        val keySpec = SecretKeySpec(secretKey.toByteArray(), "DES")
        val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        val encrypted = cipher.doFinal(text.toByteArray())
        return encrypted.joinToString("") { "%02x".format(it) }
    }
}