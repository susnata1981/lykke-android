package com.lykke.mobile.ui.businessdetails

import android.support.v4.app.Fragment
import com.lykke.mobile.ui.State
import com.lykke.mobile.ui.StateContext

class BusinessDetailsState(val context: StateContext): State(context) {
  override fun getFragment(): Fragment {
    return BusinessDetailsFragment.newInstance(context.getBusiness())
  }
//
//  override fun getEnterTransition(): Transition? {
//    return TransitionInflater.from(context.getContext()).inflateTransition(
//        R.transition.business_details_enter)
//  }
}