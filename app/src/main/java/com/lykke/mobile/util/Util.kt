package com.lykke.mobile.util

import android.content.Context
import android.os.Build
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