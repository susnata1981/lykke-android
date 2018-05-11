package com.lykke.mobile

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.lykke.mobile.domain.GetLoggedInUserInteractor
import com.lykke.mobile.domain.InteractorModule
import com.lykke.mobile.domain.LogoutInteractor
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class StartActivityViewModel(val context: Application) : AndroidViewModel(context) {

  init {
    (context as LykkeApplication)
        .appComponent.interactionBuilder()
        .interactorModule(InteractorModule())
        .build()
        .inject(this)
  }

  companion object {
    private const val TAG = "StartActivityViewModel"
  }

  @Inject
  lateinit var logoutInteractor: LogoutInteractor
  @Inject
  lateinit var getLoggedInUserInteractor: GetLoggedInUserInteractor

  private val mCompositeDisposable = CompositeDisposable()
  private val mIsLoggedIn = MutableLiveData<Boolean>()

  fun logout():LiveData<Boolean> {
    val disposable = logoutInteractor.execute("")
        .subscribe({ _ ->
          FirebaseAuth.getInstance().signOut()
          LoginManager.getInstance().logOut()
          mIsLoggedIn.value = false
        }, {
          Log.e(TAG, "User not logged in")
          mIsLoggedIn.value = false
        })

    mCompositeDisposable.add(disposable)
    return mIsLoggedIn
  }

  override fun onCleared() {
    super.onCleared()
    mCompositeDisposable.dispose()
  }
}
