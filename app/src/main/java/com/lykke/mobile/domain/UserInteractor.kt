package com.lykke.mobile.domain

import android.util.Log
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.lykke.mobile.data.Repository
import com.lykke.mobile.data.SessionStatus
import com.lykke.mobile.domain.model.Session
import com.lykke.mobile.domain.model.User
import io.reactivex.Observable
import io.reactivex.Single

class LoginInteractor(
    private val repository: Repository,
    private val createSessionInteractor: CreateSessionInteractor,
    private val getSessionInteractor: GetSessionInteractor) : Interactor<AuthCredential, Single<User>> {

  companion object {
    private const val TAG = "LoginInteractor"
  }

  override fun execute(credential: AuthCredential): Single<User> {
    return Single.create { e ->
      val auth: FirebaseAuth = FirebaseAuth.getInstance()
      Log.d(TAG, "Signing in with firebase")
      auth.signInWithCredential(credential)
          .addOnCompleteListener({
            if (it.isSuccessful) {
              Log.d(TAG, "Login successful $it")
              val email = it.result!!.user.email
              repository.getUsers()
                  .flatMapIterable { it }
                  .filter { it.email == email }
                  .subscribe({ r ->
                    Log.d(TAG, "Sign in success $r")
                    getSessionInteractor.execute(r.key!!).subscribe { session ->
                      Log.d("YYY", "session -> $session")
                      if (session.key.isNullOrEmpty()) {
                        Log.d("YYY", "creating session")
                        createSessionInteractor.execute(r.key!!).subscribe { _ ->
                          e.onSuccess(User.convert(r))
                        }
                      } else {
                        e.onSuccess(User.convert(r))
                      }
                    }
                  })
            } else {
              Log.e(TAG, "Failed to sign in with Firebase")
              e.onError(IllegalAccessError("Failed to authenticate user"))
            }
          })
    }
  }
}

class LogoutInteractor(
    private val getLoggedInUserInteractor: GetLoggedInUserInteractor,
    private val getSessionInteractor: GetSessionInteractor,
    private val updateSessionInteractor: UpdateSessionInteractor) : Interactor<String, Single<Boolean>> {

  companion object {
    private const val TAG = "LogoutInteractor"
  }

  override fun execute(na: String): Single<Boolean> {
    return Single.create { e ->

      Log.d(TAG, "Logging out user")
      getLoggedInUserInteractor.execute("").subscribe({ user ->
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        getSessionInteractor.execute(user.key)
            .take(1)
            .subscribe { session ->
          if (session.key.isNullOrEmpty()) {
            auth.signOut()
            e.onSuccess(true)
            return@subscribe
          }

          Log.d(TAG, "Found session $session, updating it...")
          val newSession = Session(
              session.key,
              session.userKey,
              SessionStatus.LOGGED_OUT,
              session.businesses,
              session.timeCreated,
              session.timeCompleted)

          SessionStatus.LOGGED_OUT
          updateSessionInteractor.execute(newSession)
              .subscribe { _ ->
                auth.signOut()
                e.onSuccess(true)
              }
        }
      }, {
        Log.d(TAG, "User not logged in!")
      })
    }
  }
}

class GetLoggedInUserInteractor(private val repository: Repository) : Interactor<String, Single<User>> {

  override fun execute(input: String): Single<User> {
    return Single.create({ e ->
      FirebaseAuth.getInstance().currentUser?.email ?: run {
        e.onError(IllegalAccessError("User not logged in"))
      }

      val email = FirebaseAuth.getInstance().currentUser?.email
      repository.getUsers()
          .flatMap { Observable.fromIterable(it) }
          .filter { it.email == email }
          .subscribe {
            Log.d("YYY", "Found logged in user -> $it")
            e.onSuccess(User.convert(it))
          }
    })
  }
}
