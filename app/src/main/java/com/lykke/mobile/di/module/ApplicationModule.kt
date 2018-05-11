package com.lykke.mobile.di.module

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.lykke.mobile.LykkeApplication
import com.lykke.mobile.data.FirebaseRepository
import com.lykke.mobile.data.Repository
import com.lykke.mobile.domain.InteractorComponent
import com.lykke.mobile.domain.LoginInteractor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(subcomponents = [InteractorComponent::class])
class ApplicationModule(val context: Application) {

  @Singleton
  @Provides
  fun providesApplicationContext(): Application = context

  @Singleton
  @Provides
  fun providesRepository(database: FirebaseDatabase): Repository = FirebaseRepository(database)

  @Singleton
  @Provides
  fun proivdeDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()
}