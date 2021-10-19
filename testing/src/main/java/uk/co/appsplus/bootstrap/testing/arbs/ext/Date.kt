package uk.co.appsplus.bootstrap.testing.arbs.ext

import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import java.util.*

private const val TWENTY_FOUR_HOURS = 86400000
private const val LAST_HOUR = 23
private const val LAST_MINUTE = 59
private const val LAST_SECOND = 59

fun Calendar.startOfToday(): Date {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    return time
}

fun Calendar.startOfYesterday(): Date {
    return Date(startOfToday().time - TWENTY_FOUR_HOURS)
}

fun Calendar.endOfToday(): Date {
    set(Calendar.HOUR_OF_DAY, LAST_HOUR)
    set(Calendar.MINUTE, LAST_MINUTE)
    set(Calendar.SECOND, LAST_SECOND)
    return time
}

fun Arb.Companion.todayDate(): Arb<Date> {
    val minDate = Calendar.getInstance().startOfToday()
    val maxDate = Calendar.getInstance().endOfToday()
    return Arb
        .long(minDate.time, maxDate.time)
        .map { Date(it) }
}

fun Arb.Companion.yesterdayDate(): Arb<Date> {
    val dayInMilliseconds = TWENTY_FOUR_HOURS
    return todayDate().map { Date(it.time - dayInMilliseconds) }
}

fun Arb.Companion.date(min: Date = Date(0), max: Date = Date()): Arb<Date> {
    return Arb
        .long(min.time, max.time)
        .map { Date(it) }
}
