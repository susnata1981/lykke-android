package com.lykke.mobile.ui.routedetails

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.lykke.mobile.LykkeApplication
import com.lykke.mobile.R
import com.lykke.mobile.domain.GetBusinessListInteractor
import com.lykke.mobile.domain.GetLoggedInUserInteractor
import com.lykke.mobile.domain.GetSessionInteractor
import com.lykke.mobile.domain.InteractorModule
import com.lykke.mobile.domain.UpdateSessionInteractor
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.domain.model.Session
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class AddBusinessToRouteViewModel(val context: Application) : AndroidViewModel(context) {

  init {
    (context as LykkeApplication).appComponent
        .interactionBuilder()
        .interactorModule(InteractorModule())
        .build()
        .inject(this)
  }

  @Inject
  lateinit var mGetLoggedInUserInteractor: GetLoggedInUserInteractor
  @Inject
  lateinit var mGetBusinessListInteractor: GetBusinessListInteractor
  @Inject
  lateinit var mUpdateessionInteractor: UpdateSessionInteractor
  @Inject
  lateinit var mGetSessionInteractor: GetSessionInteractor

  private val mCompositeDisposable = CompositeDisposable()

  private var mBusinessList = MutableLiveData<List<Business>>()
  private var mSession = MutableLiveData<Session>()
  private val mItemList = MutableLiveData<List<Item>>()
  private val mStatus = MutableLiveData<String>()

  fun fetchData() {
    var disposable = mGetBusinessListInteractor.execute("")
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { businesses ->
          mBusinessList.value = businesses
          populateItems()
        }

    mCompositeDisposable.add(disposable)
    disposable = mGetLoggedInUserInteractor.execute("")
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .subscribe { user ->
          mGetSessionInteractor.execute(user.key)
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe { session ->
                Log.d("XXX", "Current session -> ${session}")
                mSession.value = session
                populateItems()
              }
        }
    mCompositeDisposable.add(disposable)
  }

  private fun populateItems() {
    mBusinessList.value ?: return
    mSession.value ?: return

    val items = mutableListOf<Item>()
    val route = (context as LykkeApplication).currentRoute

    mBusinessList.value!!.forEach { business ->
      if (route!!.businesses.contains(business)) {
        items.add(Item(
            business.key, false, false))
      } else {
        if (mSession.value!!.businesses == null || mSession.value!!.businesses.isEmpty()) {
          items.add(Item(business.key, true, false))
        } else {
          val businessesInSession = mSession.value!!.businesses.filter { it.key.equals(business.key) }
          if (businessesInSession.isEmpty()) {
            items.add(Item(business.key, true, false))
          } else {
            items.add(Item(business.key, false, true))
          }
        }
      }
    }
    mItemList.value = items
  }

  fun handleAdd(businessKey: String) {
    with(mSession.value!!) {
      Log.d("XXX", "current session ${mSession.value}")
      val newBusinessList = mutableListOf<Business>()
      newBusinessList.addAll(businesses)
      newBusinessList.add(mBusinessList.value!!.first{ it.key == businessKey })

      Log.d("XXX", "business list $newBusinessList")
      val newSession = Session(
          key,
          userKey,
          status,
          newBusinessList,
          timeCreated,
          timeCompleted)

      mUpdateessionInteractor.execute(newSession)
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe { _ ->
            mStatus.value = context.resources.getString(
                R.string.business_add_success,
                (context as LykkeApplication).currentRoute?.key)
          }
    }
  }

  fun handleRemove(businessKey: String) {
    with(mSession.value!!) {
      val newBusinessList = mutableListOf<Business>()
      newBusinessList.addAll(businesses)
      newBusinessList.removeIf { it.key == businessKey }

      val newSession = Session(
          key,
          userKey,
          status,
          newBusinessList,
          timeCreated,
          timeCompleted)

      mUpdateessionInteractor.execute(newSession)
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe({ _ ->
            mStatus.value = context.resources.getString(R.string.business_remove_success,
                (context as LykkeApplication).currentRoute?.key)
          }, {
            mStatus.value = context.resources.getString(R.string.route_update_failed)
          })
    }
  }

  fun getUIViewModel(): UIViewModel {
    return UIViewModel()
  }

  data class Item(
      val businessKey: String,
      val isAddEnabled: Boolean,
      val isRemovedEnabled: Boolean)

  inner class UIViewModel {
    fun getItems(): LiveData<List<Item>> {
      return mItemList
    }

    fun getStatus(): LiveData<String> {
      return mStatus
    }
  }

  override fun onCleared() {
    super.onCleared()
    mCompositeDisposable.clear()
  }
}
