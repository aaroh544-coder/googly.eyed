package com.example.bluey.platform

import kotlin.random.Random

private const val HEX_DIGITS = "0123456789abcdef"

/** Random 128-bit identifier, formatted like a UUID without dashes. */
fun randomId(): String = buildString(32) {
    repeat(32) { append(HEX_DIGITS[Random.nextInt(HEX_DIGITS.length)]) }
}

/** Zero-pads [value] to two digits. */
fun twoDigits(value: Long): String = if (value < 10) "0$value" else value.toString()

/** Formats a duration in seconds as "minutes:ss". */
fun formatDuration(totalSeconds: Long): String =
    "${totalSeconds / 60}:${twoDigits(totalSeconds % 60)}"
