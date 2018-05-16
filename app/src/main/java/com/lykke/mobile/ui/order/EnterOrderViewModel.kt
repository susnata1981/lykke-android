package com.lykke.mobile.ui.order

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.lykke.mobile.LykkeApplication
import com.lykke.mobile.R
import com.lykke.mobile.domain.GetCheckinInteractor
import com.lykke.mobile.domain.GetCheckinRequest
import com.lykke.mobile.domain.GetLoggedInUserInteractor
import com.lykke.mobile.domain.InventoryInteractor
import com.lykke.mobile.domain.UpdateCheckinInteractor
import com.lykke.mobile.domain.UpdateInventoryQuantityInteractor
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.domain.model.Checkin
import com.lykke.mobile.domain.model.Inventory
import com.lykke.mobile.domain.model.Order
import com.lykke.mobile.domain.model.User
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class EnterOrderViewModel(val context: Application) : ViewModel() {

  companion object {
    const val TAG = "EnterOrderViewModel"
  }

  init {
    (context as LykkeApplication).appComponent
        .interactionBuilder()
        .build()
        .inject(this)
  }

  @Inject
  lateinit var invetory: InventoryInteractor
  @Inject
  lateinit var getCheckinInteractor: GetCheckinInteractor
  @Inject
  lateinit var updateCheckinInteractor: UpdateCheckinInteractor
  @Inject
  lateinit var getLoggedInUserInteractor: GetLoggedInUserInteractor
  @Inject
  lateinit var updateInventoryQuantityInteractor: UpdateInventoryQuantityInteractor

  private val mInventory = MutableLiveData<Inventory>()
  private val mStatus = MutableLiveData<String>()
  private val mLastOrder = MutableLiveData<Order>()
  private var mCurrentOrder: Order? = null

  private lateinit var mBusiness: Business
  private var mCurrentCheckin: Checkin? = null

  private val mCompositeDisposable = CompositeDisposable()

  private var mUser: User? = null

  fun fetchData(business: Business) {
    mBusiness = business

    invetory.execute(null)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          mInventory.value = it
        }

    val disposable = getLoggedInUserInteractor.execute("")
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .subscribe { user ->
          mUser = user
          fetchCheckins()
        }

    mCompositeDisposable.add(disposable)
  }

  private fun fetchCheckins() {
    val disposable = getCheckinInteractor.execute(
        GetCheckinRequest(mUser!!.key, mBusiness.key, Date().time))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          mCurrentCheckin = it[0]
          if (mCurrentCheckin!!.order != null) {
            mLastOrder.value = mCurrentCheckin!!.order
            mCurrentOrder = mCurrentCheckin!!.order.copy()
          }
        }
    mCompositeDisposable.add(disposable)
  }

  fun getUIViewModel(): UIViewModel {
    return UIViewModel()
  }

  private val mShowProgressBar = MutableLiveData<Boolean>()
  private val mContinue = MutableLiveData<Boolean>()

  inner class UIViewModel {
    fun getInventory(): LiveData<Inventory> {
      return mInventory
    }

    fun getLastOrder(): LiveData<Order> {
      return mLastOrder
    }

    fun getStatus(): LiveData<String> {
      return mStatus
    }

    fun updateOrder(order: Order): Boolean {
      if (!isValid(order)) {
        mStatus.value = context.resources.getString(R.string.invalid_order)
        return false
      }
      mShowProgressBar.value = true
      mCurrentCheckin!!.order = order

      val updateInventoryRequest = mutableMapOf<String, Int>()

      Log.d("MMM", "current order -> ${mCurrentOrder!!.items}")
      order.items.forEach { itemName, quantity ->
        val entry = mCurrentOrder!!.items.filter { it.key == itemName }
        if (!entry.isEmpty()) {
          Log.d("MMM", "$itemName : $quantity::${entry.entries.first().value}")
          val quantityDelta = quantity - entry.entries.first().value
          updateInventoryRequest[itemName] = quantityDelta
        } else {
          updateInventoryRequest[itemName] = quantity
        }
      }

      Log.d("KKK", "EnterOrderViewModel:: update request -> $updateInventoryRequest")
      updateInventoryQuantityInteractor.execute(updateInventoryRequest)
          .subscribe({
            updateCheckinInteractor.execute(mCurrentCheckin!!)
                .subscribe({
                  mShowProgressBar.value = false
                  mStatus.value = context.resources.getString(R.string.order_update_success)
                }, {
                  Log.e(TAG, "Failed to update order $it")
                  mShowProgressBar.value = false
                  mStatus.value = context.resources.getString(R.string.order_update_failed)
                })
          }, {
            mShowProgressBar.value = false
            mStatus.value = context.resources.getString(R.string.order_update_failed)
          })

      return true
    }

    fun shouldShowProgressBar(): LiveData<Boolean> {
      return mShowProgressBar
    }

    fun next(): MutableLiveData<Boolean> {
      return mContinue
    }
  }

  override fun onCleared() {
    super.onCleared()
    mCompositeDisposable.dispose()
  }

  private fun isValid(order: Order): Boolean {
    var isValid = true
    order.items.forEach { name, quantity ->
      if (quantity < 0) {
        isValid = false
        return@forEach
      }
    }

    return isValid && order.gross >= 0 && order.total >= 0
  }
}