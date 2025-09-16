package com.example.cargolive.utils

import android.util.Log
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException

/**
 * Utility class for date and time operations
 */
object DateTimeUtils {
    private const val TAG = "DateTimeUtils"
    
    private val DEFAULT_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMMM dd, yyyy - h:mm a")
    private val ISO_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    private val SHORT_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm")
    private val SIMPLE_FORMAT = DateTimeFormatter.ofPattern("MMM dd['st']['nd']['rd']['th'], HH:mm")
    
    /**
     * Parse a date-time string using multiple possible formats
     *
     * @param dateString The date string to parse
     * @return LocalDateTime object if parsing succeeds, null otherwise
     */
    fun parseDateTime(dateString: String): LocalDateTime? {
        return try {
            when {
                // ISO format (2023-04-28T14:30:00)
                dateString.contains("T") -> LocalDateTime.parse(dateString)
                
                // Format like "Apr 17, 2023 - 14:00"
                dateString.contains(",") && dateString.contains("-") -> {
                    try {
                        LocalDateTime.parse(dateString, SHORT_FORMAT)
                    } catch (e: Exception) {
                        // Try to extract parts manually as fallback
                        parseManually(dateString)
                    }
                }
                
                // Format like "Apr 17th, 14:00"
                dateString.contains(",") -> {
                    try {
                        LocalDateTime.parse(dateString, SIMPLE_FORMAT)
                    } catch (e: Exception) {
                        // Try to extract parts manually as fallback
                        parseManually(dateString)
                    }
                }
                
                else -> null
            }
        } catch (e: DateTimeParseException) {
            Log.e(TAG, "Date parsing error: ${e.message} for input: $dateString", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected date parsing error for input: $dateString", e)
            null
        }
    }
    
    /**
     * Format a date-time string for display
     *
     * @param dateString The date string to format
     * @return Formatted date string for display
     */
    fun formatForDisplay(dateString: String): String {
        val dateTime = parseDateTime(dateString)
        return if (dateTime != null) {
            try {
                dateTime.format(DEFAULT_DISPLAY_FORMAT)
            } catch (e: Exception) {
                Log.e(TAG, "Error formatting date for display", e)
                dateString // Return original on error
            }
        } else {
            dateString // Return original if parsing failed
        }
    }
    
    /**
     * Manual fallback parser for complex date formats
     */
    private fun parseManually(dateString: String): LocalDateTime? {
        return try {
            // Try to handle format like "Apr 17th, 14:00" or similar variations
            val parts = dateString.split(",")
            
            if (parts.size >= 2) {
                val datePart = parts[0].trim()
                val timePart = parts[1].trim()
                
                // Extract month
                val month = when (datePart.take(3).lowercase()) {
                    "jan" -> 1
                    "feb" -> 2
                    "mar" -> 3
                    "apr" -> 4
                    "may" -> 5
                    "jun" -> 6
                    "jul" -> 7
                    "aug" -> 8
                    "sep" -> 9
                    "oct" -> 10
                    "nov" -> 11
                    "dec" -> 12
                    else -> return null
                }
                
                // Extract day (removing any suffix like st, nd, rd, th)
                val dayMatch = "\\d+".toRegex().find(datePart.substring(3))
                val day = dayMatch?.value?.toIntOrNull() ?: return null
                
                // Extract hour and minute
                val timeMatch = "(\\d+):(\\d+)".toRegex().find(timePart)
                if (timeMatch != null) {
                    val hour = timeMatch.groupValues[1].toIntOrNull() ?: 0
                    val minute = timeMatch.groupValues[2].toIntOrNull() ?: 0
                    
                    // Use current year as fallback
                    val year = LocalDateTime.now().year
                    
                    return LocalDateTime.of(year, month, day, hour, minute)
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error in manual date parsing", e)
            null
        }
    }
}