package com.lykke.mobile.di.component

import com.lykke.mobile.di.module.ApplicationModule
import com.lykke.mobile.domain.InteractorComponent
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {

  fun interactionBuilder(): InteractorComponent.Builder

}

//@Module(subcomponents = [InteractorComponent::class])
//abstract class ApplicationBinders {
//  // Provide the builder to be included in a mapping used for creating the builders.
//  @Binds
//  @IntoMap
//  @SubcomponentKey(InteractorComponent.Builder::class)
//  abstract fun myActivity(impl: InteractorComponent.Builder): SubcomponentBuilder<*>
//}
//
//@MapKey
//@Target(AnnotationTarget.FUNCTION)
//@Retention(AnnotationRetention.RUNTIME)
//annotation class SubcomponentKey(val value: KClass<*>)