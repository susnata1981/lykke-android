package com.lykke.mobile.domain.model

import android.os.Parcelable
import com.lykke.mobile.data.UserEntity
import kotlinx.android.parcel.Parcelize

/**
 * Created by susnata on 4/12/18.
 */
@Parcelize
data class User(
    var key: String,
    val email: String,
    val firstname: String,
    val lastname: String,
    val role: String,
    val timeCreated: Long): Parcelable {

  companion object {
    fun convert(u: UserEntity): User {
      return User(u.key!!, u.email!!, u.firstname!!, u.lastname!!, u.role!!, u.timeCreated)
    }
  }
}
