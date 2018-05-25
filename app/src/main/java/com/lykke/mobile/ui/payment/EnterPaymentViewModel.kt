package com.lykke.mobile.ui.payment

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.os.Handler
import android.util.Log
import com.facebook.internal.Utility
import com.lykke.mobile.LykkeApplication
import com.lykke.mobile.R
import com.lykke.mobile.domain.GetCheckinInteractor
import com.lykke.mobile.domain.GetCheckinRequest
import com.lykke.mobile.domain.GetLoggedInUserInteractor
import com.lykke.mobile.domain.InteractorModule
import com.lykke.mobile.domain.UpdateCheckinInteractor
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.domain.model.Checkin
import com.lykke.mobile.domain.model.Payment
import com.lykke.mobile.domain.model.User
import com.lykke.mobile.util.format
import com.lykke.mobile.util.formatDate
import com.lykke.mobile.util.isSameDay
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class EnterPaymentViewModel(val context: Application) : AndroidViewModel(context) {

  init {
    (context as LykkeApplication)
        .appComponent
        .interactionBuilder()
        .interactorModule(InteractorModule())
        .build()
        .inject(this)
  }

  companion object {
    private const val TAG = "EnterPaymentViewModel"
    private const val EMPTY_STRING = ""
  }

  @Inject
  lateinit var getCheckinInteractor: GetCheckinInteractor
  @Inject
  lateinit var updateCheckinInteractor: UpdateCheckinInteractor
  @Inject
  lateinit var getLoggedInUserInteractor: GetLoggedInUserInteractor

  private lateinit var mBusiness: Business
  private val mCheckins = mutableListOf<Checkin>()
  private var mUser: User? = null

  private val mStatus = MutableLiveData<String>()
  private val mLastPayment = MutableLiveData<String>()
  private val mLastPaymentDate = MutableLiveData<String>()

  private val mCurrentPayment = MutableLiveData<String>()
  private val hideKeyboard = MutableLiveData<Boolean>()
  private val moveNext = MutableLiveData<Boolean>()
  private var mUIViewModel: UIViewModel? = null

  private val mCompositeDisposable = CompositeDisposable()

  fun fetchData(business: Business) {
    mBusiness = business

    val disposable = getLoggedInUserInteractor.execute("")
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .subscribe { user ->
          mUser = user
          fetchCheckin()
        }

    mCompositeDisposable.add(disposable)
  }

  private lateinit var mCurrentCheckin: Checkin

  private fun fetchCheckin() {
    val disposable = getCheckinInteractor.execute(
        GetCheckinRequest(mUser!!.key, mBusiness.key, null))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          mCheckins.addAll(it)
          mCheckins.sortByDescending { it.timeCreated }
          mCurrentCheckin = mCheckins.first { isSameDay(Date(it.timeCreated), Date()) }

          mCurrentPayment.value = mCurrentCheckin.payment.amount.toString()

          if (mCheckins.size > 1) {
            mLastPayment.value = mCheckins[1].payment.amount.format()
            mLastPaymentDate.value = formatDate(context, mCheckins[1].payment.timeCreate)
          } else {
            mLastPayment.value = context.resources.getString(R.string.NA)
            mLastPaymentDate.value = context.resources.getString(R.string.NA)
          }
        }
    mCompositeDisposable.add(disposable)
  }

  fun getUIViewModel(): UIViewModel {
    if (mUIViewModel == null) {
      mUIViewModel = UIViewModel()
    }

    return mUIViewModel!!
  }

  inner class UIViewModel {

    init {
      moveNext.value = false
      hideKeyboard.value = false
    }

    fun getLastPayment(): LiveData<String> {
      return mLastPayment
    }

    fun getLastPaymentDate(): LiveData<String> {
      return mLastPaymentDate
    }

    fun getCurrentPayment(): LiveData<String> {
      return mCurrentPayment
    }

    fun getStatus(): LiveData<String> {
      return mStatus
    }

    fun updatePayment(payment: Payment): Boolean {
      if (payment.amount < 0) {
        mStatus.value = context.resources.getString(R.string.invalid_payment)
        return false
      }

      mCurrentCheckin.payment = payment
      updateCheckinInteractor.execute(mCurrentCheckin).subscribe({ _ ->
        mStatus.value = context.resources.getString(R.string.payment_update_success)
        hideKeyboard.value = true
        moveNext.value = true
        moveNext.value = false
        Log.d("LLL", "moving to next screen")
      }, {
        Log.d(TAG, "Failed to update payment ${it.message}")
        hideKeyboard.value = true
        moveNext.value = true
        moveNext.value = false
        mStatus.value = context.resources.getString(R.string.payment_update_failed)
      })

      Handler(context.mainLooper).postDelayed({
        mStatus.value = EMPTY_STRING
      }, context.resources.getInteger(R.integer.notification_duration).toLong())

      return true
    }

    fun hideKeyboard(): LiveData<Boolean> {
      return hideKeyboard
    }

    fun moveNext(): LiveData<Boolean> {
      return moveNext
    }
  }

  override fun onCleared() {
    super.onCleared()
    mCompositeDisposable.dispose()
  }
}