package com.lykke.mobile.domain

import com.lykke.mobile.data.Repository
import com.lykke.mobile.domain.model.Business
import io.reactivex.Single

/**
 * Created by susnata on 4/12/18.
 */
class GetBusinessListInteractor(val repository: Repository) : Interactor<String, Single<List<Business>>> {
  override fun execute(input: String): Single<List<Business>> {
    return Single.create { e ->
      repository.getBusinesses()
          .subscribe { businesses ->
            var result = mutableListOf<Business>()
            businesses.forEach {
              result.add(Business.convert(it))
            }
            e.onSuccess(result)
          }
    }
  }
}

