package com.lykke.mobile.domain.model

import android.os.Parcelable
import com.lykke.mobile.data.AssignmentEntity
import kotlinx.android.parcel.Parcelize

/**
 * Created by susnata on 4/12/18.
 */
@Parcelize
data class Route(
    var key: String?,
    val businesses: List<Business>,
    val assignment: Assignment,
    val timeCreated: Long): Parcelable