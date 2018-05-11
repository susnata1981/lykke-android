package com.lykke.mobile

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.lykke.mobile.ui.businessdetails.BusinessDetailsViewModel
import com.lykke.mobile.ui.login.LoginViewModel
import com.lykke.mobile.ui.order.EnterOrderViewModel
import com.lykke.mobile.ui.payment.EnterPaymentViewModel
import com.lykke.mobile.ui.routedetails.RouteDetailsViewModel
import com.lykke.mobile.ui.routestart.RouteListViewModel

class ViewModelFactory constructor(
    private val application: Application)
  : ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T =
      with(modelClass) {
        when {
          isAssignableFrom(LoginViewModel::class.java) ->
            LoginViewModel(application)
          isAssignableFrom(RouteDetailsViewModel::class.java) ->
            RouteDetailsViewModel(application)
          isAssignableFrom(RouteListViewModel::class.java) ->
            RouteListViewModel(application)
          isAssignableFrom(BusinessDetailsViewModel::class.java) ->
            BusinessDetailsViewModel(application)
          isAssignableFrom(EnterPaymentViewModel::class.java) ->
            EnterPaymentViewModel(application)
          isAssignableFrom(EnterOrderViewModel::class.java) ->
            EnterOrderViewModel(application)
          else -> throw IllegalArgumentException("Invalid classType $modelClass")
        }
      } as T

  companion object {
    @Volatile
    private var INSTANCE: ViewModelFactory? = null

    fun getInstance(application: Application): ViewModelFactory {
      INSTANCE ?: synchronized(ViewModelFactory::class.java) {
        INSTANCE ?: ViewModelFactory(application)
            .also { INSTANCE = it }
      }
      return INSTANCE!!
    }
  }
}