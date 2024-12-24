package com.example.aplikasistory.data

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

object Helper {
    fun String.withDateFormat(): String {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return try {
            val date = parser.parse(this)
            date?.let { formatter.format(it) } ?: this
        } catch (e: ParseException) {
            this
        }
    }
}
