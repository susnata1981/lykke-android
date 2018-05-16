package com.lykke.mobile.ui.routedetails

import android.support.v4.app.Fragment
import android.transition.Transition
import android.transition.TransitionInflater
import com.lykke.mobile.R
import com.lykke.mobile.ui.State
import com.lykke.mobile.ui.StateContext

/**
 * Created by susnata on 4/11/18.
 */
class RouteDetailsState(ctx: StateContext): State(ctx) {

  override fun getFragment(): Fragment {
    return RouteDetailsFragment.newInstance(stateContext.getCurrentRoute())
  }

  override fun getEnterTransition(): Transition? {
    return TransitionInflater.from(stateContext.getContext())
        .inflateTransition(R.transition.route_details_enter_transition)
  }

  override fun getReturnTransition(): Transition? {
    return TransitionInflater.from(stateContext.getContext())
        .inflateTransition(R.transition.route_details_return_transition)
  }

  override fun getExitTransition(): Transition? {
    return TransitionInflater.from(stateContext.getContext())
        .inflateTransition(R.transition.route_details_exit_transition)
  }
}
