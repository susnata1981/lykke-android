package com.lykke.mobile.ui.order

import android.support.v4.app.Fragment
import android.transition.Transition
import android.transition.TransitionInflater
import com.lykke.mobile.R
import com.lykke.mobile.ui.State
import com.lykke.mobile.ui.StateContext

class EnterOrderState(ctx: StateContext) : State(ctx) {

  override fun getFragment(): Fragment {
    return EnterOrderFragment.newInstance()
  }

  override fun getEnterTransition(): Transition? {
    return TransitionInflater.from(stateContext.getContext()).inflateTransition(
        R.transition.enter_order_transition)
  }
}