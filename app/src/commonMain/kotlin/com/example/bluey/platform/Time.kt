package com.example.bluey.platform

/** Wall-clock time in milliseconds since the Unix epoch. */
expect fun nowMillis(): Long

/**
 * Formats [epochMillis] using a [pattern] in the way Android's `SimpleDateFormat` does
 * (for example "MMM d, h:mm a"), in the device's local time zone and locale.
 */
expect fun formatDate(epochMillis: Long, pattern: String): String
