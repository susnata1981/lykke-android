package com.lykke.mobile.ui.payment

import android.support.v4.app.Fragment
import com.lykke.mobile.ui.State
import com.lykke.mobile.ui.StateContext

class EnterPaymentState(val context: StateContext): State(context) {

  override fun getFragment(): Fragment {
    return EnterPaymentFragment.newInstance()
  }
}