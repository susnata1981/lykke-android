package com.lykke.mobile

import android.view.View
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.domain.model.Route

interface Host {

  fun next(view: View? = null)

  fun setCurrentRoute(route: Route)

  fun setCurrentBusiness(business: Business)

  fun getCurrentBusiness(): Business?

  fun handleAddBusiness()
  
  fun setToolbarTitle(title: String)
}