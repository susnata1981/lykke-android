package com.lykke.mobile.ui.businessdetails

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.lykke.mobile.LykkeApplication
import com.lykke.mobile.domain.GetCheckinInteractor
import com.lykke.mobile.domain.GetCheckinRequest
import com.lykke.mobile.domain.GetLoggedInUserInteractor
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.domain.model.Checkin
import com.lykke.mobile.util.format
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

class BusinessDetailsViewModel(val context: Application) : ViewModel() {
  companion object {
    private const val NA: String = "NA"
  }

  init {
    (context as LykkeApplication).appComponent
        .interactionBuilder()
        .build()
        .inject(this)
  }

  @Inject
  lateinit var getCheckinInteractor: GetCheckinInteractor
  @Inject
  lateinit var getLoggedInUserInteractor: GetLoggedInUserInteractor

  private val disposables: CompositeDisposable = CompositeDisposable()
  private val mCheckins = MutableLiveData<List<Checkin>>()
  private lateinit var mBusiness: Business
  private val mLastOrderAmount = MutableLiveData<String>()
  private val mLastPaymentAmount = MutableLiveData<String>()
  private val mLastVisitDate = MutableLiveData<String>()

  fun fetchData(business: Business) {
    mBusiness = business
    val disposable = getLoggedInUserInteractor.execute("")
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .subscribe { user ->
          getCheckinInteractor.execute(GetCheckinRequest(user.key, business.key, null))
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe {
                mCheckins.value = it.sortedBy { it.timeCreated }
                updateData()
              }
        }
    disposables.add(disposable)
  }

  private fun updateData() {
    if (mCheckins.value != null && mCheckins.value!!.isEmpty()) {
      mLastOrderAmount.value = NA
      mLastPaymentAmount.value = NA
      mLastVisitDate.value = NA
    } else {
      val checkin = mCheckins.value!![mCheckins.value!!.size - 1]
      mLastOrderAmount.value = checkin.order.total.format()
      mLastPaymentAmount.value = checkin.payment.amount.format()
      val df = DateFormat.getDateInstance(DateFormat.LONG, Locale.US)
      mLastVisitDate.value = df.format(Date(checkin.timeCreated))
    }
  }

  fun getUIViewModel(): UIViewModel {
    return UIViewModel()
  }

  inner class UIViewModel {
    fun getBusinessName(): String {
      return mBusiness.key!!
    }

    fun getBusinessAddress(): String {
      return mBusiness.address!!
    }

    fun getOutstandingBalance(): String {
      return mBusiness.outstandingBalance.format()
    }

    fun getLastOrderAmount(): LiveData<String> {
      return mLastOrderAmount
    }

    fun getLastVisitDate(): LiveData<String> {
      return mLastVisitDate
    }

    fun getLastPaymentAmount(): LiveData<String> {
      return mLastPaymentAmount
    }
  }

  override fun onCleared() {
    super.onCleared()
    disposables.clear()
  }
}
