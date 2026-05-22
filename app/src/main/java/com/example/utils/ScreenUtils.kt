package com.example.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object ScreenUtils {

    fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun formatDateToLocal(dateStr: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = parser.parse(dateStr) ?: return dateStr
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            formatter.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatToIdr(value: Double): String {
        return try {
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            format.minimumFractionDigits = 0
            format.maximumFractionDigits = 0
            val formatted = format.format(value)
            formatted.replace("Rp", "Rp ")
        } catch (e: Exception) {
            "Rp " + String.format(Locale.getDefault(), "%,.0f", value)
        }
    }

    fun evaluateMathExpression(expr: String): Double? {
        val sanitized = expr.replace(" ", "").replace("x", "*").replace("X", "*")
        return try {
            object : Any() {
                var pos = -1
                var ch = 0

                fun nextChar() {
                    ch = if (++pos < sanitized.length) sanitized[pos].code else -1
                }

                fun eat(charToEat: Int): Boolean {
                    while (ch == ' '.code) nextChar()
                    if (ch == charToEat) {
                        nextChar()
                        return true
                    }
                    return false
                }

                fun parse(): Double {
                    nextChar()
                    val x = parseExpression()
                    if (pos < sanitized.length) throw RuntimeException("Unexpected: " + ch.toChar())
                    return x
                }

                fun parseExpression(): Double {
                    var x = parseTerm()
                    while (true) {
                        if (eat('+'.code)) x += parseTerm()
                        else if (eat('-'.code)) x -= parseTerm()
                        else return x
                    }
                }

                fun parseTerm(): Double {
                    var x = parseFactor()
                    while (true) {
                        if (eat('*'.code)) x *= parseFactor()
                        else if (eat('/'.code)) x /= parseFactor()
                        else return x
                    }
                }

                fun parseFactor(): Double {
                    if (eat('+'.code)) return parseFactor()
                    if (eat('-'.code)) return -parseFactor()

                    var x: Double
                    val startPos = pos
                    if (eat('('.code)) {
                        x = parseExpression()
                        eat(')'.code)
                    } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) {
                        while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                        x = java.lang.Double.parseDouble(sanitized.substring(startPos, pos))
                    } else {
                        throw RuntimeException("Unexpected: " + ch.toChar())
                    }
                    return x
                }
            }.parse()
        } catch (e: Exception) {
            null
        }
    }
}
