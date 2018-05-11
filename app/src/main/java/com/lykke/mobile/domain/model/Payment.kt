package com.lykke.mobile.domain.model

import android.os.Parcelable
import com.lykke.mobile.data.PaymentEntity
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by susnata on 4/12/18.
 */
@Parcelize
data class Payment(
    val amount: Double,
    val timeCreate: Long) : Parcelable {

  companion object {
    fun convert(p: PaymentEntity?): Payment {
      p?.let {
        return Payment(p.amount, p.timeCreated)
      }
      return Payment(0.0, Date().time)
    }
  }
}