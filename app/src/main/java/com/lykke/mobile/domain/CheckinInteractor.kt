package com.lykke.mobile.domain

import com.lykke.mobile.data.CheckinEntity
import com.lykke.mobile.data.OrderEntity
import com.lykke.mobile.data.PaymentEntity
import com.lykke.mobile.data.Repository
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.domain.model.Checkin
import com.lykke.mobile.domain.model.Order
import com.lykke.mobile.domain.model.Payment
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.*

data class CreateCheckinRequest(val userKey: String, val businessKey: String)

class CreateCheckinInteractor(
    private val repository: Repository,
    private val getBusinessListInteractor: GetBusinessListInteractor,
    private val getCheckinInteractor: GetCheckinInteractor) : Interactor<CreateCheckinRequest, Observable<Checkin>> {

  override fun execute(request: CreateCheckinRequest): Observable<Checkin> {
    return Observable.create { e ->
      getCheckinInteractor.execute(GetCheckinRequest(
          request.userKey, request.businessKey, Date().time
      )).subscribe { checkins ->
        if (checkins.isNotEmpty()) {
          e.onNext(checkins[0])
          e.onComplete()
        } else {
          Observable.zip(
              getBusinessListInteractor.execute("").toObservable(),
              repository.createCheckin(request.userKey, request.businessKey),
              BiFunction<List<Business>, CheckinEntity, Checkin> { businesses, checkin ->
                val business = businesses.first {
                  it.key == request.businessKey
                }
                Checkin.convert(checkin, business)
              }).subscribe {
            e.onNext(it)
            e.onComplete()
          }
        }
      }
    }
  }
}

data class GetCheckinRequest(val userKey: String, val businessKey: String?, val timeCreated: Long?)

class GetCheckinInteractor(
    val repository: Repository,
    private val getBusinessListInteractor: GetBusinessListInteractor)
  : Interactor<GetCheckinRequest, Observable<List<Checkin>>> {

  override fun execute(request: GetCheckinRequest): Observable<List<Checkin>> {
    return Observable.create { e ->
      repository.getCheckins(request.userKey, request.businessKey, request.timeCreated)
          .map {
            val checkins = it
            val result = mutableListOf<Checkin>()
            getBusinessListInteractor.execute("")
                .subscribe { businesses ->

                  checkins.forEach { checkin ->
                    val business = businesses.first { it.key == checkin.businessKey }
                    result.add(Checkin(
                        checkin.key,
                        checkin.userKey,
                        business,
                        checkin.status,
                        Order.convert(checkin.order),
                        Payment.convert(checkin.payment),
                        checkin.timeCompleted,
                        checkin.timeCreated))
                  }
                  e.onNext(result)
                }
          }.subscribe()
    }
  }
}

class UpdateCheckinInteractor(val repository: Repository) : Interactor<Checkin, Single<Boolean>> {

  override fun execute(request: Checkin): Single<Boolean> {
    return repository.updateCheckin(CheckinEntity(
        request.key,
        request.userKey,
        request.business.key,
        request.status,
        request.timeCompleted,
        OrderEntity(
            request.order.gross,
            request.order.total,
            request.order.items),
        PaymentEntity(request.payment.amount)
    ))
  }
}

/*
data class GetCheckinsRequest(val userKey: String, val date: Date?)

class GetCheckinsInteractor(
    val repository: Repository,
    private val getBusinessListInteractor: GetBusinessListInteractor) :
    Interactor<GetCheckinsRequest, Observable<List<Checkin>>> {

  override fun execute(request: GetCheckinsRequest): Observable<List<Checkin>> {
    return Observable.create { e ->
      repository.getCheckins(request.userKey, request.date)
          .subscribe { checkins ->
            getBusinessListInteractor.execute("")
                .subscribe { businesses ->
                  val result = mutableListOf<Checkin>()
                  checkins.forEach {
                    val checkin = it
                    val business = businesses.first {
                      it.key == checkin.businessKey
                    }
                    result.add(Checkin(
                        it.key!!,
                        request.userKey,
                        business,
                        it.status!!,
                        Order.convert(it.order),
                        Payment.convert(it.payment),
                        it.timeCompleted,
                        it.timeCreated))
                  }
                  e.onNext(result)
                }
          }
    }
  }
}

data class GetCheckinsForBusinessRequest(val userKey: String, val businessKey: String)

class GetCheckinsForBusinessInteractor(
    val repository: Repository,
    private val getBusinessListInteractor: GetBusinessListInteractor) :
    Interactor<GetCheckinsForBusinessRequest, Observable<List<Checkin>>> {

  override fun execute(req: GetCheckinsForBusinessRequest): Observable<List<Checkin>> {
    return Observable.zip(
        getBusinessListInteractor.execute("").toObservable(),
        repository.getCheckins(req.userKey, Date()),
        BiFunction<List<Business>, List<CheckinEntity>, List<Checkin>> { businesses: List<Business>, checkins: List<CheckinEntity> ->
          val result = mutableListOf<Checkin>()
          checkins.forEach {
            val checkin = it
            val business = businesses.first {
              it.key == checkin.businessKey
            }

            result.add(Checkin(
                it.key,
                req.userKey,
                business,
                it.status,
                Order.convert(it.order),
                Payment.convert(it.payment),
                it.timeCompleted,
                it.timeCreated))
          }
          result
        })
  }
}
*/