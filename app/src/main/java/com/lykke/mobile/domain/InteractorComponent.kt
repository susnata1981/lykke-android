package com.lykke.mobile.domain

import com.lykke.mobile.StartActivityViewModel
import com.lykke.mobile.di.UserScope
import com.lykke.mobile.ui.businessdetails.BusinessDetailsViewModel
import com.lykke.mobile.ui.login.LoginViewModel
import com.lykke.mobile.ui.order.EnterOrderViewModel
import com.lykke.mobile.ui.payment.EnterPaymentViewModel
import com.lykke.mobile.ui.routedetails.AddBusinessToRouteViewModel
import com.lykke.mobile.ui.routedetails.RouteDetailsViewModel
import com.lykke.mobile.ui.routestart.RouteListViewModel
import com.lykke.mobile.ui.summary.SummaryViewModel
import dagger.Subcomponent

@UserScope
@Subcomponent(modules = [(InteractorModule::class)])
interface InteractorComponent {

  fun inject(viewModel: RouteListViewModel)

  fun inject(viewModel: RouteDetailsViewModel)

  fun inject(viewModel: LoginViewModel)

  fun inject(viewModel: BusinessDetailsViewModel)

  fun inject(viewModel: EnterOrderViewModel)

  fun inject(viewModel: EnterPaymentViewModel)

  fun inject(viewModel: SummaryViewModel)

  fun inject(viewModel: StartActivityViewModel)

  fun inject(viewModel: AddBusinessToRouteViewModel)

  @Subcomponent.Builder
  interface Builder {
    fun build(): InteractorComponent
    fun interactorModule(module: InteractorModule): Builder
  }
}