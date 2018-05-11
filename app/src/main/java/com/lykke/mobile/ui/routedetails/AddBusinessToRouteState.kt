package com.lykke.mobile.ui.routedetails

import android.support.v4.app.Fragment
import com.lykke.mobile.ui.State
import com.lykke.mobile.ui.StateContext

class AddBusinessToRouteState(context: StateContext): State(context) {

  override fun getFragment(): Fragment {
    return AddBusinessToRouteFragment.newInstnce()
  }
}