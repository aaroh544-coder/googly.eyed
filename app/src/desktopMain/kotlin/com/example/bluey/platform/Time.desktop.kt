package com.example.bluey.platform

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun nowMillis(): Long = System.currentTimeMillis()

actual fun formatDate(epochMillis: Long, pattern: String): String =
    SimpleDateFormat(pattern, Locale.getDefault()).format(Date(epochMillis))
