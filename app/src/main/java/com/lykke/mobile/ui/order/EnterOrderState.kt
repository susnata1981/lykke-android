package com.lykke.mobile.ui.order

import android.support.v4.app.Fragment
import com.lykke.mobile.ui.State
import com.lykke.mobile.ui.StateContext

class EnterOrderState(context: StateContext): State(context) {

  override fun getFragment(): Fragment {
    return EnterOrderFragment.newInstance()
  }
}