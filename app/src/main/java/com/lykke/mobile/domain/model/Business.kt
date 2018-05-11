package com.lykke.mobile.domain.model

import android.os.Parcelable
import com.lykke.mobile.data.BusinessEntity
import kotlinx.android.parcel.Parcelize

/**
 * Created by susnata on 4/12/18.
 */
@Parcelize
data class Business(
    var key: String,
    val address: String?,
    val outstandingBalance: Double,
    val lat: Double,
    val lng: Double,
    val timeCreated: Long) : Parcelable {

  companion object {
    fun convert(be: BusinessEntity): Business =
        Business(
            be.key,
            be.address,
            be.outstandingBalance,
            be.lat,
            be.lng,
            be.timeCreated
        )
  }
}