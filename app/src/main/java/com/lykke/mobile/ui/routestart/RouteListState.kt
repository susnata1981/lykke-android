package com.lykke.mobile.ui.routestart

import android.support.v4.app.Fragment
import com.lykke.mobile.ui.State
import com.lykke.mobile.ui.StateContext

/**
 * Created by susnata on 4/11/18.
 */
class RouteListState(val context: StateContext): State(context) {

  override fun getFragment(): Fragment {
    return RouteListFragment.newInstance()
  }
}
