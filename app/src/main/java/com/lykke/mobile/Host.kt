package com.lykke.mobile

import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.domain.model.Route

interface Host {
  fun next()

  fun setCurrentRoute(route: Route)

  fun setCurrentBusiness(business: Business)

  fun getCurrentBusiness(): Business?

  fun handleAddBusiness()
  
  fun setToolbarTitle(title: String)
}