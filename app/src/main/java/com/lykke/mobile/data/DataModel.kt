package com.lykke.mobile.data

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize
import java.util.*

open class BaseModel(val timeCreated: Long)

@Parcelize
data class BusinessEntity(
    var key: String,
    val address: String,
    val outstandingBalance: Double,
    val lat: Double,
    val lng: Double): BaseModel(timeCreated = Date().time), Parcelable {
  constructor():this("", "", 0.0, 0.0, 0.0)
}

@Parcelize
data class RouteEntity(
    var key: String?,
    val businesses: Map<String, Boolean>?,
    val assignment: AssignmentEntity?): BaseModel(timeCreated = Date().time), Parcelable {
  constructor():this(null, null, null)
}

data class RouteDetails(
    var key: String?,
    val businesses: List<BusinessEntity>,
    val assignment: AssignmentEntity?,
    val timeCreated: Long?)

@Parcelize
data class AssignmentEntity(
    val assignee: String?,
    val dayOfWeek: String?): Parcelable {
  constructor(): this(null, null)
}

@Parcelize
data class UserEntity(
    var key: String?,
    val email: String?,
    val firstname: String?,
    val lastname: String?,
    val role: String?): BaseModel(timeCreated = Date().time), Parcelable {
  constructor(): this(null, null, null, null, null)
}


enum class CheckinStatus {
  COMPLETE, IN_PROGRESS, INCOMPLETE
}

@Parcelize
data class CheckinEntity(
    @Exclude var key: String,
    val userKey: String,
    val businessKey: String,
    val status: CheckinStatus,
    val timeCompleted: Long,
    val order: OrderEntity?,
    val payment: PaymentEntity?): BaseModel(timeCreated = Date().time), Parcelable {
  constructor(): this("", "", "", CheckinStatus.INCOMPLETE, 0, null, null)
}

@Parcelize
data class ItemEntity(var name: String, val price: Double, val quantity: Int)
  : BaseModel(timeCreated = Date().time), Parcelable {
  constructor(): this("", 0.0, 0)
}

@Parcelize
data class OrderEntity(
    val gross: Double,
    val total: Double,
    val items: Map<String, Int>?): BaseModel(timeCreated = Date().time), Parcelable {
  constructor(): this(0.0, 0.0, null)
}

@Parcelize
data class PaymentEntity(
    val amount: Double): BaseModel(timeCreated = Date().time), Parcelable {
  constructor(): this(0.0)
}

enum class SessionStatus {
  LOGGED_IN, LOGGED_OUT, INVALID
}

@Parcelize
data class SessionEntity(
    @Exclude var key: String?,
    val userKey: String,
    val status: SessionStatus,
    val businesses: List<String>,
    val timeCreated: Long,
    val timeCompleted: Long): Parcelable {
  constructor(): this(
      null, "", SessionStatus.INVALID, emptyList(), Date().time, Date().time)
}
