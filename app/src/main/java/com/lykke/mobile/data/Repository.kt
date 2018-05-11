package com.lykke.mobile.data

import io.reactivex.Observable
import io.reactivex.Single
import java.util.*

interface Repository {

  fun getUsers(): Observable<List<UserEntity>>

  fun getBusinesses(): Observable<List<BusinessEntity>>

  fun getRoutes(): Observable<List<RouteEntity>>

  fun getRoute(routeName: String): Observable<RouteEntity>

  fun createCheckin(userId: String, businessName: String): Observable<CheckinEntity>

  fun getCheckins(userKey: String, businessKey: String?, timeCreated: Long?)
      :Observable<List<CheckinEntity>>

//  @Deprecated("Use getCheckins instead")
//  fun getCheckins(userId: String, date: Date?): Observable<List<CheckinEntity>>

  fun updateCheckin(checkin: CheckinEntity): Single<Boolean>

  fun getInventory(): Single<List<ItemEntity>>

  fun createSession(userKey: String): Single<SessionEntity>

  fun updateSession(sessionEntity: SessionEntity): Single<Boolean>

  fun getSession(userKey: String): Observable<SessionEntity>
}
