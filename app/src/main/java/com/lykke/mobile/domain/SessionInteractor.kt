package com.lykke.mobile.domain

import android.util.Log
import com.lykke.mobile.data.Repository
import com.lykke.mobile.data.SessionEntity
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.domain.model.Session
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction

class CreateSessionInteractor(
    val repository: Repository,
    val getBusinessListInteractor: GetBusinessListInteractor) : Interactor<String, Single<Session>> {

  companion object {
    private const val TAG = "CreateSessionInteractor"
  }

  override fun execute(userKey: String): Single<Session> {
    Log.d(TAG, "Executing createSessionInteractor", Exception())

    return Single.create { e ->
      Single.zip(
          getBusinessListInteractor.execute(""),
          repository.createSession(userKey),
          BiFunction<List<Business>, SessionEntity, Session> { businesses, sess ->
            if (sess.businesses != null && sess.businesses.isNotEmpty()) {
              val businessesInSession = businesses.filter { sess.businesses.contains(it.key) }
              Session.convert(sess, businessesInSession)
            }
            Session.convert(sess, emptyList())
          }
      ).subscribe { session ->
        e.onSuccess(session)
      }
    }
  }
}

class GetSessionInteractor(
    val repository: Repository,
    private val createSessionInteractor: CreateSessionInteractor,
    private val getBusinessListInteractor: GetBusinessListInteractor) : Interactor<String, Observable<Session>> {

  override fun execute(userKey: String): Observable<Session> {
    return Observable.create { e ->
      repository.getSession(userKey)
          .map { session ->
            getBusinessListInteractor.execute("")
                .subscribe { businesses ->
                  val businessesInSession = businesses.filter { session.businesses.contains(it.key) }
                  val result = Session.convert(session, businessesInSession)
                  e.onNext(result)
                }
          }.subscribe({}, {
            createSessionInteractor.execute(userKey)
                .subscribe { session ->
                  e.onNext(session)
                }
          })
    }
  }
}

class UpdateSessionInteractor(val repository: Repository) : Interactor<Session, Single<Boolean>> {
  override fun execute(session: Session): Single<Boolean> {
    return repository.updateSession(
        SessionEntity(
            session.key,
            session.userKey,
            session.status,
            session.businesses.map { it.key },
            session.timeCreated,
            session.timeCompleted))
  }
}
