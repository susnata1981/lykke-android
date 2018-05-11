package com.lykke.mobile.domain

import com.lykke.mobile.data.Repository
import com.lykke.mobile.di.UserScope
import dagger.Module
import dagger.Provides

@UserScope
@Module
class InteractorModule {

  @UserScope
  @Provides
  fun providesBusinessList(repository: Repository): GetBusinessListInteractor {
    return GetBusinessListInteractor(repository)
  }

  @UserScope
  @Provides
  fun providesLoginInteractor(
      repository: Repository,
      createSessionInteractor: CreateSessionInteractor,
      getSessionInteractor: GetSessionInteractor): LoginInteractor {
    return LoginInteractor(repository, createSessionInteractor, getSessionInteractor)
  }

  @UserScope
  @Provides
  fun providesLogoutInteractor(
      getLoggedInUserInteractor: GetLoggedInUserInteractor,
      getSessionInteractor: GetSessionInteractor,
      updateSessionInteractor: UpdateSessionInteractor): LogoutInteractor {
    return LogoutInteractor(getLoggedInUserInteractor, getSessionInteractor, updateSessionInteractor)
  }

  @UserScope
  @Provides
  fun providesUserInteractor(repository: Repository): GetLoggedInUserInteractor {
    return GetLoggedInUserInteractor(repository)
  }

  @UserScope
  @Provides
  fun providesGetAllRouteInteractor(repository: Repository): GetAllRoutesInteractor {
    return GetAllRoutesInteractor(repository)
  }

  @UserScope
  @Provides
  fun providesGetRouteInteractor(repository: Repository): GetRouteInteractor {
    return GetRouteInteractor(repository)
  }

  @UserScope
  @Provides
  fun provideGetPresentRouteInteractor(
      getAllRoutesInteractor: GetAllRoutesInteractor): GetPresentRouteInteractor {
    return GetPresentRouteInteractor(getAllRoutesInteractor)
  }

  @UserScope
  @Provides
  fun providesGetCheckinInteractor(
      repository: Repository,
      getBusinessListInteractor: GetBusinessListInteractor): GetCheckinInteractor {
    return GetCheckinInteractor(repository, getBusinessListInteractor)
  }

  @UserScope
  @Provides
  fun providesCreateCheckinInteractor(
      repository: Repository,
      getBusinessListInteractor: GetBusinessListInteractor,
      getCheckinInteractor: GetCheckinInteractor): CreateCheckinInteractor {
    return CreateCheckinInteractor(repository, getBusinessListInteractor, getCheckinInteractor)
  }

  @UserScope
  @Provides
  fun providesUpdateCheckinInteractor(repository: Repository): UpdateCheckinInteractor {
    return UpdateCheckinInteractor(repository)
  }

  @UserScope
  @Provides
  fun getInventoryInteractor(repository: Repository): InventoryInteractor {
    return InventoryInteractor(repository)
  }

  @UserScope
  @Provides
  fun provideCreatSeessionInteractor(
      repository: Repository,
      getBusinessListInteractor: GetBusinessListInteractor): CreateSessionInteractor {
    return CreateSessionInteractor(repository, getBusinessListInteractor)
  }

  @UserScope
  @Provides
  fun provideGetSeessionInteractor(
      repository: Repository,
      createSessionInteractor: CreateSessionInteractor,
      getBusinessListInteractor: GetBusinessListInteractor): GetSessionInteractor {
    return GetSessionInteractor(repository, createSessionInteractor, getBusinessListInteractor)
  }

  @UserScope
  @Provides
  fun provideUpdateSeessionInteractor(repository: Repository): UpdateSessionInteractor {
    return UpdateSessionInteractor(repository)
  }
}