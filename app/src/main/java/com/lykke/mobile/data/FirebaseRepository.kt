package com.lykke.mobile.data

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.lykke.mobile.domain.model.Inventory
import com.lykke.mobile.util.isSameDay
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposables
import io.reactivex.subjects.BehaviorSubject
import java.util.*

class FirebaseRepository(val database: FirebaseDatabase) : Repository {
  companion object {
    private const val TAG = "FirebaseRepository"
  }

  private val mDb = database

  private val userEntityMapper = UserEntityMapper()
  private val businessEntityMapper = BusinessEntityMapper()
  private val routeEntityMapper = RouteEntityMapper()
  private val checkinEntityMapper = CheckinEntityMapper()

  private val mUserEntitySubject = BehaviorSubject.create<List<UserEntity>>()
  private val mRouteEntitySubject = BehaviorSubject.create<List<RouteEntity>>()
  private val mBusinessEntitySubject = BehaviorSubject.create<List<BusinessEntity>>()
  private val mCheckinEntitySubject = BehaviorSubject.create<List<CheckinEntity>>()


  init {
    // Users.
    Observable.create<List<UserEntity>>({ e ->
      mDb.getReference("users")
          .addValueEventListener(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(snapshot: DataSnapshot?) {
              Log.d("XXX", "received data for users")
              if (snapshot == null || !snapshot.exists()) {
                e.onError(IllegalStateException("No users found"))
              }

              snapshot?.let {
                val result = mutableListOf<UserEntity>()
                for (item in snapshot.children) {
                  result.add(userEntityMapper.map(item))
                }
                e.onNext(result)
              }
            }
          })
    }).subscribe({
      mUserEntitySubject.onNext(it)
    }, {
      mUserEntitySubject.onError(it)
    })

    // Routes.
    Observable.create<List<RouteEntity>> { e ->
      mDb.getReference("routes")
          .addValueEventListener(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(snapshot: DataSnapshot?) {
              snapshot?.let {
                val result = snapshot.children.map { routeEntityMapper.map(it) }
                e.onNext(result)
              }
            }
          })
    }.subscribe({
      mRouteEntitySubject.onNext(it)
    }, {
      mRouteEntitySubject.onError(it)
    })

    // Businesses.
    Observable.create<List<BusinessEntity>> { e ->
      mDb.getReference("businesses")
          .addValueEventListener(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(snapshot: DataSnapshot?) {
              snapshot?.let {
                val result = mutableListOf<BusinessEntity>()
                for (item in snapshot.children) {
                  result.add(businessEntityMapper.map(item))
                }
                e.onNext(result)
              }
            }
          })
    }.subscribe({
      mBusinessEntitySubject.onNext(it)
    }, {
      mBusinessEntitySubject.onError(it)
    })

    // Checkins.
    Observable.create<List<CheckinEntity>> { e ->
      mDb.getReference("checkins").addValueEventListener(
          object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
              Log.d(TAG, "ValueEventListener::Checkins updated, ${snapshot.children.toList().size}")

              val result = snapshot.children.map { checkinEntityMapper.map(it) }
              Log.d(TAG, "ValueEventListener:: total checkins ${result.size}")
              e.onNext(result)
            }
          })
    }.subscribe({
      Log.d(TAG, "Calling onNext on subject")
      mCheckinEntitySubject.onNext(it)
    }, {
      mCheckinEntitySubject.onError(it)
    })
  }

  override fun getUsers(): Observable<List<UserEntity>> {
    return mUserEntitySubject
  }

  override fun getRoutes(): Observable<List<RouteEntity>> {
    return mRouteEntitySubject
  }

  override fun getRoute(routeName: String): Observable<RouteEntity> {
    return getRoutes()
        .map({ routes: List<RouteEntity> ->
          routes.first { it.key == routeName }
        })
  }

  private fun getRouteInfo(routeName: String): Observable<RouteEntity> {
    return getRoutes()
        .flatMapIterable { it }
        .filter { it.key == routeName }
  }

  override fun getBusinesses(): Observable<List<BusinessEntity>> {
    return mBusinessEntitySubject
  }

  override fun createCheckin(
      userKey: String,
      businessKey: String): Observable<CheckinEntity> {
    val checkin = CheckinEntity(
        "", userKey, businessKey, CheckinStatus.IN_PROGRESS, 0, null,
        null)

    Log.d("XXX", "Returning observable")
    return Observable.create { e ->
      Log.d("XXX", "inserting checking entity... $mDb")
      val ref = mDb.getReference("checkins").push()
      Log.d("XXX", "inserting checking entity... $ref")
      checkin.key = ref.key
      ref.setValue(checkin,
          { databaseError, databaseReference ->
            Log.d("XXX", "callback -> $databaseError $databaseReference")
            if (databaseError != null) {
              e.onError(databaseError.toException())
            } else {
              e.onNext(checkin)
              e.onComplete()
            }
          })
    }
  }

  override fun getCheckins(
      userKey: String,
      businessKey: String?,
      timeCreated: Long?): Observable<List<CheckinEntity>> {

    return Observable.create { e ->
      val subscription = mCheckinEntitySubject.subscribe { checkins ->
        var result = mutableListOf<CheckinEntity>()
        result.addAll(checkins.filter {
          it.userKey == userKey
        })

        if (timeCreated != null) {
          val filteredCheckins = result.filter {
            it.userKey == userKey
                && isSameDay(Date(it.timeCreated), Date(timeCreated))
          }
          result.clear()
          result.addAll(filteredCheckins)
        }

        if (businessKey != null) {
          val filteredCheckins = result.filter {
            it.businessKey == businessKey
          }
          result.clear()
          result.addAll(filteredCheckins)
        }

        e.onNext(result)
      }

      e.setDisposable(Disposables.fromAction {
        subscription.dispose()
      })
    }
  }

