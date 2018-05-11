package com.lykke.mobile.domain.model

import android.os.Parcelable
import com.lykke.mobile.data.CheckinEntity
import com.lykke.mobile.data.CheckinStatus
import kotlinx.android.parcel.Parcelize

/**
 * Created by susnata on 4/12/18.
 */
@Parcelize
data class Checkin(
    var key: String,
    val userKey: String,
    val business: Business,
    var status: CheckinStatus,
    var order: Order,
    var payment: Payment,
    val timeCompleted: Long,
    val timeCreated: Long): Parcelable {

  companion object {
    fun convert(entity: CheckinEntity, business: Business): Checkin {
      return Checkin(
          entity.key,
          entity.userKey,
          business,
          entity.status,
          Order.convert(entity.order),
          Payment.convert(entity.payment),
          entity.timeCompleted,
          entity.timeCreated
      )
    }
  }
}