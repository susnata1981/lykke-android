package com.lykke.mobile.domain.model

import android.os.Parcelable
import com.lykke.mobile.data.SessionEntity
import com.lykke.mobile.data.SessionStatus
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Session(
    val key: String?,
    val userKey: String,
    val status: SessionStatus,
    val businesses: List<Business>,
    val timeCreated: Long,
    val timeCompleted: Long) : Parcelable {

  companion object {
    fun convert(session: SessionEntity, businesses: List<Business>): Session {
      return Session(
          session.key,
          session.userKey,
          session.status,
          businesses,
          session.timeCreated,
          session.timeCompleted
      )
    }
  }
}