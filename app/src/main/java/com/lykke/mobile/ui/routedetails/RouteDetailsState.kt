package com.lykke.mobile.ui.routedetails

import android.support.v4.app.Fragment
import com.lykke.mobile.ui.State
import com.lykke.mobile.ui.StateContext

/**
 * Created by susnata on 4/11/18.
 */
class RouteDetailsState(val context: StateContext): State(context) {

  override fun getFragment(): Fragment {
    return RouteDetailsFragment.newInstance(context.getCurrentRoute())
  }
}
