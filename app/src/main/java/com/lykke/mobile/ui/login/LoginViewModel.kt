package com.lykke.mobile.ui.login

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.lykke.mobile.LykkeApplication
import com.lykke.mobile.domain.GetLoggedInUserInteractor
import com.lykke.mobile.domain.LoginInteractor
import com.lykke.mobile.domain.model.User
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LoginViewModel(val context: Application) : AndroidViewModel(context) {
    companion object {
        private const val TAG = "LoginViewModel"
    }

    private var mCallbackManager: CallbackManager
    private lateinit var mNavigator: Navigator
    private val mProgressBarVisibility = MutableLiveData<Boolean>()
    private val uiModel = UIViewModel()
    private val mStatus = MutableLiveData<String>()

    @Inject
    lateinit var loginInteractor: LoginInteractor
    @Inject
    lateinit var getLoggedInUserInteractor: GetLoggedInUserInteractor

    init {
        (context as LykkeApplication).appComponent
                .interactionBuilder()
                .build()
                .inject(this)

        mProgressBarVisibility.value = true
        getLoggedInUserInteractor.execute("")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ user: User ->
                    if (user?.email != null) {
                        mNavigator.openMainActivity()
                    } else {
                        mProgressBarVisibility.value = false
                    }
                }, {
                    mProgressBarVisibility.value = false
                })

        mCallbackManager = CallbackManager.Factory.create()
    }

    private val mFacebookCallback = object : FacebookCallback<LoginResult> {
        override fun onSuccess(loginResult: LoginResult) {
            mProgressBarVisibility.value = true
            // App code
            Log.d(TAG, "facebook login successful - " + loginResult.accessToken)
            handleFacebookAccessToken(loginResult.accessToken)
        }

        override fun onCancel() {
            // App code
            mProgressBarVisibility.value = false
        }

        override fun onError(exception: FacebookException) {
            // App code
            mProgressBarVisibility.value = false
        }
    }

    fun setNavigator(navigator: Navigator) {
        this.mNavigator = navigator
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)

        loginInteractor.execute(credential)
                .subscribe({ user ->
                    Log.d(TAG, "Successfully logged in user -> $user")
                    mProgressBarVisibility.value = false
                    mNavigator.openMainActivity()
                }, {
                    Log.d(TAG, "Failed to login user $it")
                    mProgressBarVisibility.value = false
                    mStatus.value = "Failed to login, Please try again."
                })
    }

    fun getUIViewModel(): UIViewModel {
        return uiModel
    }

    interface Navigator {
        fun openMainActivity()
        fun showLoginFailed()
    }

    inner class UIViewModel {
        fun getProgressBarVisibility(): LiveData<Boolean> {
            return mProgressBarVisibility
        }

        fun getCallbackManager(): CallbackManager {
            return mCallbackManager
        }

        fun getFacebookCallback(): FacebookCallback<LoginResult> {
            return mFacebookCallback
        }

        fun getStatus(): LiveData<String> {
            return mStatus
        }
    }
}
