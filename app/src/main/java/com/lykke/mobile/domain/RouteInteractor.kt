package com.lykke.mobile.domain

import com.lykke.mobile.data.BusinessEntity
import com.lykke.mobile.data.Repository
import com.lykke.mobile.data.RouteEntity
import com.lykke.mobile.domain.model.Assignment
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.domain.model.Route
import com.lykke.mobile.util.DAY
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.util.*

class GetRouteInteractor(val repository: Repository) : Interactor<String, Observable<Route>> {
  override fun execute(routeName: String): Observable<Route> {
    return Observable.zip(
        repository.getBusinesses(),
        repository.getRoute(routeName),
        getRoutePopulatorFunction())
  }
}

class GetAllRoutesInteractor(val repository: Repository)
  : Interactor<String, Observable<List<Route>>> {
  override fun execute(userKey: String): Observable<List<Route>> {
    val routesObservable = repository.getRoutes()
        .map { routes ->
          val result = mutableListOf<RouteEntity>()
          result.addAll(routes.filter {
            if (it.assignment == null) {
              false
            } else {
              it.assignment!!.assignee.equals(userKey)
            }
          })
          result.toList()
        }

    return Observable.zip(
        repository.getBusinesses(),
        routesObservable,
        BiFunction<List<BusinessEntity>, List<RouteEntity>, List<Route>> { businesses: List<BusinessEntity>, routes: List<RouteEntity> ->
          val result = mutableListOf<Route>()
          routes.forEach { route ->
            val bList = mutableListOf<Business>()
            if (route.businesses != null) {
              for (item in route.businesses.keys) {
                val b = businesses.first { it.key == item }
                bList.add(Business.convert(b))
              }
              result.add(
                  Route(
                      route.key,
                      bList,
                      Assignment.convert(route.assignment),
                      route.timeCreated))
            } else {
              result.add(
                  Route(
                      route.key,
                      bList,
                      Assignment.convert(route.assignment),
                      route.timeCreated))
            }
          }
          result
        })
  }
}

fun getRoutePopulatorFunction(): BiFunction<List<BusinessEntity>, RouteEntity, Route> {
  return BiFunction { businesses: List<BusinessEntity>, route: RouteEntity ->
    val bList = mutableListOf<Business>()
    if (route.businesses != null) {
      for (item in route.businesses.keys) {
        val b = businesses.first { it.key == item }
        bList.add(Business.convert(b))
      }
    }
    Route(
        route.key,
        bList,
        Assignment.convert(route.assignment),
        route.timeCreated)
  }
}


class GetPresentRouteInteractor(
    private val getAllRoutesInteractor: GetAllRoutesInteractor)
  : Interactor<String, Observable<Route>> {

  override fun execute(userKey: String): Observable<Route> {
    return getAllRoutesInteractor.execute(userKey)
        .map({ input ->
          val calendar = Calendar.getInstance()
          calendar.time = Date()
          val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
          input.first {
            if (it.assignment?.dayOfWeek != null) {
              dayOfWeek == DAY.from(it.assignment.dayOfWeek).dayNumber
            } else {
              false
            }
          }
        })
  }
}





