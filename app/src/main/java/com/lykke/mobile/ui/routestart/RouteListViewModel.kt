package com.lykke.mobile.ui.routestart

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.lykke.mobile.Host
import com.lykke.mobile.LykkeApplication
import com.lykke.mobile.R
import com.lykke.mobile.domain.GetAllRoutesInteractor
import com.lykke.mobile.domain.GetLoggedInUserInteractor
import com.lykke.mobile.domain.model.Route
import com.lykke.mobile.ui.login.LoginActivity
import com.lykke.mobile.util.DAY
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class RouteListViewModel(val context: Application) : AndroidViewModel(context) {
  companion object {
    private const val TAG = "RouteStartViewModel"
  }

  @Inject
  lateinit var getLoggedInUserInteractor: GetLoggedInUserInteractor
  @Inject
  lateinit var getAllRoutesInteractor: GetAllRoutesInteractor

  val mRouteItemList = MutableLiveData<List<RouteItem>>()
  val mRoutes = mutableListOf<Route>()

  init {
    (context as LykkeApplication).appComponent
        .interactionBuilder()
        .build()
        .inject(this)
    this.getAllRoutes()
  }

  fun logout() {
    FirebaseAuth.getInstance().signOut()
    LoginManager.getInstance().logOut()
    val intent = Intent(getApplication(), LoginActivity::class.java)
    getApplication<Application>().startActivity(intent)
  }

  private fun getAllRoutes() {
    getLoggedInUserInteractor.execute("")
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { userEntity ->
          getAllRoutesInteractor.execute(userEntity.key)
              .subscribe { routes ->
                val routeItemList = mutableListOf<RouteItem>()

                val currentDayWeek = DAY.from(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
                DAY.values().forEach { day ->
                  val route = routes.firstOrNull { DAY.from(it.assignment.dayOfWeek!!) == day }
                  val routeName = if (route == null) context.resources.getString(R.string.holiday_message) else route.key
                  if (currentDayWeek == day) {
                    routeItemList.add(0,
                        RouteItem(
                            routeName!!,
                            day,
                            route == null,
                            currentDayWeek == day))
                  } else {
                    routeItemList.add(
                        RouteItem(
                            routeName!!,
                            day,
                            route == null,
                            currentDayWeek == day))
                  }
                }

                mRouteItemList.value = routeItemList
                mRoutes.clear()
                mRoutes.addAll(routes)
              }
        }
  }

  fun getUIViewModel(): UIViewModel {
    return UIViewModel()
  }

  inner class UIViewModel {

    fun getRouteItemList(): MutableLiveData<List<RouteItem>> {
      return mRouteItemList
    }

    fun handleClick(host: Host, routeItem: RouteItem) {
      val route = mRoutes.first { it.key == routeItem.key }
      host.let {
        it.setCurrentRoute(route)
        it.next()
      }
      (context as LykkeApplication).currentRoute = route
    }
  }

  data class RouteItem(
      val key: String,
      val dayOfWeek: DAY,
      val isHoliday: Boolean,
      val isCurrentRoute: Boolean)
}
