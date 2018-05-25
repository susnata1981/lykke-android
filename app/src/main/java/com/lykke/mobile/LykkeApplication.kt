package com.lykke.mobile

import android.app.Application
import android.os.StrictMode
import com.lykke.mobile.di.component.ApplicationComponent
import com.lykke.mobile.di.component.DaggerApplicationComponent
import com.lykke.mobile.di.module.ApplicationModule
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.domain.model.Route

class LykkeApplication : Application() {
  lateinit var appComponent: ApplicationComponent

  companion object {
    private const val DEVELOPER_MODE = false
    private const val SALES_TAX = .08
  }

  var currentRoute: Route? = null
  var currentBusiness: Business? = null

  override fun onCreate() {
//    if (DEVELOPER_MODE) {
//      StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
//          .detectDiskReads()
//          .detectDiskWrites()
//          .detectNetwork()   // or .detectAll() for all detectable problems
//          .penaltyLog()
//          .build())
//      StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
//          .detectLeakedSqlLiteObjects()
//          .detectLeakedClosableObjects()
//          .penaltyLog()
//          .penaltyDeath()
//          .build())
//    }
    super.onCreate()

    appComponent = DaggerApplicationComponent.builder()
        .applicationModule(ApplicationModule(this))
        .build()
  }

  fun getSalesTax(): Double {
    return SALES_TAX
  }
}