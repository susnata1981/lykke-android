package com.lykke.mobile.ui.summary

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.lykke.mobile.LykkeApplication
import com.lykke.mobile.R
import com.lykke.mobile.data.CheckinStatus
import com.lykke.mobile.domain.GetCheckinInteractor
import com.lykke.mobile.domain.GetCheckinRequest
import com.lykke.mobile.domain.GetLoggedInUserInteractor
import com.lykke.mobile.domain.InteractorModule
import com.lykke.mobile.domain.UpdateCheckinInteractor
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.domain.model.Checkin
import com.lykke.mobile.domain.model.Order
import com.lykke.mobile.domain.model.User
import com.lykke.mobile.util.format
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class SummaryViewModel(val context: Application): AndroidViewModel(context) {

  init {
    (context as LykkeApplication)
        .appComponent.interactionBuilder()
        .interactorModule(InteractorModule())
        .build()
        .inject(this)

  }

  private var mUser: User? = null
  private val mCompositeDisposable = CompositeDisposable()
  private lateinit var mBusiness: Business
  private lateinit var mCheckin: Checkin
  private var mOrder = MutableLiveData<Order>()
  private var mStatus = MutableLiveData<String>()
  private val mPaymentAmount = MutableLiveData<String>()

  @Inject
  lateinit var getCheckinInteractor: GetCheckinInteractor
  @Inject
  lateinit var updateCheckinInteractor: UpdateCheckinInteractor
  @Inject
  lateinit var getLoggedInUserInteractor: GetLoggedInUserInteractor

  fun fetchData(business: Business) {
    mBusiness = business
    val disposable = getLoggedInUserInteractor.execute("")
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .subscribe { user ->
          Log.d("YYY", "Received user $user")
          mUser = user
          fetchCurrentCheckin(business)
        }

    mCompositeDisposable.add(disposable)
  }

  private fun fetchCurrentCheckin(business: Business) {
    val disposable = getCheckinInteractor.execute(
        GetCheckinRequest(mUser!!.key, mBusiness.key, Date().time))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          Log.d("YYY", "OrderViewModel - Found checking $it")
          mCheckin = it[0]
          mOrder.value = it[0].order
          mPaymentAmount.value = it[0].payment.amount.format()
        }
    mCompositeDisposable.add(disposable)
  }

  fun getUIViewModel(): UIViewModel {
    return UIViewModel()
  }

  fun handleFinish() {
    mCheckin.status = CheckinStatus.COMPLETE
    updateCheckinInteractor.execute(mCheckin).subscribe {
      success ->
      if (success) {
        mStatus.value = context.resources.getString(R.string.checking_finished)
      } else {
        mStatus.value = context.resources.getString(R.string.checking_failed)
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    mCompositeDisposable.dispose()
  }

  inner class UIViewModel {
    fun getOrder() : LiveData<Order> {
      return mOrder
    }

    fun getPayment() : LiveData<String> {
      return mPaymentAmount
    }

    fun getStatus(): LiveData<String> {
      return mStatus
    }
  }
}