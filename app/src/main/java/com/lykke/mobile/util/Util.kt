package com.lykke.mobile.util

import android.content.Context
import java.time.DayOfWeek
import java.util.*

fun isSameDay(d1: Date, d2: Date): Boolean {
  val cal1 = Calendar.getInstance()
  cal1.time = d1

  val cal2 = Calendar.getInstance()
  cal2.time = d2

  return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
      && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
      && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun formatDate(ctx: Context, time: Long): String? {
  val dateFormat = android.text.format.DateFormat.getDateFormat(ctx)
  return dateFormat.format(Date(time))
}

fun mapToDayOfWeek(day: String) = DayOfWeek.valueOf(day).value

fun mapDayToString(day: Int) = DayOfWeek.of(day)

//  "SUNDAY" -> Calendar.SUNDAY
//  "MONDAY" -> Calendar.MONDAY
//  "TUESDAY" -> Calendar.TUESDAY
//  "WEDNESDAY" -> Calendar.WEDNESDAY
//  "THURSDAY" -> Calendar.THURSDAY
//  "FRIDAY" -> Calendar.FRIDAY
//  "SATURDAY" -> Calendar.SATURDAY
//  else -> throw IllegalArgumentException("Invalid day $day")
//  return DayOfWeek.valueOf(day)
//}


//fun mapDayToString(day: Int) = when (day) {
//  Calendar.SUNDAY -> "SUNDAY"
//  Calendar.MONDAY -> "MONDAY"
//  Calendar.TUESDAY -> "TUESDAY"
//  Calendar.WEDNESDAY -> "WEDNESDAY"
//  Calendar.THURSDAY -> "THURSDAY"
//  Calendar.FRIDAY -> "FRIDAY"
//  Calendar.SATURDAY -> "SATURDAY"
//  else -> throw IllegalArgumentException("Invalid day $day")
//}