//  override fun getCheckins(userKey: String, date: Date?): Observable<List<CheckinEntity>> {
//    return Observable.create<List<CheckinEntity>> { e ->
//      val listener = mDb.getReference("checkins").addValueEventListener(
//          object : ValueEventListener {
//            override fun onCancelled(p0: DatabaseError?) {
//            }
//
//            override fun onDataChange(snapshot: DataSnapshot) {
//              if (!snapshot.exists()) {
//                e.onNext(emptyList())
//                return
//              }
//
//              if (date != null) {
//                val result = snapshot.children.map { checkinEntityMapper.map(it) }
//                    .filter {
//                      it.userKey == userKey &&
//                          isSameDay(Date(it.timeCreated), date)
//                    }
//                e.onNext(result)
//              } else {
//                val result = snapshot.children.map { checkinEntityMapper.map(it) }
//                    .filter {
//                      it.userKey == userKey
//                    }.sortedBy {
//                      it.timeCreated
//                    }
//                e.onNext(result)
//              }
//            }
//          })
//
//      e.setDisposable(Disposables.fromAction {
//        mDb.getReference("checkins").removeEventListener(listener)
//      })
//    }
//  }

  override fun updateCheckin(checkin: CheckinEntity): Single<Boolean> {
    return Single.create {
      val update = mutableMapOf<String, Any>()
      update["status"] = checkin.status
      update["order"] = checkin.order ?: OrderEntity(0.0, 0.0, emptyMap())
      update["payment"] = checkin.payment ?: PaymentEntity(0.0)
      Log.d(TAG, "Updating checkin ${checkin.key} with order ${checkin.order?.items}")
      mDb.getReference("checkins/${checkin.key}").updateChildren(update, { err, _ ->
        Log.d(TAG, "updated $err")
        if (err != null) {
          Log.e(TAG, "Failed to update checking $err")
          it.onError(err.toException())
        } else {
          it.onSuccess(true)
        }
      })
    }
  }

  override fun getInventory(): Single<List<ItemEntity>> {
    return Single.create<List<ItemEntity>> { e ->
      val listener = mDb.getReference("itemmaster").addValueEventListener(object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError?) {
        }

        override fun onDataChange(snapshot: DataSnapshot) {
          if (!snapshot.exists()) {
            e.onError(IllegalStateException("Missing inventory data"))
          }

          val items = snapshot.children.map {
            val item = it.getValue(ItemEntity::class.java)!!
            item.name = it.key
            item
          }
          e.onSuccess(items)
        }
      })

      e.setDisposable(Disposables.fromAction {
        mDb.getReference("items").removeEventListener(listener)
      })
    }
  }

  override fun updateInventory(input: Map<String, Int>): Single<Boolean> {
    return Single.create { e ->

      Log.d("KKK", "Updating inventory to $input")
      val dbRef = mDb.getReference("itemmaster")
      dbRef.runTransaction(object : Transaction.Handler {

        override fun onComplete(err: DatabaseError?, result: Boolean, p2: DataSnapshot?) {
          if (err != null) {
            e.onError(err.toException())
          }
          e.onSuccess(result)
        }

        override fun doTransaction(snapshot: MutableData): Transaction.Result {
          var updatedItems = mutableMapOf<String, Any>()
          val items = snapshot.children.map {
            val item = it.getValue(ItemEntity::class.java)!!
//            item.name = it.key
            val filteredItem = input.filter { it.key == item.name }
            if (!filteredItem.isEmpty()) {
              val newQuantity = item.quantity - filteredItem.entries.first().value
              updatedItems[it.key] = ItemEntity(item.name, item.price, newQuantity)
            } else {
              updatedItems[it.key] = item
            }
          }
//          Log.d("KKK", "items -> $items")
          snapshot.value = updatedItems
          return Transaction.success(snapshot)
        }
      })
    }
  }

  fun deleteCheckin(checkinRef: String): Single<Boolean> {
    return Single.create<Boolean> {
      mDb.getReference("checkins/$checkinRef").removeValue({ error, ref ->
        if (error != null) {
          it.onError(error.toException())
        } else {
          it.onSuccess(true)
        }
      })
    }
  }

  override fun createSession(userKey: String): Single<SessionEntity> {
    Log.d(TAG, "Creating session with userKey $userKey")
    val session = SessionEntity(
        key = null,
        userKey = userKey,
        status = SessionStatus.LOGGED_IN,
        businesses = emptyList(),
        timeCreated = Date().time,
        timeCompleted = 0L)

    return Single.create<SessionEntity> {
      val ref = mDb.getReference("sessions").push()
      session.key = ref.key
      ref.setValue(session, { err, _ ->
        if (err != null) {
          it.onError(err.toException())
          Log.e(TAG, "Failed to create session", Exception())
        } else {
          Log.d(TAG, "Session created successfully! for $userKey")
          it.onSuccess(session)
        }
      })
    }
  }

  override fun updateSession(session: SessionEntity): Single<Boolean> {
    val updates = mutableMapOf<String, Any>()
    updates["businesses"] = session.businesses
    updates["status"] = session.status
    Log.d("YYY", "Updating session -> $session", Exception())

    return Single.create<Boolean> {
      mDb.getReference("sessions").child(session.key).updateChildren(updates, { err, _ ->
        if (err != null) {
          it.onError(err.toException())
        } else {
          it.onSuccess(true)
        }
      })
    }
  }

  override fun getSession(userKey: String): Observable<SessionEntity> {
    Log.d(TAG, "Executing getSession")

    return Observable.create { e ->
      val listener = mDb.getReference("sessions").addValueEventListener(object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError?) {
        }

        override fun onDataChange(snapshot: DataSnapshot?) {
          if (!snapshot!!.exists()) {
//            e.onNext(SessionEntity())
            e.onError(IllegalStateException("No session found"))
            return
          }

          for (sess in snapshot.children) {
            val session = sess.getValue(SessionEntity::class.java)
            session?.let {
              if (it.userKey == userKey
                  && isSameDay(Date(session.timeCreated), Date())
                  && session.status != SessionStatus.LOGGED_OUT) {
                e.onNext(session)
                return
              }
            }
          }

          e.onError(IllegalStateException("No session found"))
//          e.onNext(SessionEntity())
        }
      })

      e.setDisposable(Disposables.fromAction {
        mDb.getReference("sessions").removeEventListener(listener)
      })
    }
  }
}
