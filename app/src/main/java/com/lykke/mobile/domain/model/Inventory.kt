package com.lykke.mobile.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Item(val key: String?, val price: Double, val quantity: Int, val timeCreated: Long): Parcelable

@Parcelize
data class Inventory(val items: List<Item>): Parcelable