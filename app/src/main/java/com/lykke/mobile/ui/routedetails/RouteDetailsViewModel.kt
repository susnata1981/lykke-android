package com.lykke.mobile.ui.routedetails

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import android.util.Log
import android.view.View
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.lykke.mobile.Host
import com.lykke.mobile.LykkeApplication
import com.lykke.mobile.R
import com.lykke.mobile.data.CheckinStatus
import com.lykke.mobile.domain.CreateCheckinInteractor
import com.lykke.mobile.domain.CreateCheckinRequest
import com.lykke.mobile.domain.GetCheckinInteractor
import com.lykke.mobile.domain.GetCheckinRequest
import com.lykke.mobile.domain.GetLoggedInUserInteractor
import com.lykke.mobile.domain.GetRouteInteractor
import com.lykke.mobile.domain.GetSessionInteractor
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.domain.model.Checkin
import com.lykke.mobile.domain.model.Route
import com.lykke.mobile.domain.model.User
import com.lykke.mobile.ui.login.LoginActivity
import com.lykke.mobile.util.DAY
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class RouteDetailsViewModel(val context: Application) : AndroidViewModel(context) {

  companion object {
    private const val TAG = "RouteDetailsViewModel"
  }

  @Inject
  lateinit var getLoggedInUserInteractor: GetLoggedInUserInteractor
  @Inject
  lateinit var getRouteInteractor: GetRouteInteractor
  @Inject
  lateinit var getCheckinInteractor: GetCheckinInteractor
  @Inject
  lateinit var createCheckinInteractor: CreateCheckinInteractor
  @Inject
  lateinit var getSessionInteractor: GetSessionInteractor

  private val mRoute = MutableLiveData<Route>()
  private val mCheckins = MutableLiveData<List<Checkin>>()

  private val mAllBusinessesInRoute = mutableListOf<Business>()
  private val mCompositeDisposable = CompositeDisposable()
  private val mCheckinFilterToBusinessesMap = mutableMapOf<CheckinStatus, MutableLiveData<List<Business>>>()
  private val mPageTitle = mutableMapOf<CheckinStatus, MutableLiveData<String>>()

  private val mIsActionEnabled = MutableLiveData<Boolean>()
  private var mUser: User? = null

  init {
    (context as LykkeApplication).appComponent
        .interactionBuilder()
        .build()
        .inject(this)

    for (status in CheckinStatus.values()) {
      val businesses = MutableLiveData<List<Business>>()
      businesses.value = emptyList()
      mCheckinFilterToBusinessesMap[status] = businesses
      mPageTitle[status] = MutableLiveData()
      mPageTitle[status]!!.value = status.name
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
        .subscribe({ userEntity ->
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
                mCheckins.value = it.first
                mRoute.value = it.second
                mIsActionEnabled.value = isActionEnabled()
                mAllBusinessesInRoute.addAll(it.second.businesses)
                refreshBusinessList()
              }
        }, {
          Log.e(TAG, "Error fetching route details information ${it.message} ${it.stackTrace}")
        })
    mCompositeDisposable.add(disposable)
  }

  private fun getCheckin() {
    mCompositeDisposable.add(
        getCheckinInteractor.execute(GetCheckinRequest(mUser!!.key!!, null, Date().time))
            .subscribe({
              mCheckins.value = it
              refreshBusinessList()
            }, {
              Log.e(TAG, "Error to get checkin information ${it.message} ${it.stackTrace}")
            }))
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

  private val mUIViewModel = UIViewModel()

  fun getUIViewModel(): UIViewModel {
    return mUIViewModel
  }

  inner class UIViewModel {
    val mBusinessListViewModels = mutableMapOf<CheckinStatus, BusinessListViewModel>()

    fun getRoute(): LiveData<Route> {
      return mRoute
    }

    fun getBusinessListViewModel(status: CheckinStatus): BusinessListViewModel {
      if (mBusinessListViewModels[status] == null) {
        mBusinessListViewModels[status] = BusinessListViewModel(
            status,
            mCheckinFilterToBusinessesMap[status]!!,
            mPageTitle[status]!!,
            status == CheckinStatus.INCOMPLETE,
            mIsActionEnabled)
      }

      return mBusinessListViewModels[status]!!
      //isActionEnabled() && status == CheckinStatus.INCOMPLETE
    }

    fun hasStartedCheckin(): Boolean {
      if (mCheckins.value == null) {
        return false
      }
      return mCheckins.value!!.isNotEmpty()
    }

    fun handleStartCheckinClick(
        fragment: BusinessListFragment, host: Host, business: Business,
        view: View?) {
      val checkin = mCheckins.value!!.firstOrNull {
        it.business.key == business.key
      }

      if (checkin != null) {
        if (checkin.status == CheckinStatus.COMPLETE) {
          fragment.showAlreadyCheckedInAlert(business, view)
          return
        }
      }

      checkin(business, host, view)
    }

    fun checkin(business: Business, host: Host, view: View?) {
      host.setCurrentBusiness(business)
      handleNextButtonClick(business)
      host.next(view)
    }

    fun isCheckinEnabled(): MutableLiveData<Boolean> {
      return mIsActionEnabled
    }
  }

  private fun getBusinesses(status: CheckinStatus): List<Business> {
    var result = mutableListOf<Business>()
    mAllBusinessesInRoute.forEach {
      if (status == CheckinStatus.INCOMPLETE) {
        if (!findBusinesses(CheckinStatus.IN_PROGRESS, it)
            && !findBusinesses(CheckinStatus.COMPLETE, it)) {
          if (!result.contains(it)) {
            result.add(it)
          }
        }
      } else {
        if (findBusinesses(status, it)) {
          if (!result.contains(it)) {
            result.add(it)
          }
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
      val businesses = getBusinesses(status)
      mCheckinFilterToBusinessesMap[status]!!.value = businesses
      mPageTitle[status]!!.value = context.resources.getString(R.string.route_details_tab_title,
          status.name,
          businesses.size)
    }
  }

  private fun isActionEnabled(): Boolean {
    val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    if (mRoute.value!!.assignment.dayOfWeek == null) {
      return false
    }

    return DAY.from(mRoute.value!!.assignment.dayOfWeek!!).dayNumber == dayOfWeek
  }
}
