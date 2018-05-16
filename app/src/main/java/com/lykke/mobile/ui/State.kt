package com.lykke.mobile.ui

import android.content.Context
import android.support.v4.app.Fragment
import android.transition.Transition
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.domain.model.Route

/**
 * Created by susnata on 4/11/18.
 */
abstract class State(val stateContext: StateContext) {
  var nextState: State? = null
  var prevState: State? = null

  abstract fun getFragment(): Fragment

  open fun getEnterTransition(): Transition? = null

  open fun getReturnTransition(): Transition? = null

  open fun getExitTransition(): Transition? = null

  open fun getReenterTransition(): Transition? = null
}

interface StateContext {

  fun getCurrentRoute(): Route

  fun getBusiness(): Business

  fun getContext(): Context
}
