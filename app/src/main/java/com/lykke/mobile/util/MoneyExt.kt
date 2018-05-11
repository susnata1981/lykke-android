package com.lykke.mobile.util

import java.text.DateFormat
import java.util.*

fun Double.format(): String {
  return "Rs. " + "%.2f".format(toFloat())
}

fun Date.format(): String {
  val df = DateFormat.getDateInstance(DateFormat.MEDIUM);
  return df.format(this)
}