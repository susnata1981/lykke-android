package com.lykke.mobile.domain.model

import android.os.Parcelable
import com.lykke.mobile.data.OrderEntity
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by susnata on 4/12/18.
 */
@Parcelize
data class Order(
    val gross: Double,
    val total: Double,
    val items: Map<String, Int>,
    val timeCreated: Long) : Parcelable {
  companion object {
    fun convert(o: OrderEntity?): Order {
      o?.let {
        return Order(
            o.gross,
            o.total,
            o.items ?: mutableMapOf(),
            o.timeCreated
        )
      }

      return Order(0.0, 0.0, emptyMap(), Date().time)
    }
  }
}
