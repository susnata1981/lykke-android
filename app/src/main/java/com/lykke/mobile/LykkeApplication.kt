package com.lykke.mobile

import android.app.Application
import com.lykke.mobile.di.component.ApplicationComponent
import com.lykke.mobile.di.component.DaggerApplicationComponent
import com.lykke.mobile.di.module.ApplicationModule
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.domain.model.Route

class LykkeApplication : Application() {
  lateinit var appComponent: ApplicationComponent

  var currentRoute: Route? = null
  var currentBusiness: Business? = null

  override fun onCreate() {
    super.onCreate()
    appComponent = DaggerApplicationComponent.builder()
        .applicationModule(ApplicationModule(this))
//        .interactorModule(InteractorModule())
        .build()
  }
}