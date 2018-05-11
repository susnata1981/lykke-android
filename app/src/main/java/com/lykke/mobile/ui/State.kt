package com.lykke.mobile.ui

import android.support.v4.app.Fragment
import com.lykke.mobile.data.RouteEntity
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.domain.model.Route

/**
 * Created by susnata on 4/11/18.
 */
abstract class State(private val context: StateContext) {
  var nextState: State? = null
  var prevState: State? = null

  abstract fun getFragment(): Fragment
}

interface StateContext {
  fun getCurrentRoute(): Route

  fun getBusiness(): Business
}