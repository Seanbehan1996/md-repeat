package com.yourname.fitnesstracker.utils

/**
 * Formats duration in seconds to a human-readable string
 * @param seconds Duration in seconds
 * @return Formatted duration string (e.g., "1:23:45" or "5:30")
 */
fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, remainingSeconds)
        else -> String.format("%d:%02d", minutes, remainingSeconds)
    }
}