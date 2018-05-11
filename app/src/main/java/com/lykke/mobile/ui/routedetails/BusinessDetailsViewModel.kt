package com.lykke.mobile.ui.routedetails

import android.arch.lifecycle.LiveData
import android.os.Parcelable
import com.lykke.mobile.data.CheckinStatus
import com.lykke.mobile.domain.model.Business
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
class BusinessListViewModel(
    val checkinStatus: CheckinStatus,
    val businesses: @RawValue LiveData<List<Business>>,
    val showAddBusinessBtn: Boolean) : Parcelable