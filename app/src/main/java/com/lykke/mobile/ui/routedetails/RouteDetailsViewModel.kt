package com.lykke.mobile.ui.routedetails

import android.app.AlertDialog
import android.app.Application
import android.app.Fragment
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import android.util.Log
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.lykke.mobile.Host
import com.lykke.mobile.LykkeApplication
import com.lykke.mobile.data.CheckinStatus
import com.lykke.mobile.domain.CreateCheckinInteractor
import com.lykke.mobile.domain.CreateCheckinRequest
import com.lykke.mobile.domain.GetCheckinInteractor
import com.lykke.mobile.domain.GetCheckinRequest
import com.lykke.mobile.domain.GetLoggedInUserInteractor
import com.lykke.mobile.domain.GetPresentRouteInteractor
import com.lykke.mobile.domain.GetRouteInteractor
import com.lykke.mobile.domain.GetSessionInteractor
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.domain.model.Checkin
import com.lykke.mobile.domain.model.Route
import com.lykke.mobile.domain.model.User
import com.lykke.mobile.ui.login.LoginActivity
import com.lykke.mobile.util.mapDayToString
import com.lykke.mobile.util.mapToDayOfWeek
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class RouteDetailsViewModel(context: Application) : AndroidViewModel(context) {

  companion object {
    private const val TAG = "RouteDetailsViewModel"
  }

  @Inject
  lateinit var getLoggedInUserInteractor: GetLoggedInUserInteractor
  @Inject
  lateinit var getRouteInteractor: GetRouteInteractor
  @Inject
  lateinit var getPresentRouteInteractor: GetPresentRouteInteractor
  @Inject
  lateinit var getCheckinInteractor: GetCheckinInteractor
  @Inject
  lateinit var createCheckinInteractor: CreateCheckinInteractor
  @Inject
  lateinit var getSessionInteractor: GetSessionInteractor

  //  val mRoutes = MutableLiveData<List<Route>>()
  private val mRoute = MutableLiveData<Route>()
  private val mCheckins = MutableLiveData<List<Checkin>>()

  private val mAllBusinessesInRoute = mutableListOf<Business>()
  private val mCompositeDisposable = CompositeDisposable()
  private val mCheckinFilterToBusinessesMap = mutableMapOf<CheckinStatus, MutableLiveData<List<Business>>>()
  //  val presentDayRoute = MutableLiveData<Route>()
  private var mUser: User? = null

  init {
    (context as LykkeApplication).appComponent
        .interactionBuilder()
        .build()
        .inject(this)

    for (status in CheckinStatus.values()) {
      mCheckinFilterToBusinessesMap[status] = MutableLiveData()
    }
  }

  fun logout() {
    FirebaseAuth.getInstance().signOut()
    LoginManager.getInstance().logOut()
    val intent = Intent(getApplication(), LoginActivity::class.java)
    getApplication<Application>().startActivity(intent)
  }

  fun getRouteDetails(routeName: String) {
    val disposable = getLoggedInUserInteractor.execute("")
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { userEntity ->
          mUser = userEntity
          getSession()
          getCheckin()
          Observable.zip(
              getCheckinInteractor.execute(GetCheckinRequest(userEntity.key!!, null, Date().time)),
              getRouteInteractor.execute(routeName),
              BiFunction<List<Checkin>, Route, Pair<List<Checkin>, Route>> { checkins, route ->
                Pair(checkins, route)
              })
              .subscribe {
                Log.d(TAG, "checkins -> ${it.first}")
                Log.d(TAG, "route -> ${it.second}")
                mCheckins.value = it.first
                mRoute.value = it.second
                mAllBusinessesInRoute.addAll(it.second.businesses)
                refreshBusinessList()
              }
        }
    mCompositeDisposable.add(disposable)
  }

  private fun getCheckin() {
    mCompositeDisposable.add(
        getCheckinInteractor.execute(GetCheckinRequest(mUser!!.key!!, null, Date().time))
            .subscribe {
              mCheckins.value = it
              refreshBusinessList()
            })
  }

  private fun getSession() {
    mCompositeDisposable.add(getSessionInteractor.execute(mUser!!.key)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { session ->
          if (session.businesses != null && session.businesses.isNotEmpty()) {
            mAllBusinessesInRoute.addAll(session.businesses)
            refreshBusinessList()
          }
        })
  }

  fun getUIViewModel(): UIViewModel {
    return UIViewModel()
  }

  inner class UIViewModel {
    fun getRoute(): LiveData<Route> {
      return mRoute
    }

//    fun getPresentDayRoute(): LiveData<Route> {
//      val today = Date()
//      val cal = Calendar.getInstance()
//      cal.time = today
//      val dayOfWeek = mapDayToString(cal.get(Calendar.DAY_OF_WEEK))
//      presentDayRoute.value = mRoutes.value?.firstOrNull {
//        it.assignment?.dayOfWeek.equals(dayOfWeek)
//      }
//
//      return presentDayRoute
//    }

    fun getBusinessListViewModel(status: CheckinStatus): BusinessListViewModel {
      return BusinessListViewModel(
          status,
          mCheckinFilterToBusinessesMap[status]!!,
          status == CheckinStatus.INCOMPLETE)
    }

    fun hasStartedCheckin(): Boolean {
      if (mCheckins.value == null) {
        return false
      }
      return mCheckins.value!!.isNotEmpty()
    }

    fun isActionEnabled(): Boolean {
      val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
      if (mRoute.value!!.assignment.dayOfWeek == null) {
        return false
      }

      return mapToDayOfWeek(mRoute.value!!.assignment.dayOfWeek!!) == dayOfWeek
    }

    fun handleStartCheckinClick(fragment: BusinessListFragment, host: Host, business: Business) {
      val checkin = mCheckins.value!!.firstOrNull {
        it.business.key == business.key
      }

      if (checkin != null) {
        if (checkin.status == CheckinStatus.COMPLETE) {
          fragment.showAlreadyCheckedInAlert(business)
          return
        }
      }

      checkin(business, host)
    }

    fun checkin(business: Business, host: Host) {
      host.setCurrentBusiness(business)
      handleNextButtonClick(business)
      host.next()
    }
  }

  private fun getBusinesses(status: CheckinStatus): List<Business> {
    var result = mutableListOf<Business>()
    mAllBusinessesInRoute.forEach {
      if (status == CheckinStatus.INCOMPLETE) {
        if (!findBusinesses(CheckinStatus.IN_PROGRESS, it) && !findBusinesses(CheckinStatus.COMPLETE, it)) {
          result.add(it)
        }
      } else {
        if (findBusinesses(status, it)) {
          result.add(it)
        }
      }
    }
    return result
  }

  fun findBusinesses(status: CheckinStatus, business: Business): Boolean {
    mCheckins.value?.forEach {
      if (it.status == status &&
          it.business.key == business.key) {
        return true
      }
    }
    return false
  }

  fun handleNextButtonClick(business: Business) {

    val disposable = createCheckinInteractor.execute(
        CreateCheckinRequest(mUser!!.key, business.key))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          Log.d(TAG, "Finished creating checkin entity")
        }
    mCompositeDisposable.add(disposable)
  }

  override fun onCleared() {
    super.onCleared()
    mCompositeDisposable.clear()
  }

  private fun refreshBusinessList() {
    for (status in CheckinStatus.values()) {
      mCheckinFilterToBusinessesMap[status]!!.value = getBusinesses(status)
    }
  }
}
