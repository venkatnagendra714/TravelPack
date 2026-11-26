package uk.ac.tees.mad.s3623092.util

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TripDateUtils {

    const val USER_DATE_PATTERN = "dd/MM/yyyy"
    const val STORAGE_DATE_PATTERN = "yyyy-MM-dd"
    const val DATE_HINT = "DD/MM/YYYY"

    fun isValidUserDate(value: String): Boolean {
        return parseStrict(value, USER_DATE_PATTERN) != null
    }

    fun convertUserDateToStorage(value: String): String? {
        val date = parseStrict(value, USER_DATE_PATTERN) ?: return null
        return formatDate(date, STORAGE_DATE_PATTERN)
    }

    fun convertStorageDateToUser(value: String): String {
        val date = parseStrict(value, STORAGE_DATE_PATTERN)
            ?: return value

        return formatDate(date, USER_DATE_PATTERN)
    }

    fun normaliseDate(value: String): String? {
        val cleanValue = value.trim()

        if (cleanValue.isBlank()) {
            return null
        }

        parseStrict(cleanValue, STORAGE_DATE_PATTERN)?.let { date ->
            return formatDate(date, STORAGE_DATE_PATTERN)
        }

        parseStrict(cleanValue, USER_DATE_PATTERN)?.let { date ->
            return formatDate(date, STORAGE_DATE_PATTERN)
        }

        return null
    }

    fun isValidDateRange(
        startDate: String,
        endDate: String
    ): Boolean {
        val start = parseAnySupportedDate(startDate)
        val end = parseAnySupportedDate(endDate)

        return start != null &&
                end != null &&
                !end.before(start)
    }

    fun isCompleted(endDate: String): Boolean {
        val tripEndDate = parseAnySupportedDate(endDate)
            ?: return false

        val today = parseStrict(
            currentDate(),
            STORAGE_DATE_PATTERN
        ) ?: return false

        return tripEndDate.before(today)
    }

    fun isUpcoming(endDate: String): Boolean {
        val tripEndDate = parseAnySupportedDate(endDate)
            ?: return false

        val today = parseStrict(
            currentDate(),
            STORAGE_DATE_PATTERN
        ) ?: return false

        return !tripEndDate.before(today)
    }

    fun compareDates(
        firstDate: String,
        secondDate: String
    ): Int {
        val first = parseAnySupportedDate(firstDate)
        val second = parseAnySupportedDate(secondDate)

        if (first == null && second == null) return 0
        if (first == null) return 1
        if (second == null) return -1

        return first.compareTo(second)
    }

    fun currentDate(): String {
        return formatDate(
            Date(),
            STORAGE_DATE_PATTERN
        )
    }

    private fun parseAnySupportedDate(value: String): Date? {
        val cleanValue = value.trim()

        return parseStrict(
            cleanValue,
            STORAGE_DATE_PATTERN
        ) ?: parseStrict(
            cleanValue,
            USER_DATE_PATTERN
        )
    }

    private fun parseStrict(
        value: String,
        pattern: String
    ): Date? {
        if (value.isBlank()) {
            return null
        }

        if (
            pattern == USER_DATE_PATTERN &&
            !value.matches(
                Regex("\\d{2}/\\d{2}/\\d{4}")
            )
        ) {
            return null
        }

        if (
            pattern == STORAGE_DATE_PATTERN &&
            !value.matches(
                Regex("\\d{4}-\\d{2}-\\d{2}")
            )
        ) {
            return null
        }

        val formatter = SimpleDateFormat(
            pattern,
            Locale.UK
        ).apply {
            isLenient = false
        }

        val position = ParsePosition(0)
        val parsedDate = formatter.parse(
            value,
            position
        )

        return if (
            parsedDate != null &&
            position.index == value.length
        ) {
            parsedDate
        } else {
            null
        }
    }

    private fun formatDate(
        date: Date,
        pattern: String
    ): String {
        return SimpleDateFormat(
            pattern,
            Locale.UK
        ).format(date)
    }
}