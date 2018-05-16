package com.lykke.mobile.ui.routedetails

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.lykke.mobile.data.CheckinStatus
import com.lykke.mobile.domain.model.Business

class BusinessListViewModel(
    val checkinStatus: CheckinStatus,
    val businesses: LiveData<List<Business>>,
    val mPageTitle: MutableLiveData<String>,
    val showAddBusinessBtn: Boolean) {
}