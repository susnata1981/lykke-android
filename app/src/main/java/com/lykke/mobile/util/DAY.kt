package com.lykke.mobile.util

import java.util.*


enum class DAY constructor(val dayNumber: Int) {
  MONDAY(Calendar.MONDAY),
  TUESDAY(Calendar.TUESDAY),
  WEDNESDAY(Calendar.WEDNESDAY),
  THURSDAY(Calendar.THURSDAY),
  FRIDAY(Calendar.FRIDAY),
  SATURDAY(Calendar.SATURDAY),
  SUNDAY(Calendar.SUNDAY);

  companion object {
    fun from(day: String): DAY {
      return DAY.valueOf(day)
    }

    fun from(day: Int): DAY {
      return DAY.values().first { it.dayNumber == day }
    }
  }
}