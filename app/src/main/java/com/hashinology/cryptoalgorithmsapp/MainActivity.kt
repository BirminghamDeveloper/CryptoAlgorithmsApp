package com.hashinology.cryptoalgorithmsapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    /*private lateinit var inputText: EditText
    private lateinit var outputText: TextView
    private lateinit var encryptButton: Button
    private lateinit var decryptButton: Button
    private lateinit var saveButton: Button
    private lateinit var loadButton: Button
    private lateinit var algorithmSpinner: Spinner*/

    private var storedSha1Hash: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputText = findViewById(R.id.inputText)
        outputText = findViewById(R.id.outputText)
        encryptButton = findViewById(R.id.encryptButton)
        decryptButton = findViewById(R.id.decryptButton)
        saveButton = findViewById(R.id.saveButton)
        loadButton = findViewById(R.id.loadButton)
        algorithmSpinner = findViewById(R.id.algorithmSpinner)

        val algorithms = arrayOf(
            "Shift Cipher", "Monoalphabetic Cipher", "Affine Cipher",
            "Playfair Cipher", "Vigenere Cipher", "Vernam Cipher", "SHA-1", "DES"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, algorithms)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        algorithmSpinner.adapter = adapter

        encryptButton.setOnClickListener {
            val text = inputText.text.toString()
            if (text.isEmpty()) {
                Toast.makeText(this, "Please enter text to encrypt", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val algorithm = algorithmSpinner.selectedItem.toString()
            val result = when (algorithm) {
                "Shift Cipher" -> shiftCipher(text, encrypt = true)
                "Monoalphabetic Cipher" -> monoalphabeticCipher(text, encrypt = true)
                "Affine Cipher" -> affineCipher(text, encrypt = true)
                "Playfair Cipher" -> playfairCipher(text, encrypt = true)
                "Vigenere Cipher" -> vigenereCipher(text, "KEYWORD", encrypt = true)
                "Vernam Cipher" -> vernamCipher(text, "KEYWORD", encrypt = true)
                "SHA-1" -> {
                    val hash = sha1Hash(text)
                    storedSha1Hash = hash
                    hash
                }
                "DES" -> desEncrypt(text)
                else -> ""
            }
            outputText.text = result
        }

        decryptButton.setOnClickListener {
            val text = inputText.text.toString()
            if (text.isEmpty()) {
                Toast.makeText(this, "Please enter text to decrypt", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val algorithm = algorithmSpinner.selectedItem.toString()
            val result = when (algorithm) {
                "Shift Cipher" -> shiftCipher(text, encrypt = false)
                "Monoalphabetic Cipher" -> monoalphabeticCipher(text, encrypt = false)
                "Affine Cipher" -> affineCipher(text, encrypt = false)
                "Playfair Cipher" -> playfairCipher(text, encrypt = false)
                "Vigenere Cipher" -> vigenereCipher(text, "KEYWORD", encrypt = false)
                "Vernam Cipher" -> vernamCipher(text, "KEYWORD", encrypt = false)
                "SHA-1" -> {
                    if (storedSha1Hash != null && sha1Hash(text) == storedSha1Hash) {
                        "Input matches the stored SHA-1 hash"
                    } else {
                        "Input does not match the stored SHA-1 hash"
                    }
                }
                "DES" -> desDecrypt(text)
                else -> ""
            }
            outputText.text = result
        }

        saveButton.setOnClickListener {
            val text = outputText.text.toString()
            if (text.isEmpty()) {
                Toast.makeText(this, "No text to save", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveToFile(text)
        }

        loadButton.setOnClickListener {
            loadFromFile()
        }
    }

    private fun shiftCipher(text: String, encrypt: Boolean): String {
        val shift = 3
        return text.map {
            if (it.isLetter()) {
                val offset = if (it.isUpperCase()) 'A' else 'a'
                val shiftAmount = if (encrypt) shift else -shift
                ((it - offset + shiftAmount + 26) % 26 + offset.toInt()).toChar()
            } else {
                it
            }
        }.joinToString("")
    }

    private fun monoalphabeticCipher(text: String, encrypt: Boolean): String {
        val alphabet = ('A'..'Z').toList()
        val shuffledAlphabet = if (encrypt) alphabet.shuffled(Random(System.currentTimeMillis())) else alphabet
        val substitutionMap = alphabet.zip(shuffledAlphabet).toMap()
        return text.uppercase().map {
            substitutionMap[it] ?: it
        }.joinToString("")
    }

    private fun affineCipher(text: String, encrypt: Boolean): String {
        val a = 5
        val b = 8
        val m = 26
        val aInv = 21 // multiplicative inverse of 5 mod 26

        return text.map {
            if (it.isLetter()) {
                val offset = if (it.isUpperCase()) 'A' else 'a'
                val x = it - offset
                val transformed = if (encrypt) {
                    (a * x + b) % m
                } else {
                    (aInv * (x - b + m)) % m
                }
                (transformed + offset.toInt()).toChar()
            } else {
                it
            }
        }.joinToString("")
    }

    private fun playfairCipher(text: String, encrypt: Boolean): String {
        // Placeholder logic for demonstration
        // You need to implement Playfair cipher here
        return text
    }

    private fun vigenereCipher(text: String, keyword: String, encrypt: Boolean): String {
        val key = keyword.uppercase()
        return text.mapIndexed { index, char ->
            if (char.isLetter()) {
                val offset = if (char.isUpperCase()) 'A' else 'a'
                val shift = if (encrypt) key[index % key.length] - 'A' else 'A' - key[index % key.length]
                ((char - offset + shift + 26) % 26 + offset.toInt()).toChar()
            } else {
                char
            }
        }.joinToString("")
    }

    private fun vernamCipher(text: String, key: String, encrypt: Boolean): String {
        return text.mapIndexed { index, char ->
            char xor key[index % key.length]
        }.joinToString("")
    }

    private infix fun Char.xor(other: Char): Char {
        return this.code.xor(other.code).toChar()
    }

    private fun sha1Hash(text: String): String {
        val md = MessageDigest.getInstance("SHA-1")
        val hash = md.digest(text.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun desEncrypt(text: String): String {
        val key = "your-key" // replace with your key
        val secretKeySpec = SecretKeySpec(key.toByteArray(), "DES")
        val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        val encrypted = cipher.doFinal(text.toByteArray())
        return encrypted.joinToString("") { "%02x".format(it) }
    }

    private fun desDecrypt(text: String): String {
        val key = "your-key" // replace with your key
        val secretKeySpec = SecretKeySpec(key.toByteArray(), "DES")
        val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        val encryptedBytes = text.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val decrypted = cipher.doFinal(encryptedBytes)
        return String(decrypted)
    }

    private fun saveToFile(text: String) {
        val fileName = "encrypted_text.txt"
        val file = File(filesDir, fileName)
        file.writeText(text)
        Toast.makeText(this, "Encrypted text saved to $fileName", Toast.LENGTH_SHORT).show()
    }

    private fun loadFromFile() {
        val fileName = "encrypted_text.txt"
        val file = File(filesDir, fileName)
        if (file.exists()) {
            val text = file.readText()
            outputText.text = text
            Toast.makeText(this, "Encrypted text loaded from $fileName", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No saved encrypted text found", Toast.LENGTH_SHORT).show()
        }
    }
}

/*
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

 */