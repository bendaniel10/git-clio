package com.bendaniel10

import kotlinx.datetime.*

fun Instant.toLocalDateTime() = this.toLocalDateTime(TimeZone.currentSystemDefault())

fun LocalDate.Companion.now() = LocalDateTime.now().date
fun LocalDateTime.Companion.now() = Clock.System.now().toLocalDateTime()

object DaysBetween {
    fun twoInstants(firstDay: Instant, secondDay: Instant) =
        firstDay.daysUntil(secondDay, TimeZone.currentSystemDefault())
}

object HoursBetween {
    fun twoInstants(firstInstant: Instant, secondInstant: Instant) = firstInstant.until(
        secondInstant,
        DateTimeUnit.HOUR
    )

    fun twoLocalDateTime(firstLocalDateTime: LocalDateTime, secondLocalDateTime: LocalDateTime) = twoInstants(
        firstLocalDateTime.toInstant(TimeZone.currentSystemDefault()),
        secondLocalDateTime.toInstant(TimeZone.currentSystemDefault())
    )
}
